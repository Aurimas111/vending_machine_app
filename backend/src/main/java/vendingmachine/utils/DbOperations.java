package vendingmachine.utils;

import vendingmachine.model.*;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.bloxbean.cardano.client.transaction.spec.script.RequireTimeBefore;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptAll;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DbOperations {

    public static Connection connectToDb() {

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

    public static void insertTxDetailedInfo(String txHash, int txIndex, int blockHeight, int blockTime, int validAddress, int amountSent, String senderAddress,
                                            int refund, int amountToMint, String policyId, Boolean refunded) throws SQLException {

        String sql = "INSERT ignore INTO `tx`(`txHash`, `txIndex`, `blockHeight`, `blockTime`, `validAddress`, `amountSent`, `senderAddress`, `refund`, `amountToMint`, `policyId`, `refunded`) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, txHash);
            statement.setInt(2, txIndex);
            statement.setInt(3, blockHeight);
            statement.setInt(4, blockTime);
            statement.setInt(5, validAddress);
            statement.setInt(6, amountSent);
            statement.setString(7, senderAddress);
            statement.setInt(8, refund);
            statement.setInt(9, amountToMint);
            statement.setString(10, policyId);
            statement.setObject(11, refunded, java.sql.Types.BOOLEAN);

            statement.execute();
        }
    }

    public static void readTxData(Policy policy, ArrayList<TransactionData> mintTransactions) throws SQLException, CborSerializationException {
        String sql = "SELECT txhash, senderAddress, sum(amountSent), amountToMint, amountMinted FROM `tx` WHERE amountToMint > 0 and policyId= ? group by txhash ORDER BY `tx`.`blockHeight` ASC";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policy.getPolicyId());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    mintTransactions.add(new TransactionData(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getInt(5)));
                }
            }
        }
    }

    public static ArrayList<TransactionData> readDetailedTxData(Policy policy) throws SQLException, CborSerializationException {
        ArrayList<TransactionData> transactions = new ArrayList<>();
        String sql = "SELECT txhash, senderAddress, sum(amountSent), amountToMint, amountMinted, blockTime, refund, policyId, refunded FROM `tx` WHERE policyId= ? group by txhash";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policy.getPolicyId());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionData(rs.getString(1), rs.getInt(6), rs.getInt(3), rs.getString(2), rs.getInt(7), rs.getInt(4), rs.getInt(5), rs.getString(8), "", rs.getBoolean(9)));
                }
            }
        }
        return transactions;
    }

    public static void insertPolicyWithAddress(Policy policy, String vKey, String sKey, String mintingAddress) throws SQLException, CborSerializationException {
        String sql = "INSERT INTO `policy`(`policyKeys`, `policyId`, `PolicyScript`, `name`, `verificationKey`, `signingKey`, `ownerWallet`) VALUES(?,?,?,?,?,?,?)";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policy.getPolicyKeys().toString());
            statement.setString(2, policy.getPolicyId());
            statement.setString(3, policy.getPolicyScript().toString());
            statement.setString(4, policy.getName());
            statement.setString(5, vKey);
            statement.setString(6, sKey);
            statement.setString(7, mintingAddress);
            statement.execute();
        }
    }

    public static String getPolicySlot(String name) throws SQLException {
        String slot = null;
        String sql = "SELECT * FROM `policy` WHERE `name` = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String script = rs.getString("PolicyScript");
                    slot = script.substring(script.indexOf("slot=")+5, script.indexOf("),"));
                }
            }
        }
        return slot;
    }

    public static int getMintedNftCount(String policyId) throws SQLException {
        int count = 0;
        String sql = "SELECT sum(amountMinted) FROM tx where `policyId` = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        }
        return count;
    }

    public static void updateAmountToMint(String txHash, int amountToMint) throws SQLException {
        String sql = "update `tx` set `amountToMint` = ? where txHash = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, amountToMint);
            statement.setString(2, txHash);
            statement.executeUpdate();
        }
    }

    public static void updateMintedAmount(String txHash, int amountMinted) throws SQLException {
        String sql = "update `tx` set `amountMinted`=? where txHash = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, amountMinted);
            statement.setString(2, txHash);
            statement.execute();
        }
    }

    public static void updateMetadataMinted(String name, String policyId, String txHash, long date, String receiver) throws SQLException {
        String sql = "update `metadata` set `is_minted` = ?, `tx_hash` = ?, `mint_blocktime` = ?, `receiver` = ? where name = ? and policy_id = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, 1);
            statement.setString(2, txHash);
            statement.setLong(3, date);
            statement.setString(4, receiver);
            statement.setString(5, name);
            statement.setString(6, policyId);
            statement.executeUpdate();
        }
    }

    public static Policy getPolicyByWallet(String walletAddress) throws SQLException {
        Policy policy = null;
        String slot = null;
        String vkey = null;
        String skey = null;
        String name = null;
        String sql = "SELECT * FROM `policy` WHERE `ownerWallet` = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, walletAddress);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String script = rs.getString("PolicyScript");
                    slot = script.substring(script.indexOf("slot=")+5, script.indexOf("),"));
                    vkey = rs.getString("verificationKey");
                    skey = rs.getString("signingKey");
                    name = rs.getString("name");
                }
            }
        }

        if (vkey == null || skey == null) {
            return policy;
        }

        RequireTimeBefore requireTimeBefore = new RequireTimeBefore(Long.parseLong(slot));
        VerificationKey verificationKey = new VerificationKey();
        verificationKey.setCborHex(vkey);

        ScriptPubkey scriptPubkey = ScriptPubkey.create(verificationKey);
        ScriptAll scriptAll = new ScriptAll().addScript(requireTimeBefore).addScript(scriptPubkey);

        SecretKey secretKey = new SecretKey();
        secretKey.setCborHex(skey);

        return new Policy(name, scriptAll).addKey(secretKey);
    }

    public static void saveMetadata(ArrayList<NftMetadata> metadataList, String policy) throws SQLException {
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

    public static ArrayList<NftMetadata> readWalletAssociatedMetadata(String policyId) throws SQLException {
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

    public static Map<String, String> parseJsonToMap(String jsonStr) {
        Map<String, String> map = new HashMap<>();
        try {
            org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) new org.json.simple.parser.JSONParser().parse(jsonStr);
            for (Object key : jsonObject.keySet()) {
                map.put((String) key, (String) jsonObject.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static ArrayList<NftMetadata> readNotMintedMetadata(String policyId) throws SQLException {
        ArrayList<NftMetadata> metadata = new ArrayList<>();
        String sql = "SELECT`name`, `file_url`, `image`, `ipfs_hash`, `is_minted`, `attributes`, `policy_id` FROM `metadata` where is_minted = 0 and policy_id = ?";

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

                    String attributesJson = rs.getString("attributes");
                    Map<String, String> attributes = parseJsonToMap(attributesJson);

                    metadata.add(new NftMetadata(name, fileUrl, image, ipfsHash, isMinted, attributes));
                }
            }
        }
        return metadata;
    }

    public static void readRefundData(Policy policy, ArrayList<String> refundAddresses, ArrayList<Integer> refundAmounts, ArrayList<String> txHashes, int refundReceiverLimit) throws SQLException, CborSerializationException {
        String sql = "SELECT senderAddress, refund, txHash FROM `tx` WHERE refunded = 0 and refund > 0 and policyId= ? group by txhash ORDER BY `tx`.`blockHeight` ASC LIMIT ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policy.getPolicyId());
            statement.setInt(2, refundReceiverLimit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    refundAddresses.add(rs.getString(1));
                    refundAmounts.add(rs.getInt(2));
                    txHashes.add(rs.getString(3));
                }
            }
        }
    }

    public static void updateRefundedTxs(Policy policy, ArrayList<String> txHashes) throws SQLException, CborSerializationException {
        String sql = "update `tx` set refunded = ? where txHash = ? and policyId = ?";

        try (Connection connection = connectToDb()) {
            for (String txHash : txHashes) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setBoolean(1, true);
                    statement.setString(2, txHash);
                    statement.setString(3, policy.getPolicyId());
                    statement.executeUpdate();
                }
            }
        }
    }

    public static void insertConfig(UserConfig userConfig) throws SQLException, CborSerializationException {
        // check if the user config already exists
        String checkSql = "SELECT 1 FROM `userconfig` WHERE `user_address` = ?";
        try (Connection connection = connectToDb();
             PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {

            checkStatement.setString(1, userConfig.getOwnerWalletAddress());
            try (ResultSet rs = checkStatement.executeQuery()) {
                boolean exists = rs.next();
                // if it exists, update the existing record
                if (exists) {
                    updateUserConfig(userConfig);
                } else {
                    // if it does not exist, insert a new record
                    String insertSql = "INSERT INTO `userconfig`(`user_address`, `policy_id`, `policy_slot`, `collection_name`, `nft_price`, `collection_size`, `nft_reserved_per_tx`, `nft_to_mint_per_tx`, `amount_of_nft_not_to_mint`, `refund_per_tx_limit`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                        insertStatement.setString(1, userConfig.getOwnerWalletAddress());
                        insertStatement.setString(2, userConfig.getPolicy().getPolicyId());
                        insertStatement.setString(3, userConfig.getPolicySlot());
                        insertStatement.setString(4, userConfig.getCollectionName());
                        insertStatement.setInt(5, userConfig.getNFTPrice());
                        insertStatement.setInt(6, userConfig.getCollectionSize());
                        insertStatement.setInt(7, userConfig.getNFTsReservedPerTx());
                        insertStatement.setInt(8, userConfig.getNFTsToMintPerTx());
                        insertStatement.setInt(9, userConfig.getAmountOfNFTsNotToMint());
                        insertStatement.setInt(10, userConfig.getRefundsPerTxLimit());
                        insertStatement.executeUpdate();
                    }
                }
            }
        }
    }

    public static UserConfig getUserConfig(String address) throws SQLException, CborSerializationException {
        UserConfig userConfig = null;
        ArrayList<MetadataResponse> metadataList = new ArrayList<>();
        Policy policy = null;
        Policy policywId = null;
        String policySlot = null;
        String collectionName = null;
        int nftPrice = 0;
        int collectionSize = 0;
        int nftsReservedPerTx = 0;
        int nftsToMintPerTx = 0;
        int amountOfNftsNotToMint = 0;
        int refundPerTxLimit = 0;

        String sql = "SELECT * FROM `userconfig` WHERE `user_address` = ?";
        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, address);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null; // No config found for user
                }
                policySlot = rs.getString("policy_slot");
                nftPrice = rs.getInt("nft_price");
                collectionSize = rs.getInt("collection_size");
                nftsReservedPerTx = rs.getInt("nft_reserved_per_tx");
                nftsToMintPerTx = rs.getInt("nft_to_mint_per_tx");
                amountOfNftsNotToMint = rs.getInt("amount_of_nft_not_to_mint");
                refundPerTxLimit = rs.getInt("refund_per_tx_limit");
            }
        }

        policy = getPolicyByWallet(address);
        if (policy != null) {
            policywId = new Policy(policy.getPolicyScript());
            metadataList = readConfigMetadata(policywId.getPolicyId());
            collectionName = policy.getName();
        }

        userConfig = new UserConfig(address, policywId, policySlot, metadataList, collectionName, nftPrice, collectionSize, nftsReservedPerTx, nftsToMintPerTx, amountOfNftsNotToMint, refundPerTxLimit);
        return userConfig;
    }

    private static ArrayList<MetadataResponse> readConfigMetadata(String policyId) {
        ArrayList<MetadataResponse> metadata = new ArrayList<>();
        String sql = "SELECT `name`, `image`, `attributes` FROM `metadata` WHERE `policy_id` = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String image = rs.getString("image");
                    String attributesJson = rs.getString("attributes");
                    Map<String, String> attributes = parseJsonToMap(attributesJson);
                    metadata.add(new MetadataResponse(name, image, attributes));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return metadata;
    }

    public static boolean deletePolicy(String address) throws SQLException {
        boolean success = false;

        try (Connection connection = connectToDb()) {
            // delete policy
            String sql = "DELETE FROM `policy` WHERE `ownerWallet` = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, address);
                int rowsDeleted = statement.executeUpdate();

                // update user config
                String sql2 = "UPDATE `userconfig` SET `policy_id` = NULL, `policy_slot` = NULL, `collection_name` = NULL WHERE `user_address` = ?";
                try (PreparedStatement statement2 = connection.prepareStatement(sql2)) {
                    statement2.setString(1, address);
                    int rowsDeleted2 = statement2.executeUpdate();

                    if (rowsDeleted > 0 && rowsDeleted2 > 0) {
                        success = true;
                    }
                }
            }
        }
        return success;
    }

    public static boolean deleteMetadata(String policyId) {
        boolean success = false;

        try (Connection connection = connectToDb()) {
            String checkSql = "SELECT 1 FROM `metadata` WHERE `policy_id` = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(checkSql)) {
                selectStatement.setString(1, policyId);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        return true; // No record found
                    }
                }
            }

            String sql = "DELETE FROM `metadata` WHERE `policy_id` = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, policyId);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    success = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static void updateUserConfig(UserConfig userConfig) {
        String sql = "UPDATE `userconfig` SET `policy_id` = ?, `policy_slot` = ?, `collection_name` = ?, `nft_price` = ?, `collection_size` = ?, `nft_reserved_per_tx` = ?, `nft_to_mint_per_tx` = ?, `amount_of_nft_not_to_mint` = ?, `refund_per_tx_limit` = ? WHERE `user_address` = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, userConfig.getPolicy().getPolicyId());
            statement.setString(2, userConfig.getPolicySlot());
            statement.setString(3, userConfig.getCollectionName());
            statement.setInt(4, userConfig.getNFTPrice());
            statement.setInt(5, userConfig.getCollectionSize());
            statement.setInt(6, userConfig.getNFTsReservedPerTx());
            statement.setInt(7, userConfig.getNFTsToMintPerTx());
            statement.setInt(8, userConfig.getAmountOfNFTsNotToMint());
            statement.setInt(9, userConfig.getRefundsPerTxLimit());
            statement.setString(10, userConfig.getOwnerWalletAddress());
            statement.executeUpdate();
        } catch (SQLException | CborSerializationException e) {
            e.printStackTrace();
        }
    }

    public static int getCollectionSize(String policyId) {
        int collectionSize = 0;
        String sql = "SELECT COUNT(DISTINCT id) FROM `metadata` WHERE `policy_id` = ?";

        try (Connection connection = connectToDb();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policyId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    collectionSize = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collectionSize;
    }
}