package vendingmachine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vendingmachine.model.TransactionData;

import java.util.ArrayList;

@Repository
public interface TransactionDataRepository extends JpaRepository<TransactionData, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO tx (txHash, txIndex, blockHeight, blockTime, validAddress, amountSent, senderAddress, refund, amountToMint, policyId, refunded) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)", nativeQuery = true)
    void insertTxDetailedInfo(String txHash, int txIndex, int blockHeight, int blockTime, int validAddress, int amountSent, String senderAddress, int refund, int amountToMint, String policyId, Boolean refunded);

    @Query(value = "SELECT id, txHash, senderAddress, sum(amountSent) AS amountSent, amountToMint, amountMinted, blockTime, refund, policyId, refunded FROM `tx` WHERE policyId= ? group by txhash", nativeQuery = true)
    ArrayList<TransactionData> findAllByPolicyId(String policyId);

    @Query("SELECT COALESCE(SUM(t.amountMinted), 0) FROM TransactionData t WHERE t.policyId = :policyId")
    Integer sumAmountMintedByPolicyId(@Param("policyId") String policyId);

}
