package vendingmachine.controllers;

import vendingmachine.model.NftMetadata;
import vendingmachine.model.TransactionData;
import vendingmachine.model.TransactionResponse;
import vendingmachine.model.UserConfig;
import vendingmachine.utils.Constant;
import vendingmachine.utils.DbOperations;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vendingmachine.utils.ReadWalletTx;
import java.sql.SQLException;
import java.util.ArrayList;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/minter")
public class TransactionController {

     // get transactions received to minter address
     // returns a list of transactions with their status, metadata, amount minted, amount to mint, refund status, nft receiver wallet address
     @PostMapping("/transactions")
     public ResponseEntity<?> getTransactions(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {

         JSONObject obj = new JSONObject(data);
         String address = obj.getString("address");
         Account account = new Account(Networks.preprod(), Constant.RECOVERY_PHRASE);
         Policy policy = DbOperations.getPolicyByWallet(address);
         UserConfig userConfig = DbOperations.getUserConfig(address);

         ReadWalletTx.readTx(account.baseAddress(), userConfig.getNFTPrice(), userConfig.getNFTsReservedPerTx(), policy);

         ArrayList<TransactionData> transactions = DbOperations.readDetailedTxData(policy);
         for(int i = 0; i < transactions.size(); i++) {
             if (transactions.get(i).getAmountToMint() == 0 && transactions.get(i).getAmountMinted()>0) {
                 transactions.get(i).setStatus("Minted");
             } else if( transactions.get(i).getAmountToMint()>0) {
                 transactions.get(i).setStatus("Minting");
             }else if(transactions.get(i).getRefunded() != null && transactions.get(i).getRefunded()) {
                 transactions.get(i).setStatus("Refunded");
             }else{
                 transactions.get(i).setStatus("Pending");
             }
         }

         ArrayList<NftMetadata> metadata = DbOperations.readWalletAssociatedMetadata(policy.getPolicyId());
         int amountMinted = DbOperations.getMintedNftCount(policy.getPolicyId());

         TransactionResponse transactionResponse = new TransactionResponse(transactions, metadata.size(), amountMinted, (double) userConfig.getNFTPrice() /1000000, account.baseAddress());

        return ResponseEntity.ok(transactionResponse);
    }
}