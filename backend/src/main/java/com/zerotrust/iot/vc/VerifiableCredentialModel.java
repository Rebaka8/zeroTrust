package com.zerotrust.iot.vc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class VerifiableCredentialModel {

    @JsonProperty("@context")
    private List<String> context = List.of(
            "https://www.w3.org/2018/credentials/v1",
            "https://w3id.org/security/suites/ed25519-2020/v1"
    );

    private String id; // urn:uuid:...
    private List<String> type = List.of("VerifiableCredential", "IoTDeviceAttestation");
    private String issuer; // did:zt:issuer:0x...
    private Instant issuanceDate;
    private Instant expirationDate;
    private CredentialSubject credentialSubject;
    private Proof proof;

    public VerifiableCredentialModel() {}

    public VerifiableCredentialModel(List<String> context, String id, List<String> type, String issuer, Instant issuanceDate, Instant expirationDate, CredentialSubject credentialSubject, Proof proof) {
        this.context = context != null ? context : List.of("https://www.w3.org/2018/credentials/v1");
        this.id = id;
        this.type = type != null ? type : List.of("VerifiableCredential", "IoTDeviceAttestation");
        this.issuer = issuer;
        this.issuanceDate = issuanceDate;
        this.expirationDate = expirationDate;
        this.credentialSubject = credentialSubject;
        this.proof = proof;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> context = List.of("https://www.w3.org/2018/credentials/v1", "https://w3id.org/security/suites/ed25519-2020/v1");
        private String id;
        private List<String> type = List.of("VerifiableCredential", "IoTDeviceAttestation");
        private String issuer;
        private Instant issuanceDate;
        private Instant expirationDate;
        private CredentialSubject credentialSubject;
        private Proof proof;

        public Builder context(List<String> context) { this.context = context; return this; }
        public Builder id(String id) { this.id = id; return this; }
        public Builder type(List<String> type) { this.type = type; return this; }
        public Builder issuer(String issuer) { this.issuer = issuer; return this; }
        public Builder issuanceDate(Instant issuanceDate) { this.issuanceDate = issuanceDate; return this; }
        public Builder expirationDate(Instant expirationDate) { this.expirationDate = expirationDate; return this; }
        public Builder credentialSubject(CredentialSubject credentialSubject) { this.credentialSubject = credentialSubject; return this; }
        public Builder proof(Proof proof) { this.proof = proof; return this; }

        public VerifiableCredentialModel build() {
            return new VerifiableCredentialModel(context, id, type, issuer, issuanceDate, expirationDate, credentialSubject, proof);
        }
    }

    public List<String> getContext() { return context; }
    public void setContext(List<String> context) { this.context = context; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getType() { return type; }
    public void setType(List<String> type) { this.type = type; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public Instant getIssuanceDate() { return issuanceDate; }
    public void setIssuanceDate(Instant issuanceDate) { this.issuanceDate = issuanceDate; }

    public Instant getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Instant expirationDate) { this.expirationDate = expirationDate; }

    public CredentialSubject getCredentialSubject() { return credentialSubject; }
    public void setCredentialSubject(CredentialSubject credentialSubject) { this.credentialSubject = credentialSubject; }

    public Proof getProof() { return proof; }
    public void setProof(Proof proof) { this.proof = proof; }
}
