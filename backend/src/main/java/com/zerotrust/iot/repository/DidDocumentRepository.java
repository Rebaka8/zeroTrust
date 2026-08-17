package com.zerotrust.iot.repository;

import com.zerotrust.iot.entity.DidDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DidDocumentRepository extends JpaRepository<DidDocument, UUID> {
    Optional<DidDocument> findByDidUri(String didUri);
    Optional<DidDocument> findByDeviceId(UUID deviceId);
    boolean existsByDidUri(String didUri);
}
