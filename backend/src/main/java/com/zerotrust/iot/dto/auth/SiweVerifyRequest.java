package com.zerotrust.iot.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class SiweVerifyRequest {
    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Signature is required")
    private String signature;

    @NotBlank(message = "Wallet address is required")
    private String address;

    public SiweVerifyRequest() {}

    public SiweVerifyRequest(String message, String signature, String address) {
        this.message = message;
        this.signature = signature;
        this.address = address;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
