package vendingmachine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vendingmachine.model.NftMetadata;

import java.util.ArrayList;

public interface NftMetadataRepository extends JpaRepository<NftMetadata, Integer> {

    public NftMetadata findByReceiverAddress(String receiverAddress);

    @Transactional
    default void insertNftMetadataList(ArrayList<NftMetadata> nftMetadataList) {
        nftMetadataList.forEach(this::save);
    }

    @Query(value = "SELECT `id`, `name`, `file_url`, `image`, `ipfs_hash`, `is_minted`, `attributes`, `policy_id`, `tx_hash`, `mint_blocktime`, `receiver` FROM `metadata` where `policy_id` =?", nativeQuery = true)
    ArrayList<NftMetadata> findAllByPolicyId(String policyId);
}
