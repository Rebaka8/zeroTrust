package com.zerotrust.iot.dto.vc;

import java.time.Instant;
import java.util.List;

public class VcVerifyResponse {
    private boolean valid;
    private String subjectDid;
    private String issuerDid;
    private String credentialType;
    private String trustTier;
    private List<String> capabilities;
    private boolean isExpired;
    private boolean isRevoked;
    private boolean signatureValid;
    private boolean firmwareIntegrityMatched;
    private String verificationSummary;
    private Instant checkedAt;

    public VcVerifyResponse() {}

    public VcVerifyResponse(boolean valid, String subjectDid, String issuerDid, String credentialType, String trustTier, List<String> capabilities, boolean isExpired, boolean isRevoked, boolean signatureValid, boolean firmwareIntegrityMatched, String verificationSummary, Instant checkedAt) {
        this.valid = valid;
        this.subjectDid = subjectDid;
        this.issuerDid = issuerDid;
        this.credentialType = credentialType;
        this.trustTier = trustTier;
        this.capabilities = capabilities;
        this.isExpired = isExpired;
        this.isRevoked = isRevoked;
        this.signatureValid = signatureValid;
        this.firmwareIntegrityMatched = firmwareIntegrityMatched;
        this.verificationSummary = verificationSummary;
        this.checkedAt = checkedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean valid;
        private String subjectDid;
        private String issuerDid;
        private String credentialType;
        private String trustTier;
        private List<String> capabilities;
        private boolean isExpired;
        private boolean isRevoked;
        private boolean signatureValid;
        private boolean firmwareIntegrityMatched;
        private String verificationSummary;
        private Instant checkedAt;

        public Builder valid(boolean valid) { this.valid = valid; return this; }
        public Builder subjectDid(String subjectDid) { this.subjectDid = subjectDid; return this; }
        public Builder issuerDid(String issuerDid) { this.issuerDid = issuerDid; return this; }
        public Builder credentialType(String credentialType) { this.credentialType = credentialType; return this; }
        public Builder trustTier(String trustTier) { this.trustTier = trustTier; return this; }
        public Builder capabilities(List<String> capabilities) { this.capabilities = capabilities; return this; }
        public Builder isExpired(boolean isExpired) { this.isExpired = isExpired; return this; }
        public Builder isRevoked(boolean isRevoked) { this.isRevoked = isRevoked; return this; }
        public Builder signatureValid(boolean signatureValid) { this.signatureValid = signatureValid; return this; }
        public Builder firmwareIntegrityMatched(boolean firmwareIntegrityMatched) { this.firmwareIntegrityMatched = firmwareIntegrityMatched; return this; }
        public Builder verificationSummary(String verificationSummary) { this.verificationSummary = verificationSummary; return this; }
        public Builder checkedAt(Instant checkedAt) { this.checkedAt = checkedAt; return this; }

        public VcVerifyResponse build() {
            return new VcVerifyResponse(valid, subjectDid, issuerDid, credentialType, trustTier, capabilities, isExpired, isRevoked, signatureValid, firmwareIntegrityMatched, verificationSummary, checkedAt);
        }
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getSubjectDid() { return subjectDid; }
    public void setSubjectDid(String subjectDid) { this.subjectDid = subjectDid; }

    public String getIssuerDid() { return issuerDid; }
    public void setIssuerDid(String issuerDid) { this.issuerDid = issuerDid; }

    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }

    public String getTrustTier() { return trustTier; }
    public void setTrustTier(String trustTier) { this.trustTier = trustTier; }

    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }

    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean isExpired) { this.isExpired = isExpired; }

    public boolean isRevoked() { return isRevoked; }
    public void setRevoked(boolean isRevoked) { this.isRevoked = isRevoked; }

    public boolean isSignatureValid() { return signatureValid; }
    public void setSignatureValid(boolean signatureValid) { this.signatureValid = signatureValid; }

    public boolean isFirmwareIntegrityMatched() { return firmwareIntegrityMatched; }
    public void setFirmwareIntegrityMatched(boolean firmwareIntegrityMatched) { this.firmwareIntegrityMatched = firmwareIntegrityMatched; }

    public String getVerificationSummary() { return verificationSummary; }
    public void setVerificationSummary(String verificationSummary) { this.verificationSummary = verificationSummary; }

    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
