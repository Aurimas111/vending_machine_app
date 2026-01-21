package vendingmachine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import vendingmachine.model.PolicyObject;

public interface PolicyRepository extends JpaRepository<PolicyObject, Integer> {

    PolicyObject findByOwnerWallet(String ownerWalletAddress);

    @Transactional
    void deleteByOwnerWallet(String ownerWallet);
}
