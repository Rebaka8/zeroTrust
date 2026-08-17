package com.zerotrust.iot;

import com.zerotrust.iot.entity.AccessPolicy;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.VerifiableCredential;
import com.zerotrust.iot.entity.enums.ActionAllowed;
import com.zerotrust.iot.entity.enums.DecisionType;
import com.zerotrust.iot.entity.enums.DeviceStatus;
import com.zerotrust.iot.entity.enums.RiskLevel;
import com.zerotrust.iot.pdp.PolicyDecisionPoint;
import com.zerotrust.iot.trust.RiskEvaluationEngine;
import com.zerotrust.iot.trust.TrustCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrustEngineAndPdpTests {

    private final TrustCalculator trustCalculator = new TrustCalculator();
    private final RiskEvaluationEngine riskEngine = new RiskEvaluationEngine(trustCalculator);
    private final PolicyDecisionPoint pdp = new PolicyDecisionPoint();

    @Test
    @DisplayName("Verify Mathematical Trust Formula T(t) with perfect nominal inputs")
    void testTrustCalculatorNominal() {
        // C=100, B=100, F=100, N=100, delta=0s, penalty=0
        int score = trustCalculator.calculateTrustScore(100, 100, 100, 100, 0, 0);
        assertEquals(100, score, "Nominal parameters must yield 100% trust score");
    }

    @Test
    @DisplayName("Verify Mathematical Trust Formula T(t) exponential decay over elapsed time")
    void testTrustCalculatorDecay() {
        // At delta = 600s (10 min), e^(-0.0005 * 600) = e^(-0.3) ≈ 0.7408
        int initialScore = trustCalculator.calculateTrustScore(100, 100, 100, 100, 0, 0);
        int decayedScore = trustCalculator.calculateTrustScore(100, 100, 100, 100, 600, 0);

        assertTrue(decayedScore < initialScore, "Score must decay over inactivity");
        assertTrue(decayedScore >= 70 && decayedScore <= 78, "Decayed score must match exponential curve (approx 74)");
    }

    @Test
    @DisplayName("Verify Mathematical Trust Formula T(t) penalty deductions and clamping")
    void testTrustCalculatorPenalties() {
        int scoreWithPenalty = trustCalculator.calculateTrustScore(100, 100, 100, 100, 0, 45);
        assertEquals(55, scoreWithPenalty, "100 - 45 penalty must equal 55");

        int clampedScore = trustCalculator.calculateTrustScore(50, 50, 50, 50, 0, 120);
        assertEquals(0, clampedScore, "Severe penalty must clamp score at minimum 0");
    }

    @Test
    @DisplayName("Verify Policy Decision Point (PDP) access decisions across ABAC rules")
    void testPdpDecisions() {
        UUID deviceId = UUID.randomUUID();
        String didUri = "did:zt:dev:0x1234567890ABCDEF";

        Device nominalDevice = Device.builder()
                .id(deviceId)
                .deviceName("HVAC Sensor Node")
                .status(DeviceStatus.ACTIVE)
                .isQuarantined(false)
                .currentTrustScore(95)
                .build();

        DidDocument didDoc = DidDocument.builder()
                .didUri(didUri)
                .isRevoked(false)
                .build();

        AccessPolicy highSecurityPolicy = AccessPolicy.builder()
                .policyName("Grid Control Actuation")
                .allowedTopics("iot/grid/actuate")
                .actionAllowed(ActionAllowed.WRITE)
                .minimumTrustScore(80)
                .build();

        // 1. Nominal Device with Score 95 -> PERMIT_FULL
        PolicyDecisionPoint.DecisionResult permitResult = pdp.evaluate(
                nominalDevice,
                didDoc,
                null,
                95,
                "iot/grid/actuate",
                "WRITE",
                List.of(highSecurityPolicy)
        );
        assertTrue(permitResult.isGranted());
        assertEquals(DecisionType.PERMIT_FULL, permitResult.getDecision());

        // 2. Degraded Device with Score 65 -> CHALLENGE_REAUTH (Below 80 threshold of policy)
        PolicyDecisionPoint.DecisionResult challengeResult = pdp.evaluate(
                nominalDevice,
                didDoc,
                null,
                65,
                "iot/grid/actuate",
                "WRITE",
                List.of(highSecurityPolicy)
        );
        assertFalse(challengeResult.isGranted());
        assertEquals(DecisionType.CHALLENGE_REAUTH, challengeResult.getDecision());

        // 3. Quarantined Device -> DENY_QUARANTINE
        Device quarantinedDevice = Device.builder()
                .id(deviceId)
                .deviceName("Compromised Actuator")
                .status(DeviceStatus.QUARANTINED)
                .isQuarantined(true)
                .currentTrustScore(20)
                .build();

        PolicyDecisionPoint.DecisionResult denyResult = pdp.evaluate(
                quarantinedDevice,
                didDoc,
                null,
                20,
                "iot/grid/actuate",
                "WRITE",
                List.of(highSecurityPolicy)
        );
        assertFalse(denyResult.isGranted());
        assertEquals(DecisionType.DENY_QUARANTINE, denyResult.getDecision());
    }
}
