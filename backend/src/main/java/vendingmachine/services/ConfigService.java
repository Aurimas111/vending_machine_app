package vendingmachine.services;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.crypto.KeyGenUtil;
import com.bloxbean.cardano.client.crypto.Keys;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import vendingmachine.model.NftMetadata;
import vendingmachine.model.UserConfig;
import vendingmachine.utils.Base;
import vendingmachine.utils.DbOperations;
import vendingmachine.utils.ReadMetadata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigService extends Base {

    public UserConfig getConfig(String address) throws SQLException, CborSerializationException {
        return DbOperations.getUserConfig(address);
    }

    public Boolean deletePolicy(String address) throws SQLException, CborSerializationException {
        Policy policy = DbOperations.getPolicyByWallet(address);

        if(DbOperations.deletePolicy(address) && DbOperations.deleteMetadata(policy.getPolicyId())){
            return true;
        } else {
            return false;
        }
    }

    public Policy createPolicy(String data, String address) throws SQLException, CborSerializationException, ApiException {

        // Check if UserConfig exists, if not, create a new one with default values
        JSONObject obj = new JSONObject(data);
        UserConfig userConfig = DbOperations.getUserConfig(address);
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
        System.out.println("Creating policy for user: " + address + ", with collection name: " + collectionName);

        Keys keys = KeyGenUtil.generateKey();
        Policy policy = ReadMetadata.createEpochPolicy(collectionName, blockService.getLatestBlock().getValue().getSlot(), epochs, keys);
        DbOperations.insertPolicyWithAddress(policy,
                keys.getVkey().getCborHex(),
                keys.getSkey().getCborHex(),
                address); // save the new policy in db

        userConfig.setPolicy(policy);
        userConfig.setPolicySlot(DbOperations.getPolicySlot(policy.getName()));
        DbOperations.insertConfig(userConfig);

        return policy;
    }

    public Boolean createMetadata(String data, String address) throws CborSerializationException, SQLException {
        JSONObject obj = new JSONObject(data);
        JSONArray metadataJsonArray = obj.getJSONArray("metadata");
        ArrayList<NftMetadata> metadataList = new ArrayList<>();
        Policy policy = DbOperations.getPolicyByWallet(address);

        for (int i = 0; i < metadataJsonArray.length(); i++) {
            JSONObject nftJson = metadataJsonArray.getJSONObject(i);
            if (!nftJson.has("name") || !nftJson.has("image") || !nftJson.has("attributes")) {
                return false;
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

        return true;
    }

    public Boolean deleteMetadata(String address) throws SQLException, CborSerializationException {
        Policy policy = DbOperations.getPolicyByWallet(address);

        if(DbOperations.deleteMetadata(policy.getPolicyId())){
            return true;
        }
        else{
            return false;
        }
    }

    public String setParameters(String data, String address) throws SQLException, CborSerializationException {
        JSONObject obj = new JSONObject(data);

        // Check if UserConfig exists
        UserConfig userConfig = DbOperations.getUserConfig(address);
        Policy policy = null;
        policy = DbOperations.getPolicyByWallet(address);

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
            return "UserConfig created successfully";
        } else {
            // Update the existing UserConfig
            userConfig.setNFTPrice(obj.getInt("nftPrice"));
            userConfig.setNFTsReservedPerTx(obj.getInt("nftsReservedPerTx"));
            userConfig.setNFTsToMintPerTx(obj.getInt("nftsToMintPerTx"));
            userConfig.setAmountOfNFTsNotToMint(obj.getInt("nftsToNotMint"));
            userConfig.setRefundsPerTxLimit(obj.getInt("refundsPerTxLimit"));

            DbOperations.updateUserConfig(userConfig);
            return "UserConfig updated successfully";
        }
    }
}
