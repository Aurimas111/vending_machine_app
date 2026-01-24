package vendingmachine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vendingmachine.model.PolicyObject;

public interface PolicyRepository extends JpaRepository<PolicyObject, Integer> {

    PolicyObject findByOwnerWallet(String ownerWalletAddress);

    @Transactional
    void deleteByOwnerWallet(String ownerWallet);

    @Query(value = "SELECT PolicyScript FROM policy WHERE ownerWallet = ?1", nativeQuery = true)
    String findPolicyScriptByOwnerWallet(String ownerWalletAddress);
}
