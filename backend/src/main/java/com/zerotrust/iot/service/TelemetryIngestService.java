package com.zerotrust.iot.service;

import com.zerotrust.iot.dto.telemetry.TelemetryIngestRequest;
import com.zerotrust.iot.dto.telemetry.TelemetryResponse;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DeviceTelemetry;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.exception.ResourceNotFoundException;
import com.zerotrust.iot.repository.DeviceRepository;
import com.zerotrust.iot.repository.DeviceTelemetryRepository;
import com.zerotrust.iot.repository.DidDocumentRepository;
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
public class TelemetryIngestService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryIngestService.class);

    private final DeviceRepository deviceRepository;
    private final DidDocumentRepository didDocumentRepository;
    private final DeviceTelemetryRepository telemetryRepository;
    private final TrustScoreService trustScoreService;
    private final SimpMessagingTemplate messagingTemplate;

    public TelemetryIngestService(
            DeviceRepository deviceRepository,
            DidDocumentRepository didDocumentRepository,
            DeviceTelemetryRepository telemetryRepository,
            TrustScoreService trustScoreService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.deviceRepository = deviceRepository;
        this.didDocumentRepository = didDocumentRepository;
        this.telemetryRepository = telemetryRepository;
        this.trustScoreService = trustScoreService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public TelemetryResponse ingestTelemetry(TelemetryIngestRequest request) {
        DidDocument didDoc = didDocumentRepository.findByDidUri(request.getDidUri())
                .orElseThrow(() -> new ResourceNotFoundException("Unrecognized DID URI: " + request.getDidUri()));

        Device device = didDoc.getDevice();

        DeviceTelemetry telemetry = DeviceTelemetry.builder()
                .device(device)
                .temperature(request.getTemperature())
                .humidity(request.getHumidity())
                .voltage(request.getVoltage())
                .cpuUtilization(request.getCpuUtilization())
                .memoryUsage(request.getMemoryUsage())
                .packetRate(request.getPacketRate() != null ? request.getPacketRate() : 10)
                .payloadSignature(request.getPayloadSignature())
                .metadata(request.getMetadata())
                .recordedAt(Instant.now())
                .build();

        DeviceTelemetry saved = telemetryRepository.save(telemetry);

        // Update device heartbeat
        device.setLastHeartbeat(Instant.now());
        deviceRepository.save(device);

        // Trigger dynamic Zero Trust evaluation T(t)
        trustScoreService.evaluateDeviceTrust(device.getId());

        TelemetryResponse response = TelemetryResponse.builder()
                .id(saved.getId())
                .didUri(request.getDidUri())
                .temperature(saved.getTemperature())
                .humidity(saved.getHumidity())
                .voltage(saved.getVoltage())
                .cpuUtilization(saved.getCpuUtilization())
                .memoryUsage(saved.getMemoryUsage())
                .packetRate(saved.getPacketRate())
                .payloadSignature(saved.getPayloadSignature())
                .recordedAt(saved.getRecordedAt())
                .build();

        // Broadcast live telemetry feed to frontend dashboard
        messagingTemplate.convertAndSend("/topic/telemetry", response);

        return response;
    }

    @Transactional(readOnly = true)
    public List<TelemetryResponse> getRecentTelemetryByDeviceId(UUID deviceId) {
        DidDocument didDoc = didDocumentRepository.findByDeviceId(deviceId).orElse(null);
        String didUri = didDoc != null ? didDoc.getDidUri() : "did:zt:unassigned";

        return telemetryRepository.findTop50ByDeviceIdOrderByRecordedAtDesc(deviceId).stream()
                .map(t -> TelemetryResponse.builder()
                        .id(t.getId())
                        .didUri(didUri)
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
    }

    @Transactional(readOnly = true)
    public Page<TelemetryResponse> getTelemetryPagedByDeviceId(UUID deviceId, Pageable pageable) {
        DidDocument didDoc = didDocumentRepository.findByDeviceId(deviceId).orElse(null);
        String didUri = didDoc != null ? didDoc.getDidUri() : "did:zt:unassigned";

        return telemetryRepository.findByDeviceIdOrderByRecordedAtDesc(deviceId, pageable)
                .map(t -> TelemetryResponse.builder()
                        .id(t.getId())
                        .didUri(didUri)
                        .temperature(t.getTemperature())
                        .humidity(t.getHumidity())
                        .voltage(t.getVoltage())
                        .cpuUtilization(t.getCpuUtilization())
                        .memoryUsage(t.getMemoryUsage())
                        .packetRate(t.getPacketRate())
                        .payloadSignature(t.getPayloadSignature())
                        .recordedAt(t.getRecordedAt())
                        .build());
    }
}
