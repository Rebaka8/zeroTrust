package com.zerotrust.iot.dto.did;

import java.time.Instant;
import java.util.UUID;

public class DidResponse {
    private UUID id;
    private UUID deviceId;
    private String deviceName;
    private String didUri;
    private String publicKeyMultibase;
    private String metadataCid;
    private String onChainTxHash;
    private Boolean isRevoked;
    private String documentJson;
    private Instant createdAt;
    private Instant updatedAt;

    public DidResponse() {}

    public DidResponse(UUID id, UUID deviceId, String deviceName, String didUri, String publicKeyMultibase, String metadataCid, String onChainTxHash, Boolean isRevoked, String documentJson, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.didUri = didUri;
        this.publicKeyMultibase = publicKeyMultibase;
        this.metadataCid = metadataCid;
        this.onChainTxHash = onChainTxHash;
        this.isRevoked = isRevoked;
        this.documentJson = documentJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID deviceId;
        private String deviceName;
        private String didUri;
        private String publicKeyMultibase;
        private String metadataCid;
        private String onChainTxHash;
        private Boolean isRevoked;
        private String documentJson;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder deviceId(UUID deviceId) { this.deviceId = deviceId; return this; }
        public Builder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public Builder didUri(String didUri) { this.didUri = didUri; return this; }
        public Builder publicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; return this; }
        public Builder metadataCid(String metadataCid) { this.metadataCid = metadataCid; return this; }
        public Builder onChainTxHash(String onChainTxHash) { this.onChainTxHash = onChainTxHash; return this; }
        public Builder isRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; return this; }
        public Builder documentJson(String documentJson) { this.documentJson = documentJson; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public DidResponse build() {
            return new DidResponse(id, deviceId, deviceName, didUri, publicKeyMultibase, metadataCid, onChainTxHash, isRevoked, documentJson, createdAt, updatedAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

    public String getPublicKeyMultibase() { return publicKeyMultibase; }
    public void setPublicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; }

    public String getMetadataCid() { return metadataCid; }
    public void setMetadataCid(String metadataCid) { this.metadataCid = metadataCid; }

    public String getOnChainTxHash() { return onChainTxHash; }
    public void setOnChainTxHash(String onChainTxHash) { this.onChainTxHash = onChainTxHash; }

    public Boolean getIsRevoked() { return isRevoked; }
    public void setIsRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; }

    public String getDocumentJson() { return documentJson; }
    public void setDocumentJson(String documentJson) { this.documentJson = documentJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
