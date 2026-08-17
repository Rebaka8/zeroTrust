package com.zerotrust.iot.did;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class W3CDidDocument {

    @JsonProperty("@context")
    private List<String> context = List.of("https://www.w3.org/ns/did/v1", "https://w3id.org/security/suites/ed25519-2020/v1");

    private String id;
    private List<VerificationMethod> verificationMethod;
    private List<String> authentication;
    private List<String> assertionMethod;
    private List<String> capabilityInvocation;
    private String metadataCid;
    private boolean deactivated = false;

    public W3CDidDocument() {}

    public W3CDidDocument(List<String> context, String id, List<VerificationMethod> verificationMethod, List<String> authentication, List<String> assertionMethod, List<String> capabilityInvocation, String metadataCid, boolean deactivated) {
        this.context = context != null ? context : List.of("https://www.w3.org/ns/did/v1");
        this.id = id;
        this.verificationMethod = verificationMethod;
        this.authentication = authentication;
        this.assertionMethod = assertionMethod;
        this.capabilityInvocation = capabilityInvocation;
        this.metadataCid = metadataCid;
        this.deactivated = deactivated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> context = List.of("https://www.w3.org/ns/did/v1", "https://w3id.org/security/suites/ed25519-2020/v1");
        private String id;
        private List<VerificationMethod> verificationMethod;
        private List<String> authentication;
        private List<String> assertionMethod;
        private List<String> capabilityInvocation;
        private String metadataCid;
        private boolean deactivated = false;

        public Builder context(List<String> context) { this.context = context; return this; }
        public Builder id(String id) { this.id = id; return this; }
        public Builder verificationMethod(List<VerificationMethod> verificationMethod) { this.verificationMethod = verificationMethod; return this; }
        public Builder authentication(List<String> authentication) { this.authentication = authentication; return this; }
        public Builder assertionMethod(List<String> assertionMethod) { this.assertionMethod = assertionMethod; return this; }
        public Builder capabilityInvocation(List<String> capabilityInvocation) { this.capabilityInvocation = capabilityInvocation; return this; }
        public Builder metadataCid(String metadataCid) { this.metadataCid = metadataCid; return this; }
        public Builder deactivated(boolean deactivated) { this.deactivated = deactivated; return this; }

        public W3CDidDocument build() {
            return new W3CDidDocument(context, id, verificationMethod, authentication, assertionMethod, capabilityInvocation, metadataCid, deactivated);
        }
    }

    public List<String> getContext() { return context; }
    public void setContext(List<String> context) { this.context = context; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<VerificationMethod> getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(List<VerificationMethod> verificationMethod) { this.verificationMethod = verificationMethod; }

    public List<String> getAuthentication() { return authentication; }
    public void setAuthentication(List<String> authentication) { this.authentication = authentication; }

    public List<String> getAssertionMethod() { return assertionMethod; }
    public void setAssertionMethod(List<String> assertionMethod) { this.assertionMethod = assertionMethod; }

    public List<String> getCapabilityInvocation() { return capabilityInvocation; }
    public void setCapabilityInvocation(List<String> capabilityInvocation) { this.capabilityInvocation = capabilityInvocation; }

    public String getMetadataCid() { return metadataCid; }
    public void setMetadataCid(String metadataCid) { this.metadataCid = metadataCid; }

    public boolean isDeactivated() { return deactivated; }
    public void setDeactivated(boolean deactivated) { this.deactivated = deactivated; }
}
