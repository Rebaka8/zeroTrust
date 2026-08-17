package com.zerotrust.iot.service;

import com.zerotrust.iot.dto.trust.TrustBreakdownResponse;
import com.zerotrust.iot.dto.trust.TrustScoreResponse;
import com.zerotrust.iot.entity.*;
import com.zerotrust.iot.entity.enums.AlertSeverity;
import com.zerotrust.iot.entity.enums.DeviceStatus;
import com.zerotrust.iot.exception.ResourceNotFoundException;
import com.zerotrust.iot.repository.*;
import com.zerotrust.iot.trust.RiskEvaluationEngine;
import com.zerotrust.iot.trust.RiskEvaluationEngine.EvaluationResult;
import com.zerotrust.iot.trust.TrustCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrustScoreService {

    private static final Logger log = LoggerFactory.getLogger(TrustScoreService.class);

    private final DeviceRepository deviceRepository;
    private final DidDocumentRepository didDocumentRepository;
    private final VerifiableCredentialRepository vcRepository;
    private final DeviceTelemetryRepository telemetryRepository;
    private final SecurityAlertRepository alertRepository;
    private final TrustScoreRepository trustScoreRepository;
    private final RiskEvaluationEngine riskEvaluationEngine;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    public TrustScoreService(
            DeviceRepository deviceRepository,
            DidDocumentRepository didDocumentRepository,
            VerifiableCredentialRepository vcRepository,
            DeviceTelemetryRepository telemetryRepository,
            SecurityAlertRepository alertRepository,
            TrustScoreRepository trustScoreRepository,
            RiskEvaluationEngine riskEvaluationEngine,
            AuditService auditService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.deviceRepository = deviceRepository;
        this.didDocumentRepository = didDocumentRepository;
        this.vcRepository = vcRepository;
        this.telemetryRepository = telemetryRepository;
        this.alertRepository = alertRepository;
        this.trustScoreRepository = trustScoreRepository;
        this.riskEvaluationEngine = riskEvaluationEngine;
        this.auditService = auditService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public TrustScoreResponse evaluateDeviceTrust(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        DidDocument didDoc = didDocumentRepository.findByDeviceId(deviceId).orElse(null);
        String didUri = didDoc != null ? didDoc.getDidUri() : "did:zt:unregistered";

        VerifiableCredential activeVc = didDoc != null
                ? vcRepository.findFirstBySubjectDidAndIsRevokedFalse(didUri).orElse(null)
                : null;

        List<DeviceTelemetry> recentTelemetry = telemetryRepository.findTop50ByDeviceIdOrderByRecordedAtDesc(deviceId);
        long unresolvedViolations = alertRepository.countByDeviceIdAndIsResolvedFalse(deviceId);

        // Run multi-factor risk engine
        EvaluationResult eval = riskEvaluationEngine.evaluateDevice(
                device,
                didDoc,
                activeVc,
                recentTelemetry,
                (int) unresolvedViolations
        );

        String reasonsJoined = String.join(" | ", eval.getReasons());

        TrustScore trustRecord = TrustScore.builder()
                .device(device)
                .overallScore(eval.getOverallScore())
                .cryptoIdentityScore(eval.getCryptoScore())
                .behavioralScore(eval.getBehavioralScore())
                .firmwareScore(eval.getFirmwareScore())
                .networkScore(eval.getNetworkScore())
                .penaltyScore(eval.getPenaltyScore())
                .riskLevel(eval.getRiskLevel())
                .evaluationReasons(reasonsJoined)
                .evaluatedAt(Instant.now())
                .build();

        TrustScore savedRecord = trustScoreRepository.save(trustRecord);

        // Update live trust score on device entity
        device.setCurrentTrustScore(eval.getOverallScore());

        // Zero Trust Continuous Enforcement: Automatic Quarantine if T(t) drops below 35
        if (eval.getOverallScore() < 35 && !device.getIsQuarantined()) {
            device.setIsQuarantined(true);
            device.setStatus(DeviceStatus.QUARANTINED);

            SecurityAlert criticalAlert = SecurityAlert.builder()
                    .device(device)
                    .alertType("DYNAMIC_TRUST_SCORE_DEGRADATION")
                    .severity(AlertSeverity.CRITICAL)
                    .description("Automatic Zero Trust Quarantine: Trust score collapsed to " + eval.getOverallScore() + "/100 (" + eval.getRiskLevel() + "). Reasons: " + reasonsJoined)
                    .isResolved(false)
                    .triggeredAt(Instant.now())
                    .build();

            alertRepository.save(criticalAlert);
            messagingTemplate.convertAndSend("/topic/alerts", criticalAlert);

            auditService.logAction(
                    null,
                    "AUTOMATIC_QUARANTINE_ENFORCED",
                    "Device",
                    deviceId,
                    "Zero Trust Engine isolated device " + device.getDeviceName() + " (Score: " + eval.getOverallScore() + ")"
            );
        }

        deviceRepository.save(device);

        TrustScoreResponse response = mapToResponse(savedRecord, device.getDeviceName(), didUri, eval.getReasons());

        // Broadcast real-time score update via WebSocket
        messagingTemplate.convertAndSend("/topic/trust", response);

        return response;
    }

    @Transactional(readOnly = true)
    public TrustBreakdownResponse getTrustBreakdown(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        DidDocument didDoc = didDocumentRepository.findByDeviceId(deviceId).orElse(null);
        String didUri = didDoc != null ? didDoc.getDidUri() : "did:zt:unregistered";

        TrustScore latest = trustScoreRepository.findTopByDeviceIdOrderByEvaluatedAtDesc(deviceId)
                .orElseGet(() -> TrustScore.builder()
                        .device(device)
                        .overallScore(device.getCurrentTrustScore())
                        .cryptoIdentityScore(100)
                        .behavioralScore(100)
                        .firmwareScore(100)
                        .networkScore(100)
                        .penaltyScore(0)
                        .riskLevel(com.zerotrust.iot.entity.enums.RiskLevel.LOW_NOMINAL)
                        .evaluationReasons("Baseline nominal trust state.")
                        .evaluatedAt(Instant.now())
                        .build());

        List<TrustScoreResponse> timeline = trustScoreRepository.findTop30ByDeviceIdOrderByEvaluatedAtDesc(deviceId).stream()
                .map(t -> mapToResponse(t, device.getDeviceName(), didUri, parseReasons(t.getEvaluationReasons())))
                .collect(Collectors.toList());

        long elapsedSeconds = device.getLastHeartbeat() != null
                ? Duration.between(device.getLastHeartbeat(), Instant.now()).getSeconds()
                : 0;

        return TrustBreakdownResponse.builder()
                .deviceId(device.getId())
                .didUri(didUri)
                .deviceName(device.getDeviceName())
                .currentTrustScore(device.getCurrentTrustScore())
                .riskLevel(latest.getRiskLevel())
                .cryptoIdentityScore(latest.getCryptoIdentityScore())
                .behavioralScore(latest.getBehavioralScore())
                .firmwareScore(latest.getFirmwareScore())
                .networkScore(latest.getNetworkScore())
                .penaltyScore(latest.getPenaltyScore())
                .cryptoWeight(TrustCalculator.WEIGHT_CRYPTO)
                .behavioralWeight(TrustCalculator.WEIGHT_BEHAVIORAL)
                .firmwareWeight(TrustCalculator.WEIGHT_FIRMWARE)
                .networkWeight(TrustCalculator.WEIGHT_NETWORK)
                .decayLambda(TrustCalculator.DECAY_LAMBDA)
                .elapsedSecondsSinceHeartbeat(elapsedSeconds)
                .contributingFactors(parseReasons(latest.getEvaluationReasons()))
                .historicalTimeline(timeline)
                .lastEvaluatedAt(latest.getEvaluatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<TrustScoreResponse> getTrustHistoryPaged(UUID deviceId, Pageable pageable) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        DidDocument didDoc = didDocumentRepository.findByDeviceId(deviceId).orElse(null);
        String didUri = didDoc != null ? didDoc.getDidUri() : "did:zt:unregistered";

        return trustScoreRepository.findByDeviceIdOrderByEvaluatedAtDesc(deviceId, pageable)
                .map(t -> mapToResponse(t, device.getDeviceName(), didUri, parseReasons(t.getEvaluationReasons())));
    }

    private List<String> parseReasons(String reasonsStr) {
        if (reasonsStr == null || reasonsStr.isBlank()) return List.of("Baseline nominal state");
        return Arrays.stream(reasonsStr.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private TrustScoreResponse mapToResponse(TrustScore score, String deviceName, String didUri, List<String> reasons) {
        return TrustScoreResponse.builder()
                .id(score.getId())
                .deviceId(score.getDevice() != null ? score.getDevice().getId() : null)
                .deviceName(deviceName)
                .didUri(didUri)
                .overallScore(score.getOverallScore())
                .cryptoIdentityScore(score.getCryptoIdentityScore())
                .behavioralScore(score.getBehavioralScore())
                .firmwareScore(score.getFirmwareScore())
                .networkScore(score.getNetworkScore())
                .penaltyScore(score.getPenaltyScore())
                .riskLevel(score.getRiskLevel())
                .evaluationReasons(score.getEvaluationReasons())
                .reasonsList(reasons)
                .evaluatedAt(score.getEvaluatedAt())
                .build();
    }
}
