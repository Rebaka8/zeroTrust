package com.zerotrust.iot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerotrust.iot.did.VerificationMethod;
import com.zerotrust.iot.did.W3CDidDocument;
import com.zerotrust.iot.dto.did.DidCreateRequest;
import com.zerotrust.iot.dto.did.DidResponse;
import com.zerotrust.iot.dto.did.DidRotateKeyRequest;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.exception.BadRequestException;
import com.zerotrust.iot.exception.ResourceNotFoundException;
import com.zerotrust.iot.repository.DeviceRepository;
import com.zerotrust.iot.repository.DidDocumentRepository;
import com.zerotrust.iot.security.UserPrincipal;
import com.zerotrust.iot.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DidService {

    private static final Logger log = LoggerFactory.getLogger(DidService.class);

    private final DidDocumentRepository didDocumentRepository;
    private final DeviceRepository deviceRepository;
    private final IpfsService ipfsService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public DidService(
            DidDocumentRepository didDocumentRepository,
            DeviceRepository deviceRepository,
            IpfsService ipfsService,
            AuditService auditService,
            ObjectMapper objectMapper
    ) {
        this.didDocumentRepository = didDocumentRepository;
        this.deviceRepository = deviceRepository;
        this.ipfsService = ipfsService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DidResponse createDid(DidCreateRequest request, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + request.getDeviceId()));

        if (didDocumentRepository.findByDeviceId(request.getDeviceId()).isPresent()) {
            throw new BadRequestException("Device already has an active DID Document!");
        }

        String didSuffix = CryptoUtils.sha256(device.getId().toString() + device.getMacAddress()).substring(0, 20).toUpperCase();
        String didUri = "did:zt:dev:0x" + didSuffix;

        VerificationMethod vm = VerificationMethod.builder()
                .id(didUri + "#key-1")
                .type(request.getKeyType() != null ? request.getKeyType() : "Ed25519VerificationKey2020")
                .controller(didUri)
                .publicKeyMultibase(request.getPublicKeyMultibase())
                .build();

        W3CDidDocument w3cDoc = W3CDidDocument.builder()
                .id(didUri)
                .verificationMethod(List.of(vm))
                .authentication(List.of(didUri + "#key-1"))
                .assertionMethod(List.of(didUri + "#key-1"))
                .capabilityInvocation(List.of(didUri + "#key-1"))
                .deactivated(false)
                .build();

        String docJson;
        try {
            docJson = objectMapper.writeValueAsString(w3cDoc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize DID Document: " + e.getMessage());
        }

        // Pin to IPFS
        String ipfsCid = ipfsService.pinJson(docJson);
        w3cDoc.setMetadataCid(ipfsCid);

        try {
            docJson = objectMapper.writeValueAsString(w3cDoc);
        } catch (Exception ignored) {}

        DidDocument didDoc = DidDocument.builder()
                .device(device)
                .didUri(didUri)
                .documentJson(docJson)
                .publicKeyMultibase(request.getPublicKeyMultibase())
                .metadataCid(ipfsCid)
                .isRevoked(false)
                .build();

        DidDocument saved = didDocumentRepository.save(didDoc);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "DID_CREATED",
                "DidDocument",
                saved.getId(),
                "Created W3C DID: " + didUri + " pinned to IPFS CID: " + ipfsCid
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public DidResponse getDidByUri(String didUri) {
        DidDocument entity = didDocumentRepository.findByDidUri(didUri)
                .orElseThrow(() -> new ResourceNotFoundException("DID not found: " + didUri));
        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<DidResponse> getAllDids(Pageable pageable) {
        return didDocumentRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public DidResponse rotateKey(String didUri, DidRotateKeyRequest request, UserPrincipal currentUser) {
        DidDocument entity = didDocumentRepository.findByDidUri(didUri)
                .orElseThrow(() -> new ResourceNotFoundException("DID not found: " + didUri));

        if (entity.getIsRevoked()) {
            throw new BadRequestException("Cannot rotate key for revoked DID: " + didUri);
        }

        entity.setPublicKeyMultibase(request.getNewPublicKeyMultibase());

        VerificationMethod vm = VerificationMethod.builder()
                .id(didUri + "#key-rotated-" + System.currentTimeMillis())
                .type("Ed25519VerificationKey2020")
                .controller(didUri)
                .publicKeyMultibase(request.getNewPublicKeyMultibase())
                .build();

        W3CDidDocument w3cDoc = W3CDidDocument.builder()
                .id(didUri)
                .verificationMethod(List.of(vm))
                .authentication(List.of(vm.getId()))
                .assertionMethod(List.of(vm.getId()))
                .capabilityInvocation(List.of(vm.getId()))
                .deactivated(false)
                .build();

        try {
            String updatedJson = objectMapper.writeValueAsString(w3cDoc);
            String newCid = ipfsService.pinJson(updatedJson);
            w3cDoc.setMetadataCid(newCid);
            entity.setMetadataCid(newCid);
            entity.setDocumentJson(objectMapper.writeValueAsString(w3cDoc));
        } catch (Exception e) {
            log.error("Failed to re-serialize rotated DID Document: {}", e.getMessage());
        }

        DidDocument updated = didDocumentRepository.save(entity);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "DID_KEY_ROTATED",
                "DidDocument",
                updated.getId(),
                "Rotated cryptographic public key for DID: " + didUri
        );

        return mapToResponse(updated);
    }

    @Transactional
    public DidResponse revokeDid(String didUri, String reason, UserPrincipal currentUser) {
        DidDocument entity = didDocumentRepository.findByDidUri(didUri)
                .orElseThrow(() -> new ResourceNotFoundException("DID not found: " + didUri));

        entity.setIsRevoked(true);
        DidDocument updated = didDocumentRepository.save(entity);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "DID_REVOKED",
                "DidDocument",
                updated.getId(),
                "Revoked DID: " + didUri + ". Reason: " + reason
        );

        return mapToResponse(updated);
    }

    private DidResponse mapToResponse(DidDocument entity) {
        return DidResponse.builder()
                .id(entity.getId())
                .deviceId(entity.getDevice() != null ? entity.getDevice().getId() : null)
                .deviceName(entity.getDevice() != null ? entity.getDevice().getDeviceName() : "System")
                .didUri(entity.getDidUri())
                .publicKeyMultibase(entity.getPublicKeyMultibase())
                .metadataCid(entity.getMetadataCid())
                .onChainTxHash(entity.getOnChainTxHash())
                .isRevoked(entity.getIsRevoked())
                .documentJson(entity.getDocumentJson())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
