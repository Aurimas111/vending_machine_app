package vendingmachine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vendingmachine.dto.RefundDataDTO;
import vendingmachine.model.TransactionData;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface TransactionDataRepository extends JpaRepository<TransactionData, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO tx (txHash, txIndex, blockHeight, blockTime, validAddress, amountSent, senderAddress, refund, amountToMint, policyId, refunded) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)", nativeQuery = true)
    void insertTxDetailedInfo(String txHash, int txIndex, int blockHeight, int blockTime, int validAddress, int amountSent, String senderAddress, int refund, int amountToMint, String policyId, Boolean refunded);

    @Query(value = "SELECT id, txIndex, txHash, senderAddress, sum(amountSent) AS amountSent, amountToMint, amountMinted, blockTime, blockHeight, refund, policyId, refunded FROM `tx` WHERE policyId= ? group by txhash", nativeQuery = true)
    ArrayList<TransactionData> findAllByPolicyId(String policyId);

    @Query("SELECT COALESCE(SUM(t.amountMinted), 0) FROM TransactionData t WHERE t.policyId = :policyId")
    Integer sumAmountMintedByPolicyId(@Param("policyId") String policyId);

    @Query(value = "SELECT id, txIndex, txhash, senderAddress, sum(amountSent) AS amountSent, amountToMint, amountMinted, blockTime, blockHeight, refund, policyId, refunded FROM `tx` WHERE amountToMint > 0 and policyId= ? group by txhash ORDER BY `tx`.`blockHeight` ASC", nativeQuery = true)
    ArrayList<TransactionData> findAllByPolicyIdForMinting(String policyId);

    @Query(value = "SELECT sum(amountMinted) FROM tx where `policyId` = ?", nativeQuery = true)
    Integer getMintedNftCountByPolicyId(String policyId);

    @Modifying
    @Transactional
    @Query("UPDATE TransactionData t SET t.amountToMint = :amountToMint WHERE t.txHash = :txHash")
    void updateAmountToMintByTxHash(@Param("txHash") String txHash, @Param("amountToMint") int amountToMint);

    @Modifying
    @Transactional
    @Query("UPDATE TransactionData t SET t.amountMinted = :amountMinted WHERE t.txHash = :txHash")
    void updateAmountMintedByTxHash(@Param("txHash") String txHash, @Param("amountMinted") int amountMinted);

    @Modifying
    @Transactional
    @Query("UPDATE TransactionData t SET t.refunded = true WHERE t.txHash IN :txHashes AND t.policyId = :policyId")
    void updateRefundedTxsByTxHashesAndPolicyId(@Param("txHashes") List<String> txHashes, @Param("policyId") String policyId);

    @Query(value = "SELECT senderAddress, refund, txHash  FROM tx WHERE refunded = false AND refund > 0 AND policyId = :policyId GROUP BY txHash ORDER BY blockHeight ASC LIMIT :limit", nativeQuery = true)
    ArrayList<RefundDataDTO> findRefundDataByPolicyId(@Param("policyId") String policyId, @Param("limit") int limit);

    @Query("SELECT t.txHash FROM TransactionData t WHERE t.txHash IN :txHashes")
    List<String> findExistingTxHashes(@Param("txHashes") List<String> txHashes);
}
