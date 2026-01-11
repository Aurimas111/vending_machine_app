package vendingmachine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vendingmachine.model.PolicyObject;

public interface PolicyRepository extends JpaRepository<PolicyObject, Integer> {

    PolicyObject findByOwnerWallet(String ownerWalletAddress);
}
