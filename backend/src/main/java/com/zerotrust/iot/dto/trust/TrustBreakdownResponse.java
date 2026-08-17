package com.zerotrust.iot.dto.trust;

import com.zerotrust.iot.entity.enums.RiskLevel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TrustBreakdownResponse {
    private UUID deviceId;
    private String didUri;
    private String deviceName;
    private Integer currentTrustScore;
    private RiskLevel riskLevel;
    private Integer cryptoIdentityScore;
    private Integer behavioralScore;
    private Integer firmwareScore;
    private Integer networkScore;
    private Integer penaltyScore;
    private double cryptoWeight;
    private double behavioralWeight;
    private double firmwareWeight;
    private double networkWeight;
    private double decayLambda;
    private long elapsedSecondsSinceHeartbeat;
    private List<String> contributingFactors;
    private List<TrustScoreResponse> historicalTimeline;
    private Instant lastEvaluatedAt;

    public TrustBreakdownResponse() {}

    public TrustBreakdownResponse(UUID deviceId, String didUri, String deviceName, Integer currentTrustScore, RiskLevel riskLevel, Integer cryptoIdentityScore, Integer behavioralScore, Integer firmwareScore, Integer networkScore, Integer penaltyScore, double cryptoWeight, double behavioralWeight, double firmwareWeight, double networkWeight, double decayLambda, long elapsedSecondsSinceHeartbeat, List<String> contributingFactors, List<TrustScoreResponse> historicalTimeline, Instant lastEvaluatedAt) {
        this.deviceId = deviceId;
        this.didUri = didUri;
        this.deviceName = deviceName;
        this.currentTrustScore = currentTrustScore;
        this.riskLevel = riskLevel;
        this.cryptoIdentityScore = cryptoIdentityScore;
        this.behavioralScore = behavioralScore;
        this.firmwareScore = firmwareScore;
        this.networkScore = networkScore;
        this.penaltyScore = penaltyScore;
        this.cryptoWeight = cryptoWeight;
        this.behavioralWeight = behavioralWeight;
        this.firmwareWeight = firmwareWeight;
        this.networkWeight = networkWeight;
        this.decayLambda = decayLambda;
        this.elapsedSecondsSinceHeartbeat = elapsedSecondsSinceHeartbeat;
        this.contributingFactors = contributingFactors;
        this.historicalTimeline = historicalTimeline;
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID deviceId;
        private String didUri;
        private String deviceName;
        private Integer currentTrustScore;
        private RiskLevel riskLevel;
        private Integer cryptoIdentityScore;
        private Integer behavioralScore;
        private Integer firmwareScore;
        private Integer networkScore;
        private Integer penaltyScore;
        private double cryptoWeight = 0.30;
        private double behavioralWeight = 0.25;
        private double firmwareWeight = 0.25;
        private double networkWeight = 0.20;
        private double decayLambda = 0.0005;
        private long elapsedSecondsSinceHeartbeat;
        private List<String> contributingFactors;
        private List<TrustScoreResponse> historicalTimeline;
        private Instant lastEvaluatedAt;

        public Builder deviceId(UUID deviceId) { this.deviceId = deviceId; return this; }
        public Builder didUri(String didUri) { this.didUri = didUri; return this; }
        public Builder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public Builder currentTrustScore(Integer currentTrustScore) { this.currentTrustScore = currentTrustScore; return this; }
        public Builder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public Builder cryptoIdentityScore(Integer cryptoIdentityScore) { this.cryptoIdentityScore = cryptoIdentityScore; return this; }
        public Builder behavioralScore(Integer behavioralScore) { this.behavioralScore = behavioralScore; return this; }
        public Builder firmwareScore(Integer firmwareScore) { this.firmwareScore = firmwareScore; return this; }
        public Builder networkScore(Integer networkScore) { this.networkScore = networkScore; return this; }
        public Builder penaltyScore(Integer penaltyScore) { this.penaltyScore = penaltyScore; return this; }
        public Builder cryptoWeight(double cryptoWeight) { this.cryptoWeight = cryptoWeight; return this; }
        public Builder behavioralWeight(double behavioralWeight) { this.behavioralWeight = behavioralWeight; return this; }
        public Builder firmwareWeight(double firmwareWeight) { this.firmwareWeight = firmwareWeight; return this; }
        public Builder networkWeight(double networkWeight) { this.networkWeight = networkWeight; return this; }
        public Builder decayLambda(double decayLambda) { this.decayLambda = decayLambda; return this; }
        public Builder elapsedSecondsSinceHeartbeat(long elapsedSecondsSinceHeartbeat) { this.elapsedSecondsSinceHeartbeat = elapsedSecondsSinceHeartbeat; return this; }
        public Builder contributingFactors(List<String> contributingFactors) { this.contributingFactors = contributingFactors; return this; }
        public Builder historicalTimeline(List<TrustScoreResponse> historicalTimeline) { this.historicalTimeline = historicalTimeline; return this; }
        public Builder lastEvaluatedAt(Instant lastEvaluatedAt) { this.lastEvaluatedAt = lastEvaluatedAt; return this; }

        public TrustBreakdownResponse build() {
            return new TrustBreakdownResponse(deviceId, didUri, deviceName, currentTrustScore, riskLevel, cryptoIdentityScore, behavioralScore, firmwareScore, networkScore, penaltyScore, cryptoWeight, behavioralWeight, firmwareWeight, networkWeight, decayLambda, elapsedSecondsSinceHeartbeat, contributingFactors, historicalTimeline, lastEvaluatedAt);
        }
    }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public Integer getCurrentTrustScore() { return currentTrustScore; }
    public void setCurrentTrustScore(Integer currentTrustScore) { this.currentTrustScore = currentTrustScore; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

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

    public double getCryptoWeight() { return cryptoWeight; }
    public void setCryptoWeight(double cryptoWeight) { this.cryptoWeight = cryptoWeight; }

    public double getBehavioralWeight() { return behavioralWeight; }
    public void setBehavioralWeight(double behavioralWeight) { this.behavioralWeight = behavioralWeight; }

    public double getFirmwareWeight() { return firmwareWeight; }
    public void setFirmwareWeight(double firmwareWeight) { this.firmwareWeight = firmwareWeight; }

    public double getNetworkWeight() { return networkWeight; }
    public void setNetworkWeight(double networkWeight) { this.networkWeight = networkWeight; }

    public double getDecayLambda() { return decayLambda; }
    public void setDecayLambda(double decayLambda) { this.decayLambda = decayLambda; }

    public long getElapsedSecondsSinceHeartbeat() { return elapsedSecondsSinceHeartbeat; }
    public void setElapsedSecondsSinceHeartbeat(long elapsedSecondsSinceHeartbeat) { this.elapsedSecondsSinceHeartbeat = elapsedSecondsSinceHeartbeat; }

    public List<String> getContributingFactors() { return contributingFactors; }
    public void setContributingFactors(List<String> contributingFactors) { this.contributingFactors = contributingFactors; }

    public List<TrustScoreResponse> getHistoricalTimeline() { return historicalTimeline; }
    public void setHistoricalTimeline(List<TrustScoreResponse> historicalTimeline) { this.historicalTimeline = historicalTimeline; }

    public Instant getLastEvaluatedAt() { return lastEvaluatedAt; }
    public void setLastEvaluatedAt(Instant lastEvaluatedAt) { this.lastEvaluatedAt = lastEvaluatedAt; }
}
