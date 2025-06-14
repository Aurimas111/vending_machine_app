package vendingmachine.model;

public class TransactionData {
    private String txHash;
    private int txIndex;
    private int blockTime;
    private int amount;
    private String senderAddress;
    private int refund;
    private int amountToMint;
    private int amountMinted;
    private String policyId;
    private Boolean refunded;
    private String status;

    public TransactionData(String txHash, int txIndex, int blockTime, int amount,
                           String senderAddress, int refund, int amountToMint, String policyId, Boolean refunded) {
        this.txHash = txHash;
        this.txIndex = txIndex;
        this.blockTime = blockTime;
        this.amount = amount;
        this.senderAddress = senderAddress;
        this.refund = refund;
        this.amountToMint = amountToMint;
        this.policyId = policyId;
        this.refunded = refunded;
    }

    public TransactionData(String txHash, int blockTime, int amount, String senderAddress, int refund, int amountToMint, int amountMinted, String policyId, String status, boolean refunded) {
        this.txHash = txHash;
        this.blockTime = blockTime;
        this.amount = amount;
        this.senderAddress = senderAddress;
        this.refund = refund;
        this.amountToMint = amountToMint;
        this.amountMinted = amountMinted;
        this.policyId = policyId;
        this.status = status;
        this.refunded = refunded;
    }

    public TransactionData(String txHash, String senderAddress, int amount, int amountToMint, int amountMinted) {
        this.txHash = txHash;
        this.senderAddress = senderAddress;
        this.amount = amount;
        this.amountToMint = amountToMint;
        this.amountMinted = amountMinted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAmountMinted() {
        return amountMinted;
    }

    public void setAmountMinted(int amountMinted) {
        this.amountMinted = amountMinted;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(int txIndex) {
        this.txIndex = txIndex;
    }

    public int getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(int blockTime) {
        this.blockTime = blockTime;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public int getRefund() {
        return refund;
    }

    public void setRefund(int refund) {
        this.refund = refund;
    }

    public int getAmountToMint() {
        return amountToMint;
    }

    public void setAmountToMint(int amountToMint) {
        this.amountToMint = amountToMint;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public Boolean getRefunded() {
        return refunded;
    }

    public void setRefunded(Boolean refunded) {
        this.refunded = refunded;
    }

    @Override
    public String toString() {
        return "TransactionData{" +
                "txHash='" + txHash + '\'' +
                ", txIndex=" + txIndex +
                ", blockTime=" + blockTime +
                ", amount=" + amount +
                ", senderAddress='" + senderAddress + '\'' +
                ", refund=" + refund +
                ", amountToMint=" + amountToMint +
                ", policyId='" + policyId + '\'' +
                ", refunded=" + refunded +
                '}';
    }
}