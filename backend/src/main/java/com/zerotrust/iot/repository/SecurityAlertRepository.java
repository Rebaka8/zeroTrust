package com.zerotrust.iot.repository;

import com.zerotrust.iot.entity.SecurityAlert;
import com.zerotrust.iot.entity.enums.AlertSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, UUID> {
    List<SecurityAlert> findTop20ByOrderByTriggeredAtDesc();
    List<SecurityAlert> findByIsResolvedFalseOrderByTriggeredAtDesc();
    List<SecurityAlert> findByDeviceIdOrderByTriggeredAtDesc(UUID deviceId);
    Page<SecurityAlert> findAllByOrderByTriggeredAtDesc(Pageable pageable);
    long countByIsResolvedFalse();
    long countBySeverityAndIsResolvedFalse(AlertSeverity severity);
    long countByDeviceIdAndIsResolvedFalse(UUID deviceId);
}
