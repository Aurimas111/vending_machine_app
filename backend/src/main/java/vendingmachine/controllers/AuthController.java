package vendingmachine.controllers;

import org.springframework.web.bind.annotation.*;
import vendingmachine.dto.request.LoginRequest;
import vendingmachine.dto.response.AuthResponse;
import vendingmachine.services.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/nonce")
    public java.util.Map<String, String> getNonce(@RequestParam("address") String address) {
        String nonce = authService.generateNonce(address);
        return java.util.Map.of("nonce", nonce);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) throws Exception {

        return authService.authenticate(request);
    }
}