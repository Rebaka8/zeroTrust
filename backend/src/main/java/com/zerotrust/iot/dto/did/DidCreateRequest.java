package com.zerotrust.iot.dto.did;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class DidCreateRequest {
    @NotNull(message = "Device ID is required")
    private UUID deviceId;

    @NotBlank(message = "Public Key Multibase is required")
    private String publicKeyMultibase;

    private String keyType; // Ed25519VerificationKey2020 or EcdsaSecp256k1RecoveryMethod2020

    public DidCreateRequest() {}

    public DidCreateRequest(UUID deviceId, String publicKeyMultibase, String keyType) {
        this.deviceId = deviceId;
        this.publicKeyMultibase = publicKeyMultibase;
        this.keyType = keyType;
    }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getPublicKeyMultibase() { return publicKeyMultibase; }
    public void setPublicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; }

    public String getKeyType() { return keyType; }
    public void setKeyType(String keyType) { this.keyType = keyType; }
}
