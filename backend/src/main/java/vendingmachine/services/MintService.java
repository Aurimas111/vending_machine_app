package vendingmachine.services;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import vendingmachine.model.NftMetadata;
import vendingmachine.utils.DbOperations;

import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class MintService {

    public ArrayList<NftMetadata> getMints(String data) throws SQLException, CborSerializationException {
        JSONObject obj = new JSONObject(data);
        Policy policy = DbOperations.getPolicyByWallet(obj.getString("walletAddress"));
        return DbOperations.readWalletAssociatedMetadata(policy.getPolicyId());
    }
}
