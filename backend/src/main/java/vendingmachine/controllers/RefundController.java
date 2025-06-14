package vendingmachine.controllers;

import vendingmachine.services.VendingMachineService;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    // start refunds for the wallet address
    @PostMapping("/startrefunds")
    public ResponseEntity<?> startRefunds(@RequestBody String data) throws SQLException, CborSerializationException {

        JSONObject obj = new JSONObject(data);

        VendingMachineService.startRefunding(obj.getString("walletAddress"));
        return ResponseEntity.ok("Refunds started");
    }

    // stop refunds for the wallet address
    @PostMapping("/stoprefunds")
    public ResponseEntity<?> stopRefunds(@RequestBody String data) throws SQLException, CborSerializationException {

        JSONObject obj = new JSONObject(data);

        VendingMachineService.stopRefunding(obj.getString("walletAddress"));
        return ResponseEntity.ok("Refunds stopped");
    }
}
