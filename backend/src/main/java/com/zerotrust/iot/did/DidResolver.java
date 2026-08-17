package com.zerotrust.iot.did;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.exception.ResourceNotFoundException;
import com.zerotrust.iot.repository.DidDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DidResolver {

    private static final Logger log = LoggerFactory.getLogger(DidResolver.class);

    private final DidDocumentRepository didDocumentRepository;
    private final ObjectMapper objectMapper;

    public DidResolver(DidDocumentRepository didDocumentRepository, ObjectMapper objectMapper) {
        this.didDocumentRepository = didDocumentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Resolves a W3C Decentralized Identifier (DID) to its corresponding DID Document.
     */
    public W3CDidDocument resolve(String didUri) {
        if (didUri == null || !didUri.startsWith("did:")) {
            throw new IllegalArgumentException("Invalid DID URI scheme: " + didUri);
        }

        DidDocument entity = didDocumentRepository.findByDidUri(didUri)
                .orElseThrow(() -> new ResourceNotFoundException("DID Document not found for: " + didUri));

        try {
            if (entity.getDocumentJson() != null && !entity.getDocumentJson().isBlank()) {
                return objectMapper.readValue(entity.getDocumentJson(), W3CDidDocument.class);
            }
        } catch (Exception e) {
            log.warn("Failed to parse stored DID document JSON for {}, rebuilding dynamically: {}", didUri, e.getMessage());
        }

        // Dynamically build standard W3C DID document if raw JSON needs refresh
        VerificationMethod vm = VerificationMethod.builder()
                .id(didUri + "#key-1")
                .type("Ed25519VerificationKey2020")
                .controller(didUri)
                .publicKeyMultibase(entity.getPublicKeyMultibase())
                .build();

        return W3CDidDocument.builder()
                .id(didUri)
                .verificationMethod(List.of(vm))
                .authentication(List.of(didUri + "#key-1"))
                .assertionMethod(List.of(didUri + "#key-1"))
                .capabilityInvocation(List.of(didUri + "#key-1"))
                .metadataCid(entity.getMetadataCid())
                .deactivated(entity.getIsRevoked())
                .build();
    }
}
