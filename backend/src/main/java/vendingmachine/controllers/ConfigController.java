package vendingmachine.controllers;

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
    @PostMapping("/getconfig")
    public ResponseEntity<?> getConfig(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        UserConfig userConfig = configService.getConfig(data);

        if (userConfig != null) {
            return ResponseEntity.ok(userConfig);
        } else {
            return ResponseEntity.ok("No config found");
        }
    }

    // delete user's policy
    @PostMapping("/deletepolicy")
    public ResponseEntity<?> deletePolicy(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        // if policy is deleted, metadata is deleted as well
        Boolean deleted = configService.deletePolicy(data);

        if(deleted){
            return ResponseEntity.ok("Policy deleted");
        }else{
            return ResponseEntity.badRequest().body("No policy found");
        }
    }

    // create a new policy for the user
    @PostMapping("/createpolicy")
    public ResponseEntity<?> createPolicy(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        Policy policy = configService.createPolicy(data);
        return ResponseEntity.ok(policy.getPolicyId());
    }

    // save collection metadata
    // metadata is a json list of NFTs with their attributes
    // metadata is stored in the database and associated with the user's policy id
    @PostMapping("/createmetadata")
    public ResponseEntity<?> createMetadata(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {
        // metadata is not editable

        Boolean success = configService.createMetadata(data);
        if(!success){
            return ResponseEntity.badRequest().body("Invalid metadata format");
        }else {
            return ResponseEntity.ok("Metadata created successfully");
        }
    }

    // delete metadata associated with user's policy
    @PostMapping("/deletemetadata")
    public ResponseEntity<?> deleteMetadata(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {

        Boolean success = configService.deleteMetadata(data);

        if(success){
            return ResponseEntity.ok("Metadata deleted");
        }
        else{
            return ResponseEntity.badRequest().body("No metadata found");
        }
    }

    // set user's parameters for minting and refunds
    @PostMapping("/setparameters")
    public ResponseEntity<?> setParameters(@RequestBody String data) throws SQLException, CborSerializationException, ApiException {

        String message = configService.setParameters(data);
        return ResponseEntity.ok(message);
    }
}