package com.zerotrust.iot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "did_documents")
public class DidDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    @Column(name = "did_uri", nullable = false, unique = true, length = 255)
    private String didUri;

    @Column(name = "document_json", nullable = false, columnDefinition = "jsonb")
    private String documentJson;

    @Column(name = "public_key_multibase", nullable = false, length = 255)
    private String publicKeyMultibase;

    @Column(name = "on_chain_tx_hash", length = 66)
    private String onChainTxHash;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;

    @Transient
    private String metadataCid;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DidDocument() {}

    public DidDocument(UUID id, Device device, String didUri, String documentJson, String publicKeyMultibase, String onChainTxHash, Boolean isRevoked, String metadataCid) {
        this.id = id;
        this.device = device;
        this.didUri = didUri;
        this.documentJson = documentJson;
        this.publicKeyMultibase = publicKeyMultibase;
        this.onChainTxHash = onChainTxHash;
        this.isRevoked = isRevoked != null ? isRevoked : false;
        this.metadataCid = metadataCid;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Device device;
        private String didUri;
        private String documentJson;
        private String publicKeyMultibase;
        private String onChainTxHash;
        private Boolean isRevoked = false;
        private String metadataCid;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder device(Device device) { this.device = device; return this; }
        public Builder didUri(String didUri) { this.didUri = didUri; return this; }
        public Builder documentJson(String documentJson) { this.documentJson = documentJson; return this; }
        public Builder publicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; return this; }
        public Builder onChainTxHash(String onChainTxHash) { this.onChainTxHash = onChainTxHash; return this; }
        public Builder isRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; return this; }
        public Builder metadataCid(String metadataCid) { this.metadataCid = metadataCid; return this; }

        public DidDocument build() {
            return new DidDocument(id, device, didUri, documentJson, publicKeyMultibase, onChainTxHash, isRevoked, metadataCid);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

    public String getDocumentJson() { return documentJson; }
    public void setDocumentJson(String documentJson) { this.documentJson = documentJson; }

    public String getPublicKeyMultibase() { return publicKeyMultibase; }
    public void setPublicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; }

    public String getOnChainTxHash() { return onChainTxHash; }
    public void setOnChainTxHash(String onChainTxHash) { this.onChainTxHash = onChainTxHash; }

    public Boolean getIsRevoked() { return isRevoked; }
    public void setIsRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; }

    public String getMetadataCid() { return metadataCid; }
    public void setMetadataCid(String metadataCid) { this.metadataCid = metadataCid; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
