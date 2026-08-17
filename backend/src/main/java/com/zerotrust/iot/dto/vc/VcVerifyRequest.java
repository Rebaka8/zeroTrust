package com.zerotrust.iot.dto.vc;

import jakarta.validation.constraints.NotBlank;

public class VcVerifyRequest {
    @NotBlank(message = "Verifiable Credential JSON payload or JWT is required")
    private String vcPayload;

    private String expectedIssuerDid;

    public VcVerifyRequest() {}

    public VcVerifyRequest(String vcPayload, String expectedIssuerDid) {
        this.vcPayload = vcPayload;
        this.expectedIssuerDid = expectedIssuerDid;
    }

    public String getVcPayload() { return vcPayload; }
    public void setVcPayload(String vcPayload) { this.vcPayload = vcPayload; }

    public String getExpectedIssuerDid() { return expectedIssuerDid; }
    public void setExpectedIssuerDid(String expectedIssuerDid) { this.expectedIssuerDid = expectedIssuerDid; }
}
