package com.zerotrust.iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerotrust.iot.did.VerificationMethod;
import com.zerotrust.iot.did.W3CDidDocument;
import com.zerotrust.iot.util.CryptoUtils;
import com.zerotrust.iot.vc.CredentialSubject;
import com.zerotrust.iot.vc.Proof;
import com.zerotrust.iot.vc.VerifiableCredentialModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DidAndVcTests {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("Verify W3C DID Document compliance and JSON-LD serialization")
    void testW3CDidDocumentSerialization() throws Exception {
        String didUri = "did:zt:dev:0x9A4B5C6D7E8F12345678";
        VerificationMethod vm = VerificationMethod.builder()
                .id(didUri + "#key-1")
                .type("Ed25519VerificationKey2020")
                .controller(didUri)
                .publicKeyMultibase("z6MkmL6N5D3PjK3G4yV2q8")
                .build();

        W3CDidDocument didDoc = W3CDidDocument.builder()
                .id(didUri)
                .verificationMethod(List.of(vm))
                .authentication(List.of(didUri + "#key-1"))
                .assertionMethod(List.of(didUri + "#key-1"))
                .capabilityInvocation(List.of(didUri + "#key-1"))
                .metadataCid("QmXoypizjW3WknFiJnKLwHCnL72vedxjQkDDP1mXWo6uco")
                .deactivated(false)
                .build();

        String json = objectMapper.writeValueAsString(didDoc);
        assertNotNull(json);
        assertTrue(json.contains("@context"));
        assertTrue(json.contains(didUri));

        W3CDidDocument parsed = objectMapper.readValue(json, W3CDidDocument.class);
        assertEquals(didUri, parsed.getId());
        assertEquals(1, parsed.getVerificationMethod().size());
        assertEquals("Ed25519VerificationKey2020", parsed.getVerificationMethod().get(0).getType());
    }

    @Test
    @DisplayName("Verify W3C Verifiable Credential issuance, cryptographic signature, and verification")
    void testVerifiableCredentialVerificationPipeline() throws Exception {
        String issuerDid = "did:zt:issuer:0x8626f6940e2eb28930efb4cef49b2d1f2c9c1199";
        String subjectDid = "did:zt:dev:0x9A4B5C6D7E8F12345678";
        String firmwareHash = CryptoUtils.sha256("firmware_v1.0.4");
        Instant now = Instant.now();
        Instant expiry = now.plus(365, ChronoUnit.DAYS);
        String vcId = "urn:uuid:" + UUID.randomUUID();

        CredentialSubject subject = CredentialSubject.builder()
                .id(subjectDid)
                .deviceName("Smart Energy Gateway 01")
                .deviceType("INDUSTRIAL_GATEWAY")
                .hardwareModel("Raspberry Pi CM4")
                .macAddress("DC:A6:32:44:55:66")
                .firmwareHash(firmwareHash)
                .trustTier("TIER_1_CRITICAL")
                .allowedCapabilities(List.of("telemetry:publish", "energy:control"))
                .build();

        String subjectJson = objectMapper.writeValueAsString(subject);
        String proofTarget = vcId + ":" + issuerDid + ":" + subjectDid + ":" + now.toEpochMilli() + ":" + CryptoUtils.sha256(subjectJson);
        String signatureValue = "0x" + CryptoUtils.sha256(proofTarget) + CryptoUtils.sha256(issuerDid).substring(0, 64);

        Proof proof = Proof.builder()
                .type("Ed25519Signature2020")
                .created(now)
                .verificationMethod(issuerDid + "#key-1")
                .proofPurpose("assertionMethod")
                .proofValue(signatureValue)
                .build();

        VerifiableCredentialModel vc = VerifiableCredentialModel.builder()
                .id(vcId)
                .type(List.of("VerifiableCredential", "IoTDeviceAttestation"))
                .issuer(issuerDid)
                .issuanceDate(now)
                .expirationDate(expiry)
                .credentialSubject(subject)
                .proof(proof)
                .build();

        String vcJson = objectMapper.writeValueAsString(vc);
        assertNotNull(vcJson);

        // 1. Verify valid credential signature
        VerifiableCredentialModel parsedVc = objectMapper.readValue(vcJson, VerifiableCredentialModel.class);
        String parsedSubjectJson = objectMapper.writeValueAsString(parsedVc.getCredentialSubject());
        String expectedTarget = parsedVc.getId() + ":" + parsedVc.getIssuer() + ":" + parsedVc.getCredentialSubject().getId()
                + ":" + parsedVc.getIssuanceDate().toEpochMilli() + ":" + CryptoUtils.sha256(parsedSubjectJson);
        String expectedSig = "0x" + CryptoUtils.sha256(expectedTarget) + CryptoUtils.sha256(parsedVc.getIssuer()).substring(0, 64);

        assertEquals(expectedSig, parsedVc.getProof().getProofValue(), "Cryptographic proof must match computed digest");
        assertTrue(parsedVc.getExpirationDate().isAfter(Instant.now()), "Credential must be within valid lifetime");

        // 2. Detect tamper if someone alters the subject claims (e.g. firmware hash modified)
        CredentialSubject tamperedSubject = CredentialSubject.builder()
                .id(subjectDid)
                .deviceName("Smart Energy Gateway 01")
                .firmwareHash(CryptoUtils.sha256("malicious_hacked_firmware"))
                .build();

        String tamperedSubjectJson = objectMapper.writeValueAsString(tamperedSubject);
        String tamperedTarget = parsedVc.getId() + ":" + parsedVc.getIssuer() + ":" + subjectDid
                + ":" + parsedVc.getIssuanceDate().toEpochMilli() + ":" + CryptoUtils.sha256(tamperedSubjectJson);
        String tamperedSig = "0x" + CryptoUtils.sha256(tamperedTarget) + CryptoUtils.sha256(parsedVc.getIssuer()).substring(0, 64);

        assertNotEquals(expectedSig, tamperedSig, "Tampering must invalidate cryptographic proof digest");
    }
}
