package com.zerotrust.iot.dto.dashboard;

import com.zerotrust.iot.dto.audit.AuditLogResponse;
import com.zerotrust.iot.dto.audit.BlockchainTxResponse;
import com.zerotrust.iot.dto.audit.SecurityAlertResponse;
import com.zerotrust.iot.dto.device.DeviceResponse;

import java.util.List;

public class DashboardStatsResponse {
    private long totalDevices;
    private long activeDevices;
    private long quarantinedDevices;
    private long suspendedDevices;
    private double averageTrustScore;
    private double averageDecisionLatencyMs;
    private long activeAlertsCount;
    private long criticalAlertsCount;
    private long totalOnChainTxCount;
    private List<DeviceResponse> topDevices;
    private List<SecurityAlertResponse> recentAlerts;
    private List<AuditLogResponse> recentAuditLogs;
    private List<BlockchainTxResponse> recentTransactions;

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(long totalDevices, long activeDevices, long quarantinedDevices, long suspendedDevices, double averageTrustScore, double averageDecisionLatencyMs, long activeAlertsCount, long criticalAlertsCount, long totalOnChainTxCount, List<DeviceResponse> topDevices, List<SecurityAlertResponse> recentAlerts, List<AuditLogResponse> recentAuditLogs, List<BlockchainTxResponse> recentTransactions) {
        this.totalDevices = totalDevices;
        this.activeDevices = activeDevices;
        this.quarantinedDevices = quarantinedDevices;
        this.suspendedDevices = suspendedDevices;
        this.averageTrustScore = averageTrustScore;
        this.averageDecisionLatencyMs = averageDecisionLatencyMs;
        this.activeAlertsCount = activeAlertsCount;
        this.criticalAlertsCount = criticalAlertsCount;
        this.totalOnChainTxCount = totalOnChainTxCount;
        this.topDevices = topDevices;
        this.recentAlerts = recentAlerts;
        this.recentAuditLogs = recentAuditLogs;
        this.recentTransactions = recentTransactions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalDevices;
        private long activeDevices;
        private long quarantinedDevices;
        private long suspendedDevices;
        private double averageTrustScore;
        private double averageDecisionLatencyMs;
        private long activeAlertsCount;
        private long criticalAlertsCount;
        private long totalOnChainTxCount;
        private List<DeviceResponse> topDevices;
        private List<SecurityAlertResponse> recentAlerts;
        private List<AuditLogResponse> recentAuditLogs;
        private List<BlockchainTxResponse> recentTransactions;

        public Builder totalDevices(long totalDevices) { this.totalDevices = totalDevices; return this; }
        public Builder activeDevices(long activeDevices) { this.activeDevices = activeDevices; return this; }
        public Builder quarantinedDevices(long quarantinedDevices) { this.quarantinedDevices = quarantinedDevices; return this; }
        public Builder suspendedDevices(long suspendedDevices) { this.suspendedDevices = suspendedDevices; return this; }
        public Builder averageTrustScore(double averageTrustScore) { this.averageTrustScore = averageTrustScore; return this; }
        public Builder averageDecisionLatencyMs(double averageDecisionLatencyMs) { this.averageDecisionLatencyMs = averageDecisionLatencyMs; return this; }
        public Builder activeAlertsCount(long activeAlertsCount) { this.activeAlertsCount = activeAlertsCount; return this; }
        public Builder criticalAlertsCount(long criticalAlertsCount) { this.criticalAlertsCount = criticalAlertsCount; return this; }
        public Builder totalOnChainTxCount(long totalOnChainTxCount) { this.totalOnChainTxCount = totalOnChainTxCount; return this; }
        public Builder topDevices(List<DeviceResponse> topDevices) { this.topDevices = topDevices; return this; }
        public Builder recentAlerts(List<SecurityAlertResponse> recentAlerts) { this.recentAlerts = recentAlerts; return this; }
        public Builder recentAuditLogs(List<AuditLogResponse> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; return this; }
        public Builder recentTransactions(List<BlockchainTxResponse> recentTransactions) { this.recentTransactions = recentTransactions; return this; }

        public DashboardStatsResponse build() {
            return new DashboardStatsResponse(totalDevices, activeDevices, quarantinedDevices, suspendedDevices, averageTrustScore, averageDecisionLatencyMs, activeAlertsCount, criticalAlertsCount, totalOnChainTxCount, topDevices, recentAlerts, recentAuditLogs, recentTransactions);
        }
    }

    public long getTotalDevices() { return totalDevices; }
    public void setTotalDevices(long totalDevices) { this.totalDevices = totalDevices; }

    public long getActiveDevices() { return activeDevices; }
    public void setActiveDevices(long activeDevices) { this.activeDevices = activeDevices; }

    public long getQuarantinedDevices() { return quarantinedDevices; }
    public void setQuarantinedDevices(long quarantinedDevices) { this.quarantinedDevices = quarantinedDevices; }

    public long getSuspendedDevices() { return suspendedDevices; }
    public void setSuspendedDevices(long suspendedDevices) { this.suspendedDevices = suspendedDevices; }

    public double getAverageTrustScore() { return averageTrustScore; }
    public void setAverageTrustScore(double averageTrustScore) { this.averageTrustScore = averageTrustScore; }

    public double getAverageDecisionLatencyMs() { return averageDecisionLatencyMs; }
    public void setAverageDecisionLatencyMs(double averageDecisionLatencyMs) { this.averageDecisionLatencyMs = averageDecisionLatencyMs; }

    public long getActiveAlertsCount() { return activeAlertsCount; }
    public void setActiveAlertsCount(long activeAlertsCount) { this.activeAlertsCount = activeAlertsCount; }

    public long getCriticalAlertsCount() { return criticalAlertsCount; }
    public void setCriticalAlertsCount(long criticalAlertsCount) { this.criticalAlertsCount = criticalAlertsCount; }

    public long getTotalOnChainTxCount() { return totalOnChainTxCount; }
    public void setTotalOnChainTxCount(long totalOnChainTxCount) { this.totalOnChainTxCount = totalOnChainTxCount; }

    public List<DeviceResponse> getTopDevices() { return topDevices; }
    public void setTopDevices(List<DeviceResponse> topDevices) { this.topDevices = topDevices; }

    public List<SecurityAlertResponse> getRecentAlerts() { return recentAlerts; }
    public void setRecentAlerts(List<SecurityAlertResponse> recentAlerts) { this.recentAlerts = recentAlerts; }

    public List<AuditLogResponse> getRecentAuditLogs() { return recentAuditLogs; }
    public void setRecentAuditLogs(List<AuditLogResponse> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; }

    public List<BlockchainTxResponse> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<BlockchainTxResponse> recentTransactions) { this.recentTransactions = recentTransactions; }
}
