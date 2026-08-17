package com.zerotrust.iot.trust;

import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DeviceTelemetry;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.VerifiableCredential;
import com.zerotrust.iot.entity.enums.RiskLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class RiskEvaluationEngine {

    private final TrustCalculator trustCalculator;

    public RiskEvaluationEngine(TrustCalculator trustCalculator) {
        this.trustCalculator = trustCalculator;
    }

    public static class EvaluationResult {
        private final int overallScore;
        private final int cryptoScore;
        private final int behavioralScore;
        private final int firmwareScore;
        private final int networkScore;
        private final int penaltyScore;
        private final RiskLevel riskLevel;
        private final List<String> reasons;

        public EvaluationResult(int overallScore, int cryptoScore, int behavioralScore, int firmwareScore, int networkScore, int penaltyScore, RiskLevel riskLevel, List<String> reasons) {
            this.overallScore = overallScore;
            this.cryptoScore = cryptoScore;
            this.behavioralScore = behavioralScore;
            this.firmwareScore = firmwareScore;
            this.networkScore = networkScore;
            this.penaltyScore = penaltyScore;
            this.riskLevel = riskLevel;
            this.reasons = reasons;
        }

        public int getOverallScore() { return overallScore; }
        public int getCryptoScore() { return cryptoScore; }
        public int getBehavioralScore() { return behavioralScore; }
        public int getFirmwareScore() { return firmwareScore; }
        public int getNetworkScore() { return networkScore; }
        public int getPenaltyScore() { return penaltyScore; }
        public RiskLevel getRiskLevel() { return riskLevel; }
        public List<String> getReasons() { return reasons; }
    }

    public EvaluationResult evaluateDevice(
            Device device,
            DidDocument didDoc,
            VerifiableCredential activeVc,
            List<DeviceTelemetry> recentTelemetry,
            int recentSecurityViolationsCount
    ) {
        List<String> reasons = new ArrayList<>();
        Instant now = Instant.now();

        // 1. Cryptographic Factor C(t)
        int cryptoScore = 100;
        if (didDoc == null) {
            cryptoScore = 0;
            reasons.add("DID_UNREGISTERED: No W3C DID document found for device.");
        } else if (didDoc.getIsRevoked()) {
            cryptoScore = 0;
            reasons.add("DID_REVOKED: Device W3C DID has been revoked.");
        } else if (activeVc == null) {
            cryptoScore = 40;
            reasons.add("NO_ACTIVE_VC: Device lacks an active W3C Verifiable Credential.");
        } else if (activeVc.getIsRevoked()) {
            cryptoScore = 10;
            reasons.add("VC_REVOKED: Device Verifiable Credential has been revoked.");
        } else if (activeVc.getExpirationDate() != null && now.isAfter(activeVc.getExpirationDate())) {
            cryptoScore = 30;
            reasons.add("VC_EXPIRED: Device Verifiable Credential is past its validity expiration.");
        } else {
            reasons.add("CRYPTO_VERIFIED: Valid W3C DID and active Verifiable Credential.");
        }

        // 2. Behavioral Telemetry Factor B(t)
        int behavioralScore = 100;
        if (recentTelemetry != null && !recentTelemetry.isEmpty()) {
            DeviceTelemetry latest = recentTelemetry.get(0);

            // Temperature anomaly check (standard operating range: -10C to +70C)
            if (latest.getTemperature() != null) {
                double temp = latest.getTemperature().doubleValue();
                if (temp > 85.0 || temp < -20.0) {
                    behavioralScore -= 40;
                    reasons.add("TEMP_CRITICAL_SPIKE: Temperature reading (" + temp + " C) exceeds safety threshold.");
                } else if (temp > 70.0) {
                    behavioralScore -= 20;
                    reasons.add("TEMP_ELEVATED: Temperature reading (" + temp + " C) is abnormally high.");
                }
            }

            // CPU utilization check
            if (latest.getCpuUtilization() != null) {
                double cpu = latest.getCpuUtilization().doubleValue();
                if (cpu > 95.0) {
                    behavioralScore -= 35;
                    reasons.add("CPU_SATURATION: Device CPU utilization is critical (" + cpu + "%). Possible crypto-jacking or DOS.");
                } else if (cpu > 80.0) {
                    behavioralScore -= 15;
                    reasons.add("CPU_HIGH: High CPU utilization (" + cpu + "%).");
                }
            }

            // Memory usage check
            if (latest.getMemoryUsage() != null) {
                double mem = latest.getMemoryUsage().doubleValue();
                if (mem > 95.0) {
                    behavioralScore -= 25;
                    reasons.add("MEMORY_LEAK_ALERT: Memory consumption reached " + mem + "%.");
                }
            }
        }
        behavioralScore = Math.max(0, behavioralScore);

        // 3. Firmware Attestation Factor F(t)
        int firmwareScore = 100;
        if (device.getFirmwareHash() == null || device.getFirmwareHash().isBlank()) {
            firmwareScore = 0;
            reasons.add("FIRMWARE_UNREGISTERED: No firmware digest recorded.");
        } else if (activeVc != null && activeVc.getClaimsJson() != null && !activeVc.getClaimsJson().contains(device.getFirmwareHash())) {
            firmwareScore = 10;
            reasons.add("FIRMWARE_TAMPER: Live firmware hash does not match Verifiable Credential attestation.");
        } else {
            reasons.add("FIRMWARE_ATTESTED: Firmware integrity verified against known attestation baseline.");
        }

        // 4. Network Factor N(t)
        int networkScore = 100;
        if (recentTelemetry != null && !recentTelemetry.isEmpty()) {
            DeviceTelemetry latest = recentTelemetry.get(0);
            if (latest.getPacketRate() != null) {
                int rate = latest.getPacketRate();
                if (rate > 250) {
                    networkScore -= 50;
                    reasons.add("PACKET_FLOOD_DETECTED: High packet ingestion rate (" + rate + " pkt/s) indicates possible DDoS or exfiltration.");
                } else if (rate > 100) {
                    networkScore -= 20;
                    reasons.add("NETWORK_TRAFFIC_ELEVATED: Elevated packet rate (" + rate + " pkt/s).");
                }
            }
        }
        networkScore = Math.max(0, networkScore);

        // 5. Penalty Score P(t)
        int penaltyScore = 0;
        if (device.getIsQuarantined()) {
            penaltyScore += 60;
            reasons.add("QUARANTINE_PENALTY: Device is flagged under quarantine isolation.");
        }
        if (recentSecurityViolationsCount > 0) {
            int violationPenalty = Math.min(recentSecurityViolationsCount * 25, 75);
            penaltyScore += violationPenalty;
            reasons.add("SECURITY_INCIDENT_PENALTY: " + recentSecurityViolationsCount + " recent security violation(s) detected (-" + violationPenalty + " pts).");
        }

        // 6. Elapsed time delta since last heartbeat (in seconds)
        long elapsedSeconds = 0;
        if (device.getLastHeartbeat() != null) {
            elapsedSeconds = Duration.between(device.getLastHeartbeat(), now).getSeconds();
        }

        // Calculate Overall Trust Score T(t)
        int overallScore = trustCalculator.calculateTrustScore(
                cryptoScore,
                behavioralScore,
                firmwareScore,
                networkScore,
                elapsedSeconds,
                penaltyScore
        );

        // Classify Risk Level
        RiskLevel riskLevel;
        if (overallScore >= 80) {
            riskLevel = RiskLevel.LOW_NOMINAL;
        } else if (overallScore >= 60) {
            riskLevel = RiskLevel.MEDIUM_ELEVATED;
        } else if (overallScore >= 35) {
            riskLevel = RiskLevel.HIGH_SUSPICIOUS;
        } else {
            riskLevel = RiskLevel.CRITICAL_COMPROMISED;
        }

        return new EvaluationResult(
                overallScore,
                cryptoScore,
                behavioralScore,
                firmwareScore,
                networkScore,
                penaltyScore,
                riskLevel,
                reasons
        );
    }
}
