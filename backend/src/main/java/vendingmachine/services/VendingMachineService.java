package vendingmachine.services;

import vendingmachine.model.*;
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

    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    private final VendingMachineStatusPublisher publisher;

    public VendingMachineService(VendingMachineStatusPublisher publisher) {
        this.publisher = publisher;
    }

     // Initiates the NFT minting process for a specific user
     // Retrieves necessary configurations and policies, and starts a new thread to process the minting logic.
     // If a minting session for the user is already active, the method will not initiate a new one.
    public void startMinting(String userAddress) throws SQLException, CborSerializationException {
        if (activeSessions.containsKey(userAddress)) {
            System.out.println("Minting already in progress for user: " + userAddress);
            return;
        }

        Session session = new Session();
        activeSessions.put(userAddress, session);

        Account sender = new Account(Networks.preprod(), Constant.RECOVERY_PHRASE);
        Policy policy = DbOperations.getPolicyByWallet(userAddress);
        UserConfig userConfig = DbOperations.getUserConfig(userAddress);
        String slot = userConfig.getPolicySlot();
        int mintLimitPerTx = userConfig.getNFTsToMintPerTx();
        int amountOfNftsNotToMint = userConfig.getAmountOfNFTsNotToMint();

        new Thread(() -> {
            try {
                System.out.println("Calling minting logic for user: " + userAddress);
                publisher.send(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STARTED, "", "Minting process started and being monitored"));
                MintMultipleNfts.queue(sender, policy, slot, mintLimitPerTx, amountOfNftsNotToMint, session.getStopFlag(), publisher, userConfig.getOwnerWalletAddress());
                Thread.sleep(5000);

            } catch (CborSerializationException | SQLException | InterruptedException | ApiException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                activeSessions.remove(userAddress);
                System.out.println("Stopped minting loop for user: " + userAddress);
            }
        }).start();
    }

    // Stops the minting process for a specific user by setting the stop flag in the session
    public void stopMinting(String userAddress) {
        Session session = activeSessions.get(userAddress);
        if (session != null) {
            session.stop();
            System.out.println("Requested stop of minting for user: " + userAddress);
        } else {
            System.out.println("No active minting session found for user: " + userAddress);
        }
    }

    // Initiates the refunding process for a specific user
    // Retrieves necessary configurations and policies, and starts a new thread to process the refunding logic.
    public void startRefunding(String userAddress) throws SQLException, CborSerializationException {

        if (activeSessions.containsKey(userAddress)) {
            System.out.println("Refunding already in progress for user: " + userAddress);
            return;
        }

        Session session = new Session();
        activeSessions.put(userAddress, session);

        Policy policy = DbOperations.getPolicyByWallet(userAddress);
        UserConfig userConfig = DbOperations.getUserConfig(userAddress);
        Account account = new Account(Networks.preprod(), Constant.RECOVERY_PHRASE);

        int amountOfRefundsPerTx = userConfig.getRefundsPerTxLimit();

        new Thread(() -> {
            try {
                publisher.send(userConfig.getOwnerWalletAddress(), new RefundStatusMessage(policy.getPolicyId(), RefundStatus.INITIATED, "", "Refund process started and being monitored"));

                System.out.println("Calling refund logic for user: " + userAddress);
                Refunds.startRefund(account, policy, amountOfRefundsPerTx, session.getStopFlag(), publisher, userConfig.getOwnerWalletAddress());
                Thread.sleep(5000);

            } catch (CborSerializationException | SQLException | InterruptedException | ApiException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                activeSessions.remove(userAddress);
                System.out.println("Stopped refund loop for user: " + userAddress);
            }
        }).start();
    }

    // Stops the refunding process for a specific user by setting the stop flag in the session
    public void stopRefunding(String userAddress){
        Session session = activeSessions.get(userAddress);
        if (session != null) {
            session.stop();
            System.out.println("Requested stop of refunds for user: " + userAddress);
        } else {
            System.out.println("No active refunding session found for user: " + userAddress);
        }
    }
}