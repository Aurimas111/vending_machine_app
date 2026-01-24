package vendingmachine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "policy")
@Getter
@Setter
public class PolicyObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "policyKeys")
    private String policyKeys;
    @Column(name = "policyId")
    private String policyId;
    @Column(name = "policyScript")
    private String policyScript;
    @Column(name = "name")
    private String name;
    @Column(name = "verificationKey")
    private String verificationKey;
    @Column(name = "signingKey")
    private String signingKey;
    @Column(name = "ownerWallet")
    private String ownerWallet;

    public PolicyObject() {
    }

    public PolicyObject(String policyKeys, String policyId, String policyScript, String name, String verificationKey, String signingKey, String ownerWallet) {
        this.policyKeys = policyKeys;
        this.policyId = policyId;
        this.policyScript = policyScript;
        this.name = name;
        this.verificationKey = verificationKey;
        this.signingKey = signingKey;
        this.ownerWallet = ownerWallet;
    }
}
