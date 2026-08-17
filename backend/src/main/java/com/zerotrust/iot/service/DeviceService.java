package com.zerotrust.iot.service;

import com.zerotrust.iot.dto.device.DeviceDetailsResponse;
import com.zerotrust.iot.dto.device.DeviceRegisterRequest;
import com.zerotrust.iot.dto.device.DeviceResponse;
import com.zerotrust.iot.dto.telemetry.TelemetryResponse;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.SecurityAlert;
import com.zerotrust.iot.entity.TrustScore;
import com.zerotrust.iot.entity.User;
import com.zerotrust.iot.entity.enums.AlertSeverity;
import com.zerotrust.iot.entity.enums.DeviceStatus;
import com.zerotrust.iot.entity.enums.RiskLevel;
import com.zerotrust.iot.exception.BadRequestException;
import com.zerotrust.iot.exception.ResourceNotFoundException;
import com.zerotrust.iot.repository.*;
import com.zerotrust.iot.security.UserPrincipal;
import com.zerotrust.iot.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final DidDocumentRepository didDocumentRepository;
    private final TrustScoreRepository trustScoreRepository;
    private final DeviceTelemetryRepository telemetryRepository;
    private final SecurityAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    public DeviceService(
            DeviceRepository deviceRepository,
            DidDocumentRepository didDocumentRepository,
            TrustScoreRepository trustScoreRepository,
            DeviceTelemetryRepository telemetryRepository,
            SecurityAlertRepository alertRepository,
            UserRepository userRepository,
            AuditService auditService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.deviceRepository = deviceRepository;
        this.didDocumentRepository = didDocumentRepository;
        this.trustScoreRepository = trustScoreRepository;
        this.telemetryRepository = telemetryRepository;
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public DeviceResponse registerDevice(DeviceRegisterRequest request, UserPrincipal currentUser) {
        if (deviceRepository.existsByMacAddress(request.getMacAddress())) {
            throw new BadRequestException("Device with MAC Address " + request.getMacAddress() + " is already registered!");
        }

        User owner = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;

        Device device = Device.builder()
                .owner(owner)
                .deviceName(request.getDeviceName())
                .deviceType(request.getDeviceType())
                .hardwareModel(request.getHardwareModel())
                .macAddress(request.getMacAddress())
                .ipAddress(request.getIpAddress())
                .firmwareHash(request.getFirmwareHash())
                .status(DeviceStatus.ACTIVE)
                .publicKey(request.getPublicKey())
                .isQuarantined(false)
                .currentTrustScore(100)
                .registeredAt(Instant.now())
                .lastHeartbeat(Instant.now())
                .build();

        Device savedDevice = deviceRepository.save(device);

        String didSuffix = CryptoUtils.sha256(savedDevice.getId().toString() + savedDevice.getMacAddress()).substring(0, 20).toUpperCase();
        String didUri = "did:zt:dev:0x" + didSuffix;

        String didDocJson = String.format(
                "{\"@context\":[\"https://www.w3.org/ns/did/v1\"],\"id\":\"%s\",\"verificationMethod\":[{\"id\":\"%s#key-1\",\"type\":\"Ed25519VerificationKey2020\",\"controller\":\"%s\",\"publicKeyMultibase\":\"%s\"}]}",
                didUri, didUri, didUri, request.getPublicKey()
        );

        DidDocument didDoc = DidDocument.builder()
                .device(savedDevice)
                .didUri(didUri)
                .documentJson(didDocJson)
                .publicKeyMultibase(request.getPublicKey())
                .isRevoked(false)
                .build();

        didDocumentRepository.save(didDoc);

        TrustScore initialTrust = TrustScore.builder()
                .device(savedDevice)
                .overallScore(100)
                .cryptoIdentityScore(100)
                .behavioralScore(100)
                .firmwareScore(100)
                .networkScore(100)
                .penaltyScore(0)
                .riskLevel(RiskLevel.LOW_NOMINAL)
                .evaluationReasons("Initial on-boarding attestation passed; device registered successfully.")
                .evaluatedAt(Instant.now())
                .build();

        trustScoreRepository.save(initialTrust);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "DEVICE_REGISTERED",
                "Device",
                savedDevice.getId(),
                "Device registered with DID: " + didUri + " (" + savedDevice.getDeviceName() + ")"
        );

        DeviceResponse response = mapToResponse(savedDevice, didUri);
        messagingTemplate.convertAndSend("/topic/devices", response);

        return response;
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable).map(device -> {
            String didUri = didDocumentRepository.findByDeviceId(device.getId())
                    .map(DidDocument::getDidUri)
                    .orElse("did:zt:unassigned");
            return mapToResponse(device, didUri);
        });
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getAllActiveDevices() {
        return deviceRepository.findByStatus(DeviceStatus.ACTIVE).stream().map(device -> {
            String didUri = didDocumentRepository.findByDeviceId(device.getId())
                    .map(DidDocument::getDidUri)
                    .orElse("did:zt:unassigned");
            return mapToResponse(device, didUri);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeviceDetailsResponse getDeviceById(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        DidDocument didDoc = didDocumentRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("DID Document not found for device: " + deviceId));

        List<TelemetryResponse> telemetry = telemetryRepository.findTop50ByDeviceIdOrderByRecordedAtDesc(deviceId).stream()
                .map(t -> TelemetryResponse.builder()
                        .id(t.getId())
                        .didUri(didDoc.getDidUri())
                        .temperature(t.getTemperature())
                        .humidity(t.getHumidity())
                        .voltage(t.getVoltage())
                        .cpuUtilization(t.getCpuUtilization())
                        .memoryUsage(t.getMemoryUsage())
                        .packetRate(t.getPacketRate())
                        .payloadSignature(t.getPayloadSignature())
                        .recordedAt(t.getRecordedAt())
                        .build())
                .collect(Collectors.toList());

        return DeviceDetailsResponse.builder()
                .id(device.getId())
                .didUri(didDoc.getDidUri())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .hardwareModel(device.getHardwareModel())
                .macAddress(device.getMacAddress())
                .ipAddress(device.getIpAddress())
                .firmwareHash(device.getFirmwareHash())
                .status(device.getStatus())
                .publicKey(device.getPublicKey())
                .isQuarantined(device.getIsQuarantined())
                .currentTrustScore(device.getCurrentTrustScore())
                .registeredAt(device.getRegisteredAt())
                .lastHeartbeat(device.getLastHeartbeat())
                .didDocumentJson(didDoc.getDocumentJson())
                .onChainTxHash(didDoc.getOnChainTxHash())
                .recentTelemetry(telemetry)
                .build();
    }

    @Transactional
    public DeviceResponse quarantineDevice(UUID deviceId, String reason, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        device.setIsQuarantined(true);
        device.setStatus(DeviceStatus.QUARANTINED);
        device.setCurrentTrustScore(Math.min(device.getCurrentTrustScore(), 20));
        Device updated = deviceRepository.save(device);

        DidDocument didDoc = didDocumentRepository.findByDeviceId(deviceId).orElse(null);
        String didUri = didDoc != null ? didDoc.getDidUri() : "did:zt:unknown";

        SecurityAlert alert = SecurityAlert.builder()
                .device(device)
                .alertType("MANUAL_QUARANTINE_OVERRIDE")
                .severity(AlertSeverity.HIGH)
                .description("Device quarantined by operator: " + reason)
                .isResolved(false)
                .triggeredAt(Instant.now())
                .build();
        alertRepository.save(alert);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "DEVICE_QUARANTINED",
                "Device",
                deviceId,
                "Device isolated. Reason: " + reason
        );

        DeviceResponse response = mapToResponse(updated, didUri);
        messagingTemplate.convertAndSend("/topic/alerts", alert);
        messagingTemplate.convertAndSend("/topic/devices", response);

        return response;
    }

    @Transactional
    public DeviceResponse restoreDevice(UUID deviceId, String reason, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        device.setIsQuarantined(false);
        device.setStatus(DeviceStatus.ACTIVE);
        device.setCurrentTrustScore(90);
        Device updated = deviceRepository.save(device);

        DidDocument didDoc = didDocumentRepository.findByDeviceId(deviceId).orElse(null);
        String didUri = didDoc != null ? didDoc.getDidUri() : "did:zt:unknown";

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "DEVICE_RESTORED",
                "Device",
                deviceId,
                "Device restored from quarantine. Reason: " + reason
        );

        DeviceResponse response = mapToResponse(updated, didUri);
        messagingTemplate.convertAndSend("/topic/devices", response);

        return response;
    }

    @Transactional
    public void deleteDevice(UUID deviceId, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        device.setStatus(DeviceStatus.DECOMMISSIONED);
        deviceRepository.save(device);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "DEVICE_DECOMMISSIONED",
                "Device",
                deviceId,
                "Device permanently decommissioned: " + device.getDeviceName()
        );
    }

    private DeviceResponse mapToResponse(Device device, String didUri) {
        return DeviceResponse.builder()
                .id(device.getId())
                .didUri(didUri)
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .hardwareModel(device.getHardwareModel())
                .macAddress(device.getMacAddress())
                .ipAddress(device.getIpAddress())
                .firmwareHash(device.getFirmwareHash())
                .status(device.getStatus())
                .publicKey(device.getPublicKey())
                .isQuarantined(device.getIsQuarantined())
                .currentTrustScore(device.getCurrentTrustScore())
                .registeredAt(device.getRegisteredAt())
                .lastHeartbeat(device.getLastHeartbeat())
                .build();
    }
}
