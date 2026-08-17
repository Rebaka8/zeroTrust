package com.zerotrust.iot.simulation;

import com.zerotrust.iot.dto.device.DeviceRegisterRequest;
import com.zerotrust.iot.dto.device.DeviceResponse;
import com.zerotrust.iot.dto.telemetry.TelemetryIngestRequest;
import com.zerotrust.iot.dto.vc.VcIssueRequest;
import com.zerotrust.iot.repository.DeviceRepository;
import com.zerotrust.iot.service.DeviceService;
import com.zerotrust.iot.service.TelemetryIngestService;
import com.zerotrust.iot.service.VcIssuerService;
import com.zerotrust.iot.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class VirtualDeviceGenerator {

    private static final Logger log = LoggerFactory.getLogger(VirtualDeviceGenerator.class);

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final VcIssuerService vcIssuerService;
    private final TelemetryIngestService telemetryIngestService;

    public VirtualDeviceGenerator(
            DeviceRepository deviceRepository,
            DeviceService deviceService,
            VcIssuerService vcIssuerService,
            TelemetryIngestService telemetryIngestService
    ) {
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
        this.vcIssuerService = vcIssuerService;
        this.telemetryIngestService = telemetryIngestService;
    }

    @Transactional
    public List<DeviceResponse> provisionVirtualFleet() {
        List<DeviceResponse> created = new ArrayList<>();

        // Virtual Device Specifications
        List<DeviceRegisterRequest> virtualNodes = List.of(
                new DeviceRegisterRequest(
                        "Smart Grid Substation Gateway 01",
                        "INDUSTRIAL_GATEWAY",
                        "Raspberry Pi CM4 Enterprise",
                        "00:1A:2B:3C:4D:5E",
                        "192.168.10.101",
                        CryptoUtils.sha256("grid_substation_firmware_v2.4.1"),
                        "z6MkmL6N5D3PjK3G4yV2q8SubstationKey",
                        "QmXoypizjW3WknFiJnKLwHCnL72vedxjQkDDP1mXWo6uco"
                ),
                new DeviceRegisterRequest(
                        "HVAC Environmental Sensor 02",
                        "SENSOR_NODE",
                        "ESP32-S3 Dual-Core SoC",
                        "00:1A:2B:3C:4D:6F",
                        "192.168.10.102",
                        CryptoUtils.sha256("hvac_sensor_firmware_v1.8.0"),
                        "z6MkmL6N5D3PjK3G4yV2q8HvacKey",
                        "QmYwAPJzv5CZsnA625s3Xf2nemtYgPpHdWEz79ojWnPbdG"
                ),
                new DeviceRegisterRequest(
                        "Water Treatment Actuator Unit 03",
                        "ACTUATOR",
                        "STM32F4 Industrial Controller",
                        "00:1A:2B:3C:4D:7A",
                        "192.168.10.103",
                        CryptoUtils.sha256("water_actuator_firmware_v3.0.2"),
                        "z6MkmL6N5D3PjK3G4yV2q8WaterKey",
                        "QmZtmD2qt8fJpq3CLDHvdzsAAsnj8ZCsszxLquqKn2P9w6"
                ),
                new DeviceRegisterRequest(
                        "Edge Perimeter Surveillance Node 04",
                        "EDGE_CAMERA",
                        "NVIDIA Jetson Orin Nano",
                        "00:1A:2B:3C:4D:8B",
                        "192.168.10.104",
                        CryptoUtils.sha256("edge_vision_firmware_v4.1.0"),
                        "z6MkmL6N5D3PjK3G4yV2q8JetsonKey",
                        "Qmaisz6NMmDB5C6WshNgkySpxpnNc3SN21XnSStqcwN2EM"
                )
        );

        for (DeviceRegisterRequest node : virtualNodes) {
            if (!deviceRepository.existsByMacAddress(node.getMacAddress())) {
                try {
                    // 1. Register device and generate W3C DID
                    DeviceResponse deviceRes = deviceService.registerDevice(node, null);
                    created.add(deviceRes);

                    // 2. Issue W3C Verifiable Credential
                    vcIssuerService.issueCredential(
                            new VcIssueRequest(
                                    deviceRes.getId(),
                                    "IoTDeviceAttestation",
                                    365,
                                    "TIER_1_CRITICAL",
                                    List.of("telemetry:publish", "heartbeat:report", "actuate:execute")
                            ),
                            null
                    );

                    // 3. Ingest initial nominal telemetry baseline
                    telemetryIngestService.ingestTelemetry(new TelemetryIngestRequest(
                            deviceRes.getDidUri(),
                            BigDecimal.valueOf(24.5),
                            BigDecimal.valueOf(45.0),
                            BigDecimal.valueOf(3.30),
                            BigDecimal.valueOf(14.2),
                            BigDecimal.valueOf(28.0),
                            12,
                            "0xINITIAL_NOMINAL_SIGNATURE",
                            "{\"status\":\"NOMINAL_BASELINE\"}"
                    ));

                    log.info("Successfully provisioned virtual device: {} ({})", deviceRes.getDeviceName(), deviceRes.getDidUri());
                } catch (Exception e) {
                    log.warn("Could not auto-provision virtual device {}: {}", node.getDeviceName(), e.getMessage());
                }
            }
        }

        return created;
    }
}
