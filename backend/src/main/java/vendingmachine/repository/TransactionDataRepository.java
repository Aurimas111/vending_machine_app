package vendingmachine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vendingmachine.model.NftMetadata;

@Repository
public interface TransactionDataRepository extends JpaRepository<NftMetadata, Integer> {



}
