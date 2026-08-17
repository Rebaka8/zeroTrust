package com.zerotrust.iot.dto.telemetry;

import java.math.BigDecimal;
import java.time.Instant;

public class TelemetryResponse {
    private Long id;
    private String didUri;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal voltage;
    private BigDecimal cpuUtilization;
    private BigDecimal memoryUsage;
    private Integer packetRate;
    private String payloadSignature;
    private Instant recordedAt;

    public TelemetryResponse() {}

    public TelemetryResponse(Long id, String didUri, BigDecimal temperature, BigDecimal humidity, BigDecimal voltage, BigDecimal cpuUtilization, BigDecimal memoryUsage, Integer packetRate, String payloadSignature, Instant recordedAt) {
        this.id = id;
        this.didUri = didUri;
        this.temperature = temperature;
        this.humidity = humidity;
        this.voltage = voltage;
        this.cpuUtilization = cpuUtilization;
        this.memoryUsage = memoryUsage;
        this.packetRate = packetRate;
        this.payloadSignature = payloadSignature;
        this.recordedAt = recordedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String didUri;
        private BigDecimal temperature;
        private BigDecimal humidity;
        private BigDecimal voltage;
        private BigDecimal cpuUtilization;
        private BigDecimal memoryUsage;
        private Integer packetRate;
        private String payloadSignature;
        private Instant recordedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder didUri(String didUri) { this.didUri = didUri; return this; }
        public Builder temperature(BigDecimal temperature) { this.temperature = temperature; return this; }
        public Builder humidity(BigDecimal humidity) { this.humidity = humidity; return this; }
        public Builder voltage(BigDecimal voltage) { this.voltage = voltage; return this; }
        public Builder cpuUtilization(BigDecimal cpuUtilization) { this.cpuUtilization = cpuUtilization; return this; }
        public Builder memoryUsage(BigDecimal memoryUsage) { this.memoryUsage = memoryUsage; return this; }
        public Builder packetRate(Integer packetRate) { this.packetRate = packetRate; return this; }
        public Builder payloadSignature(String payloadSignature) { this.payloadSignature = payloadSignature; return this; }
        public Builder recordedAt(Instant recordedAt) { this.recordedAt = recordedAt; return this; }

        public TelemetryResponse build() {
            return new TelemetryResponse(id, didUri, temperature, humidity, voltage, cpuUtilization, memoryUsage, packetRate, payloadSignature, recordedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDidUri() { return didUri; }
    public void setDidUri(String didUri) { this.didUri = didUri; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }

    public BigDecimal getVoltage() { return voltage; }
    public void setVoltage(BigDecimal voltage) { this.voltage = voltage; }

    public BigDecimal getCpuUtilization() { return cpuUtilization; }
    public void setCpuUtilization(BigDecimal cpuUtilization) { this.cpuUtilization = cpuUtilization; }

    public BigDecimal getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(BigDecimal memoryUsage) { this.memoryUsage = memoryUsage; }

    public Integer getPacketRate() { return packetRate; }
    public void setPacketRate(Integer packetRate) { this.packetRate = packetRate; }

    public String getPayloadSignature() { return payloadSignature; }
    public void setPayloadSignature(String payloadSignature) { this.payloadSignature = payloadSignature; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
