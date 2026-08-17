package com.zerotrust.iot.dto.audit;

import java.time.Instant;
import java.util.UUID;

public class AuditLogResponse {
    private Long id;
    private String username;
    private String actionType;
    private String targetEntity;
    private UUID targetId;
    private String ipAddress;
    private String details;
    private String integrityHash;
    private Instant createdAt;

    public AuditLogResponse() {}

    public AuditLogResponse(Long id, String username, String actionType, String targetEntity, UUID targetId, String ipAddress, String details, String integrityHash, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.actionType = actionType;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.ipAddress = ipAddress;
        this.details = details;
        this.integrityHash = integrityHash;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String actionType;
        private String targetEntity;
        private UUID targetId;
        private String ipAddress;
        private String details;
        private String integrityHash;
        private Instant createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder actionType(String actionType) { this.actionType = actionType; return this; }
        public Builder targetEntity(String targetEntity) { this.targetEntity = targetEntity; return this; }
        public Builder targetId(UUID targetId) { this.targetId = targetId; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder details(String details) { this.details = details; return this; }
        public Builder integrityHash(String integrityHash) { this.integrityHash = integrityHash; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public AuditLogResponse build() {
            return new AuditLogResponse(id, username, actionType, targetEntity, targetId, ipAddress, details, integrityHash, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getActionType() { return actionType; }
    public void setActionAllowed(String actionType) { this.actionType = actionType; }

    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }

    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIntegrityHash() { return integrityHash; }
    public void setIntegrityHash(String integrityHash) { this.integrityHash = integrityHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
