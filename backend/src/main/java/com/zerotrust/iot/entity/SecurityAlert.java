package com.zerotrust.iot.entity;

import com.zerotrust.iot.entity.enums.AlertSeverity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "incident_payload", columnDefinition = "jsonb")
    private String incidentPayload;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @CreationTimestamp
    @Column(name = "triggered_at", nullable = false, updatable = false)
    private Instant triggeredAt;

    public SecurityAlert() {}

    public SecurityAlert(UUID id, Device device, String alertType, AlertSeverity severity, String description, String incidentPayload, Boolean isResolved, Instant triggeredAt) {
        this.id = id;
        this.device = device;
        this.alertType = alertType;
        this.severity = severity;
        this.description = description;
        this.incidentPayload = incidentPayload;
        this.isResolved = isResolved != null ? isResolved : false;
        this.triggeredAt = triggeredAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Device device;
        private String alertType;
        private AlertSeverity severity;
        private String description;
        private String incidentPayload;
        private Boolean isResolved = false;
        private Instant triggeredAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder device(Device device) { this.device = device; return this; }
        public Builder alertType(String alertType) { this.alertType = alertType; return this; }
        public Builder severity(AlertSeverity severity) { this.severity = severity; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder incidentPayload(String incidentPayload) { this.incidentPayload = incidentPayload; return this; }
        public Builder isResolved(Boolean isResolved) { this.isResolved = isResolved; return this; }
        public Builder triggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; return this; }

        public SecurityAlert build() {
            return new SecurityAlert(id, device, alertType, severity, description, incidentPayload, isResolved, triggeredAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIncidentPayload() { return incidentPayload; }
    public void setIncidentPayload(String incidentPayload) { this.incidentPayload = incidentPayload; }

    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }

    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }
}
