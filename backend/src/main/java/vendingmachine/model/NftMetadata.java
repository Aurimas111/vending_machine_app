package vendingmachine.model;

import java.util.Map;
public class NftMetadata {

    private String name;
    private String file_url;
    private String image;
    private String ipfsHash;
    private boolean isMinted;
    private Map<String, String> dynamicAttributes;
    private String txHash;
    private int timeStamp;
    private String receiverAddress;

    public NftMetadata(String name, String file_url, String image, String ipfsHash, boolean isMinted, Map<String, String> dynamicAttributes) {
        this.name = name;
        this.file_url = file_url;
        this.image = image;
        this.ipfsHash = ipfsHash;
        this.isMinted = isMinted;
        this.dynamicAttributes = dynamicAttributes;
    }
    public NftMetadata(String name, String image, String ipfsHash, Map<String, String> dynamicAttributes) {
        this.name = name;
        this.image = image;
        this.ipfsHash = ipfsHash;
        this.dynamicAttributes = dynamicAttributes;
    }

    public NftMetadata(String name, String file_url, String image, String ipfsHash, boolean isMinted, Map<String, String> dynamicAttributes, String txHash, int timeStamp, String receiverAddress) {
        this.name = name;
        this.file_url = file_url;
        this.image = image;
        this.ipfsHash = ipfsHash;
        this.isMinted = isMinted;
        this.dynamicAttributes = dynamicAttributes;
        this.txHash = txHash;
        this.timeStamp = timeStamp;
        this.receiverAddress = receiverAddress;
    }

    public NftMetadata(String name, String file_url, String image, String ipfsHash, boolean isMinted, Map<String, String> dynamicAttributes, String txHash) {
        this.name = name;
        this.file_url = file_url;
        this.image = image;
        this.ipfsHash = ipfsHash;
        this.isMinted = isMinted;
        this.dynamicAttributes = dynamicAttributes;
        this.txHash = txHash;
    }

    public NftMetadata(String name, String file_url, String image, String ipfsHash, Map<String, String> dynamicAttributes) {
        this.name = name;
        this.file_url = file_url;
        this.image = image;
        this.ipfsHash = ipfsHash;
        this.dynamicAttributes = dynamicAttributes;
    }

    public NftMetadata(String name, String image, Map<String, String> dynamicAttributes) {
        this.name = name;
        this.image = image;
        this.dynamicAttributes = dynamicAttributes;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIpfsHash() {
        return ipfsHash;
    }

    public void setIpfsHash(String ipfsHash) {
        this.ipfsHash = ipfsHash;
    }

    public boolean isMinted() {
        return isMinted;
    }

    public void setMinted(boolean minted) {
        isMinted = minted;
    }

    public Map<String, String> getDynamicAttributes() {
        return dynamicAttributes;
    }

    public void setDynamicAttributes(Map<String, String> dynamicAttributes) {
        this.dynamicAttributes = dynamicAttributes;
    }

    @Override
    public String toString() {
        return "NftMetadata{" +
                "name='" + name + '\'' +
                ", file_url='" + file_url + '\'' +
                ", image='" + image + '\'' +
                ", ipfsHash='" + ipfsHash + '\'' +
                ", isMinted=" + isMinted +
                ", dynamicAttributes=" + dynamicAttributes +
                '}';
    }
}
