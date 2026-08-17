package com.zerotrust.iot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "target_entity", nullable = false, length = 50)
    private String targetEntity;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "client_user_agent", length = 255)
    private String clientUserAgent;

    @Column(columnDefinition = "text")
    private String details;

    @Column(name = "integrity_hash", length = 64)
    private String integrityHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AuditLog() {}

    public AuditLog(Long id, User user, String actionType, String targetEntity, UUID targetId, String ipAddress, String clientUserAgent, String details, String integrityHash, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.actionType = actionType;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.ipAddress = ipAddress;
        this.clientUserAgent = clientUserAgent;
        this.details = details;
        this.integrityHash = integrityHash;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private String actionType;
        private String targetEntity;
        private UUID targetId;
        private String ipAddress;
        private String clientUserAgent;
        private String details;
        private String integrityHash;
        private Instant createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder actionType(String actionType) { this.actionType = actionType; return this; }
        public Builder targetEntity(String targetEntity) { this.targetEntity = targetEntity; return this; }
        public Builder targetId(UUID targetId) { this.targetId = targetId; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder clientUserAgent(String clientUserAgent) { this.clientUserAgent = clientUserAgent; return this; }
        public Builder details(String details) { this.details = details; return this; }
        public Builder integrityHash(String integrityHash) { this.integrityHash = integrityHash; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public AuditLog build() {
            return new AuditLog(id, user, actionType, targetEntity, targetId, ipAddress, clientUserAgent, details, integrityHash, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getActionType() { return actionType; }
    public void setActionAllowed(String actionType) { this.actionType = actionType; }

    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }

    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getClientUserAgent() { return clientUserAgent; }
    public void setClientUserAgent(String clientUserAgent) { this.clientUserAgent = clientUserAgent; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIntegrityHash() { return integrityHash; }
    public void setIntegrityHash(String integrityHash) { this.integrityHash = integrityHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
