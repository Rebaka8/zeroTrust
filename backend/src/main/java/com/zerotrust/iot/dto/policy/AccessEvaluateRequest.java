package com.zerotrust.iot.dto.policy;

import jakarta.validation.constraints.NotBlank;

public class AccessEvaluateRequest {
    @NotBlank(message = "DID URI is required")
    private String didUri;

    @NotBlank(message = "Resource / Topic is required")
    private String resource;

    @NotBlank(message = "Action is required")
    private String action;

    public AccessEvaluateRequest() {}

    public AccessEvaluateRequest(String didUri, String resource, String action) {
        this.didUri = didUri;
        this.resource = resource;
        this.action = action;
    }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
