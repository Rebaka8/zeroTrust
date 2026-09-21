package com.zerotrust.iot.dto.simulation;

import com.zerotrust.iot.dto.audit.SecurityAlertResponse;
import com.zerotrust.iot.dto.trust.TrustScoreResponse;
import com.zerotrust.iot.entity.enums.DecisionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AttackSimulationResponse {
    private UUID simulationId;
    private AttackType attackType;
    private UUID targetDeviceId;
    private String targetDeviceName;
    private String targetDidUri;
    private boolean attackDetected;
    private boolean automatedQuarantineTriggered;
    private DecisionType pdpDecision;
    private Integer preAttackTrustScore;
    private Integer postAttackTrustScore;
    private String defenseSummary;
    private List<String> anomalyIndicators;
    private SecurityAlertResponse generatedAlert;
    private TrustScoreResponse updatedTrustScore;
    private Double defenseLatencyMs;
    private Instant executedAt;

    public AttackSimulationResponse() {}

    public AttackSimulationResponse(UUID simulationId, AttackType attackType, UUID targetDeviceId, String targetDeviceName, String targetDidUri, boolean attackDetected, boolean automatedQuarantineTriggered, DecisionType pdpDecision, Integer preAttackTrustScore, Integer postAttackTrustScore, String defenseSummary, List<String> anomalyIndicators, SecurityAlertResponse generatedAlert, TrustScoreResponse updatedTrustScore, Double defenseLatencyMs, Instant executedAt) {
        this.simulationId = simulationId;
        this.attackType = attackType;
        this.targetDeviceId = targetDeviceId;
        this.targetDeviceName = targetDeviceName;
        this.targetDidUri = targetDidUri;
        this.attackDetected = attackDetected;
        this.automatedQuarantineTriggered = automatedQuarantineTriggered;
        this.pdpDecision = pdpDecision;
        this.preAttackTrustScore = preAttackTrustScore;
        this.postAttackTrustScore = postAttackTrustScore;
        this.defenseSummary = defenseSummary;
        this.anomalyIndicators = anomalyIndicators;
        this.generatedAlert = generatedAlert;
        this.updatedTrustScore = updatedTrustScore;
        this.defenseLatencyMs = defenseLatencyMs;
        this.executedAt = executedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID simulationId;
        private AttackType attackType;
        private UUID targetDeviceId;
        private String targetDeviceName;
        private String targetDidUri;
        private boolean attackDetected;
        private boolean automatedQuarantineTriggered;
        private DecisionType pdpDecision;
        private Integer preAttackTrustScore;
        private Integer postAttackTrustScore;
        private String defenseSummary;
        private List<String> anomalyIndicators;
        private SecurityAlertResponse generatedAlert;
        private TrustScoreResponse updatedTrustScore;
        private Double defenseLatencyMs;
        private Instant executedAt;

        public Builder simulationId(UUID simulationId) { this.simulationId = simulationId; return this; }
        public Builder attackType(AttackType attackType) { this.attackType = attackType; return this; }
        public Builder targetDeviceId(UUID targetDeviceId) { this.targetDeviceId = targetDeviceId; return this; }
        public Builder targetDeviceName(String targetDeviceName) { this.targetDeviceName = targetDeviceName; return this; }
        public Builder targetDidUri(String targetDidUri) { this.targetDidUri = targetDidUri; return this; }
        public Builder attackDetected(boolean attackDetected) { this.attackDetected = attackDetected; return this; }
        public Builder automatedQuarantineTriggered(boolean automatedQuarantineTriggered) { this.automatedQuarantineTriggered = automatedQuarantineTriggered; return this; }
        public Builder pdpDecision(DecisionType pdpDecision) { this.pdpDecision = pdpDecision; return this; }
        public Builder preAttackTrustScore(Integer preAttackTrustScore) { this.preAttackTrustScore = preAttackTrustScore; return this; }
        public Builder postAttackTrustScore(Integer postAttackTrustScore) { this.postAttackTrustScore = postAttackTrustScore; return this; }
        public Builder defenseSummary(String defenseSummary) { this.defenseSummary = defenseSummary; return this; }
        public Builder anomalyIndicators(List<String> anomalyIndicators) { this.anomalyIndicators = anomalyIndicators; return this; }
        public Builder generatedAlert(SecurityAlertResponse generatedAlert) { this.generatedAlert = generatedAlert; return this; }
        public Builder updatedTrustScore(TrustScoreResponse updatedTrustScore) { this.updatedTrustScore = updatedTrustScore; return this; }
        public Builder defenseLatencyMs(Double defenseLatencyMs) { this.defenseLatencyMs = defenseLatencyMs; return this; }
        public Builder executedAt(Instant executedAt) { this.executedAt = executedAt; return this; }

        public AttackSimulationResponse build() {
            return new AttackSimulationResponse(simulationId, attackType, targetDeviceId, targetDeviceName, targetDidUri, attackDetected, automatedQuarantineTriggered, pdpDecision, preAttackTrustScore, postAttackTrustScore, defenseSummary, anomalyIndicators, generatedAlert, updatedTrustScore, defenseLatencyMs, executedAt);
        }
    }

    public UUID getSimulationId() { return simulationId; }
    public void setSimulationId(UUID simulationId) { this.simulationId = simulationId; }

    public AttackType getAttackType() { return attackType; }
    public void setAttackType(AttackType attackType) { this.attackType = attackType; }

    public UUID getTargetDeviceId() { return targetDeviceId; }
    public void setTargetDeviceId(UUID targetDeviceId) { this.targetDeviceId = targetDeviceId; }

    public String getTargetDeviceName() { return targetDeviceName; }
    public void setTargetDeviceName(String targetDeviceName) { this.targetDeviceName = targetDeviceName; }

    public String getTargetDidUri() { return targetDidUri; }
    public void setTargetDidUri(String targetDidUri) { this.targetDidUri = targetDidUri; }

    public boolean isAttackDetected() { return attackDetected; }
    public void setAttackDetected(boolean attackDetected) { this.attackDetected = attackDetected; }

    public boolean isAutomatedQuarantineTriggered() { return automatedQuarantineTriggered; }
    public void setAutomatedQuarantineTriggered(boolean automatedQuarantineTriggered) { this.automatedQuarantineTriggered = automatedQuarantineTriggered; }

    public DecisionType getPdpDecision() { return pdpDecision; }
    public void setPdpDecision(DecisionType pdpDecision) { this.pdpDecision = pdpDecision; }

    public Integer getPreAttackTrustScore() { return preAttackTrustScore; }
    public void setPreAttackTrustScore(Integer preAttackTrustScore) { this.preAttackTrustScore = preAttackTrustScore; }

    public Integer getPostAttackTrustScore() { return postAttackTrustScore; }
    public void setPostAttackTrustScore(Integer postAttackTrustScore) { this.postAttackTrustScore = postAttackTrustScore; }

    public String getDefenseSummary() { return defenseSummary; }
    public void setDefenseSummary(String defenseSummary) { this.defenseSummary = defenseSummary; }

    public List<String> getAnomalyIndicators() { return anomalyIndicators; }
    public void setAnomalyIndicators(List<String> anomalyIndicators) { this.anomalyIndicators = anomalyIndicators; }

    public SecurityAlertResponse getGeneratedAlert() { return generatedAlert; }
    public void setGeneratedAlert(SecurityAlertResponse generatedAlert) { this.generatedAlert = generatedAlert; }

    public TrustScoreResponse getUpdatedTrustScore() { return updatedTrustScore; }
    public void setUpdatedTrustScore(TrustScoreResponse updatedTrustScore) { this.updatedTrustScore = updatedTrustScore; }

    public Double getDefenseLatencyMs() { return defenseLatencyMs; }
    public void setDefenseLatencyMs(Double defenseLatencyMs) { this.defenseLatencyMs = defenseLatencyMs; }

    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}
