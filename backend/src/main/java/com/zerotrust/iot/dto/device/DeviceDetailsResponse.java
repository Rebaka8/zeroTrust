package com.zerotrust.iot.dto.device;

import com.zerotrust.iot.dto.telemetry.TelemetryResponse;
import com.zerotrust.iot.entity.enums.DeviceStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DeviceDetailsResponse {
    private UUID id;
    private String didUri;
    private String deviceName;
    private String deviceType;
    private String hardwareModel;
    private String macAddress;
    private String ipAddress;
    private String firmwareHash;
    private DeviceStatus status;
    private String publicKey;
    private Boolean isQuarantined;
    private Integer currentTrustScore;
    private Instant registeredAt;
    private Instant lastHeartbeat;
    private String didDocumentJson;
    private String onChainTxHash;
    private List<TelemetryResponse> recentTelemetry;

    public DeviceDetailsResponse() {}

    public DeviceDetailsResponse(UUID id, String didUri, String deviceName, String deviceType, String hardwareModel, String macAddress, String ipAddress, String firmwareHash, DeviceStatus status, String publicKey, Boolean isQuarantined, Integer currentTrustScore, Instant registeredAt, Instant lastHeartbeat, String didDocumentJson, String onChainTxHash, List<TelemetryResponse> recentTelemetry) {
        this.id = id;
        this.didUri = didUri;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.hardwareModel = hardwareModel;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.firmwareHash = firmwareHash;
        this.status = status;
        this.publicKey = publicKey;
        this.isQuarantined = isQuarantined;
        this.currentTrustScore = currentTrustScore;
        this.registeredAt = registeredAt;
        this.lastHeartbeat = lastHeartbeat;
        this.didDocumentJson = didDocumentJson;
        this.onChainTxHash = onChainTxHash;
        this.recentTelemetry = recentTelemetry;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String didUri;
        private String deviceName;
        private String deviceType;
        private String hardwareModel;
        private String macAddress;
        private String ipAddress;
        private String firmwareHash;
        private DeviceStatus status;
        private String publicKey;
        private Boolean isQuarantined;
        private Integer currentTrustScore;
        private Instant registeredAt;
        private Instant lastHeartbeat;
        private String didDocumentJson;
        private String onChainTxHash;
        private List<TelemetryResponse> recentTelemetry;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder didUri(String didUri) { this.didUri = didUri; return this; }
        public Builder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public Builder deviceType(String deviceType) { this.deviceType = deviceType; return this; }
        public Builder hardwareModel(String hardwareModel) { this.hardwareModel = hardwareModel; return this; }
        public Builder macAddress(String macAddress) { this.macAddress = macAddress; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder firmwareHash(String firmwareHash) { this.firmwareHash = firmwareHash; return this; }
        public Builder status(DeviceStatus status) { this.status = status; return this; }
        public Builder publicKey(String publicKey) { this.publicKey = publicKey; return this; }
        public Builder isQuarantined(Boolean isQuarantined) { this.isQuarantined = isQuarantined; return this; }
        public Builder currentTrustScore(Integer currentTrustScore) { this.currentTrustScore = currentTrustScore; return this; }
        public Builder registeredAt(Instant registeredAt) { this.registeredAt = registeredAt; return this; }
        public Builder lastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; return this; }
        public Builder didDocumentJson(String didDocumentJson) { this.didDocumentJson = didDocumentJson; return this; }
        public Builder onChainTxHash(String onChainTxHash) { this.onChainTxHash = onChainTxHash; return this; }
        public Builder recentTelemetry(List<TelemetryResponse> recentTelemetry) { this.recentTelemetry = recentTelemetry; return this; }

        public DeviceDetailsResponse build() {
            return new DeviceDetailsResponse(id, didUri, deviceName, deviceType, hardwareModel, macAddress, ipAddress, firmwareHash, status, publicKey, isQuarantined, currentTrustScore, registeredAt, lastHeartbeat, didDocumentJson, onChainTxHash, recentTelemetry);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getHardwareModel() { return hardwareModel; }
    public void setHardwareModel(String hardwareModel) { this.hardwareModel = hardwareModel; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getFirmwareHash() { return firmwareHash; }
    public void setFirmwareHash(String firmwareHash) { this.firmwareHash = firmwareHash; }

    public DeviceStatus getStatus() { return status; }
    public void setStatus(DeviceStatus status) { this.status = status; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public Boolean getIsQuarantined() { return isQuarantined; }
    public void setIsQuarantined(Boolean isQuarantined) { this.isQuarantined = isQuarantined; }

    public Integer getCurrentTrustScore() { return currentTrustScore; }
    public void setCurrentTrustScore(Integer currentTrustScore) { this.currentTrustScore = currentTrustScore; }

    public Instant getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }

    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public String getDidDocumentJson() { return didDocumentJson; }
    public void setDidDocumentJson(String didDocumentJson) { this.didDocumentJson = didDocumentJson; }

    public String getOnChainTxHash() { return onChainTxHash; }
    public void setOnChainTxHash(String onChainTxHash) { this.onChainTxHash = onChainTxHash; }

    public List<TelemetryResponse> getRecentTelemetry() { return recentTelemetry; }
    public void setRecentTelemetry(List<TelemetryResponse> recentTelemetry) { this.recentTelemetry = recentTelemetry; }
}
