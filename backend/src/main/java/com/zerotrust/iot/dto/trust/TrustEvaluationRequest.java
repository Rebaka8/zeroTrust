package com.zerotrust.iot.dto.trust;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class TrustEvaluationRequest {
    @NotNull(message = "Device ID is required")
    private UUID deviceId;

    private boolean forceReevaluation = false;

    public TrustEvaluationRequest() {}

    public TrustEvaluationRequest(UUID deviceId, boolean forceReevaluation) {
        this.deviceId = deviceId;
        this.forceReevaluation = forceReevaluation;
    }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public boolean isForceReevaluation() { return forceReevaluation; }
    public void setForceReevaluation(boolean forceReevaluation) { this.forceReevaluation = forceReevaluation; }
}
