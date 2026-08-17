package com.zerotrust.iot.dto.policy;

import com.zerotrust.iot.entity.enums.ActionAllowed;

import java.time.Instant;
import java.util.UUID;

public class PolicyResponse {
    private UUID id;
    private String policyName;
    private String description;
    private Integer minimumTrustScore;
    private String requiredCredentialType;
    private String allowedTopics;
    private ActionAllowed actionAllowed;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public PolicyResponse() {}

    public PolicyResponse(UUID id, String policyName, String description, Integer minimumTrustScore, String requiredCredentialType, String allowedTopics, ActionAllowed actionAllowed, Boolean isActive, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.policyName = policyName;
        this.description = description;
        this.minimumTrustScore = minimumTrustScore;
        this.requiredCredentialType = requiredCredentialType;
        this.allowedTopics = allowedTopics;
        this.actionAllowed = actionAllowed;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String policyName;
        private String description;
        private Integer minimumTrustScore;
        private String requiredCredentialType;
        private String allowedTopics;
        private ActionAllowed actionAllowed;
        private Boolean isActive;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder policyName(String policyName) { this.policyName = policyName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder minimumTrustScore(Integer minimumTrustScore) { this.minimumTrustScore = minimumTrustScore; return this; }
        public Builder requiredCredentialType(String requiredCredentialType) { this.requiredCredentialType = requiredCredentialType; return this; }
        public Builder allowedTopics(String allowedTopics) { this.allowedTopics = allowedTopics; return this; }
        public Builder actionAllowed(ActionAllowed actionAllowed) { this.actionAllowed = actionAllowed; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public PolicyResponse build() {
            return new PolicyResponse(id, policyName, description, minimumTrustScore, requiredCredentialType, allowedTopics, actionAllowed, isActive, createdAt, updatedAt);
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
