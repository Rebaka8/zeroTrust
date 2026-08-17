package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.trust.TrustBreakdownResponse;
import com.zerotrust.iot.dto.trust.TrustScoreResponse;
import com.zerotrust.iot.service.TrustScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trust")
@Tag(name = "Zero Trust Risk & Scoring Engine", description = "Endpoints for dynamic trust score formulation T(t), risk analytics, and mathematical breakdown")
public class TrustScoreController {

    private final TrustScoreService trustScoreService;

    public TrustScoreController(TrustScoreService trustScoreService) {
        this.trustScoreService = trustScoreService;
    }

    @PostMapping("/evaluate/{deviceId}")
    @Operation(summary = "Trigger dynamic multi-factor trust evaluation T(t) for an IoT device")
    public ResponseEntity<TrustScoreResponse> evaluateDeviceTrust(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(trustScoreService.evaluateDeviceTrust(deviceId));
    }

    @GetMapping("/{deviceId}/breakdown")
    @Operation(summary = "Retrieve mathematical trust formula variables (weights, decay lambda, sub-scores, penalties)")
    public ResponseEntity<TrustBreakdownResponse> getTrustBreakdown(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(trustScoreService.getTrustBreakdown(deviceId));
    }

    @GetMapping("/{deviceId}/history")
    @Operation(summary = "Retrieve paginated time-series audit trail of trust score fluctuations")
    public ResponseEntity<Page<TrustScoreResponse>> getTrustHistory(
            @PathVariable UUID deviceId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(trustScoreService.getTrustHistoryPaged(deviceId, pageable));
    }
}
