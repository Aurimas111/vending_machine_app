package vendingmachine.security;

import vendingmachine.model.UserConfig;

import java.time.Instant;

public class JwtValidationResult {

    private boolean valid;
    private UserConfig userConfig;
    private Instant expiresAt;
    private String errorMessage;

    public JwtValidationResult(boolean valid, UserConfig userConfig, Instant expiresAt, String errorMessage) {
        this.valid = valid;
        this.userConfig = userConfig;
        this.expiresAt = expiresAt;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public UserConfig getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(UserConfig userConfig) {
        this.userConfig = userConfig;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
