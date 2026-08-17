package com.zerotrust.iot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "device_telemetry")
public class DeviceTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(precision = 6, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(precision = 6, scale = 2)
    private BigDecimal voltage;

    @Column(name = "cpu_utilization", precision = 5, scale = 2)
    private BigDecimal cpuUtilization;

    @Column(name = "memory_usage", precision = 5, scale = 2)
    private BigDecimal memoryUsage;

    @Column(name = "packet_rate")
    private Integer packetRate;

    @Column(name = "payload_signature", length = 255)
    private String payloadSignature;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    public DeviceTelemetry() {}

    public DeviceTelemetry(Long id, Device device, BigDecimal temperature, BigDecimal humidity, BigDecimal voltage, BigDecimal cpuUtilization, BigDecimal memoryUsage, Integer packetRate, String payloadSignature, String metadata, Instant recordedAt) {
        this.id = id;
        this.device = device;
        this.temperature = temperature;
        this.humidity = humidity;
        this.voltage = voltage;
        this.cpuUtilization = cpuUtilization;
        this.memoryUsage = memoryUsage;
        this.packetRate = packetRate;
        this.payloadSignature = payloadSignature;
        this.metadata = metadata;
        this.recordedAt = recordedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Device device;
        private BigDecimal temperature;
        private BigDecimal humidity;
        private BigDecimal voltage;
        private BigDecimal cpuUtilization;
        private BigDecimal memoryUsage;
        private Integer packetRate;
        private String payloadSignature;
        private String metadata;
        private Instant recordedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder device(Device device) { this.device = device; return this; }
        public Builder temperature(BigDecimal temperature) { this.temperature = temperature; return this; }
        public Builder humidity(BigDecimal humidity) { this.humidity = humidity; return this; }
        public Builder voltage(BigDecimal voltage) { this.voltage = voltage; return this; }
        public Builder cpuUtilization(BigDecimal cpuUtilization) { this.cpuUtilization = cpuUtilization; return this; }
        public Builder memoryUsage(BigDecimal memoryUsage) { this.memoryUsage = memoryUsage; return this; }
        public Builder packetRate(Integer packetRate) { this.packetRate = packetRate; return this; }
        public Builder payloadSignature(String payloadSignature) { this.payloadSignature = payloadSignature; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder recordedAt(Instant recordedAt) { this.recordedAt = recordedAt; return this; }

        public DeviceTelemetry build() {
            return new DeviceTelemetry(id, device, temperature, humidity, voltage, cpuUtilization, memoryUsage, packetRate, payloadSignature, metadata, recordedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

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

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
