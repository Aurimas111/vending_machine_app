package vendingmachine.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.cardanofoundation.cip30.AddressFormat;
import org.cardanofoundation.cip30.CIP30Verifier;
import org.cardanofoundation.cip30.Cip30VerificationResult;
import org.cardanofoundation.cip30.MessageFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vendingmachine.dto.request.LoginRequest;
import vendingmachine.dto.response.AuthResponse;
import vendingmachine.repository.UserConfigRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserConfigRepository userConfigRepository;
    @Mock CIP30VerifierFactory verifierFactory;
    @Mock CIP30Verifier mockVerifier;
    @Mock
    private Cip30VerificationResult mockResult;

    private AuthService authService;

    private static final String TEST_SECRET_KEY = "ThisIsAVeryLongSecretKeyForTestingPurposesOnly123456";
    private static final int TEST_JWT_EXPIRATION = 3600000;
    private static final String TEST_ADDRESS = "addr_test1qz2fxv2umyhttkxyxp8x0dlpdt3k6cwng5pxj3jhsydzer3jcu5d8ps7zex2k2xt3uqxgjqnnj83ws8lhrn648jjxtwq2ytjqp";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userConfigRepository, verifierFactory);
        ReflectionTestUtils.setField(authService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(authService, "jwtExpiration", TEST_JWT_EXPIRATION);
    }

    @Test
    void generateNonce_ShouldReturnUniqueNonce() {
        String nonce1 = authService.generateNonce(TEST_ADDRESS);
        String nonce2 = authService.generateNonce(TEST_ADDRESS + "2");

        assertNotNull(nonce1);
        assertNotNull(nonce2);
        assertNotEquals(nonce1, nonce2);
    }

    @Test
    void generateNonce_ShouldOverwritePreviousNonceForSameAddress() {
        String nonce1 = authService.generateNonce(TEST_ADDRESS);
        String nonce2 = authService.generateNonce(TEST_ADDRESS);

        assertNotNull(nonce1);
        assertNotNull(nonce2);
        assertEquals(nonce2, authService.getNonce(TEST_ADDRESS));
    }

    @Test
    void getNonce_ShouldReturnStoredNonce() {
        String generatedNonce = authService.generateNonce(TEST_ADDRESS);
        String retrievedNonce = authService.getNonce(TEST_ADDRESS);

        assertEquals(generatedNonce, retrievedNonce);
    }

    @Test
    void getNonce_ShouldReturnNullForUnknownAddress() {
        String nonce = authService.getNonce("unknown_address");

        assertNull(nonce);
    }

    @Test
    void clearNonce_ShouldRemoveStoredNonce() {
        authService.generateNonce(TEST_ADDRESS);
        assertNotNull(authService.getNonce(TEST_ADDRESS));

        authService.clearNonce(TEST_ADDRESS);

        assertNull(authService.getNonce(TEST_ADDRESS));
    }

    @Test
    void clearNonce_ShouldNotThrowForUnknownAddress() {
        assertDoesNotThrow(() -> authService.clearNonce("unknown_address"));
    }

    @Test
    void authenticate_ShouldFailWhenNonceIsNull() {
        LoginRequest request = new LoginRequest(TEST_ADDRESS, "key", "signature");

        AuthResponse response = authService.authenticate(request);

        assertFalse(response.isSuccess());
        assertNull(response.getToken());
        assertEquals("Invalid or expired nonce", response.getMessage());
        assertNull(response.getUserConfig());
    }

    @Test
    void authenticate_ShouldFailWhenNonceIsExpiredOrInvalid() {
        LoginRequest request = new LoginRequest(TEST_ADDRESS, "key", "signature");

        AuthResponse response = authService.authenticate(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid or expired nonce", response.getMessage());
    }

    @Test
    void generateJWT_ShouldReturnValidToken() {
        String token = authService.generateJWT(TEST_ADDRESS);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(TEST_ADDRESS, claims.getSubject());
        assertEquals(TEST_ADDRESS, claims.get("address"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void generateJWT_ShouldSetCorrectExpiration() {
        String token = authService.generateJWT(TEST_ADDRESS);

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long expirationTime = claims.getExpiration().getTime();
        long issuedTime = claims.getIssuedAt().getTime();
        long difference = expirationTime - issuedTime;

        assertTrue(difference >= TEST_JWT_EXPIRATION - 1000 && difference <= TEST_JWT_EXPIRATION + 1000);
    }

    @Test
    void generateJWT_ShouldGenerateDifferentTokensForDifferentAddresses() {
        String token1 = authService.generateJWT(TEST_ADDRESS);
        String token2 = authService.generateJWT(TEST_ADDRESS + "2");

        assertNotEquals(token1, token2);
    }

    @Test
    void multipleAddresses_ShouldMaintainSeparateNonces() {
        String address1 = TEST_ADDRESS;
        String address2 = TEST_ADDRESS + "2";

        String nonce1 = authService.generateNonce(address1);
        String nonce2 = authService.generateNonce(address2);

        assertEquals(nonce1, authService.getNonce(address1));
        assertEquals(nonce2, authService.getNonce(address2));

        authService.clearNonce(address1);

        assertNull(authService.getNonce(address1));
        assertEquals(nonce2, authService.getNonce(address2));
    }

    @Test
    void authenticate_ShouldPassWithValidSignature() {
        String nonce = authService.generateNonce(TEST_ADDRESS);
        LoginRequest request = new LoginRequest(TEST_ADDRESS, "key", "signature");

        when(verifierFactory.createCIP30Verifier(request.getSignature(), request.getKey())).thenReturn(mockVerifier);
        when(mockVerifier.verify()).thenReturn(mockResult);
        when(mockResult.isValid()).thenReturn(true);
        when(mockResult.getMessage(MessageFormat.TEXT)).thenReturn(nonce);
        when(mockResult.getAddress(AddressFormat.TEXT)).thenReturn(Optional.of(TEST_ADDRESS));
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(null);

        AuthResponse response = authService.authenticate(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getToken());
        assertEquals("Authentication successful", response.getMessage());
        assertNotNull(response.getUserConfig());

        assertNull(authService.getNonce(TEST_ADDRESS));
    }

    @Test
    void authenticate_ShouldFailWhenSignatureIsInvalid() {
        authService.generateNonce(TEST_ADDRESS);
        LoginRequest request = new LoginRequest(TEST_ADDRESS, "key", "signature");

        when(verifierFactory.createCIP30Verifier(request.getSignature(), request.getKey())).thenReturn(mockVerifier);
        when(mockVerifier.verify()).thenReturn(mockResult);
        when(mockResult.isValid()).thenReturn(false);

        AuthResponse response = authService.authenticate(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid signature", response.getMessage());
    }

}
