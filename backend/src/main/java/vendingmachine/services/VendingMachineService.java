package vendingmachine.services;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.springframework.beans.factory.annotation.Value;
import vendingmachine.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vendingmachine.repository.PolicyRepository;
import vendingmachine.repository.UserConfigRepository;
import vendingmachine.transactions.MintMultipleNfts;
import vendingmachine.transactions.Refunds;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
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

    private final MintMultipleNfts mintMultipleNfts;
    private final PolicyRepository policyRepository;
    private final UserConfigRepository userConfigRepository;
    private final DbOperations dbOperations;
    private final Refunds refunds;
    private final StatusPublisher statusPublisher;

    @Value("${recovery.phrase}")
    private String recoveryPhrase;
    @Value("${network.id}")
    private int networkId;
    @Value("${network.magic}")
    private long networkMagic;

    public VendingMachineService(MintMultipleNfts mintMultipleNfts, PolicyRepository policyRepository, UserConfigRepository userConfigRepository, DbOperations dbOperations, Refunds refunds, StatusPublisher statusPublisher) {
        this.mintMultipleNfts = mintMultipleNfts;
        this.policyRepository = policyRepository;
        this.userConfigRepository = userConfigRepository;
        this.dbOperations = dbOperations;
        this.refunds = refunds;
        this.statusPublisher = statusPublisher;
    }

    public void startMinting(String userAddress) throws SQLException {
        if (activeSessions.containsKey(userAddress)) {
            log.info("Minting already in progress for user: {}", userAddress);
            return;
        }

        Session session = new Session();
        activeSessions.put(userAddress, session);

        Account sender = new Account(new Network(networkId, networkMagic), recoveryPhrase);
        Policy policy = dbOperations.getPolicyByWallet(userAddress);
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(userAddress);
        String slot = userConfig.getPolicySlot();
        int mintLimitPerTx = userConfig.getNFTsToMintPerTx();
        int amountOfNftsNotToMint = userConfig.getAmountOfNFTsNotToMint();

        new Thread(() -> {
            try {
                log.info("Calling minting logic for user: {}", userAddress);
                statusPublisher.sendMintStatus(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STARTED, "", "Minting process started and being monitored"));
                mintMultipleNfts.queue(sender, policy, slot, mintLimitPerTx, amountOfNftsNotToMint, session.getStopFlag(), userConfig.getOwnerWalletAddress());
                Thread.sleep(5000);

            } catch (CborSerializationException | SQLException | InterruptedException | ApiException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                activeSessions.remove(userAddress);
                log.info("Stopped minting loop for user: {}", userAddress);
                try {
                    statusPublisher.sendMintStatus(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STOPPED, "", "Minting has been stopped"));
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
            statusPublisher.sendMintStatus(userConfig.getOwnerWalletAddress(), new MintStatusMessage(policy.getPolicyId(), MintStatus.STARTED, "", "Minting will be stopped after the last submitted transaction is confirmed"));
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
        Account account = new Account(new Network(networkId, networkMagic), recoveryPhrase);

        int amountOfRefundsPerTx = userConfig.getRefundsPerTxLimit();

        new Thread(() -> {
            try {
                statusPublisher.sendRefundStatus(userConfig.getOwnerWalletAddress(), new RefundStatusMessage(policy.getPolicyId(), RefundStatus.INITIATED, "", "Refund process started and being monitored"));

                log.info("Calling refund logic for user: {}", userAddress);
                refunds.startRefund(account, policy, amountOfRefundsPerTx, session.getStopFlag(), userConfig.getOwnerWalletAddress());
                Thread.sleep(5000);

            } catch (CborSerializationException | SQLException | InterruptedException | ApiException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                activeSessions.remove(userAddress);
                log.info("Stopped refund loop for user: {}", userAddress);
                try {
                    statusPublisher.sendRefundStatus(userConfig.getOwnerWalletAddress(), new RefundStatusMessage(policy.getPolicyId(), RefundStatus.STOPPED, "", "Refund process has been stopped"));
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
            statusPublisher.sendRefundStatus(userConfig.getOwnerWalletAddress(), new RefundStatusMessage(policy.getPolicyId(), RefundStatus.STOPPED, "", "Refund process has been stopped"));
        } else {
            log.info("No active refunding session found for user: {}", userAddress);
        }
    }
}