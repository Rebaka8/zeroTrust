package com.zerotrust.iot.service;

import com.zerotrust.iot.dto.audit.AuditLogResponse;
import com.zerotrust.iot.entity.AuditLog;
import com.zerotrust.iot.entity.User;
import com.zerotrust.iot.repository.AuditLogRepository;
import com.zerotrust.iot.repository.UserRepository;
import com.zerotrust.iot.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuditLog logAction(
            UUID userId,
            String actionType,
            String targetEntity,
            UUID targetId,
            String details
    ) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        String previousHash = auditLogRepository.findTopByOrderByIdDesc()
                .map(AuditLog::getIntegrityHash)
                .orElse("0000000000000000000000000000000000000000000000000000000000000000");

        Instant now = Instant.now();
        String recordToHash = previousHash + ":" + (userId != null ? userId.toString() : "SYSTEM")
                + ":" + actionType + ":" + targetEntity + ":" + (targetId != null ? targetId.toString() : "")
                + ":" + now.toEpochMilli();

        String currentIntegrityHash = CryptoUtils.sha256(recordToHash);

        AuditLog logEntry = AuditLog.builder()
                .user(user)
                .actionType(actionType)
                .targetEntity(targetEntity)
                .targetId(targetId)
                .details(details)
                .integrityHash(currentIntegrityHash)
                .createdAt(now)
                .build();

        return auditLogRepository.save(logEntry);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentAuditLogs() {
        return auditLogRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsPaged(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .username(auditLog.getUser() != null ? auditLog.getUser().getUsername() : "SYSTEM")
                .actionType(auditLog.getActionType())
                .targetEntity(auditLog.getTargetEntity())
                .targetId(auditLog.getTargetId())
                .ipAddress(auditLog.getIpAddress())
                .details(auditLog.getDetails())
                .integrityHash(auditLog.getIntegrityHash())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
