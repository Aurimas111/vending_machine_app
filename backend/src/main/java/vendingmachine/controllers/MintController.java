package vendingmachine.controllers;

import vendingmachine.model.NftMetadata;
import vendingmachine.services.VendingMachineService;
import vendingmachine.utils.DbOperations;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/minter")
public class MintController {

    // get NFTs associated with the wallet address
    @PostMapping("/getmints")
    public ResponseEntity<?> getMints(@RequestBody String data) throws SQLException, CborSerializationException {
        JSONObject obj = new JSONObject(data);

        Policy policy = DbOperations.getPolicyByWallet(obj.getString("walletAddress"));

        ArrayList<NftMetadata> metadata = DbOperations.readWalletAssociatedMetadata(policy.getPolicyId());

        return ResponseEntity.ok(metadata);
    }

    // start minting process for the wallet address
    @PostMapping("/startmint")
    public ResponseEntity<?> startMint(@RequestBody String data) throws SQLException, CborSerializationException {
        JSONObject obj = new JSONObject(data);

        VendingMachineService.startMinting(obj.getString("walletAddress"));

        return ResponseEntity.ok("Minting started");
    }

    // stop minting process for the wallet address
    @PostMapping("/stopmint")
    public ResponseEntity<?> stopMint(@RequestBody String data) throws SQLException, CborSerializationException {
        JSONObject obj = new JSONObject(data);

        VendingMachineService.stopMinting(obj.getString("walletAddress"));
        return ResponseEntity.ok("Minting stopped");
    }
}