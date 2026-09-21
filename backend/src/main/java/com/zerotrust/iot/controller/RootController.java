package com.zerotrust.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "API Information", description = "Base root endpoint and system discovery")
public class RootController {

    @GetMapping({"/", "/api/v1"})
    @Operation(summary = "Get API service status, version, and documentation links")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "Zero Trust IoT Security Framework Backend");
        response.put("version", "1.0.0");
        response.put("status", "OPERATIONAL");
        response.put("timestamp", Instant.now().toString());

        Map<String, String> links = new LinkedHashMap<>();
        links.put("swaggerDocumentation", "/swagger-ui/index.html");
        links.put("openApiJson", "/v3/api-docs");
        links.put("healthCheck", "/actuator/health");
        links.put("authLogin", "/api/v1/auth/login");
        links.put("siweNonce", "/api/v1/auth/siwe/nonce?address=0x...");
        links.put("devices", "/api/v1/devices");
        links.put("dashboardStats", "/api/v1/dashboard/stats");
        links.put("auditLogs", "/api/v1/audit/logs");
        response.put("endpoints", links);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/ping")
    @Operation(summary = "Real-time health ping and latency measurement endpoint")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
