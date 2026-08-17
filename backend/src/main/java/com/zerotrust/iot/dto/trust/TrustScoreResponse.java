package com.zerotrust.iot.dto.trust;

import com.zerotrust.iot.entity.enums.RiskLevel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TrustScoreResponse {
    private Long id;
    private UUID deviceId;
    private String deviceName;
    private String didUri;
    private Integer overallScore;
    private Integer cryptoIdentityScore;
    private Integer behavioralScore;
    private Integer firmwareScore;
    private Integer networkScore;
    private Integer penaltyScore;
    private RiskLevel riskLevel;
    private String evaluationReasons;
    private List<String> reasonsList;
    private Instant evaluatedAt;

    public TrustScoreResponse() {}

    public TrustScoreResponse(Long id, UUID deviceId, String deviceName, String didUri, Integer overallScore, Integer cryptoIdentityScore, Integer behavioralScore, Integer firmwareScore, Integer networkScore, Integer penaltyScore, RiskLevel riskLevel, String evaluationReasons, List<String> reasonsList, Instant evaluatedAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.didUri = didUri;
        this.overallScore = overallScore;
        this.cryptoIdentityScore = cryptoIdentityScore;
        this.behavioralScore = behavioralScore;
        this.firmwareScore = firmwareScore;
        this.networkScore = networkScore;
        this.penaltyScore = penaltyScore;
        this.riskLevel = riskLevel;
        this.evaluationReasons = evaluationReasons;
        this.reasonsList = reasonsList;
        this.evaluatedAt = evaluatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private UUID deviceId;
        private String deviceName;
        private String didUri;
        private Integer overallScore;
        private Integer cryptoIdentityScore;
        private Integer behavioralScore;
        private Integer firmwareScore;
        private Integer networkScore;
        private Integer penaltyScore;
        private RiskLevel riskLevel;
        private String evaluationReasons;
        private List<String> reasonsList;
        private Instant evaluatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder deviceId(UUID deviceId) { this.deviceId = deviceId; return this; }
        public Builder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public Builder didUri(String didUri) { this.didUri = didUri; return this; }
        public Builder overallScore(Integer overallScore) { this.overallScore = overallScore; return this; }
        public Builder cryptoIdentityScore(Integer cryptoIdentityScore) { this.cryptoIdentityScore = cryptoIdentityScore; return this; }
        public Builder behavioralScore(Integer behavioralScore) { this.behavioralScore = behavioralScore; return this; }
        public Builder firmwareScore(Integer firmwareScore) { this.firmwareScore = firmwareScore; return this; }
        public Builder networkScore(Integer networkScore) { this.networkScore = networkScore; return this; }
        public Builder penaltyScore(Integer penaltyScore) { this.penaltyScore = penaltyScore; return this; }
        public Builder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public Builder evaluationReasons(String evaluationReasons) { this.evaluationReasons = evaluationReasons; return this; }
        public Builder reasonsList(List<String> reasonsList) { this.reasonsList = reasonsList; return this; }
        public Builder evaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; return this; }

        public TrustScoreResponse build() {
            return new TrustScoreResponse(id, deviceId, deviceName, didUri, overallScore, cryptoIdentityScore, behavioralScore, firmwareScore, networkScore, penaltyScore, riskLevel, evaluationReasons, reasonsList, evaluatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public Integer getCryptoIdentityScore() { return cryptoIdentityScore; }
    public void setCryptoIdentityScore(Integer cryptoIdentityScore) { this.cryptoIdentityScore = cryptoIdentityScore; }

    public Integer getBehavioralScore() { return behavioralScore; }
    public void setBehavioralScore(Integer behavioralScore) { this.behavioralScore = behavioralScore; }

    public Integer getFirmwareScore() { return firmwareScore; }
    public void setFirmwareScore(Integer firmwareScore) { this.firmwareScore = firmwareScore; }

    public Integer getNetworkScore() { return networkScore; }
    public void setNetworkScore(Integer networkScore) { this.networkScore = networkScore; }

    public Integer getPenaltyScore() { return penaltyScore; }
    public void setPenaltyScore(Integer penaltyScore) { this.penaltyScore = penaltyScore; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getEvaluationReasons() { return evaluationReasons; }
    public void setEvaluationReasons(String evaluationReasons) { this.evaluationReasons = evaluationReasons; }

    public List<String> getReasonsList() { return reasonsList; }
    public void setReasonsList(List<String> reasonsList) { this.reasonsList = reasonsList; }

    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
