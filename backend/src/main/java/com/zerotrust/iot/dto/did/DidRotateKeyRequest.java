package com.zerotrust.iot.dto.did;

import jakarta.validation.constraints.NotBlank;

public class DidRotateKeyRequest {
    @NotBlank(message = "New Public Key is required")
    private String newPublicKeyMultibase;

    private String signatureOfOldKey;

    public DidRotateKeyRequest() {}

    public DidRotateKeyRequest(String newPublicKeyMultibase, String signatureOfOldKey) {
        this.newPublicKeyMultibase = newPublicKeyMultibase;
        this.signatureOfOldKey = signatureOfOldKey;
    }

    public String getNewPublicKeyMultibase() { return newPublicKeyMultibase; }
    public void setNewPublicKeyMultibase(String newPublicKeyMultibase) { this.newPublicKeyMultibase = newPublicKeyMultibase; }

    public String getSignatureOfOldKey() { return signatureOfOldKey; }
    public void setSignatureOfOldKey(String signatureOfOldKey) { this.signatureOfOldKey = signatureOfOldKey; }
}
