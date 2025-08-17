package vendingmachine.dto.request;

public class LoginRequest {
    private String address;
    private String key;
    private String signature;

    public LoginRequest(String address, String key, String signature) {
        this.address = address;
        this.key = key;
        this.signature = signature;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}