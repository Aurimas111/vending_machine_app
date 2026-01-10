package vendingmachine.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vendingmachine.model.NftMetadata;
import vendingmachine.services.MintService;
import vendingmachine.services.VendingMachineService;
import com.bloxbean.cardano.client.exception.CborSerializationException;
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
    @GetMapping("/getmints")
    public ResponseEntity<?> getMints(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException {

        ArrayList<NftMetadata> metadata = mintService.getMints(wallet);
        return ResponseEntity.ok(metadata);
    }

    // start minting process for the wallet address
    @PostMapping("/startmint")
    public ResponseEntity<?> startMint(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException {

        vendingMachineService.startMinting(wallet);
        return ResponseEntity.ok("Minting started");
    }

    // stop minting process for the wallet address
    @PostMapping("/stopmint")
    public ResponseEntity<?> stopMint(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException {

        vendingMachineService.stopMinting(wallet);
        return ResponseEntity.ok("Minting stopped");
    }
}