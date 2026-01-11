package vendingmachine.services;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.springframework.stereotype.Service;
import vendingmachine.model.NftMetadata;

import vendingmachine.utils.DbOperations;

import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class MintService {

//    private NftMetadataRepository nftMetadataRepository;
    private DbOperations dbOperations;

    public MintService(DbOperations dbOperations) {
        this.dbOperations = dbOperations;
    }

    public ArrayList<NftMetadata> getMints(String address) throws SQLException, CborSerializationException {
        Policy policy = dbOperations.getPolicyByWallet(address);
        return DbOperations.readWalletAssociatedMetadata(policy.getPolicyId());
    }
}
