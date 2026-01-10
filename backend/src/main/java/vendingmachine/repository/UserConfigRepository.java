package vendingmachine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vendingmachine.model.UserConfig;

@Repository
public interface UserConfigRepository extends JpaRepository<UserConfig, Integer> {

    UserConfig findByOwnerWalletAddress(String ownerWalletAddress);

}
