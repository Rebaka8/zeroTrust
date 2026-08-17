package com.zerotrust.iot.entity;

import com.zerotrust.iot.entity.enums.DecisionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_requests")
public class AccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private AccessPolicy policy;

    @Column(name = "requested_resource", nullable = false, length = 255)
    private String requestedResource;

    @Column(name = "requested_action", nullable = false, length = 50)
    private String requestedAction;

    @Column(name = "trust_score_at_request", nullable = false)
    private Integer trustScoreAtRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private DecisionType decision;

    @Column(columnDefinition = "text")
    private String reason;

    @Column(name = "on_chain_audit_tx", length = 66)
    private String onChainAuditTx;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    public AccessRequest() {}

    public AccessRequest(UUID id, Device device, AccessPolicy policy, String requestedResource, String requestedAction, Integer trustScoreAtRequest, DecisionType decision, String reason, String onChainAuditTx, Instant requestedAt) {
        this.id = id;
        this.device = device;
        this.policy = policy;
        this.requestedResource = requestedResource;
        this.requestedAction = requestedAction;
        this.trustScoreAtRequest = trustScoreAtRequest;
        this.decision = decision;
        this.reason = reason;
        this.onChainAuditTx = onChainAuditTx;
        this.requestedAt = requestedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Device device;
        private AccessPolicy policy;
        private String requestedResource;
        private String requestedAction;
        private Integer trustScoreAtRequest;
        private DecisionType decision;
        private String reason;
        private String onChainAuditTx;
        private Instant requestedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder device(Device device) { this.device = device; return this; }
        public Builder policy(AccessPolicy policy) { this.policy = policy; return this; }
        public Builder requestedResource(String requestedResource) { this.requestedResource = requestedResource; return this; }
        public Builder requestedAction(String requestedAction) { this.requestedAction = requestedAction; return this; }
        public Builder trustScoreAtRequest(Integer trustScoreAtRequest) { this.trustScoreAtRequest = trustScoreAtRequest; return this; }
        public Builder decision(DecisionType decision) { this.decision = decision; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder onChainAuditTx(String onChainAuditTx) { this.onChainAuditTx = onChainAuditTx; return this; }
        public Builder requestedAt(Instant requestedAt) { this.requestedAt = requestedAt; return this; }

        public AccessRequest build() {
            return new AccessRequest(id, device, policy, requestedResource, requestedAction, trustScoreAtRequest, decision, reason, onChainAuditTx, requestedAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public AccessPolicy getPolicy() { return policy; }
    public void setPolicy(AccessPolicy policy) { this.policy = policy; }

    public String getRequestedResource() { return requestedResource; }
    public void setRequestedResource(String requestedResource) { this.requestedResource = requestedResource; }

    public String getRequestedAction() { return requestedAction; }
    public void setRequestedAction(String requestedAction) { this.requestedAction = requestedAction; }

    public Integer getTrustScoreAtRequest() { return trustScoreAtRequest; }
    public void setTrustScoreAtRequest(Integer trustScoreAtRequest) { this.trustScoreAtRequest = trustScoreAtRequest; }

    public DecisionType getDecision() { return decision; }
    public void setDecision(DecisionType decision) { this.decision = decision; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getOnChainAuditTx() { return onChainAuditTx; }
    public void setOnChainAuditTx(String onChainAuditTx) { this.onChainAuditTx = onChainAuditTx; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
}
