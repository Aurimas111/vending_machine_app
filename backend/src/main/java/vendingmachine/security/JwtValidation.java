package vendingmachine.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vendingmachine.model.UserConfig;
import vendingmachine.repository.UserConfigRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtValidation {

    @Value("${vending.app.jwtSecret}")
    private String jwtSecret;

    private final UserConfigRepository userConfigRepository;

    public JwtValidation(UserConfigRepository userConfigRepository) {
        this.userConfigRepository = userConfigRepository;
    }

    public JwtValidationResult validateToken(String token) {
        try {

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();

            Date exp = claims.getExpiration();

            String address = claims.get("address", String.class);

            UserConfig userConfig = userConfigRepository.findByOwnerWalletAddress(address);

            return new JwtValidationResult(
                    true,
                    userConfig,
                    exp != null ? exp.toInstant() : null,
                    null
            );

        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            Instant exp = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;
            return new JwtValidationResult(false, null, exp, "Token expired");
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