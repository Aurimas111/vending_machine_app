package vendingmachine.utils;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.*;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.util.JsonUtil;
import org.springframework.stereotype.Service;

@Service
public class TransactionHelper {

    private final BlockService blockService;
    private final TransactionService transactionService;

    public TransactionHelper(BlockService blockService, TransactionService transactionService) {
        this.blockService = blockService;
        this.transactionService = transactionService;
    }

    public long getTtl() throws ApiException {
        Block block = blockService.getLatestBlock().getValue();
        long slot = block.getSlot();
        return slot + 2000;
    }

    public void waitForTransaction(Result<TransactionResult> result) {
        try {
            if (result.isSuccessful()) { //Wait for transaction to be produced
                int count = 0;
                while (count < 180) {
                    Result<TransactionContent> txnResult = transactionService.getTransaction(result.getValue().getTransactionId());
                    if (txnResult.isSuccessful()) {
                        System.out.println(JsonUtil.getPrettyJson(txnResult.getValue()));
                        break;
                    } else {
                        System.out.println("Waiting for transaction to be processed ....");
                    }

                    count++;
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
