package com.zerotrust.iot.service;

import com.zerotrust.iot.dto.audit.BlockchainTxResponse;
import com.zerotrust.iot.entity.BlockchainTransaction;
import com.zerotrust.iot.repository.BlockchainTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlockchainSyncService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainSyncService.class);

    private final Web3j web3j;
    private final BlockchainTransactionRepository txRepository;

    @Value("${blockchain.contracts.did-registry}")
    private String didRegistryAddress;

    @Value("${blockchain.contracts.device-registry}")
    private String deviceRegistryAddress;

    @Value("${blockchain.contracts.access-control}")
    private String accessControlAddress;

    @Value("${blockchain.contracts.audit-log}")
    private String auditLogAddress;

    public BlockchainSyncService(Web3j web3j, BlockchainTransactionRepository txRepository) {
        this.web3j = web3j;
        this.txRepository = txRepository;
    }

    public Long getLatestBlockNumber() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber().longValue();
        } catch (Exception e) {
            log.warn("Failed to fetch latest block number from Web3j: {}", e.getMessage());
            return 0L;
        }
    }

    @Transactional
    public BlockchainTransaction recordTransaction(
            String txHash,
            String contractName,
            String functionName,
            String fromAddress,
            String toAddress,
            Long blockNumber,
            BigDecimal gasUsed,
            String status
    ) {
        BlockchainTransaction tx = BlockchainTransaction.builder()
                .txHash(txHash)
                .contractName(contractName)
                .functionName(functionName)
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .blockNumber(blockNumber)
                .gasUsed(gasUsed)
                .status(status)
                .minedAt(Instant.now())
                .build();

        return txRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public List<BlockchainTxResponse> getRecentTransactions() {
        return txRepository.findTop20ByOrderByMinedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BlockchainTxResponse> getTransactionsPaged(Pageable pageable) {
        return txRepository.findAllByOrderByMinedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    private BlockchainTxResponse mapToResponse(BlockchainTransaction tx) {
        return BlockchainTxResponse.builder()
                .id(tx.getId())
                .txHash(tx.getTxHash())
                .contractName(tx.getContractName())
                .functionName(tx.getFunctionName())
                .blockNumber(tx.getBlockNumber())
                .fromAddress(tx.getFromAddress())
                .toAddress(tx.getToAddress())
                .gasUsed(tx.getGasUsed())
                .status(tx.getStatus())
                .minedAt(tx.getMinedAt())
                .build();
    }
}
