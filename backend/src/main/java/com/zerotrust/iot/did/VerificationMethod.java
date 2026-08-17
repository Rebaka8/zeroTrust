package com.zerotrust.iot.did;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationMethod {
    private String id;
    private String type; // e.g. Ed25519VerificationKey2020 or EcdsaSecp256k1RecoveryMethod2020
    private String controller;
    private String publicKeyMultibase;
    private String publicKeyHex;

    public VerificationMethod() {}

    public VerificationMethod(String id, String type, String controller, String publicKeyMultibase, String publicKeyHex) {
        this.id = id;
        this.type = type;
        this.controller = controller;
        this.publicKeyMultibase = publicKeyMultibase;
        this.publicKeyHex = publicKeyHex;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String type;
        private String controller;
        private String publicKeyMultibase;
        private String publicKeyHex;

        public Builder id(String id) { this.id = id; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder controller(String controller) { this.controller = controller; return this; }
        public Builder publicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; return this; }
        public Builder publicKeyHex(String publicKeyHex) { this.publicKeyHex = publicKeyHex; return this; }

        public VerificationMethod build() {
            return new VerificationMethod(id, type, controller, publicKeyMultibase, publicKeyHex);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getController() { return controller; }
    public void setController(String controller) { this.controller = controller; }

    public String getPublicKeyMultibase() { return publicKeyMultibase; }
    public void setPublicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; }

    public String getPublicKeyHex() { return publicKeyHex; }
    public void setPublicKeyHex(String publicKeyHex) { this.publicKeyHex = publicKeyHex; }
}
