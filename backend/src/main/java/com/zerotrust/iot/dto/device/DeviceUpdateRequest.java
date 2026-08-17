package com.zerotrust.iot.dto.device;

public class DeviceUpdateRequest {
    private String deviceName;
    private String ipAddress;
    private String firmwareHash;

    public DeviceUpdateRequest() {}

    public DeviceUpdateRequest(String deviceName, String ipAddress, String firmwareHash) {
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.firmwareHash = firmwareHash;
    }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getFirmwareHash() { return firmwareHash; }
    public void setFirmwareHash(String firmwareHash) { this.firmwareHash = firmwareHash; }
}
