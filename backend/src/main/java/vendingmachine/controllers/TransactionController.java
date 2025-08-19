package vendingmachine.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vendingmachine.dto.response.TransactionResponse;
import vendingmachine.services.TransactionService;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/minter")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

     // get transactions received to minter address
     // returns a list of transactions with their status, metadata, amount minted, amount to mint, refund status, nft receiver wallet address
     @GetMapping("/transactions")
     public ResponseEntity<?> getTransactions(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException, ApiException {

        TransactionResponse transactionResponse = transactionService.getTransactions(wallet);
        return ResponseEntity.ok(transactionResponse);
    }
}