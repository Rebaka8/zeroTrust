package com.zerotrust.iot.dto.auth;

import java.util.List;
import java.util.UUID;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UUID id;
    private String username;
    private String email;
    private String walletAddress;
    private List<String> roles;

    public AuthResponse() {}

    public AuthResponse(String token, String tokenType, UUID id, String username, String email, String walletAddress, List<String> roles) {
        this.token = token;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.walletAddress = walletAddress;
        this.roles = roles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String tokenType = "Bearer";
        private UUID id;
        private String username;
        private String email;
        private String walletAddress;
        private List<String> roles;

        public Builder token(String token) { this.token = token; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder walletAddress(String walletAddress) { this.walletAddress = walletAddress; return this; }
        public Builder roles(List<String> roles) { this.roles = roles; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, tokenType, id, username, email, walletAddress, roles);
        }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
