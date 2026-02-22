package vendingmachine.services;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.crypto.KeyGenUtil;
import com.bloxbean.cardano.client.crypto.Keys;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vendingmachine.model.NftMetadata;
import vendingmachine.model.PolicyObject;
import vendingmachine.model.UserConfig;
import vendingmachine.repository.NftMetadataRepository;
import vendingmachine.repository.PolicyRepository;
import vendingmachine.repository.UserConfigRepository;
import vendingmachine.utils.DbOperations;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigService {
    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private final UserConfigRepository userConfigRepository;
    private final NftMetadataRepository nftMetadataRepository;
    private final PolicyRepository policyRepository;
    private final DbOperations dbOperations;
    private final ReadMetadataService readMetadataService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final BlockService blockService;

    public ConfigService(UserConfigRepository userConfigRepository, NftMetadataRepository nftMetadataRepository, DbOperations dbOperations, PolicyRepository policyRepository, ReadMetadataService readMetadataService, BlockService blockService) {
        this.userConfigRepository = userConfigRepository;
        this.nftMetadataRepository = nftMetadataRepository;
        this.dbOperations = dbOperations;
        this.policyRepository = policyRepository;
        this.readMetadataService = readMetadataService;
        this.blockService = blockService;
    }

    public UserConfig getConfig(String address) throws SQLException {
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(address);
        userConfig.setPolicy(dbOperations.getPolicyByWallet(address));
        userConfig.setMetadataList(nftMetadataRepository.readConfigMetadata(userConfig.getPolicyId()));

        return userConfig;
    }

    public Boolean deletePolicy(String address) throws SQLException {
        Policy policy = dbOperations.getPolicyByWallet(address);

        if (policy == null) {
            return false;
        }
        try {
            policyRepository.deleteByOwnerWallet(address);
            userConfigRepository.clearPolicyDataByOwnerWalletAddress(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Policy createPolicy(String data, String address) throws CborSerializationException, ApiException {

        // Check if UserConfig exists, if not, create a new one with default values
        JSONObject obj = new JSONObject(data);
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(address);
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
        } else {
            userConfig.setCollectionName(collectionName);
        }
        int epochs = obj.getInt("policyLockEpoch");
        log.info("Creating policy for user: {}, with collection name: {}", address, collectionName);

        Keys keys = KeyGenUtil.generateKey();
        Policy policy = readMetadataService.createEpochPolicy(collectionName, blockService.getLatestBlock().getValue().getSlot(), epochs, keys);
        PolicyObject policyObject = new PolicyObject(policy.getPolicyKeys().toString(), policy.getPolicyId(), policy.getPolicyScript().toString(), policy.getName(), keys.getVkey().getCborHex(), keys.getSkey().getCborHex(), address);
        policyRepository.save(policyObject);

        userConfig.setPolicy(policy);
        String policySlot = policy.getPolicyScript().toString().substring(policy.getPolicyScript().toString().indexOf("slot=") + 5, policy.getPolicyScript().toString().indexOf("),"));
        userConfig.setPolicySlot(policySlot);
        userConfigRepository.insertUserConfig(userConfig);

        return policy;
    }

    public Boolean createMetadata(String data, String address) throws CborSerializationException, SQLException, JsonProcessingException {
        JSONObject obj = new JSONObject(data);
        JSONArray metadataJsonArray = obj.getJSONArray("metadata");
        ArrayList<NftMetadata> metadataList = new ArrayList<>();
        Policy policy = dbOperations.getPolicyByWallet(address);

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

            String attributesJson = null;
            try {
                attributesJson = mapper.writeValueAsString(attributes);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            NftMetadata metadata = new NftMetadata(name, ipfs, attributesJson, policy.getPolicyId());
            metadataList.add(metadata);
        }
        Collections.shuffle(metadataList); // shuffle to not mint all NFTs in a row
        nftMetadataRepository.insertNftMetadataList(metadataList);

        return true;
    }

    public Boolean deleteMetadata(String address) throws SQLException {
        Policy policy = dbOperations.getPolicyByWallet(address);

        try {
            if (nftMetadataRepository.findAllByPolicyId(policy.getPolicyId()).isEmpty()) {
                return true; // No records found
            }
            nftMetadataRepository.deleteByPolicyId(policy.getPolicyId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String setParameters(String data, String address) throws SQLException, CborSerializationException {
        JSONObject obj = new JSONObject(data);

        // Check if UserConfig exists
        UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(address);
        Policy policy = null;
        policy = dbOperations.getPolicyByWallet(address);

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
            if (policy != null) {
                userConfig.setPolicy(policy);
                String policyScript = policyRepository.findPolicyScriptByOwnerWallet(address);
                userConfig.setPolicySlot(policyScript.substring(policyScript.indexOf("slot=") + 5, policyScript.indexOf("),")));
            }

            userConfigRepository.insertUserConfig(userConfig);
            return "UserConfig created successfully";
        } else {
            // Update the existing UserConfig
            userConfig.setNftPrice(obj.getInt("nftPrice"));
            userConfig.setNftsReservedPerTx(obj.getInt("nftsReservedPerTx"));
            userConfig.setNftsToMintPerTx(obj.getInt("nftsToMintPerTx"));
            userConfig.setAmountOfNFTsNotToMint(obj.getInt("nftsToNotMint"));
            userConfig.setRefundsPerTxLimit(obj.getInt("refundsPerTxLimit"));

            if (policy != null) {
                userConfig.setPolicyId(policy.getPolicyId());
            }

            userConfigRepository.save(userConfig);

            return "UserConfig updated successfully";
        }
    }
}
