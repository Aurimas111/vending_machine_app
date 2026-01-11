package vendingmachine.services;

import vendingmachine.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vendingmachine.transactions.MintMultipleNfts;
import vendingmachine.transactions.Refunds;
import vendingmachine.utils.Constant;
import vendingmachine.utils.DbOperations;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VendingMachineService {
    private static final Logger log = LoggerFactory.getLogger(VendingMachineService.class);
    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    private final VendingMachineStatusPublisher publisher;
    private DbOperations dbOperations;

    public VendingMachineService(VendingMachineStatusPublisher publisher, DbOperations dbOperations) {
        this.publisher = publisher;
        this.dbOperations = dbOperations;
    }

     // Initiates the NFT minting process for a specific user
     // Retrieves necessary configurations and policies, and starts a new thread to process the minting logic.
     // If a minting session for the user is already active, the method will not initiate a new one.
    public void startMinting(String userAddress) throws SQLException, CborSerializationException {
        if (activeSessions.containsKey(userAddress)) {
            log.info("Minting already in progress for user: {}", userAddress);
            return;
        }

        Session session = new Session();
        activeSessions.put(userAddress, session);

        Account sender = new Account(Networks.preprod(), Constant.RECOVERY_PHRASE);
        Policy policy = dbOperations.getPolicyByWallet(userAddress);
        UserConfig userConfig = dbOperations.getUserConfig(userAddress);
        String slot = userConfig.getPolicySlot();
        int mintLimitPerTx = userConfig.getNFTsToMintPerTx();
        int amountOfNftsNotToMint = userConfig.getAmountOfNFTsNotToMint();

        new Thread(() -> {
            try {
                log.info("Calling minting logic for user: {}", userAddress);
                publisher.send(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STARTED, "", "Minting process started and being monitored"));
                MintMultipleNfts.queue(sender, policy, slot, mintLimitPerTx, amountOfNftsNotToMint, session.getStopFlag(), publisher, userConfig.getOwnerWalletAddress());
                Thread.sleep(5000);

            } catch (CborSerializationException | SQLException | InterruptedException | ApiException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                activeSessions.remove(userAddress);
                log.info("Stopped minting loop for user: {}", userAddress);
                try {
                    publisher.send(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STOPPED, "", "Minting has been stopped"));
                } catch (CborSerializationException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    // Stops the minting process for a specific user by setting the stop flag in the session
    public void stopMinting(String userAddress) throws SQLException, CborSerializationException {
        Session session = activeSessions.get(userAddress);
        UserConfig userConfig = dbOperations.getUserConfig(userAddress);
        Policy policy = dbOperations.getPolicyByWallet(userAddress);
        if (session != null) {
            session.stop();
            log.info("Requested to stop minting for user: {}", userAddress);
            publisher.send(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STARTED, "", "Minting will be stopped after the last submitted transaction is confirmed"));
        } else {
            log.info("No active minting session found for user: {}", userAddress);
        }
    }

    // Initiates the refunding process for a specific user
    // Retrieves necessary configurations and policies, and starts a new thread to process the refunding logic.
    public void startRefunding(String userAddress) throws SQLException, CborSerializationException {
        if (activeSessions.containsKey(userAddress)) {
            log.info("Refunding already in progress for user: {}", userAddress);
            return;
        }

        Session session = new Session();
        activeSessions.put(userAddress, session);

        Policy policy = dbOperations.getPolicyByWallet(userAddress);
        UserConfig userConfig = dbOperations.getUserConfig(userAddress);
        Account account = new Account(Networks.preprod(), Constant.RECOVERY_PHRASE);

        int amountOfRefundsPerTx = userConfig.getRefundsPerTxLimit();

        new Thread(() -> {
            try {
                publisher.send(userConfig.getOwnerWalletAddress(), new RefundStatusMessage(policy.getPolicyId(), RefundStatus.INITIATED, "", "Refund process started and being monitored"));

                log.info("Calling refund logic for user: {}", userAddress);
                Refunds.startRefund(account, policy, amountOfRefundsPerTx, session.getStopFlag(), publisher, userConfig.getOwnerWalletAddress());
                Thread.sleep(5000);

            } catch (CborSerializationException | SQLException | InterruptedException | ApiException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                activeSessions.remove(userAddress);
                log.info("Stopped refund loop for user: {}", userAddress);
                try {
                    publisher.send(userConfig.getOwnerWalletAddress(), new RefundStatusMessage(policy.getPolicyId(), RefundStatus.STOPPED, "", "Refund process has been stopped"));
                } catch (CborSerializationException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    // Stops the refunding process for a specific user by setting the stop flag in the session
    public void stopRefunding(String userAddress) throws SQLException, CborSerializationException {
        UserConfig userConfig = dbOperations.getUserConfig(userAddress);
        Policy policy = dbOperations.getPolicyByWallet(userAddress);
        Session session = activeSessions.get(userAddress);
        if (session != null) {
            session.stop();
            log.info("Requested to stop refunding for user: {}", userAddress);
        publisher.send(userConfig.getOwnerWalletAddress(), new RefundStatusMessage(policy.getPolicyId(), RefundStatus.STOPPED, "", "Refund process has been stopped"));
        } else {
            log.info("No active refunding session found for user: {}", userAddress);
        }
    }
}