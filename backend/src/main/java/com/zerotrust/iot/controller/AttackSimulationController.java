package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.device.DeviceResponse;
import com.zerotrust.iot.dto.simulation.AttackSimulationRequest;
import com.zerotrust.iot.dto.simulation.AttackSimulationResponse;
import com.zerotrust.iot.dto.simulation.SimulationScenarioDto;
import com.zerotrust.iot.simulation.AttackSimulationService;
import com.zerotrust.iot.simulation.VirtualDeviceGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulation")
@Tag(name = "Adversarial Attack Simulation Engine", description = "Endpoints for simulating MITM, Replay, Firmware Tampering, Sybil DIDs, and DDoS attacks to demonstrate Zero Trust defenses")
public class AttackSimulationController {

    private final AttackSimulationService attackSimulationService;
    private final VirtualDeviceGenerator virtualDeviceGenerator;

    public AttackSimulationController(
            AttackSimulationService attackSimulationService,
            VirtualDeviceGenerator virtualDeviceGenerator
    ) {
        this.attackSimulationService = attackSimulationService;
        this.virtualDeviceGenerator = virtualDeviceGenerator;
    }

    @GetMapping("/scenarios")
    @Operation(summary = "List all available adversarial attack scenarios with descriptions and expected defense mechanisms")
    public ResponseEntity<List<SimulationScenarioDto>> getAvailableScenarios() {
        return ResponseEntity.ok(attackSimulationService.getAvailableScenarios());
    }

    @PostMapping("/attack")
    @Operation(summary = "Trigger a simulated cyberattack on an IoT node and observe real-time Zero Trust autonomous defense response")
    public ResponseEntity<AttackSimulationResponse> executeAttack(@Valid @RequestBody AttackSimulationRequest request) {
        return ResponseEntity.ok(attackSimulationService.executeAttack(request));
    }

    @PostMapping("/provision-virtual-devices")
    @Operation(summary = "Provision a fleet of 4 realistic virtual IoT nodes (Smart Grid Gateway, HVAC Sensor, Water Actuator, Edge Camera) with registered DIDs and VCs")
    public ResponseEntity<List<DeviceResponse>> provisionVirtualFleet() {
        return ResponseEntity.ok(virtualDeviceGenerator.provisionVirtualFleet());
    }
}
