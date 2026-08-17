package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.device.DeviceDetailsResponse;
import com.zerotrust.iot.dto.device.DeviceRegisterRequest;
import com.zerotrust.iot.dto.device.DeviceResponse;
import com.zerotrust.iot.security.UserPrincipal;
import com.zerotrust.iot.service.DeviceService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "IoT Devices Lifecycle", description = "Endpoints for managing IoT devices, DIDs, hardware attestation, and quarantine states")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @Operation(summary = "Register and provision a new IoT device with W3C DID document")
    public ResponseEntity<DeviceResponse> registerDevice(
            @Valid @RequestBody DeviceRegisterRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.registerDevice(request, currentUser));
    }

    @GetMapping
    @Operation(summary = "Retrieve paginated list of registered IoT devices")
    public ResponseEntity<Page<DeviceResponse>> getAllDevices(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(deviceService.getAllDevices(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Retrieve list of all active non-quarantined IoT devices")
    public ResponseEntity<List<DeviceResponse>> getActiveDevices() {
        return ResponseEntity.ok(deviceService.getAllActiveDevices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve detailed device record, W3C DID document, and recent telemetry")
    public ResponseEntity<DeviceDetailsResponse> getDeviceById(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @PostMapping("/{id}/quarantine")
    @Operation(summary = "Manually isolate / quarantine an IoT device due to detected anomaly")
    public ResponseEntity<DeviceResponse> quarantineDevice(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Manual Operator Intervention") String reason,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(deviceService.quarantineDevice(id, reason, currentUser));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore an isolated device back to operational ACTIVE status")
    public ResponseEntity<DeviceResponse> restoreDevice(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Manual Verification and Attestation Re-run") String reason,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(deviceService.restoreDevice(id, reason, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Decommission an IoT device permanently")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        deviceService.deleteDevice(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
