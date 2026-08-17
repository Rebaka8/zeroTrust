package com.zerotrust.iot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerotrust.iot.dto.vc.VcVerifyRequest;
import com.zerotrust.iot.dto.vc.VcVerifyResponse;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.VerifiableCredential;
import com.zerotrust.iot.repository.DeviceRepository;
import com.zerotrust.iot.repository.DidDocumentRepository;
import com.zerotrust.iot.repository.VerifiableCredentialRepository;
import com.zerotrust.iot.util.CryptoUtils;
import com.zerotrust.iot.vc.CredentialSubject;
import com.zerotrust.iot.vc.Proof;
import com.zerotrust.iot.vc.VerifiableCredentialModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VcVerifierService {

    private static final Logger log = LoggerFactory.getLogger(VcVerifierService.class);

    private final VerifiableCredentialRepository vcRepository;
    private final DidDocumentRepository didDocumentRepository;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;

    public VcVerifierService(
            VerifiableCredentialRepository vcRepository,
            DidDocumentRepository didDocumentRepository,
            DeviceRepository deviceRepository,
            ObjectMapper objectMapper
    ) {
        this.vcRepository = vcRepository;
        this.didDocumentRepository = didDocumentRepository;
        this.deviceRepository = deviceRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public VcVerifyResponse verifyCredential(VcVerifyRequest request) {
        Instant now = Instant.now();
        List<String> auditFindings = new ArrayList<>();

        VerifiableCredentialModel vcModel;
        try {
            vcModel = objectMapper.readValue(request.getVcPayload(), VerifiableCredentialModel.class);
        } catch (Exception e) {
            log.warn("Failed to parse VC payload: {}", e.getMessage());
            return VcVerifyResponse.builder()
                    .valid(false)
                    .verificationSummary("Malformed W3C Verifiable Credential JSON: " + e.getMessage())
                    .checkedAt(now)
                    .build();
        }

        CredentialSubject subject = vcModel.getCredentialSubject();
        if (subject == null || subject.getId() == null) {
            return VcVerifyResponse.builder()
                    .valid(false)
                    .verificationSummary("Missing required 'credentialSubject' or Subject DID.")
                    .checkedAt(now)
                    .build();
        }

        String subjectDid = subject.getId();
        String issuerDid = vcModel.getIssuer();
        String credentialType = vcModel.getType() != null && !vcModel.getType().isEmpty()
                ? vcModel.getType().get(vcModel.getType().size() - 1)
                : "Unknown";

        // 1. Check expiration date
        boolean isExpired = vcModel.getExpirationDate() != null && now.isAfter(vcModel.getExpirationDate());
        if (isExpired) {
            auditFindings.add("EXPIRED: Credential expired on " + vcModel.getExpirationDate());
        } else {
            auditFindings.add("VALID_LIFETIME: Valid until " + vcModel.getExpirationDate());
        }

        // 2. Check DID & Revocation state
        Optional<DidDocument> didDocOpt = didDocumentRepository.findByDidUri(subjectDid);
        boolean isRevoked = false;

        if (didDocOpt.isEmpty()) {
            auditFindings.add("UNKNOWN_DID: Subject DID not registered in local trust registry.");
        } else {
            DidDocument didDoc = didDocOpt.get();
            if (didDoc.getIsRevoked()) {
                isRevoked = true;
                auditFindings.add("REVOKED_DID: Subject DID has been revoked.");
            }

            // Check database VC table for explicit VC revocation
            Optional<VerifiableCredential> vcDbOpt = vcRepository.findFirstBySubjectDidAndIsRevokedFalse(subjectDid);
            if (vcDbOpt.isEmpty()) {
                // If not found active in DB, check if marked revoked
                isRevoked = true;
                auditFindings.add("REVOKED_CREDENTIAL: Credential explicitly revoked in registry.");
            }
        }

        // 3. Cryptographic Proof verification
        Proof proof = vcModel.getProof();
        boolean signatureValid = false;
        if (proof != null && proof.getProofValue() != null && !proof.getProofValue().isBlank()) {
            try {
                String subjectJson = objectMapper.writeValueAsString(subject);
                String expectedProofTarget = vcModel.getId() + ":" + issuerDid + ":" + subjectDid + ":" + vcModel.getIssuanceDate().toEpochMilli() + ":" + CryptoUtils.sha256(subjectJson);
                String expectedSig = "0x" + CryptoUtils.sha256(expectedProofTarget) + CryptoUtils.sha256(issuerDid).substring(0, 64);

                signatureValid = expectedSig.equalsIgnoreCase(proof.getProofValue());
                if (signatureValid) {
                    auditFindings.add("SIGNATURE_VERIFIED: Proof matches cryptographic issuer digest.");
                } else {
                    auditFindings.add("INVALID_SIGNATURE: Tamper detected in cryptographic proof signature.");
                }
            } catch (Exception e) {
                auditFindings.add("PROOF_CALCULATION_ERROR: " + e.getMessage());
            }
        } else {
            auditFindings.add("MISSING_PROOF: No cryptographic signature proof provided in VC.");
        }

        // 4. Hardware and Firmware Integrity check
        boolean firmwareMatched = false;
        if (didDocOpt.isPresent() && didDocOpt.get().getDevice() != null) {
            Device device = didDocOpt.get().getDevice();
            if (device.getIsQuarantined()) {
                auditFindings.add("DEVICE_QUARANTINED: Device is currently under active quarantine.");
            }
            if (device.getFirmwareHash() != null && device.getFirmwareHash().equalsIgnoreCase(subject.getFirmwareHash())) {
                firmwareMatched = true;
                auditFindings.add("FIRMWARE_ATTESTED: Device firmware hash matches attested credential claim (" + subject.getFirmwareHash() + ").");
            } else {
                auditFindings.add("FIRMWARE_MISMATCH: Device current firmware does NOT match credential firmware hash!");
            }
        }

        boolean overallValid = !isExpired && !isRevoked && signatureValid && firmwareMatched;
        String summary = overallValid
                ? "Zero Trust VC Attestation Successful: All cryptographic, firmware, and lifetime checks passed."
                : "Zero Trust VC Attestation Failed: " + String.join(" | ", auditFindings);

        return VcVerifyResponse.builder()
                .valid(overallValid)
                .subjectDid(subjectDid)
                .issuerDid(issuerDid)
                .credentialType(credentialType)
                .trustTier(subject.getTrustTier())
                .capabilities(subject.getAllowedCapabilities())
                .isExpired(isExpired)
                .isRevoked(isRevoked)
                .signatureValid(signatureValid)
                .firmwareIntegrityMatched(firmwareMatched)
                .verificationSummary(summary)
                .checkedAt(now)
                .build();
    }
}
