package vendingmachine.services;

import org.springframework.stereotype.Service;
import vendingmachine.model.NftMetadata;

import vendingmachine.model.PolicyObject;
import vendingmachine.repository.NftMetadataRepository;
import vendingmachine.repository.PolicyRepository;

import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class MintService {

    private final NftMetadataRepository nftMetadataRepository;
    private final PolicyRepository policyRepository;

    public MintService(NftMetadataRepository nftMetadataRepository, PolicyRepository policyRepository) {
        this.nftMetadataRepository = nftMetadataRepository;
        this.policyRepository = policyRepository;
    }

    public ArrayList<NftMetadata> getMints(String address) throws SQLException {
        PolicyObject policy = policyRepository.findByOwnerWallet(address);
        return nftMetadataRepository.findAllByPolicyId(policy.getPolicyId());
    }
}
