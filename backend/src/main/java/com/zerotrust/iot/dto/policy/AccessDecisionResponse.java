package com.zerotrust.iot.dto.policy;

import com.zerotrust.iot.entity.enums.DecisionType;

import java.time.Instant;
import java.util.UUID;

public class AccessDecisionResponse {
    private UUID requestId;
    private String didUri;
    private String resource;
    private String action;
    private Integer currentTrustScore;
    private DecisionType decision;
    private boolean granted;
    private String reason;
    private String onChainAuditTx;
    private Instant evaluatedAt;

    public AccessDecisionResponse() {}

    public AccessDecisionResponse(UUID requestId, String didUri, String resource, String action, Integer currentTrustScore, DecisionType decision, boolean granted, String reason, String onChainAuditTx, Instant evaluatedAt) {
        this.requestId = requestId;
        this.didUri = didUri;
        this.resource = resource;
        this.action = action;
        this.currentTrustScore = currentTrustScore;
        this.decision = decision;
        this.granted = granted;
        this.reason = reason;
        this.onChainAuditTx = onChainAuditTx;
        this.evaluatedAt = evaluatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID requestId;
        private String didUri;
        private String resource;
        private String action;
        private Integer currentTrustScore;
        private DecisionType decision;
        private boolean granted;
        private String reason;
        private String onChainAuditTx;
        private Instant evaluatedAt;

        public Builder requestId(UUID requestId) { this.requestId = requestId; return this; }
        public Builder didUri(String didUri) { this.didUri = didUri; return this; }
        public Builder resource(String resource) { this.resource = resource; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder currentTrustScore(Integer currentTrustScore) { this.currentTrustScore = currentTrustScore; return this; }
        public Builder decision(DecisionType decision) { this.decision = decision; return this; }
        public Builder granted(boolean granted) { this.granted = granted; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder onChainAuditTx(String onChainAuditTx) { this.onChainAuditTx = onChainAuditTx; return this; }
        public Builder evaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; return this; }

        public AccessDecisionResponse build() {
            return new AccessDecisionResponse(requestId, didUri, resource, action, currentTrustScore, decision, granted, reason, onChainAuditTx, evaluatedAt);
        }
    }

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Integer getCurrentTrustScore() { return currentTrustScore; }
    public void setCurrentTrustScore(Integer currentTrustScore) { this.currentTrustScore = currentTrustScore; }

    public DecisionType getDecision() { return decision; }
    public void setDecision(DecisionType decision) { this.decision = decision; }

    public boolean isGranted() { return granted; }
    public void setGranted(boolean granted) { this.granted = granted; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getOnChainAuditTx() { return onChainAuditTx; }
    public void setOnChainAuditTx(String onChainAuditTx) { this.onChainAuditTx = onChainAuditTx; }

    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
