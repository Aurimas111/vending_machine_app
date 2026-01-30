package vendingmachine.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vendingmachine.dto.response.TransactionResponse;
import vendingmachine.services.TransactionFetchingService;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/minter")
public class TransactionController {

    private final TransactionFetchingService transactionService;

    public TransactionController(TransactionFetchingService transactionService) {
        this.transactionService = transactionService;
    }

     @GetMapping("/transactions")
     public ResponseEntity<?> getTransactions(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException, ApiException {

        TransactionResponse transactionResponse = transactionService.getTransactions(wallet);
        return ResponseEntity.ok(transactionResponse);
    }
}