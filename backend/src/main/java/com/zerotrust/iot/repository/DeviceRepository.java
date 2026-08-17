package com.zerotrust.iot.repository;

import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.enums.DeviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findByMacAddress(String macAddress);
    boolean existsByMacAddress(String macAddress);

    List<Device> findByStatus(DeviceStatus status);
    List<Device> findByOwnerId(UUID ownerId);
    Page<Device> findAll(Pageable pageable);

    long countByStatus(DeviceStatus status);
    long countByIsQuarantinedTrue();

    @Query("SELECT AVG(d.currentTrustScore) FROM Device d WHERE d.status != 'DECOMMISSIONED'")
    Double calculateAverageTrustScore();

    @Query("SELECT d FROM Device d WHERE d.isQuarantined = true")
    List<Device> findAllQuarantinedDevices();
}
