package com.zerotrust.iot.dto.policy;

import com.zerotrust.iot.entity.enums.ActionAllowed;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PolicyCreateRequest {
    @NotBlank(message = "Policy name is required")
    private String policyName;

    private String description;

    @NotNull(message = "Minimum trust score is required")
    @Min(value = 0, message = "Minimum trust score must be at least 0")
    @Max(value = 100, message = "Minimum trust score cannot exceed 100")
    private Integer minimumTrustScore;

    private String requiredCredentialType;

    @NotBlank(message = "Allowed topics pattern is required")
    private String allowedTopics;

    @NotNull(message = "Action allowed is required")
    private ActionAllowed actionAllowed;

    public PolicyCreateRequest() {}

    public PolicyCreateRequest(String policyName, String description, Integer minimumTrustScore, String requiredCredentialType, String allowedTopics, ActionAllowed actionAllowed) {
        this.policyName = policyName;
        this.description = description;
        this.minimumTrustScore = minimumTrustScore;
        this.requiredCredentialType = requiredCredentialType;
        this.allowedTopics = allowedTopics;
        this.actionAllowed = actionAllowed;
    }

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
}
