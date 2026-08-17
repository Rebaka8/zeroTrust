package com.zerotrust.iot.dto.vc;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class VcIssueRequest {
    @NotNull(message = "Device ID is required")
    private UUID deviceId;

    private String credentialType; // default: IoTDeviceAttestation
    private Integer validityDays; // default: 365
    private String trustTier; // default: TIER_1_STANDARD
    private List<String> capabilities;

    public VcIssueRequest() {}

    public VcIssueRequest(UUID deviceId, String credentialType, Integer validityDays, String trustTier, List<String> capabilities) {
        this.deviceId = deviceId;
        this.credentialType = credentialType;
        this.validityDays = validityDays;
        this.trustTier = trustTier;
        this.capabilities = capabilities;
    }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }

    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }

    public String getTrustTier() { return trustTier; }
    public void setTrustTier(String trustTier) { this.trustTier = trustTier; }

    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
}
