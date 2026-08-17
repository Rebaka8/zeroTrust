package com.zerotrust.iot.repository;

import com.zerotrust.iot.entity.AccessRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, UUID> {
    List<AccessRequest> findTop20ByOrderByRequestedAtDesc();
    Page<AccessRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);
    List<AccessRequest> findByDeviceIdOrderByRequestedAtDesc(UUID deviceId);
}
