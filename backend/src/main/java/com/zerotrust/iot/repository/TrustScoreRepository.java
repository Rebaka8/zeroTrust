package com.zerotrust.iot.repository;

import com.zerotrust.iot.entity.TrustScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrustScoreRepository extends JpaRepository<TrustScore, Long> {
    Optional<TrustScore> findTopByDeviceIdOrderByEvaluatedAtDesc(UUID deviceId);
    List<TrustScore> findTop30ByDeviceIdOrderByEvaluatedAtDesc(UUID deviceId);
    Page<TrustScore> findByDeviceIdOrderByEvaluatedAtDesc(UUID deviceId, Pageable pageable);
}
