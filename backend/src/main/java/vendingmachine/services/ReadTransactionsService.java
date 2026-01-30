package vendingmachine.services;

import com.bloxbean.cardano.client.api.common.OrderEnum;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.model.AddressTransactionContent;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.backend.model.TxContentUtxo;
import com.bloxbean.cardano.client.backend.api.AddressService;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import org.springframework.stereotype.Service;
import vendingmachine.model.PolicyObject;
import vendingmachine.repository.TransactionDataRepository;
import vendingmachine.model.TransactionData;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReadTransactionsService {

    private final TransactionDataRepository transactionDataRepository;
    private final AddressService addressService;
    private final TransactionService transactionService;

    public ReadTransactionsService(TransactionDataRepository transactionDataRepository, AddressService addressService, TransactionService transactionService) {
        this.transactionDataRepository = transactionDataRepository;
        this.addressService = addressService;
        this.transactionService = transactionService;
    }

    public void readTx(String mintingAddress, int mintPrice, int limitAmountPerTx, PolicyObject policy) throws ApiException {

        // Read all transactions received to the minting address and save their txhash, blocktime, blockheight, txindex
        Result<List<AddressTransactionContent>> result;
        ArrayList<AddressTransactionContent> tx = new ArrayList<>();
        ArrayList<TransactionData> transactionsToSave = new ArrayList<>();

        int pages = 1;
        while (true) {
            result = addressService.getTransactions(mintingAddress, 100, pages, OrderEnum.asc);

            for (int i = 0; i < result.getValue().size(); i++)
                tx.add((new AddressTransactionContent(
                        result.getValue().get(i).getTxHash(),
                        result.getValue().get(i).getTxIndex(),
                        result.getValue().get(i).getBlockHeight(),
                        result.getValue().get(i).getBlockTime())));

            if (result.getValue().isEmpty()) {
                break;
            }

            pages++;
        }

        Set<String> existingTxHashes = new HashSet<>(transactionDataRepository.findExistingTxHashes(tx.stream().map(AddressTransactionContent::getTxHash).collect(Collectors.toList())));

        // Txhash allows to get all tx utxos
        for (int i = 0; i < tx.size(); i++) {
            Result<TxContentUtxo> txUtxo = null;
            try {
                txUtxo = transactionService.getTransactionUtxos(tx.get(i).getTxHash());
            } catch (ApiException e) {
                e.printStackTrace();
            }

            // filter out valid transactions that have outputs with minting address, mint price and are not sent by minting address
            for (int j = 0; j < (txUtxo.getValue().getOutputs().size()); j++) {
                Boolean refunded = null;
                if (txUtxo.getValue().getOutputs().get(j).getAddress().equals(mintingAddress) &&
                        Integer.parseInt(txUtxo.getValue().getOutputs().get(j).getAmount().get(0).getQuantity()) >= mintPrice &&
                        txUtxo.getValue().getOutputs().get(j).getAmount().get(0).getUnit().equals("lovelace") && !Objects.equals(txUtxo.getValue().getInputs().get(0).getAddress(), mintingAddress)) { //Patikrinti ar nera problemu su tuo kad naudotojas galimai su 1 output isisunte daugiau nei viena skiritnga asseta

                    int amountToMint = Integer.parseInt(txUtxo.getValue().getOutputs().get(j).getAmount().get(0).getQuantity()) / mintPrice;
                    if (amountToMint >= limitAmountPerTx)
                        amountToMint = limitAmountPerTx;

                    int refund = Integer.parseInt(txUtxo.getValue().getOutputs().get(j).getAmount().get(0).getQuantity()) - (amountToMint * mintPrice);
                    if (refund < 1500000) { //cant make a transaction with <1 ada
                        refund = 0;
                    } else {
                        refunded = false;
                    }

                    if (!existingTxHashes.contains(tx.get(i).getTxHash())) {
                        TransactionData transactionData = new TransactionData(
                                tx.get(i).getTxHash(),
                                txUtxo.getValue().getInputs().get(0).getAddress(),
                                Integer.parseInt(txUtxo.getValue().getOutputs().get(j).getAmount().get(0).getQuantity()),
                                amountToMint,
                                0
                        );
                        transactionData.setTxIndex(tx.get(i).getTxIndex());
                        transactionData.setBlockHeight((int) tx.get(i).getBlockHeight());
                        transactionData.setBlockTime((int) tx.get(i).getBlockTime());
                        transactionData.setValidAddress(true);
                        transactionData.setRefund(refund);
                        transactionData.setPolicyId(policy.getPolicyId());
                        transactionData.setRefunded(refunded);

                        transactionsToSave.add(transactionData);
                    }
                }
            }
            if (!transactionsToSave.isEmpty()) {
                transactionDataRepository.saveAll(transactionsToSave);
            }
        }
    }

    public long getTxDate(String txHash) throws ApiException {
        Result<TransactionContent> txContentResult = transactionService.getTransaction(txHash);
        long blockTime = 0;

        if (txContentResult.isSuccessful()) {
            blockTime = txContentResult.getValue().getBlockTime();
        }
        return blockTime;
    }
}