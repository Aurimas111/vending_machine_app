package vendingmachine.utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;
import vendingmachine.model.*;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.bloxbean.cardano.client.transaction.spec.script.RequireTimeBefore;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptAll;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;
import vendingmachine.repository.PolicyRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class DbOperations {

    private final PolicyRepository policyRepository;

    public DbOperations(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    private Connection connectToDb() {

        String DB_URL = Constant.DB_URL;
        String USER = Constant.DB_USER;
        String PASS = Constant.DB_PASSWORD;
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public Policy getPolicyByWallet(String walletAddress) throws SQLException {
        String slot = null;

        PolicyObject policyObject = policyRepository.findByOwnerWallet(walletAddress);

        String script = policyObject.getPolicyScript();
        slot = script.substring(script.indexOf("slot=") + 5, script.indexOf("),"));
        if (policyObject.getVerificationKey() == null || policyObject.getSigningKey() == null) {
            return null;
        }

        RequireTimeBefore requireTimeBefore = new RequireTimeBefore(Long.parseLong(slot));
        VerificationKey verificationKey = new VerificationKey();
        verificationKey.setCborHex(policyObject.getVerificationKey());

        ScriptPubkey scriptPubkey = ScriptPubkey.create(verificationKey);
        ScriptAll scriptAll = new ScriptAll().addScript(requireTimeBefore).addScript(scriptPubkey);

        SecretKey secretKey = new SecretKey();
        secretKey.setCborHex(policyObject.getSigningKey());

        return new Policy(policyObject.getName(), scriptAll).addKey(secretKey);
    }

    public void saveMetadata(ArrayList<NftMetadata> metadataList, String policy) throws SQLException {
        String sql = "INSERT INTO metadata (name, file_url, image, is_minted, attributes, policy_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (NftMetadata metadata : metadataList) {
                statement.setString(1, metadata.getName());
                statement.setString(2, metadata.getFile_url());
                statement.setString(3, metadata.getImage());
                statement.setBoolean(4, metadata.isMinted());

                Map<String, String> attributes = metadata.getDynamicAttributes();
                String attributesJson = new org.json.simple.JSONObject(attributes).toString();
                statement.setString(5, attributesJson);
                statement.setString(6, policy);

                statement.executeUpdate();
            }
        }
    }

    public ArrayList<NftMetadata> readWalletAssociatedMetadata(String policyId) throws SQLException {
        ArrayList<NftMetadata> metadata = new ArrayList<>();
        String sql = "SELECT `name`, `file_url`, `image`, `ipfs_hash`, `is_minted`, `attributes`, `policy_id`, `tx_hash`, `mint_blocktime`, `receiver` FROM `metadata` where `policy_id` =?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String fileUrl = rs.getString("file_url");
                    String image = rs.getString("image");
                    String ipfsHash = rs.getString("ipfs_hash");
                    boolean isMinted = rs.getBoolean("is_minted");
                    String txHash = rs.getString("tx_hash");
                    int blockTime = rs.getInt("mint_blocktime");
                    String receiver = rs.getString("receiver");

                    String attributesJson = rs.getString("attributes");
                    Map<String, String> attributes = parseJsonToMap(attributesJson);

                    metadata.add(new NftMetadata(name, fileUrl, image, ipfsHash, isMinted, attributes, txHash, blockTime, receiver));
                }
            }
        }
        return metadata;
    }

    private Map<String, String> parseJsonToMap(String jsonStr) {
        Map<String, String> map = new HashMap<>();
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonStr);
            for (Object key : jsonObject.keySet()) {
                map.put((String) key, (String) jsonObject.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

}