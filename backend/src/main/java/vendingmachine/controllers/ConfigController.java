package vendingmachine.controllers;

import vendingmachine.model.NftMetadata;
import vendingmachine.model.UserConfig;
import vendingmachine.utils.Base;
import vendingmachine.utils.DbOperations;
import vendingmachine.utils.ReadMetadata;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.crypto.KeyGenUtil;
import com.bloxbean.cardano.client.crypto.Keys;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/minter")
public class ConfigController extends Base {

    // get user config for the wallet address
    @PostMapping("/getconfig")
    public ResponseEntity<?> getConfig(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        JSONObject obj = new JSONObject(data);

        UserConfig userConfig = DbOperations.getUserConfig(obj.getString("address"));

        if (userConfig != null) {
            return ResponseEntity.ok(userConfig);
        } else {
            return ResponseEntity.ok("No config found");
        }
    }

    // delete user's policy
    @PostMapping("/deletepolicy")
    public ResponseEntity<?> deletePolicy(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        JSONObject obj = new JSONObject(data);

        // if policy is deleted, metadata is deleted as well

        Policy policy = DbOperations.getPolicyByWallet(obj.getString("address"));

        if(DbOperations.deletePolicy(obj.getString("address")) && DbOperations.deleteMetadata(policy.getPolicyId())){
            return ResponseEntity.ok("");
        }
        else{
            return ResponseEntity.badRequest().body("No policy found");
        }
    }

    // create a new policy for the user
    @PostMapping("/createpolicy")
    public ResponseEntity<?> createPolicy(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {

        // Check if UserConfig exists, if not, create a new one with default values
        JSONObject obj = new JSONObject(data);
        UserConfig userConfig = DbOperations.getUserConfig(obj.getString("address"));
        String userWalletAddress = obj.getString("address");
        String collectionName = obj.getString("collectionName");

        if (userConfig == null) {
            userConfig = new UserConfig(
                    obj.getString("address"),
                    obj.getString("collectionName"),
                    obj.getInt("nftPrice"),
                    obj.getInt("nftsReservedPerTx"),
                    obj.getInt("nftsToMintPerTx"),
                    obj.getInt("nftsToNotMint"),
                    obj.getInt("refundsPerTxLimit")
            );
        }else{
            userConfig.setCollectionName(collectionName);
        }

        int epochs = obj.getInt("policyLockEpoch");

        System.out.println("Creating policy for user: " + userWalletAddress + ", with collection name: " + collectionName);

        Keys keys = KeyGenUtil.generateKey();
        Policy policy = ReadMetadata.createEpochPolicy(collectionName, blockService.getLatestBlock().getValue().getSlot(), epochs, keys);
        DbOperations.insertPolicyWithAddress(policy, keys.getVkey().getCborHex(), keys.getSkey().getCborHex(), userWalletAddress); // save the new policy in db

        userConfig.setPolicy(policy);
        userConfig.setPolicySlot(DbOperations.getPolicySlot(policy.getName()));
        DbOperations.insertConfig(userConfig);

        return ResponseEntity.ok(policy.getPolicyId());
    }

    // save collection metadata
    // metadata is a json list of NFTs with their attributes
    // metadata is stored in the database and associated with the user's policy id
    @PostMapping("/createmetadata")
    public ResponseEntity<?> createMetadata(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        // metadata is not editable
        JSONObject obj = new JSONObject(data);
        JSONArray metadataJsonArray = obj.getJSONArray("metadata");
        ArrayList<NftMetadata> metadataList = new ArrayList<>();
        Policy policy = DbOperations.getPolicyByWallet(obj.getString("address"));

        for (int i = 0; i < metadataJsonArray.length(); i++) {
            JSONObject nftJson = metadataJsonArray.getJSONObject(i);
            if (!nftJson.has("name") || !nftJson.has("image") || !nftJson.has("attributes")) {
                return ResponseEntity.badRequest().body("Invalid metadata format");
            }
            String name = nftJson.getString("name");
            String ipfs = nftJson.getString("image");

            JSONArray attributesArray = nftJson.getJSONArray("attributes");
            Map<String, String> attributes = new HashMap<>();

            for (int j = 0; j < attributesArray.length(); j++) {
                JSONObject attr = attributesArray.getJSONObject(j);
                String key = attr.getString("trait_type");
                String value = attr.getString("value");
                attributes.put(key, value);
            }

            NftMetadata metadata = new NftMetadata(name, ipfs, attributes);
            metadataList.add(metadata);
        }

        Collections.shuffle(metadataList); // shuffle to not mint all NFTs in a row

        DbOperations.saveMetadata(metadataList, policy.getPolicyId());

        return ResponseEntity.ok("Metadata created successfully");
    }

    // delete metadata associated with user's policy
    @PostMapping("/deletemetadata")
    public ResponseEntity<?> deleteMetadata(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        JSONObject obj = new JSONObject(data);
        Policy policy = DbOperations.getPolicyByWallet(obj.getString("address"));

        if(DbOperations.deleteMetadata(policy.getPolicyId())){
            return ResponseEntity.ok("poli");
        }
        else{
            return ResponseEntity.badRequest().body("No metadata found");
        }
    }

    // set user's parameters for minting and refunds
    @PostMapping("/setparameters")
    public ResponseEntity<?> setParameters(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        JSONObject obj = new JSONObject(data);

        // Check if UserConfig exists
        UserConfig userConfig = DbOperations.getUserConfig(obj.getString("walletAddress"));
        Policy policy = null;
        policy = DbOperations.getPolicyByWallet(obj.getString("walletAddress"));

        if (userConfig == null) {
            userConfig = new UserConfig(
                    obj.getString("walletAddress"),
                    obj.getString("collectionName"),
                    obj.getInt("nftPrice"),
                    obj.getInt("nftsReservedPerTx"),
                    obj.getInt("nftsToMintPerTx"),
                    obj.getInt("nftsToNotMint"),
                    obj.getInt("refundsPerTxLimit")
            );
            if(policy!=null){
                userConfig.setPolicy(policy);
                userConfig.setPolicySlot(DbOperations.getPolicySlot(policy.getName()));
            }

            DbOperations.insertConfig(userConfig);
            return ResponseEntity.ok("UserConfig created successfully");
        } else {
            // Update the existing UserConfig
            userConfig.setNFTPrice(obj.getInt("nftPrice"));
            userConfig.setNFTsReservedPerTx(obj.getInt("nftsReservedPerTx"));
            userConfig.setNFTsToMintPerTx(obj.getInt("nftsToMintPerTx"));
            userConfig.setAmountOfNFTsNotToMint(obj.getInt("nftsToNotMint"));
            userConfig.setRefundsPerTxLimit(obj.getInt("refundsPerTxLimit"));

            DbOperations.updateUserConfig(userConfig);
            return ResponseEntity.ok("UserConfig updated successfully");
        }
    }
}