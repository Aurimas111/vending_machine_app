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
    private int nftPrice;
    @Column(name = "collection_size")
    private int collectionSize;
    @Column(name = "nft_reserved_per_tx")
    private int nftsReservedPerTx;
    @Column(name = "nft_to_mint_per_tx")
    private int nftsToMintPerTx;
    @Column(name = "amount_of_nft_not_to_mint")
    private int amountOfNFTsNotToMint;
    @Column(name = "refund_per_tx_limit")
    private int refundsPerTxLimit;
    @Column(name = "policy_id")
    private String policyId;

    public UserConfig() {
    }

    public UserConfig(String ownerWalletAddress, Policy policy, String policySlot, ArrayList<MetadataResponse> metadataList, String collectionName, int nftPrice, int collectionSize, int nftsReservedPerTx, int nftsToMintPerTx, int amountOfNFTsNotToMint, int refundsPerTxLimit) {
        this.ownerWalletAddress = ownerWalletAddress;
        this.policy = policy;
        this.policySlot = policySlot;
        this.metadataList = metadataList;
        this.collectionName = collectionName;
        this.nftPrice = nftPrice;
        this.collectionSize = collectionSize;
        this.nftsReservedPerTx = nftsReservedPerTx;
        this.nftsToMintPerTx = nftsToMintPerTx;
        this.amountOfNFTsNotToMint = amountOfNFTsNotToMint;
        this.refundsPerTxLimit = refundsPerTxLimit;
    }

    public UserConfig(String ownerWalletAddress, String collectionName, int nftPrice, int collectionSize, int nftsReservedPerTx, int nftsToMintPerTx, int amountOfNFTsNotToMint, int refundsPerTxLimit) {
        this.ownerWalletAddress = ownerWalletAddress;
        this.collectionName = collectionName;
        this.nftPrice = nftPrice;
        this.collectionSize = collectionSize;
        this.nftsReservedPerTx = nftsReservedPerTx;
        this.nftsToMintPerTx = nftsToMintPerTx;
        this.amountOfNFTsNotToMint = amountOfNFTsNotToMint;
        this.refundsPerTxLimit = refundsPerTxLimit;
    }

    public UserConfig(String address, String collectionName, int nftPrice, int nftsReservedPerTx, int nftsToMintPerTx, int nftsToNotMint, int refundsPerTxLimit) {
        this.ownerWalletAddress = address;
        this.collectionName = collectionName;
        this.nftPrice = nftPrice;
        this.nftsReservedPerTx = nftsReservedPerTx;
        this.nftsToMintPerTx = nftsToMintPerTx;
        this.amountOfNFTsNotToMint = nftsToNotMint;
        this.refundsPerTxLimit = refundsPerTxLimit;
    }

    public UserConfig(String address, int nftPrice, int nftsReservedPerTx, int nftsToMintPerTx, int nftsToNotMint, int refundsPerTxLimit) {
        this.ownerWalletAddress = address;
        this.nftPrice = nftPrice;
        this.nftsReservedPerTx = nftsReservedPerTx;
        this.nftsToMintPerTx = nftsToMintPerTx;
        this.amountOfNFTsNotToMint = nftsToNotMint;
        this.refundsPerTxLimit = refundsPerTxLimit;
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

    public int getNftPrice() {

        return nftPrice;
    }

    public void setNftPrice(int nftPrice) {

        this.nftPrice = nftPrice;
    }

    public int getCollectionSize() {

        return collectionSize;
    }

    public void setCollectionSize(int collectionSize) {

        this.collectionSize = collectionSize;
    }

    public int getNftsReservedPerTx() {

        return nftsReservedPerTx;
    }

    public void setNftsReservedPerTx(int nftsReservedPerTx) {

        this.nftsReservedPerTx = nftsReservedPerTx;
    }

    public int getNftsToMintPerTx() {

        return nftsToMintPerTx;
    }

    public void setNftsToMintPerTx(int nftsToMintPerTx) {

        this.nftsToMintPerTx = nftsToMintPerTx;
    }

    public int getAmountOfNFTsNotToMint() {

        return amountOfNFTsNotToMint;
    }

    public void setAmountOfNFTsNotToMint(int amountOfNFTsNotToMint) {

        this.amountOfNFTsNotToMint = amountOfNFTsNotToMint;
    }

    public int getRefundsPerTxLimit() {

        return refundsPerTxLimit;
    }

    public void setRefundsPerTxLimit(int refundsPerTxLimit) {
        this.refundsPerTxLimit = refundsPerTxLimit;
    }

    @Override
    public String toString() {
        return "UserConfig{" +
                "ownerWalletAddress='" + ownerWalletAddress + '\'' +
                ", policy=" + policy +
                ", policySlot='" + policySlot + '\'' +
                ", metadataList=" + metadataList +
                ", collectionName='" + collectionName + '\'' +
                ", nftPrice=" + nftPrice +
                ", collectionSize=" + collectionSize +
                ", nFTsReservedPerTx=" + nftsReservedPerTx +
                ", nFTsToMintPerTx=" + nftsToMintPerTx +
                ", amountOfNFTsNotToMint=" + amountOfNFTsNotToMint +
                ", refundsPerTxLimit=" + refundsPerTxLimit +
                '}';
    }
}