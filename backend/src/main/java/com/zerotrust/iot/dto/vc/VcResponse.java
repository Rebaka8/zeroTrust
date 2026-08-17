package com.zerotrust.iot.dto.vc;

import java.time.Instant;
import java.util.UUID;

public class VcResponse {
    private UUID id;
    private UUID deviceId;
    private String deviceName;
    private String credentialType;
    private String issuerDid;
    private String subjectDid;
    private String rawVcJwt;
    private String claimsJson;
    private Instant issuanceDate;
    private Instant expirationDate;
    private Boolean isRevoked;
    private Instant createdAt;

    public VcResponse() {}

    public VcResponse(UUID id, UUID deviceId, String deviceName, String credentialType, String issuerDid, String subjectDid, String rawVcJwt, String claimsJson, Instant issuanceDate, Instant expirationDate, Boolean isRevoked, Instant createdAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.credentialType = credentialType;
        this.issuerDid = issuerDid;
        this.subjectDid = subjectDid;
        this.rawVcJwt = rawVcJwt;
        this.claimsJson = claimsJson;
        this.issuanceDate = issuanceDate;
        this.expirationDate = expirationDate;
        this.isRevoked = isRevoked;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID deviceId;
        private String deviceName;
        private String credentialType;
        private String issuerDid;
        private String subjectDid;
        private String rawVcJwt;
        private String claimsJson;
        private Instant issuanceDate;
        private Instant expirationDate;
        private Boolean isRevoked;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder deviceId(UUID deviceId) { this.deviceId = deviceId; return this; }
        public Builder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public Builder credentialType(String credentialType) { this.credentialType = credentialType; return this; }
        public Builder issuerDid(String issuerDid) { this.issuerDid = issuerDid; return this; }
        public Builder subjectDid(String subjectDid) { this.subjectDid = subjectDid; return this; }
        public Builder rawVcJwt(String rawVcJwt) { this.rawVcJwt = rawVcJwt; return this; }
        public Builder claimsJson(String claimsJson) { this.claimsJson = claimsJson; return this; }
        public Builder issuanceDate(Instant issuanceDate) { this.issuanceDate = issuanceDate; return this; }
        public Builder expirationDate(Instant expirationDate) { this.expirationDate = expirationDate; return this; }
        public Builder isRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public VcResponse build() {
            return new VcResponse(id, deviceId, deviceName, credentialType, issuerDid, subjectDid, rawVcJwt, claimsJson, issuanceDate, expirationDate, isRevoked, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }

    public String getIssuerDid() { return issuerDid; }
    public void setIssuerDid(String issuerDid) { this.issuerDid = issuerDid; }

    public String getSubjectDid() { return subjectDid; }
    public void setSubjectDid(String subjectDid) { this.subjectDid = subjectDid; }

    public String getRawVcJwt() { return rawVcJwt; }
    public void setRawVcJwt(String rawVcJwt) { this.rawVcJwt = rawVcJwt; }

    public String getClaimsJson() { return claimsJson; }
    public void setClaimsJson(String claimsJson) { this.claimsJson = claimsJson; }

    public Instant getIssuanceDate() { return issuanceDate; }
    public void setIssuanceDate(Instant issuanceDate) { this.issuanceDate = issuanceDate; }

    public Instant getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Instant expirationDate) { this.expirationDate = expirationDate; }

    public Boolean getIsRevoked() { return isRevoked; }
    public void setIsRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
