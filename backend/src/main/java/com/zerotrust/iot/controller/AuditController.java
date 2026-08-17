package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.audit.AuditLogResponse;
import com.zerotrust.iot.dto.audit.BlockchainTxResponse;
import com.zerotrust.iot.service.AuditService;
import com.zerotrust.iot.service.BlockchainSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Trail & Blockchain Ledger", description = "Endpoints for tamper-evident audit logs and on-chain transaction records")
public class AuditController {

    private final AuditService auditService;
    private final BlockchainSyncService blockchainSyncService;

    public AuditController(AuditService auditService, BlockchainSyncService blockchainSyncService) {
        this.auditService = auditService;
        this.blockchainSyncService = blockchainSyncService;
    }

    @GetMapping("/logs")
    @Operation(summary = "Retrieve paginated list of tamper-evident system and operator audit logs")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getAuditLogsPaged(pageable));
    }

    @GetMapping("/logs/recent")
    @Operation(summary = "Retrieve top 20 recent audit logs")
    public ResponseEntity<List<AuditLogResponse>> getRecentAuditLogs() {
        return ResponseEntity.ok(auditService.getRecentAuditLogs());
    }

    @GetMapping("/transactions")
    @Operation(summary = "Retrieve paginated list of anchored blockchain transactions")
    public ResponseEntity<Page<BlockchainTxResponse>> getBlockchainTransactions(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(blockchainSyncService.getTransactionsPaged(pageable));
    }

    @GetMapping("/block-number")
    @Operation(summary = "Retrieve latest Ethereum block number from Web3j client")
    public ResponseEntity<Map<String, Long>> getLatestBlockNumber() {
        Long blockNumber = blockchainSyncService.getLatestBlockNumber();
        return ResponseEntity.ok(Map.of("latestBlockNumber", blockNumber));
    }
}
