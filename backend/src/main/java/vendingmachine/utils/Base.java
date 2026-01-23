package vendingmachine.utils;

import com.bloxbean.cardano.client.api.ProtocolParamsSupplier;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.api.helper.UtxoTransactionBuilder;
import com.bloxbean.cardano.client.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.*;
import com.bloxbean.cardano.client.backend.blockfrost.common.Constants;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.util.JsonUtil;


public class Base {
    protected static BackendService backendService;
    protected static FeeCalculationService feeCalculationService;
    protected static TransactionHelperService transactionHelperService;
    protected static TransactionService transactionService;
    protected static BlockService blockService;
    protected static AssetService assetService;
    protected static NetworkInfoService networkInfoService;
    protected static UtxoService utxoService;
    protected static EpochService epochService;
    protected static UtxoTransactionBuilder utxoTransactionBuilder;
    protected static UtxoSupplier utxoSupplier;
    protected static ProtocolParamsSupplier protocolParamsSupplier;
    protected static AddressService addressService;

    static {
        backendService = new BFBackendService(Constants.BLOCKFROST_PREPROD_URL, Constant.BF_PROJECT_KEY);

        feeCalculationService = backendService.getFeeCalculationService();
        transactionHelperService = backendService.getTransactionHelperService();
        transactionService = backendService.getTransactionService();
        blockService = backendService.getBlockService();
        assetService = backendService.getAssetService();
        utxoService = backendService.getUtxoService();
        networkInfoService = backendService.getNetworkInfoService();
        epochService = backendService.getEpochService();
        utxoTransactionBuilder = backendService.getUtxoTransactionBuilder();
        utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        protocolParamsSupplier = new DefaultProtocolParamsSupplier(epochService);
        addressService = backendService.getAddressService();
    }

    protected static long getTtl() throws ApiException {
        Block block = blockService.getLatestBlock().getValue();
        long slot = block.getSlot();
        return slot + 2000;
    }

    protected void waitForTransaction(Result<TransactionResult> result) {
        try {
            if (result.isSuccessful()) { //Wait for transaction to be mined
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