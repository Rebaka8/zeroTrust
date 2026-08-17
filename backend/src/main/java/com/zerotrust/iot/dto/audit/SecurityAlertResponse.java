package com.zerotrust.iot.dto.audit;

import com.zerotrust.iot.entity.enums.AlertSeverity;

import java.time.Instant;
import java.util.UUID;

public class SecurityAlertResponse {
    private UUID id;
    private UUID deviceId;
    private String deviceName;
    private String didUri;
    private String alertType;
    private AlertSeverity severity;
    private String description;
    private String incidentPayload;
    private Boolean isResolved;
    private Instant triggeredAt;

    public SecurityAlertResponse() {}

    public SecurityAlertResponse(UUID id, UUID deviceId, String deviceName, String didUri, String alertType, AlertSeverity severity, String description, String incidentPayload, Boolean isResolved, Instant triggeredAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.didUri = didUri;
        this.alertType = alertType;
        this.severity = severity;
        this.description = description;
        this.incidentPayload = incidentPayload;
        this.isResolved = isResolved;
        this.triggeredAt = triggeredAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID deviceId;
        private String deviceName;
        private String didUri;
        private String alertType;
        private AlertSeverity severity;
        private String description;
        private String incidentPayload;
        private Boolean isResolved = false;
        private Instant triggeredAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder deviceId(UUID deviceId) { this.deviceId = deviceId; return this; }
        public Builder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public Builder didUri(String didUri) { this.didUri = didUri; return this; }
        public Builder alertType(String alertType) { this.alertType = alertType; return this; }
        public Builder severity(AlertSeverity severity) { this.severity = severity; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder incidentPayload(String incidentPayload) { this.incidentPayload = incidentPayload; return this; }
        public Builder isResolved(Boolean isResolved) { this.isResolved = isResolved; return this; }
        public Builder triggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; return this; }

        public SecurityAlertResponse build() {
            return new SecurityAlertResponse(id, deviceId, deviceName, didUri, alertType, severity, description, incidentPayload, isResolved, triggeredAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

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
