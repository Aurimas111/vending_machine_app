package vendingmachine.transactions;

import vendingmachine.utils.Base;
import vendingmachine.utils.Constant;
import vendingmachine.utils.DbOperations;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.*;
import com.bloxbean.cardano.client.backend.blockfrost.common.Constants;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.cip.cip20.MessageMetadata;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.Policy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class Refunds extends Base {

    static ArrayList<String> refundAddresses = new ArrayList<>();
    static ArrayList<Integer> refundAmounts = new ArrayList<>();
    static ArrayList<String> txHashes = new ArrayList<>();


    public static void startRefund(Account sender, Policy policy, int refundReceiverLimit, AtomicBoolean stopFlag) throws SQLException, CborSerializationException, ApiException, InterruptedException {
        boolean refundsDone = false;
        new Refunds();

        BackendService backendService =
                new BFBackendService(Constants.BLOCKFROST_PREPROD_URL, Constant.BF_PROJECT_KEY);

        String senderAddress = sender.baseAddress();

        while(!stopFlag.get()) {
            System.out.println("Refunds started for policy: " + policy.getPolicyId());

            DbOperations.readRefundData(policy, refundAddresses, refundAmounts, txHashes, refundReceiverLimit);
            if (refundAddresses.isEmpty())
                refundsDone = true;

            System.out.printf("Refunds to be processed: %d\n", refundAddresses.size());

            while (!refundsDone) {
                if (stopFlag.get()) {
                    System.out.println("Refund process stopped externally.");
                    return;
                }

                Tx tx = new Tx();
                for (int i = 0; i < refundAddresses.size(); i++) {

                    tx.payToAddress(refundAddresses.get(i), Amount.ada((refundAmounts.get(i) / 1000000) - 0.2));
                }

                tx.attachMetadata(MessageMetadata.create().add("NFT mint refund")).from(senderAddress);

                QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
                Result<String> result = quickTxBuilder
                        .compose(tx)
                        .feePayer(senderAddress)
                        .withSigner(SignerProviders.signerFrom(sender))
                        .completeAndWait(System.out::println);

                if (result.isSuccessful()) {
                    // update db with refunded information and read more txs for refunds
                    DbOperations.updateRefundedTxs(policy, txHashes);
                    refundAddresses.clear();
                    refundAmounts.clear();
                    txHashes.clear();

                    System.out.println("sleeping for 10 seconds");
                    TimeUnit.SECONDS.sleep(10);

                    DbOperations.readRefundData(policy, refundAddresses, refundAmounts, txHashes, refundReceiverLimit);
                    if (refundAddresses.isEmpty())
                        refundsDone = true;


                } else {
                    System.out.println("Transaction failed: " + result);
                }
            }

            System.out.println("Refunds completed!");
            System.out.println("Sleeping for 10 seconds before next check");
            TimeUnit.SECONDS.sleep(10);
        }
    }
}