package com.zerotrust.iot.vc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proof {
    private String type; // e.g. Ed25519Signature2020 or JsonWebSignature2020
    private Instant created;
    private String verificationMethod; // did:zt:issuer:0x...#key-1
    private String proofPurpose; // assertionMethod
    private String jws;
    private String proofValue;

    public Proof() {}

    public Proof(String type, Instant created, String verificationMethod, String proofPurpose, String jws, String proofValue) {
        this.type = type;
        this.created = created != null ? created : Instant.now();
        this.verificationMethod = verificationMethod;
        this.proofPurpose = proofPurpose != null ? proofPurpose : "assertionMethod";
        this.jws = jws;
        this.proofValue = proofValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private Instant created;
        private String verificationMethod;
        private String proofPurpose;
        private String jws;
        private String proofValue;

        public Builder type(String type) { this.type = type; return this; }
        public Builder created(Instant created) { this.created = created; return this; }
        public Builder verificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; return this; }
        public Builder proofPurpose(String proofPurpose) { this.proofPurpose = proofPurpose; return this; }
        public Builder jws(String jws) { this.jws = jws; return this; }
        public Builder proofValue(String proofValue) { this.proofValue = proofValue; return this; }

        public Proof build() {
            return new Proof(type, created, verificationMethod, proofPurpose, jws, proofValue);
        }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getCreated() { return created; }
    public void setCreated(Instant created) { this.created = created; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public String getProofPurpose() { return proofPurpose; }
    public void setProofPurpose(String proofPurpose) { this.proofPurpose = proofPurpose; }

    public String getJws() { return jws; }
    public void setJws(String jws) { this.jws = jws; }

    public String getProofValue() { return proofValue; }
    public void setProofValue(String proofValue) { this.proofValue = proofValue; }
}
