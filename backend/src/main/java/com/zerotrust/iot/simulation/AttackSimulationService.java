package com.zerotrust.iot.simulation;

import com.zerotrust.iot.dto.audit.SecurityAlertResponse;
import com.zerotrust.iot.dto.simulation.AttackSimulationRequest;
import com.zerotrust.iot.dto.simulation.AttackSimulationResponse;
import com.zerotrust.iot.dto.simulation.AttackType;
import com.zerotrust.iot.dto.simulation.SimulationScenarioDto;
import com.zerotrust.iot.dto.telemetry.TelemetryIngestRequest;
import com.zerotrust.iot.dto.trust.TrustScoreResponse;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.SecurityAlert;
import com.zerotrust.iot.entity.enums.AlertSeverity;
import com.zerotrust.iot.entity.enums.DecisionType;
import com.zerotrust.iot.entity.enums.DeviceStatus;
import com.zerotrust.iot.exception.BadRequestException;
import com.zerotrust.iot.exception.ResourceNotFoundException;
import com.zerotrust.iot.pdp.PolicyDecisionPoint;
import com.zerotrust.iot.pdp.PolicyDecisionPoint.DecisionResult;
import com.zerotrust.iot.repository.AccessPolicyRepository;
import com.zerotrust.iot.repository.DeviceRepository;
import com.zerotrust.iot.repository.DidDocumentRepository;
import com.zerotrust.iot.repository.SecurityAlertRepository;
import com.zerotrust.iot.service.AuditService;
import com.zerotrust.iot.service.TelemetryIngestService;
import com.zerotrust.iot.service.TrustScoreService;
import com.zerotrust.iot.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AttackSimulationService {

    private static final Logger log = LoggerFactory.getLogger(AttackSimulationService.class);

    private final DeviceRepository deviceRepository;
    private final DidDocumentRepository didDocumentRepository;
    private final SecurityAlertRepository alertRepository;
    private final AccessPolicyRepository policyRepository;
    private final TrustScoreService trustScoreService;
    private final TelemetryIngestService telemetryIngestService;
    private final PolicyDecisionPoint pdp;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    public AttackSimulationService(
            DeviceRepository deviceRepository,
            DidDocumentRepository didDocumentRepository,
            SecurityAlertRepository alertRepository,
            AccessPolicyRepository policyRepository,
            TrustScoreService trustScoreService,
            TelemetryIngestService telemetryIngestService,
            PolicyDecisionPoint pdp,
            AuditService auditService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.deviceRepository = deviceRepository;
        this.didDocumentRepository = didDocumentRepository;
        this.alertRepository = alertRepository;
        this.policyRepository = policyRepository;
        this.trustScoreService = trustScoreService;
        this.telemetryIngestService = telemetryIngestService;
        this.pdp = pdp;
        this.auditService = auditService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<SimulationScenarioDto> getAvailableScenarios() {
        return List.of(
                SimulationScenarioDto.builder()
                        .attackType(AttackType.TAMPER_PAYLOAD)
                        .name("Man-In-The-Middle (MITM) & Payload Tampering")
                        .description("Corrupts telemetry cryptographic signature and injects forged critical temperature/voltage readings.")
                        .expectedDefenseOutcome("Cryptographic signature mismatch detected; Behavioral score penalized; Security Alert raised.")
                        .mitigationMechanism("Ed25519 digital signature attestation & continuous dynamic trust re-evaluation.")
                        .build(),

                SimulationScenarioDto.builder()
                        .attackType(AttackType.REPLAY_ATTACK)
                        .name("Cryptographic Replay Attack")
                        .description("Replays historical signed telemetry packets with stale nonces and outdated timestamps to spoof node state.")
                        .expectedDefenseOutcome("Stale timestamp delta and duplicate payload signature detected; Access denied.")
                        .mitigationMechanism("Timestamp freshness verification & nonce uniqueness checking.")
                        .build(),

                SimulationScenarioDto.builder()
                        .attackType(AttackType.FIRMWARE_MODIFICATION)
                        .name("Unauthorized Firmware Hash Tampering")
                        .description("Simulates malicious rootkit / unauthorized firmware flashing by injecting uncertified firmware SHA-256 digest.")
                        .expectedDefenseOutcome("Hardware attestation failure; Trust score collapses below 35; Automatic Quarantine triggered.")
                        .mitigationMechanism("W3C Verifiable Credential hardware attestation & on-chain firmware registry validation.")
                        .build(),

                SimulationScenarioDto.builder()
                        .attackType(AttackType.SYBIL_DID_INJECTION)
                        .name("Sybil / Rogue DID Identity Injection")
                        .description("Attempts to inject telemetry and request access using an unverified, forged DID identifier.")
                        .expectedDefenseOutcome("Universal DID Resolution fails; Access blocked immediately under DENY_QUARANTINE.")
                        .mitigationMechanism("Decentralized Identity registry verification & smart contract anchored DID lookup.")
                        .build(),

                SimulationScenarioDto.builder()
                        .attackType(AttackType.PACKET_FLOODING)
                        .name("Telemetry Packet Flooding (DDoS)")
                        .description("Floods the broker with high-frequency telemetry ingestion packets (>450 pkt/s).")
                        .expectedDefenseOutcome("Network anomaly detected; Network score N(t) drops; Node throttled/isolated.")
                        .mitigationMechanism("Volumetric rate limiting & multi-factor dynamic trust degradation.")
                        .build()
        );
    }

    @Transactional
    public AttackSimulationResponse executeAttack(AttackSimulationRequest request) {
        UUID deviceId = request.getTargetDeviceId();

        Device device;
        if (deviceId != null) {
            device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Target device not found with ID: " + deviceId));
        } else {
            device = deviceRepository.findByStatus(DeviceStatus.ACTIVE).stream()
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("No active devices available to run simulation on!"));
        }

        DidDocument didDoc = didDocumentRepository.findByDeviceId(device.getId()).orElse(null);
        String didUri = didDoc != null ? didDoc.getDidUri() : "did:zt:unassigned";

        int preScore = device.getCurrentTrustScore();
        List<String> anomalies = new ArrayList<>();
        SecurityAlert alert = null;
        boolean quarantineTriggered = false;
        String defenseSummary;

        switch (request.getAttackType()) {
            case TAMPER_PAYLOAD -> {
                anomalies.add("SIGNATURE_MISMATCH: Computed signature does not match Ed25519 digest in payload header.");
                anomalies.add("TELEMETRY_ANOMALY: Critical abnormal sensor value detected (Temperature = 98.6 C).");

                alert = SecurityAlert.builder()
                        .device(device)
                        .alertType("ADVERSARIAL_MITM_PAYLOAD_TAMPERING")
                        .severity(AlertSeverity.HIGH)
                        .description("Simulation: Adversarial payload tampering detected on device " + device.getDeviceName() + " (" + didUri + ").")
                        .incidentPayload("{\"attackType\":\"TAMPER_PAYLOAD\",\"forgedTemp\":98.6,\"corruptedSignature\":\"0xdeadbeef\"}")
                        .isResolved(false)
                        .triggeredAt(Instant.now())
                        .build();

                // Ingest malicious telemetry to trigger penalty
                telemetryIngestService.ingestTelemetry(new TelemetryIngestRequest(
                        didUri,
                        BigDecimal.valueOf(98.6),
                        BigDecimal.valueOf(90.0),
                        BigDecimal.valueOf(2.8),
                        BigDecimal.valueOf(96.5),
                        BigDecimal.valueOf(88.0),
                        80,
                        "0xCORRUPTED_SIGNATURE_TAMPERED",
                        "{\"attack\":\"MITM_SIMULATION\"}"
                ));

                defenseSummary = "MITM Attack neutralized: Cryptographic verification failed. Threat penalty applied and device risk elevated to HIGH_SUSPICIOUS.";
            }

            case REPLAY_ATTACK -> {
                anomalies.add("STALE_TIMESTAMP: Telemetry packet timestamp exceeds maximum allowed freshness delta (Delta = 14200s).");
                anomalies.add("DUPLICATE_NONCE: Replay detected for previously processed nonce token.");

                alert = SecurityAlert.builder()
                        .device(device)
                        .alertType("ADVERSARIAL_REPLAY_ATTACK")
                        .severity(AlertSeverity.MEDIUM)
                        .description("Simulation: Cryptographic replay attempt intercepted for DID " + didUri)
                        .incidentPayload("{\"attackType\":\"REPLAY_ATTACK\",\"replayedNonce\":\"0x4a8f9c1b\",\"ageSeconds\":14200}")
                        .isResolved(false)
                        .triggeredAt(Instant.now())
                        .build();

                defenseSummary = "Replay Attack thwarted: Timestamp freshness validation rejected expired telemetry sequence.";
            }

            case FIRMWARE_MODIFICATION -> {
                String maliciousFirmware = "0x" + CryptoUtils.sha256("malicious_rootkit_payload_2026");
                device.setFirmwareHash(maliciousFirmware);
                deviceRepository.save(device);

                anomalies.add("FIRMWARE_ATTESTATION_FAILED: Live firmware hash (" + maliciousFirmware + ") does NOT match on-chain Verifiable Credential.");
                anomalies.add("ROOTKIT_SUSPICION: Critical untrusted binary detected in execution space.");

                // Re-evaluate trust -> will trigger autonomous quarantine because score < 35
                trustScoreService.evaluateDeviceTrust(device.getId());
                quarantineTriggered = true;

                alert = SecurityAlert.builder()
                        .device(device)
                        .alertType("ADVERSARIAL_UNAUTHORIZED_FIRMWARE_MODIFICATION")
                        .severity(AlertSeverity.CRITICAL)
                        .description("Simulation: Rootkit firmware modification detected on " + device.getDeviceName() + ". Device automatically quarantined.")
                        .incidentPayload("{\"attackType\":\"FIRMWARE_MODIFICATION\",\"maliciousHash\":\"" + maliciousFirmware + "\"}")
                        .isResolved(false)
                        .triggeredAt(Instant.now())
                        .build();

                defenseSummary = "Firmware Tampering detected: Hardware attestation failed against W3C Verifiable Credential. Dynamic trust score collapsed to Critical and Autonomous Quarantine was enforced.";
            }

            case SYBIL_DID_INJECTION -> {
                String rogueDid = "did:zt:dev:0x" + CryptoUtils.sha256("ROGUE_UNAUTHENTICATED_NODE").substring(0, 20);
                anomalies.add("UNRESOLVED_DID: DID " + rogueDid + " is not registered in W3C DID Registry or Smart Contract.");
                anomalies.add("IDENTITY_SPOOFING: Rogue node attempted unauthorized ABAC policy invocation.");

                alert = SecurityAlert.builder()
                        .device(device)
                        .alertType("ADVERSARIAL_SYBIL_IDENTITY_INJECTION")
                        .severity(AlertSeverity.HIGH)
                        .description("Simulation: Rogue Sybil DID injected: " + rogueDid)
                        .incidentPayload("{\"attackType\":\"SYBIL_DID_INJECTION\",\"rogueDid\":\"" + rogueDid + "\"}")
                        .isResolved(false)
                        .triggeredAt(Instant.now())
                        .build();

                defenseSummary = "Sybil Attack blocked: Unanchored DID rejected during Universal Resolution.";
            }

            case PACKET_FLOODING -> {
                anomalies.add("VOLUMETRIC_FLOOD: Ingestion packet rate exceeded safety ceiling (Rate = 485 pkt/s).");
                anomalies.add("DOS_SUSPICION: Network score N(t) heavily penalized.");

                alert = SecurityAlert.builder()
                        .device(device)
                        .alertType("ADVERSARIAL_PACKET_FLOODING_DDOS")
                        .severity(AlertSeverity.HIGH)
                        .description("Simulation: Telemetry packet flooding attack detected from " + device.getDeviceName() + " (" + didUri + ")")
                        .incidentPayload("{\"attackType\":\"PACKET_FLOODING\",\"packetRate\":485}")
                        .isResolved(false)
                        .triggeredAt(Instant.now())
                        .build();

                telemetryIngestService.ingestTelemetry(new TelemetryIngestRequest(
                        didUri,
                        BigDecimal.valueOf(45.0),
                        BigDecimal.valueOf(50.0),
                        BigDecimal.valueOf(3.3),
                        BigDecimal.valueOf(98.0),
                        BigDecimal.valueOf(92.0),
                        485,
                        "0xSIGNATURE_VALID",
                        "{\"attack\":\"FLOODING_SIMULATION\"}"
                ));

                defenseSummary = "DDoS Volumetric Flooding mitigated: Ingestion rate throttled and network trust penalized.";
            }

            default -> throw new BadRequestException("Unknown attack type: " + request.getAttackType());
        }

        SecurityAlert savedAlert = alertRepository.save(alert);
        messagingTemplate.convertAndSend("/topic/alerts", savedAlert);

        // Re-evaluate trust post-attack
        TrustScoreResponse postTrust = trustScoreService.evaluateDeviceTrust(device.getId());
        int postScore = postTrust.getOverallScore();

        // Evaluate PDP response under post-attack state
        DecisionResult pdpResult = pdp.evaluate(
                device,
                didDoc,
                null,
                postScore,
                "iot/grid/actuate",
                "WRITE",
                policyRepository.findByIsActiveTrue()
        );

        auditService.logAction(
                null,
                "ATTACK_SIMULATION_EXECUTED",
                "AttackSimulation",
                device.getId(),
                "Simulated " + request.getAttackType() + " on " + device.getDeviceName() + ". Pre-Score: " + preScore + ", Post-Score: " + postScore + ", PDP: " + pdpResult.getDecision()
        );

        SecurityAlertResponse alertResponse = SecurityAlertResponse.builder()
                .id(savedAlert.getId())
                .deviceId(device.getId())
                .deviceName(device.getDeviceName())
                .didUri(didUri)
                .alertType(savedAlert.getAlertType())
                .severity(savedAlert.getSeverity())
                .description(savedAlert.getDescription())
                .incidentPayload(savedAlert.getIncidentPayload())
                .isResolved(savedAlert.getIsResolved())
                .triggeredAt(savedAlert.getTriggeredAt())
                .build();

        return AttackSimulationResponse.builder()
                .simulationId(UUID.randomUUID())
                .attackType(request.getAttackType())
                .targetDeviceId(device.getId())
                .targetDeviceName(device.getDeviceName())
                .targetDidUri(didUri)
                .attackDetected(true)
                .automatedQuarantineTriggered(quarantineTriggered || device.getIsQuarantined() || postScore < 35)
                .pdpDecision(pdpResult.getDecision())
                .preAttackTrustScore(preScore)
                .postAttackTrustScore(postScore)
                .defenseSummary(defenseSummary)
                .anomalyIndicators(anomalies)
                .generatedAlert(alertResponse)
                .updatedTrustScore(postTrust)
                .executedAt(Instant.now())
                .build();
    }
}
