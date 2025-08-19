package vendingmachine.dto.response;

import vendingmachine.model.UserConfig;

public class AuthResponse {

    private boolean success;
    private String token;
    private String message;
    private UserConfig userConfig;

    public AuthResponse(boolean success, String token, String message, UserConfig userConfig) {
        this.success = success;
        this.token = token;
        this.message = message;
        this.userConfig = userConfig;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public UserConfig getUserConfig() {
        return userConfig;
    }
    public void setUserConfig(UserConfig userConfig) {
        this.userConfig = userConfig;
    }
}