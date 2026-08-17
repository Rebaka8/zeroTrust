-- =================================================================
-- Zero Trust IoT Security Framework - PostgreSQL Database Schema
-- Version: 1.0.0 (Production Grade)
-- Target: PostgreSQL 14 / 15 / 16+
-- =================================================================

-- 1. Enable Required Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =================================================================
-- 2. Drop Existing Tables (Cascade) for Clean Setup
-- =================================================================
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS blockchain_transactions CASCADE;
DROP TABLE IF EXISTS security_alerts CASCADE;
DROP TABLE IF EXISTS access_requests CASCADE;
DROP TABLE IF EXISTS access_policies CASCADE;
DROP TABLE IF EXISTS trust_scores CASCADE;
DROP TABLE IF EXISTS device_telemetry CASCADE;
DROP TABLE IF EXISTS verifiable_credentials CASCADE;
DROP TABLE IF EXISTS did_documents CASCADE;
DROP TABLE IF EXISTS devices CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =================================================================
-- 3. Utility Function: Update Timestamp Trigger
-- =================================================================
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =================================================================
-- 4. User Authentication & RBAC Tables
-- =================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    wallet_address VARCHAR(66) UNIQUE, -- EIP-55 Ethereum Address (0x...)
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER set_timestamp_users
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =================================================================
-- 5. IoT Device Registry & Lifecycle
-- =================================================================
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES users(id) ON DELETE SET NULL,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50) NOT NULL, -- e.g., SENSOR_TEMPERATURE, SMART_METER, INDUSTRIAL_ACTUATOR, GATEWAY
    hardware_model VARCHAR(100) NOT NULL,
    mac_address VARCHAR(17) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    firmware_hash VARCHAR(64) NOT NULL, -- SHA-256 Digest of verified firmware image
    status VARCHAR(20) NOT NULL DEFAULT 'PROVISIONED' CHECK (status IN ('PROVISIONED', 'ACTIVE', 'SUSPENDED', 'QUARANTINED', 'DECOMMISSIONED')),
    public_key VARCHAR(255) NOT NULL, -- Hex-encoded Ed25519 or Secp256k1 public key
    is_quarantined BOOLEAN NOT NULL DEFAULT FALSE,
    current_trust_score INT NOT NULL DEFAULT 100 CHECK (current_trust_score BETWEEN 0 AND 100),
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_heartbeat TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER set_timestamp_devices
BEFORE UPDATE ON devices
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- =================================================================
-- 6. W3C Decentralized Identity (DID) Documents
-- =================================================================
CREATE TABLE did_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL UNIQUE REFERENCES devices(id) ON DELETE CASCADE,
    did_uri VARCHAR(255) NOT NULL UNIQUE, -- e.g., did:zt:dev:0x1A4F98E4...
    document_json JSONB NOT NULL, -- Valid W3C JSON-LD DID Document representation
    public_key_multibase VARCHAR(255) NOT NULL,
    on_chain_tx_hash VARCHAR(66), -- Transaction hash anchoring DID to DIDRegistry.sol
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER set_timestamp_did_documents
BEFORE UPDATE ON did_documents
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- =================================================================
-- 7. W3C Verifiable Credentials (VCs)
-- =================================================================
CREATE TABLE verifiable_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    credential_type VARCHAR(100) NOT NULL, -- e.g., IoTDeviceAttestation, IndustrialGatewayAuthorization
    issuer_did VARCHAR(255) NOT NULL,
    subject_did VARCHAR(255) NOT NULL,
    raw_vc_jwt TEXT NOT NULL, -- Cryptographically signed JWT/JSON-LD proof
    claims_json JSONB NOT NULL,
    issuance_date TIMESTAMPTZ NOT NULL,
    expiration_date TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =================================================================
