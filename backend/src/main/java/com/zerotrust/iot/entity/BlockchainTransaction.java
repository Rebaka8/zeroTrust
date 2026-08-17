package com.zerotrust.iot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blockchain_transactions")
public class BlockchainTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tx_hash", nullable = false, unique = true, length = 66)
    private String txHash;

    @Column(name = "contract_name", nullable = false, length = 50)
    private String contractName;

    @Column(name = "function_name", nullable = false, length = 50)
    private String functionName;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "from_address", nullable = false, length = 66)
    private String fromAddress;

    @Column(name = "to_address", nullable = false, length = 66)
    private String toAddress;

    @Column(name = "gas_used", precision = 20, scale = 0)
    private BigDecimal gasUsed;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, MINED, FAILED

    @CreationTimestamp
    @Column(name = "mined_at", nullable = false, updatable = false)
    private Instant minedAt;

    public BlockchainTransaction() {}

    public BlockchainTransaction(UUID id, String txHash, String contractName, String functionName, Long blockNumber, String fromAddress, String toAddress, BigDecimal gasUsed, String status, Instant minedAt) {
        this.id = id;
        this.txHash = txHash;
        this.contractName = contractName;
        this.functionName = functionName;
        this.blockNumber = blockNumber;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.gasUsed = gasUsed;
        this.status = status;
        this.minedAt = minedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String txHash;
        private String contractName;
        private String functionName;
        private Long blockNumber;
        private String fromAddress;
        private String toAddress;
        private BigDecimal gasUsed;
        private String status;
        private Instant minedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder txHash(String txHash) { this.txHash = txHash; return this; }
        public Builder contractName(String contractName) { this.contractName = contractName; return this; }
        public Builder functionName(String functionName) { this.functionName = functionName; return this; }
        public Builder blockNumber(Long blockNumber) { this.blockNumber = blockNumber; return this; }
        public Builder fromAddress(String fromAddress) { this.fromAddress = fromAddress; return this; }
        public Builder toAddress(String toAddress) { this.toAddress = toAddress; return this; }
        public Builder gasUsed(BigDecimal gasUsed) { this.gasUsed = gasUsed; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder minedAt(Instant minedAt) { this.minedAt = minedAt; return this; }

        public BlockchainTransaction build() {
            return new BlockchainTransaction(id, txHash, contractName, functionName, blockNumber, fromAddress, toAddress, gasUsed, status, minedAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }

    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public BigDecimal getGasUsed() { return gasUsed; }
    public void setGasUsed(BigDecimal gasUsed) { this.gasUsed = gasUsed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getMinedAt() { return minedAt; }
    public void setMinedAt(Instant minedAt) { this.minedAt = minedAt; }
}
