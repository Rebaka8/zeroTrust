// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

/**
 * @title ImmutableAuditLog
 * @dev Append-only tamper-proof on-chain audit ledger for Zero Trust IoT security events.
 */
contract ImmutableAuditLog is Ownable, ReentrancyGuard {

    enum EventSeverity { INFO, LOW, MEDIUM, HIGH, CRITICAL }

    struct AuditEntry {
        uint256 logId;
        string didUri;
        string eventType;         // e.g., "REPLAY_ATTACK_BLOCKED", "DEVICE_QUARANTINED", "POLICY_EVALUATION"
        EventSeverity severity;
        string detailsCid;        // IPFS CID of comprehensive event payload & proof
        bytes32 payloadHash;      // Cryptographic SHA-256 digest of the event payload
        address reporter;         // Address of reporting oracle / node
        uint256 timestamp;
    }

    // List of all audit entries
    AuditEntry[] private _auditLogs;

    // Authorized loggers mapping (e.g. Backend PDP oracle, Smart Contracts)
    mapping(address => bool) public authorizedLoggers;

    // Events
    event SecurityEventLogged(
        uint256 indexed logId,
        string indexed didUri,
        string eventType,
        EventSeverity severity,
        bytes32 payloadHash,
        address indexed reporter,
        uint256 timestamp
    );

    event BatchAuditCommitted(
        uint256 startLogId,
        uint256 count,
        bytes32 merkleRoot,
        uint256 timestamp
    );

    event LoggerAuthorizationChanged(address indexed logger, bool authorized);

    // Custom Errors
    error UnauthorizedLogger(address caller);
    error InvalidParameters();

    modifier onlyAuthorizedLogger() {
        if (msg.sender != owner() && !authorizedLoggers[msg.sender]) {
            revert UnauthorizedLogger(msg.sender);
        }
        _;
    }

    constructor() Ownable(msg.sender) {
        authorizedLoggers[msg.sender] = true;
    }

    /**
     * @notice Set authorization for logging entities
     */
    function setLoggerAuthorization(address logger, bool authorized) external onlyOwner {
        if (logger == address(0)) revert InvalidParameters();
        authorizedLoggers[logger] = authorized;
        emit LoggerAuthorizationChanged(logger, authorized);
    }

    /**
     * @notice Record a single security or access audit event
     */
    function logEvent(
        string calldata didUri,
        string calldata eventType,
        EventSeverity severity,
        string calldata detailsCid,
        bytes32 payloadHash
    ) external onlyAuthorizedLogger returns (uint256) {
        if (bytes(eventType).length == 0 || payloadHash == bytes32(0)) {
            revert InvalidParameters();
        }

        uint256 logId = _auditLogs.length;

        _auditLogs.push(AuditEntry({
            logId: logId,
            didUri: didUri,
            eventType: eventType,
            severity: severity,
            detailsCid: detailsCid,
            payloadHash: payloadHash,
            reporter: msg.sender,
            timestamp: block.timestamp
        }));

        emit SecurityEventLogged(logId, didUri, eventType, severity, payloadHash, msg.sender, block.timestamp);
        return logId;
    }

    /**
     * @notice Commit a batch audit Merkle root for gas-optimized telemetry logging
     */
    function commitBatchAudit(
        uint256 startLogId,
        uint256 count,
        bytes32 merkleRoot
    ) external onlyAuthorizedLogger {
        if (count == 0 || merkleRoot == bytes32(0)) revert InvalidParameters();
        emit BatchAuditCommitted(startLogId, count, merkleRoot, block.timestamp);
    }

    /**
     * @notice Get audit log entry by ID
     */
    function getAuditEntry(uint256 logId) external view returns (AuditEntry memory) {
        if (logId >= _auditLogs.length) revert InvalidParameters();
        return _auditLogs[logId];
    }

    /**
     * @notice Get total audit entries count
     */
    function getTotalLogs() external view returns (uint256) {
        return _auditLogs.length;
    }

    /**
     * @notice Get the most recent N logs
     */
    function getRecentLogs(uint256 count) external view returns (AuditEntry[] memory) {
        uint256 total = _auditLogs.length;
        if (count > total) {
            count = total;
        }

        AuditEntry[] memory recent = new AuditEntry[](count);
        for (uint256 i = 0; i < count; i++) {
            recent[i] = _auditLogs[total - 1 - i];
        }
        return recent;
    }
}
