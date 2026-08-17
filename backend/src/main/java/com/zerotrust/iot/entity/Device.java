package com.zerotrust.iot.entity;

import com.zerotrust.iot.entity.enums.DeviceStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType;

    @Column(name = "hardware_model", nullable = false, length = 100)
    private String hardwareModel;

    @Column(name = "mac_address", nullable = false, unique = true, length = 17)
    private String macAddress;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "firmware_hash", nullable = false, length = 64)
    private String firmwareHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status = DeviceStatus.PROVISIONED;

    @Column(name = "public_key", nullable = false, length = 255)
    private String publicKey;

    @Column(name = "is_quarantined", nullable = false)
    private Boolean isQuarantined = false;

    @Column(name = "current_trust_score", nullable = false)
    private Integer currentTrustScore = 100;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Device() {}

    public Device(UUID id, User owner, String deviceName, String deviceType, String hardwareModel, String macAddress, String ipAddress, String firmwareHash, DeviceStatus status, String publicKey, Boolean isQuarantined, Integer currentTrustScore, Instant registeredAt, Instant lastHeartbeat) {
        this.id = id;
        this.owner = owner;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.hardwareModel = hardwareModel;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.firmwareHash = firmwareHash;
        this.status = status != null ? status : DeviceStatus.PROVISIONED;
        this.publicKey = publicKey;
        this.isQuarantined = isQuarantined != null ? isQuarantined : false;
        this.currentTrustScore = currentTrustScore != null ? currentTrustScore : 100;
        this.registeredAt = registeredAt;
        this.lastHeartbeat = lastHeartbeat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User owner;
        private String deviceName;
        private String deviceType;
        private String hardwareModel;
        private String macAddress;
        private String ipAddress;
        private String firmwareHash;
        private DeviceStatus status = DeviceStatus.PROVISIONED;
        private String publicKey;
        private Boolean isQuarantined = false;
        private Integer currentTrustScore = 100;
        private Instant registeredAt;
        private Instant lastHeartbeat;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder owner(User owner) { this.owner = owner; return this; }
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

        public Device build() {
            return new Device(id, owner, deviceName, deviceType, hardwareModel, macAddress, ipAddress, firmwareHash, status, publicKey, isQuarantined, currentTrustScore, registeredAt, lastHeartbeat);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

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

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
