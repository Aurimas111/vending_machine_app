package vendingmachine.services;

import org.cardanofoundation.cip30.CIP30Verifier;
import org.springframework.stereotype.Component;

@Component
public class CIP30VerifierFactoryImpl implements CIP30VerifierFactory {
    @Override
    public CIP30Verifier createCIP30Verifier(String policyId, String key) {
        return new CIP30Verifier(policyId, key);
    }
}
