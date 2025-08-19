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

    public ArrayList<NftMetadata> getMints(String address) throws SQLException, CborSerializationException {
        Policy policy = DbOperations.getPolicyByWallet(address);
        return DbOperations.readWalletAssociatedMetadata(policy.getPolicyId());
    }
}
