package vendingmachine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vendingmachine.model.UserConfig;

@Repository
public interface UserConfigRepository extends JpaRepository<UserConfig, Integer> {

    UserConfig findByOwnerWalletAddress(String ownerWalletAddress);

    @Transactional
    default void insertUserConfig(UserConfig userConfig) {
        save(userConfig);
    }

    @Modifying
    @Transactional
    @Query("UPDATE UserConfig u SET u.policyId = NULL, u.policySlot = NULL, u.collectionName = NULL WHERE u.ownerWalletAddress = :ownerWalletAddress")
    void clearPolicyDataByOwnerWalletAddress(@Param("ownerWalletAddress") String ownerWalletAddress);

}
