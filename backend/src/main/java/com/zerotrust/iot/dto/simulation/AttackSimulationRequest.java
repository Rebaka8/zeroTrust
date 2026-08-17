package com.zerotrust.iot.dto.simulation;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AttackSimulationRequest {
    @NotNull(message = "Attack type is required")
    private AttackType attackType;

    private UUID targetDeviceId; // Optional: If omitted, applies to a random active device

    private Integer intensity; // 1 (low) to 10 (high)

    private String simulatedPayload;

    public AttackSimulationRequest() {}

    public AttackSimulationRequest(AttackType attackType, UUID targetDeviceId, Integer intensity, String simulatedPayload) {
        this.attackType = attackType;
        this.targetDeviceId = targetDeviceId;
        this.intensity = intensity != null ? intensity : 5;
        this.simulatedPayload = simulatedPayload;
    }

    public AttackType getAttackType() { return attackType; }
    public void setAttackType(AttackType attackType) { this.attackType = attackType; }

    public UUID getTargetDeviceId() { return targetDeviceId; }
    public void setTargetDeviceId(UUID targetDeviceId) { this.targetDeviceId = targetDeviceId; }

    public Integer getIntensity() { return intensity; }
    public void setIntensity(Integer intensity) { this.intensity = intensity; }

    public String getSimulatedPayload() { return simulatedPayload; }
    public void setSimulatedPayload(String simulatedPayload) { this.simulatedPayload = simulatedPayload; }
}
