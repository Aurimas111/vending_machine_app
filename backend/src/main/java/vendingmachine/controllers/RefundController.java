package vendingmachine.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vendingmachine.services.VendingMachineService;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/minter")
public class RefundController {

    private final VendingMachineService vendingMachineService;

    public RefundController(VendingMachineService vendingMachineService) {
        this.vendingMachineService = vendingMachineService;
    }

    @PostMapping("/startrefunds")
    public ResponseEntity<?> startRefunds(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException {

        vendingMachineService.startRefunding(wallet);
        return ResponseEntity.ok("Refunds started");
    }

    @PostMapping("/stoprefunds")
    public ResponseEntity<?> stopRefunds(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException {

        vendingMachineService.stopRefunding(wallet);
        return ResponseEntity.ok("Refunds stopped");
    }
}