package com.zerotrust.iot.service;

import com.zerotrust.iot.dto.audit.AuditLogResponse;
import com.zerotrust.iot.dto.audit.BlockchainTxResponse;
import com.zerotrust.iot.dto.audit.SecurityAlertResponse;
import com.zerotrust.iot.dto.dashboard.DashboardStatsResponse;
import com.zerotrust.iot.dto.device.DeviceResponse;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.SecurityAlert;
import com.zerotrust.iot.entity.enums.AlertSeverity;
import com.zerotrust.iot.entity.enums.DeviceStatus;
import com.zerotrust.iot.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final DidDocumentRepository didDocumentRepository;
    private final SecurityAlertRepository alertRepository;
    private final BlockchainTransactionRepository txRepository;
    private final AuditService auditService;
    private final BlockchainSyncService blockchainSyncService;

    public DashboardService(
            DeviceRepository deviceRepository,
            DidDocumentRepository didDocumentRepository,
            SecurityAlertRepository alertRepository,
            BlockchainTransactionRepository txRepository,
            AuditService auditService,
            BlockchainSyncService blockchainSyncService
    ) {
        this.deviceRepository = deviceRepository;
        this.didDocumentRepository = didDocumentRepository;
        this.alertRepository = alertRepository;
        this.txRepository = txRepository;
        this.auditService = auditService;
        this.blockchainSyncService = blockchainSyncService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalDevices = deviceRepository.count();
        long activeDevices = deviceRepository.countByStatus(DeviceStatus.ACTIVE);
        long quarantinedDevices = deviceRepository.countByStatus(DeviceStatus.QUARANTINED);
        long suspendedDevices = deviceRepository.countByStatus(DeviceStatus.SUSPENDED);

        Double avgScore = deviceRepository.calculateAverageTrustScore();
        double averageTrustScore = avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 100.0;

        long activeAlerts = alertRepository.countByIsResolvedFalse();
        long criticalAlerts = alertRepository.countBySeverityAndIsResolvedFalse(AlertSeverity.CRITICAL);
        long totalTxCount = txRepository.count();

        List<DeviceResponse> topDevices = deviceRepository.findAll().stream()
                .limit(5)
                .map(d -> {
                    String didUri = didDocumentRepository.findByDeviceId(d.getId())
                            .map(DidDocument::getDidUri)
                            .orElse("did:zt:unassigned");
                    return DeviceResponse.builder()
                            .id(d.getId())
                            .didUri(didUri)
                            .deviceName(d.getDeviceName())
                            .deviceType(d.getDeviceType())
                            .hardwareModel(d.getHardwareModel())
                            .macAddress(d.getMacAddress())
                            .ipAddress(d.getIpAddress())
                            .firmwareHash(d.getFirmwareHash())
                            .status(d.getStatus())
                            .publicKey(d.getPublicKey())
                            .isQuarantined(d.getIsQuarantined())
                            .currentTrustScore(d.getCurrentTrustScore())
                            .registeredAt(d.getRegisteredAt())
                            .lastHeartbeat(d.getLastHeartbeat())
                            .build();
                })
                .collect(Collectors.toList());

        List<SecurityAlertResponse> recentAlerts = alertRepository.findTop20ByOrderByTriggeredAtDesc().stream()
                .limit(6)
                .map(this::mapAlertToResponse)
                .collect(Collectors.toList());

        List<AuditLogResponse> recentAuditLogs = auditService.getRecentAuditLogs().stream()
                .limit(6)
                .collect(Collectors.toList());

        List<BlockchainTxResponse> recentTransactions = blockchainSyncService.getRecentTransactions().stream()
                .limit(6)
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalDevices(totalDevices)
                .activeDevices(activeDevices)
                .quarantinedDevices(quarantinedDevices)
                .suspendedDevices(suspendedDevices)
                .averageTrustScore(averageTrustScore)
                .activeAlertsCount(activeAlerts)
                .criticalAlertsCount(criticalAlerts)
                .totalOnChainTxCount(totalTxCount)
                .topDevices(topDevices)
                .recentAlerts(recentAlerts)
                .recentAuditLogs(recentAuditLogs)
                .recentTransactions(recentTransactions)
                .build();
    }

    private SecurityAlertResponse mapAlertToResponse(SecurityAlert alert) {
        String didUri = "did:zt:system";
        String deviceName = "System";

        if (alert.getDevice() != null) {
            deviceName = alert.getDevice().getDeviceName();
            didUri = didDocumentRepository.findByDeviceId(alert.getDevice().getId())
                    .map(DidDocument::getDidUri)
                    .orElse("did:zt:unknown");
        }

        return SecurityAlertResponse.builder()
                .id(alert.getId())
                .deviceId(alert.getDevice() != null ? alert.getDevice().getId() : null)
                .deviceName(deviceName)
                .didUri(didUri)
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .description(alert.getDescription())
                .incidentPayload(alert.getIncidentPayload())
                .isResolved(alert.getIsResolved())
                .triggeredAt(alert.getTriggeredAt())
                .build();
    }
}
