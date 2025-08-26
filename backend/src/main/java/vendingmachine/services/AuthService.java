package vendingmachine.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.cardanofoundation.cip30.AddressFormat;
import org.cardanofoundation.cip30.CIP30Verifier;
import org.cardanofoundation.cip30.MessageFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vendingmachine.dto.request.LoginRequest;
import vendingmachine.dto.response.AuthResponse;
import vendingmachine.model.UserConfig;
import vendingmachine.utils.DbOperations;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Value("${vending.app.jwtSecret}")
    private String secretKey;

    @Value("${vending.app.jwtExpiration}")
    private int jwtExpiration;

    private final Map<String, String> nonces = new ConcurrentHashMap<>();

    public String generateNonce(String address) {
        String nonce = UUID.randomUUID().toString();
        nonces.put(address, nonce);
        return nonce;
    }

    public String getNonce(String address) {
        return nonces.get(address);
    }

    public void clearNonce(String address) {
        nonces.remove(address);
    }

    public AuthResponse authenticate(LoginRequest request) {
        try {
            String expectedNonce = getNonce(request.getAddress());
            if (expectedNonce == null) {
                return new AuthResponse(false, null, "Invalid or expired nonce", null);
            }

            var verifier = new CIP30Verifier(request.getSignature(), request.getKey());
            var verificationResult = verifier.verify();

            if (!verificationResult.isValid()) {
                return new AuthResponse(false, null, "Invalid signature", null);
            }

            String signedMessage = verificationResult.getMessage(MessageFormat.TEXT);
            if (!expectedNonce.equals(signedMessage)) {
                return new AuthResponse(false, null, "Signed message doesn't match nonce", null);
            }

            String signedAddress = verificationResult.getAddress(AddressFormat.TEXT)
                    .orElseThrow(() -> new RuntimeException("No address found in signature"));

            if (!request.getAddress().equals(signedAddress)) {
                return new AuthResponse(false, null, "Address mismatch", null);
            }

            clearNonce(request.getAddress());
            String token = generateJWT(request.getAddress());

            UserConfig userConfig = DbOperations.getUserConfig(request.getAddress());

            if (userConfig == null) {
                userConfig = new UserConfig(request.getAddress(), 10000000, 5, 5, 0, 3);
                DbOperations.insertConfig(userConfig);
            }

            return new AuthResponse(true, token, "Authentication successful", userConfig);

        } catch (Exception e) {
            return new AuthResponse(false, null, "Authentication failed", null);
        }
    }

    public String generateJWT(String address) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(address)
                .claim("address", address)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(jwtExpiration, ChronoUnit.MILLIS)))
                .signWith(key)
                .compact();
    }
}