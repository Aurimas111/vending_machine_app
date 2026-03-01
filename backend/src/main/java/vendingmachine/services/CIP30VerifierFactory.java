package vendingmachine.services;

import org.cardanofoundation.cip30.CIP30Verifier;

public interface CIP30VerifierFactory {
    CIP30Verifier createCIP30Verifier(String policyId, String key);
}
