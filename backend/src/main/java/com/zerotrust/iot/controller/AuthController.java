package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.auth.*;
import com.zerotrust.iot.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & Identity", description = "Endpoints for JWT login, registration, and MetaMask SIWE authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with username/email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new device owner or operator user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @GetMapping("/siwe/nonce")
    @Operation(summary = "Generate a cryptographic SIWE (EIP-4361) challenge nonce for Web3 login")
    public ResponseEntity<SiweNonceResponse> getSiweNonce(@RequestParam String address) {
        return ResponseEntity.ok(authService.generateSiweNonce(address));
    }

    @PostMapping("/siwe/verify")
    @Operation(summary = "Verify MetaMask EIP-191 signature and issue JWT token")
    public ResponseEntity<AuthResponse> verifySiwe(@Valid @RequestBody SiweVerifyRequest request) {
        return ResponseEntity.ok(authService.verifySiwe(request));
    }
}