-- 8. IoT Real-Time Telemetry & Metric Ingestion
-- =================================================================
CREATE TABLE device_telemetry (
    id BIGSERIAL PRIMARY KEY,
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    temperature NUMERIC(6, 2),
    humidity NUMERIC(5, 2),
    voltage NUMERIC(6, 2),
    cpu_utilization NUMERIC(5, 2),
    memory_usage NUMERIC(5, 2),
    packet_rate INT,
    payload_signature VARCHAR(255),
    metadata JSONB,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =================================================================
-- 9. Zero Trust Continuous Risk & Trust Scoring Ledger
-- =================================================================
CREATE TABLE trust_scores (
    id BIGSERIAL PRIMARY KEY,
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    overall_score INT NOT NULL CHECK (overall_score BETWEEN 0 AND 100),
    crypto_identity_score INT NOT NULL CHECK (crypto_identity_score BETWEEN 0 AND 100),
    behavioral_score INT NOT NULL CHECK (behavioral_score BETWEEN 0 AND 100),
    firmware_score INT NOT NULL CHECK (firmware_score BETWEEN 0 AND 100),
    network_score INT NOT NULL CHECK (network_score BETWEEN 0 AND 100),
    penalty_score INT NOT NULL DEFAULT 0,
    risk_level VARCHAR(25) NOT NULL CHECK (risk_level IN ('LOW_NOMINAL', 'MEDIUM_ELEVATED', 'HIGH_SUSPICIOUS', 'CRITICAL_COMPROMISED')),
    evaluation_reasons TEXT,
    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =================================================================
-- 10. Zero Trust Access Control Policies (ABAC / Rule Definitions)
-- =================================================================
CREATE TABLE access_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    minimum_trust_score INT NOT NULL DEFAULT 70 CHECK (minimum_trust_score BETWEEN 0 AND 100),
    required_credential_type VARCHAR(100),
    allowed_topics VARCHAR(255) NOT NULL, -- MQTT topic pattern, e.g., "iot/+/telemetry", "iot/grid/control"
    action_allowed VARCHAR(20) NOT NULL CHECK (action_allowed IN ('READ', 'WRITE', 'EXECUTE', 'ADMIN')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER set_timestamp_access_policies
BEFORE UPDATE ON access_policies
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- =================================================================
-- 11. Policy Decision Point (PDP) Access Requests & Audit Log
-- =================================================================
CREATE TABLE access_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    policy_id UUID REFERENCES access_policies(id) ON DELETE SET NULL,
    requested_resource VARCHAR(255) NOT NULL,
    requested_action VARCHAR(50) NOT NULL,
    trust_score_at_request INT NOT NULL,
    decision VARCHAR(25) NOT NULL CHECK (decision IN ('PERMIT_FULL', 'PERMIT_RESTRICTED', 'CHALLENGE_REAUTH', 'DENY_QUARANTINE')),
    reason TEXT,
    on_chain_audit_tx VARCHAR(66), -- Transaction hash anchored to ImmutableAuditLog.sol
    requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =================================================================
-- 12. Security Incidents & Threat Alerts
-- =================================================================
CREATE TABLE security_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
    alert_type VARCHAR(50) NOT NULL, -- e.g., REPLAY_ATTACK, SPOOFED_IDENTITY, ANOMALY_BURST, FIRMWARE_TAMPER, UNAUTHORIZED_TOPIC
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    description TEXT NOT NULL,
    incident_payload JSONB,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    triggered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =================================================================
-- 13. Blockchain Transaction Ledger & Synchronization
-- =================================================================
CREATE TABLE blockchain_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tx_hash VARCHAR(66) NOT NULL UNIQUE,
    contract_name VARCHAR(50) NOT NULL,
    function_name VARCHAR(50) NOT NULL,
    block_number BIGINT,
    from_address VARCHAR(66) NOT NULL,
    to_address VARCHAR(66) NOT NULL,
    gas_used NUMERIC(20, 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'MINED', 'FAILED')),
    mined_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =================================================================
-- 14. Administrative & System Audit Trail (Tamper Evident)
-- =================================================================
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action_type VARCHAR(50) NOT NULL, -- e.g., DEVICE_REGISTERED, POLICY_MODIFIED, QUARANTINE_OVERRIDE, VC_ISSUED
    target_entity VARCHAR(50) NOT NULL,
    target_id UUID,
    ip_address VARCHAR(45),
    client_user_agent VARCHAR(255),
    details TEXT,
    integrity_hash VARCHAR(64), -- SHA-256(prev_hash + record_content)
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =================================================================
-- 15. Performance & Indexing Optimization
-- =================================================================
CREATE INDEX idx_devices_owner ON devices(owner_id);
CREATE INDEX idx_devices_mac ON devices(mac_address);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_devices_trust_score ON devices(current_trust_score);

CREATE INDEX idx_did_uri ON did_documents(did_uri);
CREATE INDEX idx_did_device ON did_documents(device_id);

CREATE INDEX idx_vc_device ON verifiable_credentials(device_id);
CREATE INDEX idx_vc_subject ON verifiable_credentials(subject_did);
CREATE INDEX idx_vc_expiration ON verifiable_credentials(expiration_date);

CREATE INDEX idx_telemetry_device_time ON device_telemetry(device_id, recorded_at DESC);
CREATE INDEX idx_trust_scores_device_time ON trust_scores(device_id, evaluated_at DESC);
CREATE INDEX idx_access_requests_device_time ON access_requests(device_id, requested_at DESC);
CREATE INDEX idx_security_alerts_device ON security_alerts(device_id);
CREATE INDEX idx_security_alerts_severity ON security_alerts(severity, is_resolved);
CREATE INDEX idx_bc_tx_hash ON blockchain_transactions(tx_hash);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);

-- GIN Indexes for fast JSONB querying
CREATE INDEX idx_did_doc_jsonb ON did_documents USING GIN (document_json);
CREATE INDEX idx_vc_claims_jsonb ON verifiable_credentials USING GIN (claims_json);
CREATE INDEX idx_telemetry_meta_jsonb ON device_telemetry USING GIN (metadata);
CREATE INDEX idx_alerts_payload_jsonb ON security_alerts USING GIN (incident_payload);

-- =================================================================
-- 16. Initial Seed Data (Production & Showcase Readiness)
-- =================================================================

-- Roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Platform Administrator with full governance privileges'),
('ROLE_OPERATOR', 'Security Operations Center (SOC) Operator'),
('ROLE_AUDITOR', 'Read-only compliance and blockchain auditor'),
('ROLE_DEVICE_OWNER', 'End-user or enterprise fleet device owner');

-- Default Admin User (Password: "AdminPassword123!" hashed with BCrypt)
INSERT INTO users (id, username, email, password_hash, wallet_address, status) VALUES
('a0000000-0000-0000-0000-000000000001', 'admin', 'admin@zerotrust-iot.io', '$2a$10$wK1yC79W3u2Hq.aFkUfZ7.Wk4d1/3Vv6P5D9m7W7mCgU2aFz1A2aG', '0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266', 'ACTIVE');

-- Assign Admin Role
INSERT INTO user_roles (user_id, role_id)
SELECT 'a0000000-0000-0000-0000-000000000001', id FROM roles WHERE name = 'ROLE_ADMIN';

-- Default Zero Trust Access Policies
INSERT INTO access_policies (id, policy_name, description, minimum_trust_score, required_credential_type, allowed_topics, action_allowed, is_active) VALUES
('b0000000-0000-0000-0000-000000000001', 'Telemetry Ingestion Policy', 'Standard policy allowing low-risk sensor data transmission', 60, 'IoTDeviceAttestation', 'iot/+/telemetry', 'WRITE', TRUE),
('b0000000-0000-0000-0000-000000000002', 'Critical Grid Actuation Policy', 'High-assurance policy requiring strict trust score for industrial actuators', 85, 'IndustrialGatewayAuthorization', 'iot/grid/control', 'EXECUTE', TRUE),
('b0000000-0000-0000-0000-000000000003', 'Firmware Over-The-Air (FOTA) Policy', 'Restricted policy for firmware update downloads and verification', 90, 'FOTACapabilityToken', 'iot/fota/update', 'READ', TRUE);

-- Sample Provisioned Devices for Initial Showcase
INSERT INTO devices (id, owner_id, device_name, device_type, hardware_model, mac_address, ip_address, firmware_hash, status, public_key, is_quarantined, current_trust_score, registered_at, last_heartbeat) VALUES
('d0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'Smart Grid Substation Alpha', 'SMART_METER', 'Siemens-SICAM-A8000', '00:1A:2B:3C:4D:5E', '192.168.1.101', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'ACTIVE', '0x04bfcab57f1b6f8f9e1e1279163b4e2170360f07b19641e7a0ffae80e0bb8625ec131497562192eecd161ce38d3304581ec263dae0fe0436a5fa50ca7940100f45', FALSE, 96, NOW() - INTERVAL '14 days', NOW()),
('d0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'Industrial Gateway Edge-04', 'GATEWAY', 'Advantech-UNO-2271G', '00:1A:2B:3C:4D:5F', '192.168.1.102', 'a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e', 'ACTIVE', '0x04467f700f9ad9723385e33937e57b6c460b68dc3b9f9a65777787964d26302ed87e58e82584d0885324f97ec35eb460ad63eee583ec5b56a83f0c75b525f4ced7', FALSE, 88, NOW() - INTERVAL '7 days', NOW()),
('d0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'HVAC Valve Actuator 09', 'INDUSTRIAL_ACTUATOR', 'Honeywell-VRN2', '00:1A:2B:3C:4D:60', '192.168.1.103', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'QUARANTINED', '0x04358a980e8e63e26cf8d1ff5b2a0c647b0a70f3f619b02a9b3a0b127419f9f8f2b84260b4570ff22ec28c9b1f7f02ec3b7b25867160759f237ef401f705141940', TRUE, 22, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 hours');

-- Corresponding DID Documents
INSERT INTO did_documents (device_id, did_uri, document_json, public_key_multibase, on_chain_tx_hash, is_revoked) VALUES
('d0000000-0000-0000-0000-000000000001', 'did:zt:dev:0x1A4F98E4B72C8120391A', '{"@context": ["https://www.w3.org/ns/did/v1"], "id": "did:zt:dev:0x1A4F98E4B72C8120391A", "verificationMethod": [{"id": "did:zt:dev:0x1A4F98E4B72C8120391A#key-1", "type": "Ed25519VerificationKey2020", "controller": "did:zt:dev:0x1A4F98E4B72C8120391A", "publicKeyMultibase": "z6MkuT4X89J897vY6LqK2xPzW902"}]}', 'z6MkuT4X89J897vY6LqK2xPzW902', '0x3ab945fe2189d0473957291a1827402685718294a7e930172649581029482910', FALSE),
('d0000000-0000-0000-0000-000000000002', 'did:zt:dev:0x8B2F61C99014E738102B', '{"@context": ["https://www.w3.org/ns/did/v1"], "id": "did:zt:dev:0x8B2F61C99014E738102B", "verificationMethod": [{"id": "did:zt:dev:0x8B2F61C99014E738102B#key-1", "type": "Ed25519VerificationKey2020", "controller": "did:zt:dev:0x8B2F61C99014E738102B", "publicKeyMultibase": "z6MkoQ1V82K765uX5KpL1wOzV801"}]}', 'z6MkoQ1V82K765uX5KpL1wOzV801', '0x8fa37291048b4e10948572019485720194857201948572019485720194857201', FALSE),
('d0000000-0000-0000-0000-000000000003', 'did:zt:dev:0x5F9A10382910C738192C', '{"@context": ["https://www.w3.org/ns/did/v1"], "id": "did:zt:dev:0x5F9A10382910C738192C", "verificationMethod": [{"id": "did:zt:dev:0x5F9A10382910C738192C#key-1", "type": "Ed25519VerificationKey2020", "controller": "did:zt:dev:0x5F9A10382910C738192C", "publicKeyMultibase": "z6MkmP0U71J654tW4JoK0vNyU700"}]}', 'z6MkmP0U71J654tW4JoK0vNyU700', '0x77ae991c01928374650192837465019283746501928374650192837465019283', FALSE);

-- Sample Verifiable Credentials
INSERT INTO verifiable_credentials (device_id, credential_type, issuer_did, subject_did, raw_vc_jwt, claims_json, issuance_date, expiration_date, is_revoked) VALUES
('d0000000-0000-0000-0000-000000000001', 'IoTDeviceAttestation', 'did:zt:issuer:root-authority', 'did:zt:dev:0x1A4F98E4B72C8120391A', 'eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9...', '{"firmwareVerified": true, "securityLevel": "EAL4+", "compliance": "IEC-62443"}', NOW() - INTERVAL '10 days', NOW() + INTERVAL '355 days', FALSE),
('d0000000-0000-0000-0000-000000000002', 'IndustrialGatewayAuthorization', 'did:zt:issuer:root-authority', 'did:zt:dev:0x8B2F61C99014E738102B', 'eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9...', '{"networkRole": "GATEWAY_ROUTER", "maxAllowedThroughput": 1000}', NOW() - INTERVAL '5 days', NOW() + INTERVAL '360 days', FALSE);

-- Sample Trust Score Logs
INSERT INTO trust_scores (device_id, overall_score, crypto_identity_score, behavioral_score, firmware_score, network_score, penalty_score, risk_level, evaluation_reasons, evaluated_at) VALUES
('d0000000-0000-0000-0000-000000000001', 96, 100, 95, 100, 90, 0, 'LOW_NOMINAL', 'Cryptographic verification passed; firmware SHA-256 matches on-chain digest; packet rate nominal.', NOW()),
('d0000000-0000-0000-0000-000000000002', 88, 100, 85, 100, 80, 0, 'LOW_NOMINAL', 'Nominal gateway operation with slight latency variance.', NOW()),
('d0000000-0000-0000-0000-000000000003', 22, 50, 10, 0, 40, 70, 'CRITICAL_COMPROMISED', 'Firmware hash mismatch detected! Replay attack nonce reuse identified. Device quarantined automatically.', NOW());
