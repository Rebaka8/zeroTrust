package com.zerotrust.iot.vc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialSubject {
    private String id; // Subject DID: did:zt:dev:0x...
    private String deviceName;
    private String deviceType;
    private String hardwareModel;
    private String macAddress;
    private String firmwareHash;
    private String trustTier; // e.g. "TIER_1_CRITICAL", "TIER_2_STANDARD"
    private List<String> allowedCapabilities;

    public CredentialSubject() {}

    public CredentialSubject(String id, String deviceName, String deviceType, String hardwareModel, String macAddress, String firmwareHash, String trustTier, List<String> allowedCapabilities) {
        this.id = id;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.hardwareModel = hardwareModel;
        this.macAddress = macAddress;
        this.firmwareHash = firmwareHash;
        this.trustTier = trustTier;
        this.allowedCapabilities = allowedCapabilities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String deviceName;
        private String deviceType;
        private String hardwareModel;
        private String macAddress;
        private String firmwareHash;
        private String trustTier;
        private List<String> allowedCapabilities;

        public Builder id(String id) { this.id = id; return this; }
        public Builder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public Builder deviceType(String deviceType) { this.deviceType = deviceType; return this; }
        public Builder hardwareModel(String hardwareModel) { this.hardwareModel = hardwareModel; return this; }
        public Builder macAddress(String macAddress) { this.macAddress = macAddress; return this; }
        public Builder firmwareHash(String firmwareHash) { this.firmwareHash = firmwareHash; return this; }
        public Builder trustTier(String trustTier) { this.trustTier = trustTier; return this; }
        public Builder allowedCapabilities(List<String> allowedCapabilities) { this.allowedCapabilities = allowedCapabilities; return this; }

        public CredentialSubject build() {
            return new CredentialSubject(id, deviceName, deviceType, hardwareModel, macAddress, firmwareHash, trustTier, allowedCapabilities);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getHardwareModel() { return hardwareModel; }
    public void setHardwareModel(String hardwareModel) { this.hardwareModel = hardwareModel; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getFirmwareHash() { return firmwareHash; }
    public void setFirmwareHash(String firmwareHash) { this.firmwareHash = firmwareHash; }

    public String getTrustTier() { return trustTier; }
    public void setTrustTier(String trustTier) { this.trustTier = trustTier; }

    public List<String> getAllowedCapabilities() { return allowedCapabilities; }
    public void setAllowedCapabilities(List<String> allowedCapabilities) { this.allowedCapabilities = allowedCapabilities; }
}
