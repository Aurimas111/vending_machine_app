package vendingmachine.controllers;

import vendingmachine.model.NftMetadata;
import vendingmachine.services.MintService;
import vendingmachine.services.VendingMachineService;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/minter")
public class MintController {

    private final VendingMachineService vendingMachineService;
    private final MintService mintService;

    public MintController(VendingMachineService vendingMachineService, MintService mintService) {
        this.vendingMachineService = vendingMachineService;
        this.mintService = mintService;
    }

    // get NFTs associated with the wallet address
    @PostMapping("/getmints")
    public ResponseEntity<?> getMints(@RequestBody String data) throws SQLException, CborSerializationException {

        ArrayList<NftMetadata> metadata = mintService.getMints(data);
        return ResponseEntity.ok(metadata);
    }

    // start minting process for the wallet address
    @PostMapping("/startmint")
    public ResponseEntity<?> startMint(@RequestBody String data) throws SQLException, CborSerializationException {

        JSONObject obj = new JSONObject(data);
        vendingMachineService.startMinting(obj.getString("walletAddress"));
        return ResponseEntity.ok("Minting started");
    }

    // stop minting process for the wallet address
    @PostMapping("/stopmint")
    public ResponseEntity<?> stopMint(@RequestBody String data) throws SQLException, CborSerializationException {

        JSONObject obj = new JSONObject(data);
        vendingMachineService.stopMinting(obj.getString("walletAddress"));
        return ResponseEntity.ok("Minting stopped");
    }
}