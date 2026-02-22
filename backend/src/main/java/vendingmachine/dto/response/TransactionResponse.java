package vendingmachine.dto.response;

import vendingmachine.model.TransactionData;

import java.util.ArrayList;

public class TransactionResponse {
    private ArrayList<TransactionData> transactions;
    private int totalNFTCount;
    private int mintedNFTCount;
    private double nftPrice;
    private String monitoringAddress;

    public TransactionResponse(ArrayList<TransactionData> transactions, int totalNFTCount, int mintedNFTCount, double nftPrice, String monitoringAddress) {
        this.transactions = transactions;
        this.totalNFTCount = totalNFTCount;
        this.mintedNFTCount = mintedNFTCount;
        this.nftPrice = nftPrice;
        this.monitoringAddress = monitoringAddress;
    }

    public String getMonitoringAddress() {
        return monitoringAddress;
    }

    public void setMonitoringAddress(String monitoringAddress) {
        this.monitoringAddress = monitoringAddress;
    }

    public ArrayList<TransactionData> getTransactions() {

        return transactions;
    }
    public void setTransactions(ArrayList<TransactionData> transactions) {

        this.transactions = transactions;
    }
    public int getTotalNFTCount() {

        return totalNFTCount;
    }
    public void setTotalNFTCount(int totalNFTCount) {

        this.totalNFTCount = totalNFTCount;
    }
    public int getMintedNFTCount() {

        return mintedNFTCount;
    }
    public void setMintedNFTCount(int mintedNFTCount) {

        this.mintedNFTCount = mintedNFTCount;
    }
    public double getNftPrice() {

        return nftPrice;
    }
    public void setNftPrice(double nftPrice) {

        this.nftPrice = nftPrice;
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "transactions=" + transactions +
                ", totalNFTCount=" + totalNFTCount +
                ", mintedNFTCount=" + mintedNFTCount +
                ", nftPrice=" + nftPrice +
                '}';
    }
}
