package com.zerotrust.iot.repository;

import com.zerotrust.iot.entity.BlockchainTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, UUID> {
    Optional<BlockchainTransaction> findByTxHash(String txHash);
    List<BlockchainTransaction> findTop20ByOrderByMinedAtDesc();
    Page<BlockchainTransaction> findAllByOrderByMinedAtDesc(Pageable pageable);
    long countByStatus(String status);
}
