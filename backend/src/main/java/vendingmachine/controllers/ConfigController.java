package vendingmachine.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vendingmachine.model.UserConfig;
import vendingmachine.services.ConfigService;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/minter")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    // get user config for users wallet address
    @GetMapping("/getconfig")
    public ResponseEntity<?> getConfig(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException, ApiException {
        UserConfig userConfig = configService.getConfig(wallet);

        if (userConfig != null) {
            return ResponseEntity.ok(userConfig);
        } else {
            return ResponseEntity.ok("No config found");
        }
    }

    // delete user's policy
    @DeleteMapping("/deletepolicy")
    public ResponseEntity<?> deletePolicy(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException, ApiException {
        // if policy is deleted, metadata is deleted as well
        Boolean deleted = configService.deletePolicy(wallet);

        if(deleted){
            return ResponseEntity.ok("Policy deleted");
        }else{
            return ResponseEntity.badRequest().body("No policy found");
        }
    }

    // create a new policy for the user
    @PostMapping("/createpolicy")
    public ResponseEntity<?> createPolicy(@AuthenticationPrincipal String wallet, @RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        Policy policy = configService.createPolicy(data, wallet);
        return ResponseEntity.ok(policy.getPolicyId());
    }

    // save collection metadata
    // metadata is a json list of NFTs with their attributes
    // metadata is stored in the database and associated with the user's policy id
    @PostMapping("/createmetadata")
    public ResponseEntity<?> createMetadata(@AuthenticationPrincipal String wallet, @RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        // metadata is not editable

        Boolean success = configService.createMetadata(data, wallet);
        if(!success){
            return ResponseEntity.badRequest().body("Invalid metadata format");
        }else {
            return ResponseEntity.ok("Metadata created successfully");
        }
    }

    // delete metadata associated with user's policy
    @DeleteMapping("/deletemetadata")
    public ResponseEntity<?> deleteMetadata(@AuthenticationPrincipal String wallet) throws SQLException, CborSerializationException, ApiException {

        Boolean success = configService.deleteMetadata(wallet);

        if(success){
            return ResponseEntity.ok("Metadata deleted");
        }
        else{
            return ResponseEntity.badRequest().body("No metadata found");
        }
    }

    // set user's parameters for minting and refunds
    @PutMapping("/setparameters")
    public ResponseEntity<?> setParameters(@AuthenticationPrincipal String wallet, @RequestBody String data) throws SQLException, CborSerializationException, ApiException {

        String message = configService.setParameters(data, wallet);
        return ResponseEntity.ok(message);
    }
}