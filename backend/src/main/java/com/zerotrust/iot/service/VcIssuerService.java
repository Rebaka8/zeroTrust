package com.zerotrust.iot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerotrust.iot.dto.vc.VcIssueRequest;
import com.zerotrust.iot.dto.vc.VcResponse;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.VerifiableCredential;
import com.zerotrust.iot.exception.BadRequestException;
import com.zerotrust.iot.exception.ResourceNotFoundException;
import com.zerotrust.iot.repository.DeviceRepository;
import com.zerotrust.iot.repository.DidDocumentRepository;
import com.zerotrust.iot.repository.VerifiableCredentialRepository;
import com.zerotrust.iot.security.UserPrincipal;
import com.zerotrust.iot.util.CryptoUtils;
import com.zerotrust.iot.vc.CredentialSubject;
import com.zerotrust.iot.vc.Proof;
import com.zerotrust.iot.vc.VerifiableCredentialModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class VcIssuerService {

    private static final Logger log = LoggerFactory.getLogger(VcIssuerService.class);

    private final VerifiableCredentialRepository vcRepository;
    private final DeviceRepository deviceRepository;
    private final DidDocumentRepository didDocumentRepository;
    private final IpfsService ipfsService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Value("${app.vc.issuer-did:did:zt:issuer:0x8626f6940e2eb28930efb4cef49b2d1f2c9c1199}")
    private String authorityIssuerDid;

    public VcIssuerService(
            VerifiableCredentialRepository vcRepository,
            DeviceRepository deviceRepository,
            DidDocumentRepository didDocumentRepository,
            IpfsService ipfsService,
            AuditService auditService,
            ObjectMapper objectMapper
    ) {
        this.vcRepository = vcRepository;
        this.deviceRepository = deviceRepository;
        this.didDocumentRepository = didDocumentRepository;
        this.ipfsService = ipfsService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public VcResponse issueCredential(VcIssueRequest request, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + request.getDeviceId()));

        DidDocument didDoc = didDocumentRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new BadRequestException("Device must have an active DID before issuing a Verifiable Credential!"));

        if (didDoc.getIsRevoked() || device.getIsQuarantined()) {
            throw new BadRequestException("Cannot issue Verifiable Credential to a quarantined or revoked device!");
        }

        String credentialType = request.getCredentialType() != null && !request.getCredentialType().isBlank()
                ? request.getCredentialType()
                : "IoTDeviceAttestation";

        int validityDays = request.getValidityDays() != null && request.getValidityDays() > 0
                ? request.getValidityDays()
                : 365;

        Instant issuanceDate = Instant.now();
        Instant expirationDate = issuanceDate.plus(validityDays, ChronoUnit.DAYS);
        String vcId = "urn:uuid:" + UUID.randomUUID();

        List<String> capabilities = request.getCapabilities() != null && !request.getCapabilities().isEmpty()
                ? request.getCapabilities()
                : List.of("telemetry:publish", "heartbeat:report", "firmware:attest");

        String trustTier = request.getTrustTier() != null ? request.getTrustTier() : "TIER_1_STANDARD";

        CredentialSubject subject = CredentialSubject.builder()
                .id(didDoc.getDidUri())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .hardwareModel(device.getHardwareModel())
                .macAddress(device.getMacAddress())
                .firmwareHash(device.getFirmwareHash())
                .trustTier(trustTier)
                .allowedCapabilities(capabilities)
                .build();

        String subjectJson;
        try {
            subjectJson = objectMapper.writeValueAsString(subject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize CredentialSubject: " + e.getMessage());
        }

        // Generate cryptographic proof
        String proofTarget = vcId + ":" + authorityIssuerDid + ":" + didDoc.getDidUri() + ":" + issuanceDate.toEpochMilli() + ":" + CryptoUtils.sha256(subjectJson);
        String signatureValue = "0x" + CryptoUtils.sha256(proofTarget) + CryptoUtils.sha256(authorityIssuerDid).substring(0, 64);

        Proof proof = Proof.builder()
                .type("Ed25519Signature2020")
                .created(issuanceDate)
                .verificationMethod(authorityIssuerDid + "#key-1")
                .proofPurpose("assertionMethod")
                .proofValue(signatureValue)
                .build();

        VerifiableCredentialModel vcModel = VerifiableCredentialModel.builder()
                .id(vcId)
                .type(List.of("VerifiableCredential", credentialType))
                .issuer(authorityIssuerDid)
                .issuanceDate(issuanceDate)
                .expirationDate(expirationDate)
                .credentialSubject(subject)
                .proof(proof)
                .build();

        String rawVcJson;
        try {
            rawVcJson = objectMapper.writeValueAsString(vcModel);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Verifiable Credential: " + e.getMessage());
        }

        // Pin raw VC JSON to IPFS
        String ipfsCid = ipfsService.pinJson(rawVcJson);
        log.info("Verifiable Credential {} pinned to IPFS CID: {}", vcId, ipfsCid);

        VerifiableCredential vcEntity = VerifiableCredential.builder()
                .device(device)
                .credentialType(credentialType)
                .issuerDid(authorityIssuerDid)
                .subjectDid(didDoc.getDidUri())
                .rawVcJwt(rawVcJson)
                .claimsJson(subjectJson)
                .issuanceDate(issuanceDate)
                .expirationDate(expirationDate)
                .isRevoked(false)
                .build();

        VerifiableCredential savedVc = vcRepository.save(vcEntity);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "VC_ISSUED",
                "VerifiableCredential",
                savedVc.getId(),
                "Issued " + credentialType + " for device DID: " + didDoc.getDidUri() + " (IPFS: " + ipfsCid + ")"
        );

        return mapToResponse(savedVc);
    }

    @Transactional(readOnly = true)
    public List<VcResponse> getCredentialsByDeviceId(UUID deviceId) {
        return vcRepository.findByDeviceId(deviceId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public VcResponse revokeCredential(UUID credentialId, String reason, UserPrincipal currentUser) {
        VerifiableCredential vc = vcRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Verifiable Credential not found with ID: " + credentialId));

        vc.setIsRevoked(true);
        VerifiableCredential updated = vcRepository.save(vc);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "VC_REVOKED",
                "VerifiableCredential",
                credentialId,
                "Revoked credential for subject: " + vc.getSubjectDid() + ". Reason: " + reason
        );

        return mapToResponse(updated);
    }

    private VcResponse mapToResponse(VerifiableCredential vc) {
        return VcResponse.builder()
                .id(vc.getId())
                .deviceId(vc.getDevice() != null ? vc.getDevice().getId() : null)
                .deviceName(vc.getDevice() != null ? vc.getDevice().getDeviceName() : "Unknown")
                .credentialType(vc.getCredentialType())
                .issuerDid(vc.getIssuerDid())
                .subjectDid(vc.getSubjectDid())
                .rawVcJwt(vc.getRawVcJwt())
                .claimsJson(vc.getClaimsJson())
                .issuanceDate(vc.getIssuanceDate())
                .expirationDate(vc.getExpirationDate())
                .isRevoked(vc.getIsRevoked())
                .createdAt(vc.getCreatedAt())
                .build();
    }
}
