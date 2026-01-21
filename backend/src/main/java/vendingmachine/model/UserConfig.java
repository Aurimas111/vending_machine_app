package vendingmachine.model;

import com.bloxbean.cardano.client.transaction.spec.Policy;
import jakarta.persistence.*;
import vendingmachine.dto.response.MetadataResponse;

import java.util.ArrayList;

@Entity
@Table(name = "userconfig")
public class UserConfig {

    @Id
    private Integer id;
    @Column(name = "user_address")
    private String ownerWalletAddress;
    @Transient
    private Policy policy;
    @Column(name = "policy_slot")
    private String policySlot;
    @Transient
    private ArrayList<MetadataResponse> metadataList;
    @Column(name = "collection_name")
    private String collectionName;
    @Column(name = "nft_price")
    private int NFTPrice;
    @Column(name = "collection_size")
    private int collectionSize;
    @Column(name = "nft_reserved_per_tx")
    private int NFTsReservedPerTx;
    @Column(name = "nft_to_mint_per_tx")
    private int NFTsToMintPerTx;
    @Column(name = "amount_of_nft_not_to_mint")
    private int AmountOfNFTsNotToMint;
    @Column(name = "refund_per_tx_limit")
    private int RefundsPerTxLimit;
    @Column(name = "policy_id")
    private String policyId;

    public UserConfig() {
    }

    public UserConfig(String ownerWalletAddress, Policy policy, String policySlot, ArrayList<MetadataResponse> metadataList, String collectionName, int NFTPrice, int collectionSize, int NFTsReservedPerTx, int NFTsToMintPerTx, int amountOfNFTsNotToMint, int refundsPerTxLimit) {
        this.ownerWalletAddress = ownerWalletAddress;
        this.policy = policy;
        this.policySlot = policySlot;
        this.metadataList = metadataList;
        this.collectionName = collectionName;
        this.NFTPrice = NFTPrice;
        this.collectionSize = collectionSize;
        this.NFTsReservedPerTx = NFTsReservedPerTx;
        this.NFTsToMintPerTx = NFTsToMintPerTx;
        this.AmountOfNFTsNotToMint = amountOfNFTsNotToMint;
        this.RefundsPerTxLimit = refundsPerTxLimit;
    }

    public UserConfig(String ownerWalletAddress, String collectionName, int NFTPrice, int collectionSize, int NFTsReservedPerTx, int NFTsToMintPerTx, int amountOfNFTsNotToMint, int refundsPerTxLimit) {
        this.ownerWalletAddress = ownerWalletAddress;
        this.collectionName = collectionName;
        this.NFTPrice = NFTPrice;
        this.collectionSize = collectionSize;
        this.NFTsReservedPerTx = NFTsReservedPerTx;
        this.NFTsToMintPerTx = NFTsToMintPerTx;
        this.AmountOfNFTsNotToMint = amountOfNFTsNotToMint;
        this.RefundsPerTxLimit = refundsPerTxLimit;
    }

    public UserConfig(String address, String collectionName, int nftPrice, int nftsReservedPerTx, int nftsToMintPerTx, int nftsToNotMint, int refundsPerTxLimit) {
        this.ownerWalletAddress = address;
        this.collectionName = collectionName;
        this.NFTPrice = nftPrice;
        this.NFTsReservedPerTx = nftsReservedPerTx;
        this.NFTsToMintPerTx = nftsToMintPerTx;
        this.AmountOfNFTsNotToMint = nftsToNotMint;
        this.RefundsPerTxLimit = refundsPerTxLimit;
    }

    public UserConfig(String address, int nftPrice, int nftsReservedPerTx, int nftsToMintPerTx, int nftsToNotMint, int refundsPerTxLimit) {
        this.ownerWalletAddress = address;
        this.NFTPrice = nftPrice;
        this.NFTsReservedPerTx = nftsReservedPerTx;
        this.NFTsToMintPerTx = nftsToMintPerTx;
        this.AmountOfNFTsNotToMint = nftsToNotMint;
        this.RefundsPerTxLimit = refundsPerTxLimit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getOwnerWalletAddress() {
        return ownerWalletAddress;
    }

    public void setOwnerWalletAddress(String ownerWalletAddress) {

        this.ownerWalletAddress = ownerWalletAddress;
    }

    public Policy getPolicy() {

        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public String getPolicySlot() {

        return policySlot;
    }

    public void setPolicySlot(String policySlot) {

        this.policySlot = policySlot;
    }

    public ArrayList<MetadataResponse> getMetadataList() {

        return metadataList;
    }

    public void setMetadataList(ArrayList<MetadataResponse> metadataList) {

        this.metadataList = metadataList;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {

        this.collectionName = collectionName;
    }

    public int getNFTPrice() {

        return NFTPrice;
    }

    public void setNFTPrice(int NFTPrice) {

        this.NFTPrice = NFTPrice;
    }

    public int getCollectionSize() {

        return collectionSize;
    }

    public void setCollectionSize(int collectionSize) {

        this.collectionSize = collectionSize;
    }

    public int getNFTsReservedPerTx() {

        return NFTsReservedPerTx;
    }

    public void setNFTsReservedPerTx(int NFTsReservedPerTx) {

        this.NFTsReservedPerTx = NFTsReservedPerTx;
    }

    public int getNFTsToMintPerTx() {

        return NFTsToMintPerTx;
    }

    public void setNFTsToMintPerTx(int NFTsToMintPerTx) {

        this.NFTsToMintPerTx = NFTsToMintPerTx;
    }

    public int getAmountOfNFTsNotToMint() {

        return AmountOfNFTsNotToMint;
    }

    public void setAmountOfNFTsNotToMint(int amountOfNFTsNotToMint) {

        AmountOfNFTsNotToMint = amountOfNFTsNotToMint;
    }

    public int getRefundsPerTxLimit() {

        return RefundsPerTxLimit;
    }

    public void setRefundsPerTxLimit(int refundsPerTxLimit) {
        RefundsPerTxLimit = refundsPerTxLimit;
    }

    @Override
    public String toString() {
        return "UserConfig{" +
                "ownerWalletAddress='" + ownerWalletAddress + '\'' +
                ", policy=" + policy +
                ", policySlot='" + policySlot + '\'' +
                ", metadataList=" + metadataList +
                ", collectionName='" + collectionName + '\'' +
                ", NFTPrice=" + NFTPrice +
                ", collectionSize=" + collectionSize +
                ", NFTsReservedPerTx=" + NFTsReservedPerTx +
                ", NFTsToMintPerTx=" + NFTsToMintPerTx +
                ", AmountOfNFTsNotToMint=" + AmountOfNFTsNotToMint +
                ", RefundsPerTxLimit=" + RefundsPerTxLimit +
                '}';
    }
}