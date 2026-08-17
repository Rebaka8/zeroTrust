package com.zerotrust.iot.entity;

import com.zerotrust.iot.entity.enums.RiskLevel;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "trust_scores")
public class TrustScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @Column(name = "crypto_identity_score", nullable = false)
    private Integer cryptoIdentityScore;

    @Column(name = "behavioral_score", nullable = false)
    private Integer behavioralScore;

    @Column(name = "firmware_score", nullable = false)
    private Integer firmwareScore;

    @Column(name = "network_score", nullable = false)
    private Integer networkScore;

    @Column(name = "penalty_score", nullable = false)
    private Integer penaltyScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 25)
    private RiskLevel riskLevel;

    @Column(name = "evaluation_reasons", columnDefinition = "text")
    private String evaluationReasons;

    @CreationTimestamp
    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private Instant evaluatedAt;

    public TrustScore() {}

    public TrustScore(Long id, Device device, Integer overallScore, Integer cryptoIdentityScore, Integer behavioralScore, Integer firmwareScore, Integer networkScore, Integer penaltyScore, RiskLevel riskLevel, String evaluationReasons, Instant evaluatedAt) {
        this.id = id;
        this.device = device;
        this.overallScore = overallScore;
        this.cryptoIdentityScore = cryptoIdentityScore;
        this.behavioralScore = behavioralScore;
        this.firmwareScore = firmwareScore;
        this.networkScore = networkScore;
        this.penaltyScore = penaltyScore != null ? penaltyScore : 0;
        this.riskLevel = riskLevel;
        this.evaluationReasons = evaluationReasons;
        this.evaluatedAt = evaluatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Device device;
        private Integer overallScore;
        private Integer cryptoIdentityScore;
        private Integer behavioralScore;
        private Integer firmwareScore;
        private Integer networkScore;
        private Integer penaltyScore = 0;
        private RiskLevel riskLevel;
        private String evaluationReasons;
        private Instant evaluatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder device(Device device) { this.device = device; return this; }
        public Builder overallScore(Integer overallScore) { this.overallScore = overallScore; return this; }
        public Builder cryptoIdentityScore(Integer cryptoIdentityScore) { this.cryptoIdentityScore = cryptoIdentityScore; return this; }
        public Builder behavioralScore(Integer behavioralScore) { this.behavioralScore = behavioralScore; return this; }
        public Builder firmwareScore(Integer firmwareScore) { this.firmwareScore = firmwareScore; return this; }
        public Builder networkScore(Integer networkScore) { this.networkScore = networkScore; return this; }
        public Builder penaltyScore(Integer penaltyScore) { this.penaltyScore = penaltyScore; return this; }
        public Builder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public Builder evaluationReasons(String evaluationReasons) { this.evaluationReasons = evaluationReasons; return this; }
        public Builder evaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; return this; }

        public TrustScore build() {
            return new TrustScore(id, device, overallScore, cryptoIdentityScore, behavioralScore, firmwareScore, networkScore, penaltyScore, riskLevel, evaluationReasons, evaluatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

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

    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
