package vendingmachine.transactions;

import org.springframework.stereotype.Service;
import vendingmachine.dto.RefundDataDTO;
import vendingmachine.model.RefundStatus;
import vendingmachine.model.RefundStatusMessage;
import vendingmachine.repository.TransactionDataRepository;
import vendingmachine.services.VendingMachineStatusPublisher;
import vendingmachine.utils.Base;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.cip.cip20.MessageMetadata;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.Policy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class Refunds extends Base {

    ArrayList<RefundDataDTO> refundDataDTOS = new ArrayList<>();

    private final TransactionDataRepository transactionDataRepository;

    public Refunds(TransactionDataRepository transactionDataRepository) {
        this.transactionDataRepository = transactionDataRepository;
    }

    public void startRefund(Account sender, Policy policy, int refundReceiverLimit, AtomicBoolean stopFlag, VendingMachineStatusPublisher publisher, String ownerWalletAddress) throws SQLException, CborSerializationException, ApiException, InterruptedException {

        boolean refundsDone = false;
        String senderAddress = sender.baseAddress();

        while(!stopFlag.get()) {
            System.out.println("Refunds started for policy: " + policy.getPolicyId());

            refundDataDTOS = transactionDataRepository.findRefundDataByPolicyId(policy.getPolicyId(), refundReceiverLimit);
            if (refundDataDTOS.isEmpty())
                refundsDone = true;

            System.out.printf("Refunds to be processed: %d\n", refundDataDTOS.size());

            while (!refundsDone) {
                if (stopFlag.get()) {
                    System.out.println("Refund process stopped externally.");
                    return;
                }

                Tx tx = new Tx();
                for (int i = 0; i < refundDataDTOS.size(); i++) {
                    tx.payToAddress(refundDataDTOS.get(i).getRefundAddress(), Amount.ada((refundDataDTOS.get(i).getAmount() / 1000000) - 0.2));
                }

                tx.attachMetadata(MessageMetadata.create().add("NFT mint refund")).from(senderAddress);

                QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
                Result<String> result = quickTxBuilder
                        .compose(tx)
                        .feePayer(senderAddress)
                        .withSigner(SignerProviders.signerFrom(sender))
                        .completeAndWait(System.out::println);

                if (result.isSuccessful()) {
                    publisher.send(ownerWalletAddress, new RefundStatusMessage(
                            policy.getPolicyId(),
                            RefundStatus.COMPLETED,
                            result.getValue(),
                            "Refund confirmed on Cardano network"
                    ));
                    // update db with refunded information and read more txs for refunds
                    List<String> txHashList = refundDataDTOS.stream()
                            .map(RefundDataDTO::getTxHash)
                            .collect(Collectors.toList());
                    transactionDataRepository.updateRefundedTxsByTxHashesAndPolicyId(txHashList, policy.getPolicyId());
                    refundDataDTOS.clear();

                    System.out.println("sleeping for 10 seconds");
                    TimeUnit.SECONDS.sleep(10);

                    refundDataDTOS = transactionDataRepository.findRefundDataByPolicyId(policy.getPolicyId(), refundReceiverLimit);
                    if (refundDataDTOS.isEmpty())
                        refundsDone = true;


                } else {
                    publisher.send(ownerWalletAddress, new RefundStatusMessage(
                            policy.getPolicyId(),
                            RefundStatus.FAILED,
                            result.getValue(),
                            "Refund Failed"
                    ));
                    System.out.println("Transaction failed: " + result);
                }
            }

            System.out.println("Refunds completed!");
            System.out.println("Sleeping for 10 seconds before next check");
            TimeUnit.SECONDS.sleep(10);
        }
    }
}