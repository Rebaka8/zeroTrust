package com.zerotrust.iot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verifiable_credentials")
public class VerifiableCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "credential_type", nullable = false, length = 100)
    private String credentialType;

    @Column(name = "issuer_did", nullable = false, length = 255)
    private String issuerDid;

    @Column(name = "subject_did", nullable = false, length = 255)
    private String subjectDid;

    @Column(name = "raw_vc_jwt", nullable = false, columnDefinition = "text")
    private String rawVcJwt;

    @Column(name = "claims_json", nullable = false, columnDefinition = "jsonb")
    private String claimsJson;

    @Column(name = "issuance_date", nullable = false)
    private Instant issuanceDate;

    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public VerifiableCredential() {}

    public VerifiableCredential(UUID id, Device device, String credentialType, String issuerDid, String subjectDid, String rawVcJwt, String claimsJson, Instant issuanceDate, Instant expirationDate, Boolean isRevoked) {
        this.id = id;
        this.device = device;
        this.credentialType = credentialType;
        this.issuerDid = issuerDid;
        this.subjectDid = subjectDid;
        this.rawVcJwt = rawVcJwt;
        this.claimsJson = claimsJson;
        this.issuanceDate = issuanceDate;
        this.expirationDate = expirationDate;
        this.isRevoked = isRevoked != null ? isRevoked : false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Device device;
        private String credentialType;
        private String issuerDid;
        private String subjectDid;
        private String rawVcJwt;
        private String claimsJson;
        private Instant issuanceDate;
        private Instant expirationDate;
        private Boolean isRevoked = false;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder device(Device device) { this.device = device; return this; }
        public Builder credentialType(String credentialType) { this.credentialType = credentialType; return this; }
        public Builder issuerDid(String issuerDid) { this.issuerDid = issuerDid; return this; }
        public Builder subjectDid(String subjectDid) { this.subjectDid = subjectDid; return this; }
        public Builder rawVcJwt(String rawVcJwt) { this.rawVcJwt = rawVcJwt; return this; }
        public Builder claimsJson(String claimsJson) { this.claimsJson = claimsJson; return this; }
        public Builder issuanceDate(Instant issuanceDate) { this.issuanceDate = issuanceDate; return this; }
        public Builder expirationDate(Instant expirationDate) { this.expirationDate = expirationDate; return this; }
        public Builder isRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; return this; }

        public VerifiableCredential build() {
            return new VerifiableCredential(id, device, credentialType, issuerDid, subjectDid, rawVcJwt, claimsJson, issuanceDate, expirationDate, isRevoked);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

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
