package vendingmachine.services;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.common.model.Network;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vendingmachine.model.NftMetadata;
import vendingmachine.model.PolicyObject;
import vendingmachine.model.TransactionData;
import vendingmachine.dto.response.TransactionResponse;
import vendingmachine.model.UserConfig;
import vendingmachine.repository.NftMetadataRepository;
import vendingmachine.repository.PolicyRepository;
import vendingmachine.repository.TransactionDataRepository;
import vendingmachine.repository.UserConfigRepository;

import java.util.ArrayList;

@Service
public class TransactionFetchingService {

    private final PolicyRepository policyRepository;
    private final UserConfigRepository userConfigRepository;
    private final ReadTransactionsService readTransactionsService;
    private final TransactionDataRepository transactionDataRepository;
    private final NftMetadataRepository nftMetadataRepository;

    @Value("${recovery.phrase}")
    private String recoveryPhrase;
    @Value("${network.id}")
    private int networkId;
    @Value("${network.magic}")
    private long networkMagic;

    public TransactionFetchingService(ReadTransactionsService readTransactionsService, PolicyRepository policyRepository, UserConfigRepository userConfigRepository, TransactionDataRepository transactionDataRepository, NftMetadataRepository nftMetadataRepository) {
        this.readTransactionsService = readTransactionsService;
        this.policyRepository = policyRepository;
        this.userConfigRepository = userConfigRepository;
        this.transactionDataRepository = transactionDataRepository;
        this.nftMetadataRepository = nftMetadataRepository;
    }

    public TransactionResponse getTransactions(String address) throws ApiException {
        Account account = new Account(new Network(networkId, networkMagic), recoveryPhrase);
        PolicyObject policy = policyRepository.findByOwnerWallet(address);
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(address);

        readTransactionsService.readTx(account.baseAddress(), userConfig.getNftPrice(), userConfig.getNftsReservedPerTx(), policy);

        ArrayList<TransactionData> transactions = transactionDataRepository.findAllByPolicyId(policy.getPolicyId());
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getAmountToMint() == 0 && transactions.get(i).getAmountMinted() > 0) {
                transactions.get(i).setStatus("Minted");
            } else if (transactions.get(i).getAmountToMint() > 0) {
                transactions.get(i).setStatus("Minting");
            } else if (transactions.get(i).getRefunded() != null && transactions.get(i).getRefunded()) {
                transactions.get(i).setStatus("Refunded");
            } else {
                transactions.get(i).setStatus("Pending");
            }
        }

        ArrayList<NftMetadata> metadata = nftMetadataRepository.findAllByPolicyId(policy.getPolicyId());
        int amountMinted = transactionDataRepository.sumAmountMintedByPolicyId(policy.getPolicyId());

        TransactionResponse transactionResponse = new TransactionResponse(transactions,
                metadata.size(),
                amountMinted,
                (double) userConfig.getNftPrice() / 1000000,
                account.baseAddress());

        return transactionResponse;
    }
}