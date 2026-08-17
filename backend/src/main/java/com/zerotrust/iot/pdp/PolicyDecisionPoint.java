package com.zerotrust.iot.pdp;

import com.zerotrust.iot.entity.AccessPolicy;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.VerifiableCredential;
import com.zerotrust.iot.entity.enums.ActionAllowed;
import com.zerotrust.iot.entity.enums.DecisionType;
import com.zerotrust.iot.entity.enums.DeviceStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PolicyDecisionPoint {

    public static class DecisionResult {
        private final DecisionType decision;
        private final boolean granted;
        private final String reason;
        private final AccessPolicy matchedPolicy;

        public DecisionResult(DecisionType decision, boolean granted, String reason, AccessPolicy matchedPolicy) {
            this.decision = decision;
            this.granted = granted;
            this.reason = reason;
            this.matchedPolicy = matchedPolicy;
        }

        public DecisionType getDecision() { return decision; }
        public boolean isGranted() { return granted; }
        public String getReason() { return reason; }
        public AccessPolicy getMatchedPolicy() { return matchedPolicy; }
    }

    /**
     * Evaluates Zero Trust PDP Access Decision across 6 ABAC dimensions:
     * 1. DID status (not revoked)
     * 2. Device quarantine & lifecycle state
     * 3. VC Attestation presence & validity
     * 4. Dynamic trust score T(t) threshold against policy requirement
     * 5. Topic / Resource glob matching
     * 6. Action permission matching
     */
    public DecisionResult evaluate(
            Device device,
            DidDocument didDoc,
            VerifiableCredential activeVc,
            int currentTrustScore,
            String requestedResource,
            String requestedAction,
            List<AccessPolicy> activePolicies
    ) {
        // 1. Check DID Revocation
        if (didDoc == null || didDoc.getIsRevoked()) {
            return new DecisionResult(
                    DecisionType.DENY_QUARANTINE,
                    false,
                    "Zero Trust Violation: Device DID does not exist or has been revoked on-chain.",
                    null
            );
        }

        // 2. Check Device Isolation
        if (device.getIsQuarantined() || device.getStatus() == DeviceStatus.QUARANTINED) {
            return new DecisionResult(
                    DecisionType.DENY_QUARANTINE,
                    false,
                    "Access Denied: Device is currently under security QUARANTINE isolation.",
                    null
            );
        }

        if (device.getStatus() == DeviceStatus.DECOMMISSIONED || device.getStatus() == DeviceStatus.SUSPENDED) {
            return new DecisionResult(
                    DecisionType.DENY_QUARANTINE,
                    false,
                    "Access Denied: Device status is " + device.getStatus() + ".",
                    null
            );
        }

        // 3. Find matching active ABAC policy for the resource/topic
        AccessPolicy matchedPolicy = activePolicies.stream()
                .filter(p -> topicMatches(p.getAllowedTopics(), requestedResource))
                .filter(p -> actionMatches(p.getActionAllowed(), requestedAction))
                .findFirst()
                .orElse(null);

        // 4. Evaluate Dynamic Trust Score against Matched Policy or Baseline
        if (currentTrustScore < 35) {
            return new DecisionResult(
                    DecisionType.DENY_QUARANTINE,
                    false,
                    "Critical Security Risk: Trust score (" + currentTrustScore + "/100) has breached the critical threshold (35). Access Denied and Device Flagged.",
                    matchedPolicy
            );
        }

        if (matchedPolicy != null) {
            if (currentTrustScore < matchedPolicy.getMinimumTrustScore()) {
                return new DecisionResult(
                    DecisionType.CHALLENGE_REAUTH,
                    false,
                    "Policy Requirement Not Met: Dynamic trust score (" + currentTrustScore + ") is below required threshold (" + matchedPolicy.getMinimumTrustScore() + ") for policy '" + matchedPolicy.getPolicyName() + "'. Re-attestation challenge required.",
                    matchedPolicy
                );
            }

            // Check if policy requires specific credential type
            if (matchedPolicy.getRequiredCredentialType() != null && !matchedPolicy.getRequiredCredentialType().isBlank()) {
                if (activeVc == null || !matchedPolicy.getRequiredCredentialType().equalsIgnoreCase(activeVc.getCredentialType())) {
                    return new DecisionResult(
                            DecisionType.CHALLENGE_REAUTH,
                            false,
                            "Missing Required Credential: Policy '" + matchedPolicy.getPolicyName() + "' requires active '" + matchedPolicy.getRequiredCredentialType() + "' Verifiable Credential.",
                            matchedPolicy
                    );
                }
            }
        }

        // 5. Categorize Granted Permissions based on Trust Level
        if (currentTrustScore >= 80) {
            return new DecisionResult(
                    DecisionType.PERMIT_FULL,
                    true,
                    "Zero Trust Policy Verification Passed: Trust Score (" + currentTrustScore + "/100) qualifies for Full Operational Access.",
                    matchedPolicy
            );
        } else if (currentTrustScore >= 60) {
            return new DecisionResult(
                    DecisionType.PERMIT_FULL,
                    true,
                    "Zero Trust Policy Verification Passed: Trust Score (" + currentTrustScore + "/100) qualifies for Standard Operational Access.",
                    matchedPolicy
            );
        } else {
            // Trust score between 35 and 59: Restricted access
            return new DecisionResult(
                    DecisionType.PERMIT_RESTRICTED,
                    true,
                    "Restricted Access Granted: Trust Score (" + currentTrustScore + "/100) is in elevated risk zone. Read-only telemetry permitted; actuation disabled.",
                    matchedPolicy
            );
        }
    }

    private boolean topicMatches(String pattern, String topic) {
        if (pattern == null || topic == null) return false;
        if (pattern.equals("*") || pattern.equals("#") || pattern.equals(topic)) return true;
        String regex = pattern.replace("+", "[^/]+").replace("#", ".*");
        return topic.matches(regex);
    }

    private boolean actionMatches(ActionAllowed allowed, String requestedAction) {
        if (allowed == null || requestedAction == null) return true;
        if (allowed == ActionAllowed.ADMIN) return true;
        return allowed.name().equalsIgnoreCase(requestedAction);
    }
}
