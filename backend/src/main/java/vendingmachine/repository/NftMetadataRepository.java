package vendingmachine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vendingmachine.model.NftMetadata;

public interface NftMetadataRepository extends JpaRepository<NftMetadata, Integer> {

    public NftMetadata findByReceiverAddress(String receiverAddress);
}
