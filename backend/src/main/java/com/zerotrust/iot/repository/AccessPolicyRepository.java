package com.zerotrust.iot.repository;

import com.zerotrust.iot.entity.AccessPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessPolicyRepository extends JpaRepository<AccessPolicy, UUID> {
    Optional<AccessPolicy> findByPolicyName(String policyName);
    List<AccessPolicy> findByIsActiveTrue();
}
