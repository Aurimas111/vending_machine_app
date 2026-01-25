package vendingmachine.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "metadata")
public class NftMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "file_url")
    private String file_url;
    @Column(name = "image")
    private String image;
    @Column(name = "ipfs_hash")
    private String ipfsHash;
    @Column(name = "is_minted")
    private boolean isMinted;
    @Transient
    private Map<String, String> dynamicAttributes;
    @Column(name = "attributes")
    private String attributes;
    @Column(name = "tx_hash")
    private String txHash;
    @Column(name = "mint_blocktime")
    private Integer timeStamp;
    @Column(name = "receiver")
    private String receiverAddress;
    @Column(name = "policy_id")
    private String policyId;

    public NftMetadata() {
    }

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

    public NftMetadata(String name, String image, Map<String, String> dynamicAttributes, String policyId) {
        this.name = name;
        this.image = image;
        this.dynamicAttributes = dynamicAttributes;
        this.policyId = policyId;
    }

    public NftMetadata(String name, String ipfs, String attributes, String policyId) {
        this.name = name;
        this.image = ipfs;
        this.attributes = attributes;
        this.policyId = policyId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public Integer getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Integer timeStamp) {
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

    @PostLoad
    public void onPostLoad() {
        parseDynamicAttributes();
    }

    public void parseDynamicAttributes() {
        if (this.attributes != null && !this.attributes.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.dynamicAttributes = mapper.readValue(this.attributes, new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                this.dynamicAttributes = new HashMap<>();
            }
        }
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
