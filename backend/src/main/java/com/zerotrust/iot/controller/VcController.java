package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.vc.VcIssueRequest;
import com.zerotrust.iot.dto.vc.VcResponse;
import com.zerotrust.iot.dto.vc.VcVerifyRequest;
import com.zerotrust.iot.dto.vc.VcVerifyResponse;
import com.zerotrust.iot.security.UserPrincipal;
import com.zerotrust.iot.service.VcIssuerService;
import com.zerotrust.iot.service.VcVerifierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vc")
@Tag(name = "W3C Verifiable Credentials (VCs)", description = "Endpoints for issuing, verifying, resolving, and revoking cryptographically signed Verifiable Credentials")
public class VcController {

    private final VcIssuerService vcIssuerService;
    private final VcVerifierService vcVerifierService;

    public VcController(VcIssuerService vcIssuerService, VcVerifierService vcVerifierService) {
        this.vcIssuerService = vcIssuerService;
        this.vcVerifierService = vcVerifierService;
    }

    @PostMapping("/issue")
    @Operation(summary = "Issue a tamper-evident W3C Verifiable Credential for an attested IoT device")
    public ResponseEntity<VcResponse> issueCredential(
            @Valid @RequestBody VcIssueRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vcIssuerService.issueCredential(request, currentUser));
    }

    @PostMapping("/verify")
    @Operation(summary = "Cryptographically verify a W3C Verifiable Credential, lifetime, and firmware attestation")
    public ResponseEntity<VcVerifyResponse> verifyCredential(@Valid @RequestBody VcVerifyRequest request) {
        return ResponseEntity.ok(vcVerifierService.verifyCredential(request));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Retrieve all active and historic Verifiable Credentials for a device")
    public ResponseEntity<List<VcResponse>> getDeviceCredentials(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(vcIssuerService.getCredentialsByDeviceId(deviceId));
    }

    @PostMapping("/{credentialId}/revoke")
    @Operation(summary = "Revoke a specific Verifiable Credential")
    public ResponseEntity<VcResponse> revokeCredential(
            @PathVariable UUID credentialId,
            @RequestParam(defaultValue = "Credential invalidated or re-attested") String reason,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(vcIssuerService.revokeCredential(credentialId, reason, currentUser));
    }
}
