package com.zerotrust.iot.dto.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DeviceRegisterRequest {
    @NotBlank(message = "Device name is required")
    private String deviceName;

    @NotBlank(message = "Device type is required")
    private String deviceType;

    @NotBlank(message = "Hardware model is required")
    private String hardwareModel;

    @NotBlank(message = "MAC address is required")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "Invalid MAC address format (e.g. 00:1A:2B:3C:4D:5E)")
    private String macAddress;

    private String ipAddress;

    @NotBlank(message = "Firmware hash is required")
    private String firmwareHash;

    @NotBlank(message = "Public key is required")
    private String publicKey;

    private String metadataCid;

    public DeviceRegisterRequest() {}

    public DeviceRegisterRequest(String deviceName, String deviceType, String hardwareModel, String macAddress, String ipAddress, String firmwareHash, String publicKey, String metadataCid) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.hardwareModel = hardwareModel;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.firmwareHash = firmwareHash;
        this.publicKey = publicKey;
        this.metadataCid = metadataCid;
    }

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

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getMetadataCid() { return metadataCid; }
    public void setMetadataCid(String metadataCid) { this.metadataCid = metadataCid; }
}
