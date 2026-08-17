# 📡 REST API & Integration Documentation
## **Zero Trust IoT Security Framework v1.0**

The Spring Boot backend exposes OpenAPI 3.0 compliant RESTful endpoints and STOMP WebSocket channels on port `8085`.
- **Interactive Swagger UI**: `http://localhost:8085/swagger-ui.html`
- **OpenAPI 3.0 Specification**: `http://localhost:8085/v3/api-docs`

---

## 🔐 1. Authentication & Session (`/api/v1/auth`)

### `POST /api/v1/auth/login`
Authenticates an administrator or operator and issues a signed JWT Bearer token.
- **Request Body**:
  ```json
  {
    "usernameOrEmail": "admin",
    "password": "Admin@123456"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "id": "7f2b58cc-964e-4b2c-a699-5e325a1852d1",
    "username": "admin",
    "email": "admin@zerotrust-iot.io",
    "roles": ["ROLE_OPERATOR", "ROLE_ADMIN"]
  }
  ```

---

## 🏷️ 2. W3C Decentralized Identifiers (`/api/v1/did`)

### `GET /api/v1/did/resolve/{didUri}`
Universal W3C DID Resolver returning a JSON-LD compliant DID Document.
- **Example URL**: `http://localhost:8085/api/v1/did/resolve/did:zt:dev:0x19D91B736A9727B87EC7`
- **Response `200 OK`**:
  ```json
  {
    "@context": ["https://www.w3.org/ns/did/v1"],
    "id": "did:zt:dev:0x19D91B736A9727B87EC7",
    "verificationMethod": [
      {
        "id": "did:zt:dev:0x19D91B736A9727B87EC7#key-1",
        "type": "Ed25519VerificationKey2020",
        "controller": "did:zt:dev:0x19D91B736A9727B87EC7",
        "publicKeyHex": "04a1b2c3d4e5f6..."
      }
    ],
    "authentication": ["did:zt:dev:0x19D91B736A9727B87EC7#key-1"],
    "assertionMethod": ["did:zt:dev:0x19D91B736A9727B87EC7#key-1"],
    "deactivated": false
  }
  ```

---

## 🛡️ 3. Verifiable Credentials (`/api/v1/vc`)

### `POST /api/v1/vc/verify`
Cryptographically verifies a Verifiable Credential JWT, signature validity, expiration timestamp, and on-chain revocation state.
- **Request Body**:
  ```json
  {
    "vcPayload": "eyJhbGciOiJSUzI1NiJ9..."
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "valid": true,
    "subjectDid": "did:zt:dev:0x19D91B736A9727B87EC7",
    "issuerDid": "did:zt:issuer:0x8626f6940e2eb28930efb4cef49b2d1f2c9c1199",
    "credentialType": "ZeroTrustDeviceAttestation",
    "trustTier": "TIER_1_CRITICAL_INFRASTRUCTURE",
    "isExpired": false,
    "isRevoked": false,
    "signatureValid": true,
    "firmwareIntegrityMatched": true,
    "verificationSummary": "Verifiable Credential signature and claims cryptographically verified."
  }
  ```

---

## ⚡ 4. Dynamic Risk Engine & PDP (`/api/v1/trust` & `/api/v1/policies`)

### `POST /api/v1/trust/evaluate/{deviceId}`
Calculates real-time multi-dimensional trust score $T(t)$ for an IoT node.

### `POST /api/v1/policies/evaluate`
ABAC Policy Decision Point evaluation.
- **Request Body**:
  ```json
  {
    "didUri": "did:zt:dev:0x19D91B736A9727B87EC7",
    "resource": "smartgrid/substation/telemetry",
    "action": "READ"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "requestId": "90e67b2d-128a-4952-ba74-dfcb4628f804",
    "didUri": "did:zt:dev:0x19D91B736A9727B87EC7",
    "resource": "smartgrid/substation/telemetry",
    "action": "READ",
    "currentTrustScore": 97,
    "decision": "PERMIT_FULL",
    "granted": true,
    "reason": "Zero Trust Policy Verification Passed: Trust Score (97/100) qualifies for Full Operational Access."
  }
  ```

---

## 📊 5. Kaggle Dataset Replay & Benchmarking (`/api/v1/dataset`)

### `POST /api/v1/dataset/replay-preset/{presetId}`
Streams a pre-bundled Kaggle dataset (e.g. `iot-23-smart-meter` or `ciciot-2023-ddos-flood`).

### `POST /api/v1/dataset/upload-replay` (`multipart/form-data`)
Uploads and processes any user-provided Kaggle CSV file.
- **Response `200 OK`**:
  ```json
  {
    "datasetName": "Kaggle IoT-23 (Mirai & PortScan)",
    "totalRowsProcessed": 12,
    "benignPacketsCount": 7,
    "maliciousPacketsCount": 5,
    "truePositives": 5,
    "falsePositives": 0,
    "trueNegatives": 7,
    "falseNegatives": 0,
    "accuracyPercentage": 100.0,
    "precisionPercentage": 100.0,
    "recallPercentage": 100.0,
    "f1ScorePercentage": 100.0,
    "averageDetectionLatencyMs": 14.8,
    "automaticQuarantinesEnforced": 3,
    "detectedThreatSignatures": ["PortScan", "Mirai_Botnet_Recon", "Mirai_BruteForce"]
  }
  ```
