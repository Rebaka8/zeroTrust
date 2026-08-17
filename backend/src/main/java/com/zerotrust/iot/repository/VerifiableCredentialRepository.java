package com.zerotrust.iot.repository;

import com.zerotrust.iot.entity.VerifiableCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerifiableCredentialRepository extends JpaRepository<VerifiableCredential, UUID> {
    List<VerifiableCredential> findByDeviceId(UUID deviceId);
    List<VerifiableCredential> findBySubjectDid(String subjectDid);
    List<VerifiableCredential> findByIsRevokedFalse();
    Optional<VerifiableCredential> findFirstBySubjectDidAndIsRevokedFalse(String subjectDid);
}
