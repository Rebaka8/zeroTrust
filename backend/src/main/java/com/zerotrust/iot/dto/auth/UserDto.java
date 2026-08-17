package com.zerotrust.iot.dto.auth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String walletAddress;
    private String status;
    private List<String> roles;
    private Instant createdAt;

    public UserDto() {}

    public UserDto(UUID id, String username, String email, String walletAddress, String status, List<String> roles, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.walletAddress = walletAddress;
        this.status = status;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String username;
        private String email;
        private String walletAddress;
        private String status;
        private List<String> roles;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder walletAddress(String walletAddress) { this.walletAddress = walletAddress; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder roles(List<String> roles) { this.roles = roles; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public UserDto build() {
            return new UserDto(id, username, email, walletAddress, status, roles, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
