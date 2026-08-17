package com.zerotrust.iot.controller;

import com.zerotrust.iot.did.DidResolver;
import com.zerotrust.iot.did.W3CDidDocument;
import com.zerotrust.iot.dto.did.DidCreateRequest;
import com.zerotrust.iot.dto.did.DidResponse;
import com.zerotrust.iot.dto.did.DidRotateKeyRequest;
import com.zerotrust.iot.security.UserPrincipal;
import com.zerotrust.iot.service.DidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/did")
@Tag(name = "W3C Decentralized Identifiers (DIDs)", description = "Endpoints for DID registration, universal resolution, cryptographic key rotation, and revocation")
public class DidController {

    private final DidService didService;
    private final DidResolver didResolver;

    public DidController(DidService didService, DidResolver didResolver) {
        this.didService = didService;
        this.didResolver = didResolver;
    }

    @PostMapping
    @Operation(summary = "Register and create a new W3C Decentralized Identifier (DID)")
    public ResponseEntity<DidResponse> createDid(
            @Valid @RequestBody DidCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(didService.createDid(request, currentUser));
    }

    @GetMapping("/resolve/{didUri}")
    @Operation(summary = "Universal W3C DID Resolution: Resolves a DID URI to its JSON-LD compliant DID Document")
    public ResponseEntity<W3CDidDocument> resolveDid(@PathVariable String didUri) {
        return ResponseEntity.ok(didResolver.resolve(didUri));
    }

    @GetMapping("/{didUri}")
    @Operation(summary = "Retrieve registered DID metadata, IPFS CID, and status")
    public ResponseEntity<DidResponse> getDidDetails(@PathVariable String didUri) {
        return ResponseEntity.ok(didService.getDidByUri(didUri));
    }

    @GetMapping
    @Operation(summary = "Retrieve paginated list of all registered DIDs")
    public ResponseEntity<Page<DidResponse>> getAllDids(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(didService.getAllDids(pageable));
    }

    @PostMapping("/{didUri}/rotate-key")
    @Operation(summary = "Rotate cryptographic public key for an active DID")
    public ResponseEntity<DidResponse> rotateKey(
            @PathVariable String didUri,
            @Valid @RequestBody DidRotateKeyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(didService.rotateKey(didUri, request, currentUser));
    }

    @PostMapping("/{didUri}/revoke")
    @Operation(summary = "Revoke an active DID permanently")
    public ResponseEntity<DidResponse> revokeDid(
            @PathVariable String didUri,
            @RequestParam(defaultValue = "Security compromise or decommission") String reason,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(didService.revokeDid(didUri, reason, currentUser));
    }
}
