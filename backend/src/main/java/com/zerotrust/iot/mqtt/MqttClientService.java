package com.zerotrust.iot.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerotrust.iot.dto.telemetry.TelemetryIngestRequest;
import com.zerotrust.iot.service.TelemetryIngestService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class MqttClientService implements MqttCallbackExtended {

    private static final Logger log = LoggerFactory.getLogger(MqttClientService.class);

    private final TelemetryIngestService telemetryIngestService;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.broker-url:${app.mqtt.broker-url:tcp://localhost:1883}}")
    private String brokerUrl;

    @Value("${mqtt.client-id:${app.mqtt.client-id:ZeroTrustBackendIngestionNode}}")
    private String clientId;

    private MqttAsyncClient mqttClient;

    public MqttClientService(TelemetryIngestService telemetryIngestService, ObjectMapper objectMapper) {
        this.telemetryIngestService = telemetryIngestService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            mqttClient = new MqttAsyncClient(brokerUrl, clientId + "_" + System.currentTimeMillis(), new MemoryPersistence());
            mqttClient.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);

            log.info("Connecting to Mosquitto MQTT Broker at: {}", brokerUrl);
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    log.info("Successfully connected to MQTT Broker at {}", brokerUrl);
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    log.warn("Could not immediately connect to MQTT broker ({}: {}). Automatic reconnect active.", brokerUrl, exception.getMessage());
                }
            });
        } catch (Exception e) {
            log.warn("MQTT Ingestion initialized in passive mode: {}", e.getMessage());
        }
    }

    private void subscribeToTopics() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe("iot/+/telemetry", 1);
                mqttClient.subscribe("iot/+/status", 1);
                log.info("Subscribed to MQTT topics: iot/+/telemetry, iot/+/status");
            }
        } catch (Exception e) {
            log.error("Failed to subscribe to MQTT topics: {}", e.getMessage());
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT Connection established (reconnect={}) with broker: {}", reconnect, serverURI);
        subscribeToTopics();
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost: {}", cause != null ? cause.getMessage() : "Unknown reason");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            log.debug("Received MQTT message on topic [{}]: {}", topic, payload);

            TelemetryIngestRequest request = objectMapper.readValue(payload, TelemetryIngestRequest.class);
            telemetryIngestService.ingestTelemetry(request);
        } catch (Exception e) {
            log.warn("Failed to process incoming MQTT telemetry from {}: {}", topic, e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used for pure ingestion
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception ignored) {}
    }
}
