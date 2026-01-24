package vendingmachine.services;

import com.bloxbean.cardano.client.transaction.spec.Policy;
import vendingmachine.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vendingmachine.repository.PolicyRepository;
import vendingmachine.repository.UserConfigRepository;
import vendingmachine.transactions.MintMultipleNfts;
import vendingmachine.transactions.Refunds;
import vendingmachine.utils.Constant;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.springframework.stereotype.Service;
import vendingmachine.utils.DbOperations;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VendingMachineService {
    private static final Logger log = LoggerFactory.getLogger(VendingMachineService.class);
    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    private final VendingMachineStatusPublisher publisher;
    private final MintMultipleNfts mintMultipleNfts;
    private final PolicyRepository policyRepository;
    private final UserConfigRepository userConfigRepository;
    private DbOperations dbOperations;
    private final Refunds refunds;

    public VendingMachineService(VendingMachineStatusPublisher publisher, MintMultipleNfts mintMultipleNfts, PolicyRepository policyRepository, UserConfigRepository userConfigRepository, DbOperations dbOperations, Refunds refunds) {
        this.publisher = publisher;
        this.mintMultipleNfts = mintMultipleNfts;
        this.policyRepository = policyRepository;
        this.userConfigRepository = userConfigRepository;
        this.dbOperations = dbOperations;
        this.refunds = refunds;
    }

    public void startMinting(String userAddress) throws SQLException {
        if (activeSessions.containsKey(userAddress)) {
            log.info("Minting already in progress for user: {}", userAddress);
            return;
        }

        Session session = new Session();
        activeSessions.put(userAddress, session);

        Account sender = new Account(Networks.preprod(), Constant.RECOVERY_PHRASE);
        Policy policy = dbOperations.getPolicyByWallet(userAddress);
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(userAddress);
        String slot = userConfig.getPolicySlot();
        int mintLimitPerTx = userConfig.getNFTsToMintPerTx();
        int amountOfNftsNotToMint = userConfig.getAmountOfNFTsNotToMint();

        new Thread(() -> {
            try {
                log.info("Calling minting logic for user: {}", userAddress);
                publisher.send(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STARTED, "", "Minting process started and being monitored"));
                mintMultipleNfts.queue(sender, policy, slot, mintLimitPerTx, amountOfNftsNotToMint, session.getStopFlag(), publisher, userConfig.getOwnerWalletAddress());
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

    public void stopMinting(String userAddress) {
        Session session = activeSessions.get(userAddress);
        PolicyObject policy = policyRepository.findByOwnerWallet(userAddress);
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(userAddress);
        if (session != null) {
            session.stop();
            log.info("Requested to stop minting for user: {}", userAddress);
            publisher.send(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STARTED, "", "Minting will be stopped after the last submitted transaction is confirmed"));
        } else {
            log.info("No active minting session found for user: {}", userAddress);
        }
    }

    public void startRefunding(String userAddress) throws SQLException {
        if (activeSessions.containsKey(userAddress)) {
            log.info("Refunding already in progress for user: {}", userAddress);
            return;
        }

        Session session = new Session();
        activeSessions.put(userAddress, session);

        Policy policy = dbOperations.getPolicyByWallet(userAddress);
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(userAddress);
        Account account = new Account(Networks.preprod(), Constant.RECOVERY_PHRASE);

        int amountOfRefundsPerTx = userConfig.getRefundsPerTxLimit();

        new Thread(() -> {
            try {
                publisher.send(userConfig.getOwnerWalletAddress(), new RefundStatusMessage(policy.getPolicyId(), RefundStatus.INITIATED, "", "Refund process started and being monitored"));

                log.info("Calling refund logic for user: {}", userAddress);
                refunds.startRefund(account, policy, amountOfRefundsPerTx, session.getStopFlag(), publisher, userConfig.getOwnerWalletAddress());
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

    public void stopRefunding(String userAddress) {
        PolicyObject policy = policyRepository.findByOwnerWallet(userAddress);
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(userAddress);
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