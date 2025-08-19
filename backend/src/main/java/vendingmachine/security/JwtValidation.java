package vendingmachine.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vendingmachine.model.UserConfig;
import vendingmachine.utils.DbOperations;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtValidation {

    @Value("${vending.app.jwtSecret}")
    private String jwtSecret;

    public JwtValidationResult validateToken(String token) {
        try {

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            // Parse and validate the token
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String address = claims.get("address", String.class);

            UserConfig userConfig = DbOperations.getUserConfig(address);

            return new JwtValidationResult(
                    true,
                    userConfig,
                    claims.getExpiration().toInstant(),
                    null
            );

        } catch (ExpiredJwtException e) {
            return new JwtValidationResult(false, null, null, "Token expired");
        } catch (MalformedJwtException e) {
            return new JwtValidationResult(false, null, null, "Invalid token format");
        } catch (SignatureException e) {
            return new JwtValidationResult(false, null, null, "Invalid token signature");
        } catch (Exception e) {
            return new JwtValidationResult(false, null, null, "Token validation failed: " + e.getMessage());
        }
    }

    public JwtValidationResult validateFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            return new JwtValidationResult(false, null, null, "No token provided");
        }

        return validateToken(token);
    }
}