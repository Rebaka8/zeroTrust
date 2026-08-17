package com.zerotrust.iot.config;

import com.zerotrust.iot.entity.AccessPolicy;
import com.zerotrust.iot.entity.Role;
import com.zerotrust.iot.entity.User;
import com.zerotrust.iot.entity.enums.ActionAllowed;
import com.zerotrust.iot.repository.AccessPolicyRepository;
import com.zerotrust.iot.repository.DeviceRepository;
import com.zerotrust.iot.repository.RoleRepository;
import com.zerotrust.iot.repository.UserRepository;
import com.zerotrust.iot.simulation.VirtualDeviceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccessPolicyRepository policyRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final VirtualDeviceGenerator virtualDeviceGenerator;

    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AccessPolicyRepository policyRepository,
            DeviceRepository deviceRepository,
            PasswordEncoder passwordEncoder,
            VirtualDeviceGenerator virtualDeviceGenerator
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.policyRepository = policyRepository;
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
        this.virtualDeviceGenerator = virtualDeviceGenerator;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and bootstrapping Zero Trust system seed records...");

        // 1. Ensure Roles exist
        Role adminRole = getOrCreateRole("ROLE_ADMIN", "Platform Administrator with full governance privileges");
        Role operatorRole = getOrCreateRole("ROLE_OPERATOR", "Security Operations Center (SOC) Operator");
        Role auditorRole = getOrCreateRole("ROLE_AUDITOR", "Read-only compliance and blockchain auditor");
        Role deviceOwnerRole = getOrCreateRole("ROLE_DEVICE_OWNER", "Device fleet owner");

        // 2. Ensure Admin User exists with guaranteed password 'Admin@123456'
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .username("admin")
                    .email("admin@zerotrust-iot.io")
                    .passwordHash(passwordEncoder.encode("Admin@123456"))
                    .walletAddress("0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266")
                    .status("ACTIVE")
                    .roles(new HashSet<>(Set.of(adminRole, operatorRole)))
                    .build();
            userRepository.save(admin);
            log.info("Created default administrator: admin / Admin@123456");
        } else {
            // Update password hash to ensure exact match with Admin@123456
            admin.setPasswordHash(passwordEncoder.encode("Admin@123456"));
            admin.setStatus("ACTIVE");
            if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
                admin.setRoles(new HashSet<>(Set.of(adminRole)));
            }
            userRepository.save(admin);
            log.info("Refreshed administrator credentials: admin / Admin@123456");
        }

        // 3. Ensure Default Access Policies exist
        if (policyRepository.count() == 0) {
            policyRepository.saveAll(List.of(
                    AccessPolicy.builder()
                            .policyName("Telemetry Ingestion Policy")
                            .description("Standard policy allowing nominal sensor data transmission")
                            .minimumTrustScore(60)
                            .requiredCredentialType("IoTDeviceAttestation")
                            .allowedTopics("iot/+/telemetry")
                            .actionAllowed(ActionAllowed.WRITE)
                            .isActive(true)
                            .build(),

                    AccessPolicy.builder()
                            .policyName("Critical Grid Actuation Policy")
                            .description("High-assurance policy requiring strict trust score for industrial actuators")
                            .minimumTrustScore(85)
                            .requiredCredentialType("IoTDeviceAttestation")
                            .allowedTopics("iot/grid/control")
                            .actionAllowed(ActionAllowed.EXECUTE)
                            .isActive(true)
                            .build(),

                    AccessPolicy.builder()
                            .policyName("Firmware Over-The-Air (FOTA) Policy")
                            .description("Restricted policy for firmware update verification and flashing")
                            .minimumTrustScore(90)
                            .requiredCredentialType("IoTDeviceAttestation")
                            .allowedTopics("iot/fota/update")
                            .actionAllowed(ActionAllowed.READ)
                            .isActive(true)
                            .build()
            ));
            log.info("Initialized default Zero Trust ABAC access policies.");
        }

        // 4. Auto-provision virtual fleet if no devices present
        if (deviceRepository.count() == 0) {
            try {
                virtualDeviceGenerator.provisionVirtualFleet();
                log.info("Auto-provisioned initial virtual IoT fleet.");
            } catch (Exception e) {
                log.warn("Deferred virtual fleet provisioning: {}", e.getMessage());
            }
        }
    }

    private Role getOrCreateRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).description(description).build()));
    }
}
