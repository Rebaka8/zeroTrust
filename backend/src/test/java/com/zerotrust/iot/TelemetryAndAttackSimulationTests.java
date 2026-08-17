package com.zerotrust.iot;

import com.zerotrust.iot.dto.simulation.AttackType;
import com.zerotrust.iot.dto.simulation.SimulationScenarioDto;
import com.zerotrust.iot.simulation.AttackSimulationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryAndAttackSimulationTests {

    @Test
    @DisplayName("Verify that all 5 Adversarial Cyberattack Scenarios are defined with mitigation profiles")
    void testAvailableSimulationScenarios() {
        AttackSimulationService service = new AttackSimulationService(
                null, null, null, null, null, null, null, null, null
        );

        List<SimulationScenarioDto> scenarios = service.getAvailableScenarios();
        assertNotNull(scenarios);
        assertEquals(5, scenarios.size(), "All 5 attack vectors must be available in simulation catalog");

        assertTrue(scenarios.stream().anyMatch(s -> s.getAttackType() == AttackType.TAMPER_PAYLOAD));
        assertTrue(scenarios.stream().anyMatch(s -> s.getAttackType() == AttackType.REPLAY_ATTACK));
        assertTrue(scenarios.stream().anyMatch(s -> s.getAttackType() == AttackType.FIRMWARE_MODIFICATION));
        assertTrue(scenarios.stream().anyMatch(s -> s.getAttackType() == AttackType.SYBIL_DID_INJECTION));
        assertTrue(scenarios.stream().anyMatch(s -> s.getAttackType() == AttackType.PACKET_FLOODING));

        for (SimulationScenarioDto scenario : scenarios) {
            assertNotNull(scenario.getName(), "Scenario must have a title");
            assertNotNull(scenario.getDescription(), "Scenario must describe attack vector");
            assertNotNull(scenario.getExpectedDefenseOutcome(), "Scenario must describe expected defense response");
            assertNotNull(scenario.getMitigationMechanism(), "Scenario must state the mitigation mechanism");
        }
    }
}
