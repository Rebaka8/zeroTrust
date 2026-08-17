package com.zerotrust.iot.entity;

import com.zerotrust.iot.entity.enums.ActionAllowed;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_policies")
public class AccessPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "policy_name", nullable = false, unique = true, length = 100)
    private String policyName;

    @Column(length = 255)
    private String description;

    @Column(name = "minimum_trust_score", nullable = false)
    private Integer minimumTrustScore = 70;

    @Column(name = "required_credential_type", length = 100)
    private String requiredCredentialType;

    @Column(name = "allowed_topics", nullable = false, length = 255)
    private String allowedTopics;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_allowed", nullable = false, length = 20)
    private ActionAllowed actionAllowed;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AccessPolicy() {}

    public AccessPolicy(UUID id, String policyName, String description, Integer minimumTrustScore, String requiredCredentialType, String allowedTopics, ActionAllowed actionAllowed, Boolean isActive) {
        this.id = id;
        this.policyName = policyName;
        this.description = description;
        this.minimumTrustScore = minimumTrustScore != null ? minimumTrustScore : 70;
        this.requiredCredentialType = requiredCredentialType;
        this.allowedTopics = allowedTopics;
        this.actionAllowed = actionAllowed;
        this.isActive = isActive != null ? isActive : true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String policyName;
        private String description;
        private Integer minimumTrustScore = 70;
        private String requiredCredentialType;
        private String allowedTopics;
        private ActionAllowed actionAllowed;
        private Boolean isActive = true;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder policyName(String policyName) { this.policyName = policyName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder minimumTrustScore(Integer minimumTrustScore) { this.minimumTrustScore = minimumTrustScore; return this; }
        public Builder requiredCredentialType(String requiredCredentialType) { this.requiredCredentialType = requiredCredentialType; return this; }
        public Builder allowedTopics(String allowedTopics) { this.allowedTopics = allowedTopics; return this; }
        public Builder actionAllowed(ActionAllowed actionAllowed) { this.actionAllowed = actionAllowed; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }

        public AccessPolicy build() {
            return new AccessPolicy(id, policyName, description, minimumTrustScore, requiredCredentialType, allowedTopics, actionAllowed, isActive);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMinimumTrustScore() { return minimumTrustScore; }
    public void setMinimumTrustScore(Integer minimumTrustScore) { this.minimumTrustScore = minimumTrustScore; }

    public String getRequiredCredentialType() { return requiredCredentialType; }
    public void setRequiredCredentialType(String requiredCredentialType) { this.requiredCredentialType = requiredCredentialType; }

    public String getAllowedTopics() { return allowedTopics; }
    public void setAllowedTopics(String allowedTopics) { this.allowedTopics = allowedTopics; }

    public ActionAllowed getActionAllowed() { return actionAllowed; }
    public void setActionAllowed(ActionAllowed actionAllowed) { this.actionAllowed = actionAllowed; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
