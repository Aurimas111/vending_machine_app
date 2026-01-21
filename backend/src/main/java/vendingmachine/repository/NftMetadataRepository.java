package vendingmachine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vendingmachine.model.NftMetadata;

import java.util.ArrayList;

public interface NftMetadataRepository extends JpaRepository<NftMetadata, Integer> {

    NftMetadata findByReceiverAddress(String receiverAddress);

    @Transactional
    default void insertNftMetadataList(ArrayList<NftMetadata> nftMetadataList) {
        nftMetadataList.forEach(this::save);
    }

    @Query(value = "SELECT `id`, `name`, `file_url`, `image`, `ipfs_hash`, `is_minted`, `attributes`, `policy_id`, `tx_hash`, `mint_blocktime`, `receiver` FROM `metadata` where `policy_id` =?", nativeQuery = true)
    ArrayList<NftMetadata> findAllByPolicyId(String policyId);

    @Query(value = "SELECT id, name, file_url, image, ipfs_hash, is_minted, attributes, policy_id, tx_hash, mint_blocktime, receiver FROM metadata WHERE is_minted = 0 AND policy_id = ?", nativeQuery = true)
    ArrayList<NftMetadata> findAllNotMintedByPolicyId(String policyId);

    @Query(value = "SELECT COUNT(DISTINCT id) FROM `metadata` WHERE `policy_id` = ?", nativeQuery = true)
    int getCollectionSizeByPolicyId(String policyId);

    @Modifying
    @Transactional
    @Query("UPDATE NftMetadata n SET n.isMinted = true, n.txHash = :txHash, n.timeStamp = :mint_blocktime, n.receiverAddress = :receiver WHERE n.policyId = :policyId and n.name = :name")
    void updateMetadataMinted(@Param("txHash") String txHash, @Param("mint_blocktime") long date, @Param("receiver") String receiver, @Param("policyId") String policyId, @Param("name") String name);

    @Modifying
    @Transactional
    void deleteByPolicyId(String policyId);

}
