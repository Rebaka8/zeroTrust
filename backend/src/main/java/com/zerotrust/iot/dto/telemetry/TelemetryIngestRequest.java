package com.zerotrust.iot.dto.telemetry;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class TelemetryIngestRequest {
    @NotBlank(message = "DID URI is required")
    private String didUri;

    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal voltage;
    private BigDecimal cpuUtilization;
    private BigDecimal memoryUsage;
    private Integer packetRate;
    private String payloadSignature;
    private String metadata;

    public TelemetryIngestRequest() {}

    public TelemetryIngestRequest(String didUri, BigDecimal temperature, BigDecimal humidity, BigDecimal voltage, BigDecimal cpuUtilization, BigDecimal memoryUsage, Integer packetRate, String payloadSignature, String metadata) {
        this.didUri = didUri;
        this.temperature = temperature;
        this.humidity = humidity;
        this.voltage = voltage;
        this.cpuUtilization = cpuUtilization;
        this.memoryUsage = memoryUsage;
        this.packetRate = packetRate;
        this.payloadSignature = payloadSignature;
        this.metadata = metadata;
    }

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

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
