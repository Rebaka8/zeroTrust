package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.telemetry.TelemetryIngestRequest;
import com.zerotrust.iot.dto.telemetry.TelemetryResponse;
import com.zerotrust.iot.service.TelemetryIngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "IoT Telemetry Ingestion", description = "Endpoints for ingesting IoT sensor data, environmental telemetry, and real-time streaming")
public class TelemetryController {

    private final TelemetryIngestService telemetryIngestService;

    public TelemetryController(TelemetryIngestService telemetryIngestService) {
        this.telemetryIngestService = telemetryIngestService;
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest IoT telemetry packet and trigger real-time trust evaluation T(t)")
    public ResponseEntity<TelemetryResponse> ingestTelemetry(@Valid @RequestBody TelemetryIngestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(telemetryIngestService.ingestTelemetry(request));
    }

    @GetMapping("/device/{deviceId}/recent")
    @Operation(summary = "Retrieve recent 50 telemetry records for a specific device")
    public ResponseEntity<List<TelemetryResponse>> getRecentTelemetry(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(telemetryIngestService.getRecentTelemetryByDeviceId(deviceId));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Retrieve paginated historical telemetry records for a specific device")
    public ResponseEntity<Page<TelemetryResponse>> getTelemetryPaged(
            @PathVariable UUID deviceId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(telemetryIngestService.getTelemetryPagedByDeviceId(deviceId, pageable));
    }
}
