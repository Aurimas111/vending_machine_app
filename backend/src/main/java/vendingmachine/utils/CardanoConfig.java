package vendingmachine.utils;

import com.bloxbean.cardano.client.api.ProtocolParamsSupplier;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.backend.api.*;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardanoConfig {

    @Value("${blockfrost.api.url}")
    private String blockfrostUrl;
    @Value("${blockfrost.api.key}")
    private String blockfrostApiKey;

    @Bean
    public BackendService backendService() {
        return new BFBackendService(blockfrostUrl, blockfrostApiKey);
    }

    @Bean
    public FeeCalculationService feeCalculationService(BackendService backendService) {
        return backendService.getFeeCalculationService();
    }

    @Bean
    public TransactionHelperService transactionHelperService(BackendService backendService) {
        return backendService.getTransactionHelperService();
    }

    @Bean
    public TransactionService transactionService(BackendService backendService) {
        return backendService.getTransactionService();
    }

    @Bean
    public BlockService blockService(BackendService backendService) {
        return backendService.getBlockService();
    }

    @Bean
    public AssetService assetService(BackendService backendService) {
        return backendService.getAssetService();
    }

    @Bean
    public NetworkInfoService networkInfoService(BackendService backendService) {
        return backendService.getNetworkInfoService();
    }

    @Bean
    public UtxoService utxoService(BackendService backendService) {
        return backendService.getUtxoService();
    }

    @Bean
    public EpochService epochService(BackendService backendService) {
        return backendService.getEpochService();
    }

    @Bean
    public UtxoSupplier utxoSupplier(UtxoService utxoService) {
        return new DefaultUtxoSupplier(utxoService);
    }

    @Bean
    public ProtocolParamsSupplier protocolParamsSupplier(EpochService epochService) {
        return new DefaultProtocolParamsSupplier(epochService);
    }

    @Bean
    public AddressService addressService(BackendService backendService) {
        return backendService.getAddressService();
    }
}
