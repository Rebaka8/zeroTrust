// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

interface IDeviceRegistry {
    function isDeviceOperational(string calldata didUri) external view returns (bool);
}

interface IDIDRegistry {
    function isDIDActive(string calldata didUri) external view returns (bool);
}

/**
 * @title AccessControlManager
 * @dev On-chain Zero Trust Attribute-Based Access Control (ABAC) engine.
 * Validates dynamic trust thresholds and role policies before critical IoT actuation.
 */
contract AccessControlManager is AccessControl, Pausable, ReentrancyGuard {

    bytes32 public constant OPERATOR_ROLE = keccak256("OPERATOR_ROLE");
    bytes32 public constant ORACLE_ROLE = keccak256("ORACLE_ROLE");

    struct Policy {
        bytes32 policyId;             // Unique Policy Identifier
        string policyName;            // Human-readable policy name
        uint256 minimumTrustScore;    // Min trust score required (0-100)
        string requiredCredentialType;// e.g., "IndustrialGatewayAuthorization"
        string resourcePattern;       // e.g., "iot/grid/substation-1/actuator"
        string allowedAction;         // "READ", "WRITE", "EXECUTE"
        bool isActive;                // Active flag
    }

    IDeviceRegistry public deviceRegistry;
    IDIDRegistry public didRegistry;

    // Mapping from policyId to Policy definition
    mapping(bytes32 => Policy) private _policies;
    bytes32[] private _allPolicyIds;

    // Events
    event PolicyCreated(
        bytes32 indexed policyId,
        string policyName,
        uint256 minimumTrustScore,
        string resourcePattern,
        string allowedAction
    );

    event PolicyUpdated(
        bytes32 indexed policyId,
        uint256 minimumTrustScore,
        bool isActive
    );

    event AccessEvaluated(
        string indexed didUri,
        bytes32 indexed policyId,
        uint256 currentTrustScore,
        bool granted,
        string reason,
        uint256 timestamp
    );

    // Custom Errors
    error PolicyAlreadyExists(bytes32 policyId);
    error PolicyNotFound(bytes32 policyId);
    error PolicyInactive(bytes32 policyId);
    error DeviceNotOperational(string didUri);
    error DIDNotActive(string didUri);
    error InsufficientTrustScore(uint256 current, uint256 required);
    error InvalidConfiguration();

    constructor(address _deviceRegistry, address _didRegistry) {
        if (_deviceRegistry == address(0) || _didRegistry == address(0)) {
            revert InvalidConfiguration();
        }
        _grantRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _grantRole(OPERATOR_ROLE, msg.sender);
        _grantRole(ORACLE_ROLE, msg.sender);

        deviceRegistry = IDeviceRegistry(_deviceRegistry);
        didRegistry = IDIDRegistry(_didRegistry);
    }

    /**
     * @notice Create a new Zero Trust access policy
     */
    function createPolicy(
        string calldata policyName,
        uint256 minimumTrustScore,
        string calldata requiredCredentialType,
        string calldata resourcePattern,
        string calldata allowedAction
    ) external onlyRole(OPERATOR_ROLE) returns (bytes32) {
        if (minimumTrustScore > 100) revert InvalidConfiguration();

        bytes32 policyId = keccak256(abi.encodePacked(policyName, resourcePattern, allowedAction, block.timestamp));
        if (_policies[policyId].isActive) revert PolicyAlreadyExists(policyId);

        _policies[policyId] = Policy({
            policyId: policyId,
            policyName: policyName,
            minimumTrustScore: minimumTrustScore,
            requiredCredentialType: requiredCredentialType,
            resourcePattern: resourcePattern,
            allowedAction: allowedAction,
            isActive: true
        });

        _allPolicyIds.push(policyId);

        emit PolicyCreated(policyId, policyName, minimumTrustScore, resourcePattern, allowedAction);
        return policyId;
    }

    /**
     * @notice Update an existing policy's threshold or status
     */
    function updatePolicy(
        bytes32 policyId,
        uint256 minimumTrustScore,
        bool isActive
    ) external onlyRole(OPERATOR_ROLE) {
        if (_policies[policyId].policyId == bytes32(0)) revert PolicyNotFound(policyId);
        if (minimumTrustScore > 100) revert InvalidConfiguration();

        _policies[policyId].minimumTrustScore = minimumTrustScore;
        _policies[policyId].isActive = isActive;

        emit PolicyUpdated(policyId, minimumTrustScore, isActive);
    }

    /**
     * @notice Evaluate an access request on-chain according to Zero Trust principles
     * @dev Checks: (1) DID active, (2) Device operational, (3) Trust score >= threshold
     */
    function evaluateAccess(
        string calldata didUri,
        bytes32 policyId,
        uint256 currentTrustScore
    ) external onlyRole(ORACLE_ROLE) returns (bool granted, string memory reason) {
        if (_policies[policyId].policyId == bytes32(0)) revert PolicyNotFound(policyId);
        Policy memory policy = _policies[policyId];

        if (!policy.isActive) {
            emit AccessEvaluated(didUri, policyId, currentTrustScore, false, "Policy Inactive", block.timestamp);
            return (false, "Policy is currently inactive");
        }

        if (!didRegistry.isDIDActive(didUri)) {
            emit AccessEvaluated(didUri, policyId, currentTrustScore, false, "DID Inactive or Revoked", block.timestamp);
            return (false, "Device DID is inactive or revoked");
        }

        if (!deviceRegistry.isDeviceOperational(didUri)) {
            emit AccessEvaluated(didUri, policyId, currentTrustScore, false, "Device Quarantined or Suspended", block.timestamp);
            return (false, "Device is quarantined or suspended");
        }

        if (currentTrustScore < policy.minimumTrustScore) {
            emit AccessEvaluated(didUri, policyId, currentTrustScore, false, "Insufficient Trust Score", block.timestamp);
            return (false, "Trust score falls below required threshold");
        }

        emit AccessEvaluated(didUri, policyId, currentTrustScore, true, "Zero Trust Verification Passed", block.timestamp);
        return (true, "Access Granted under Zero Trust Policy");
    }

    /**
     * @notice Retrieve policy details
     */
    function getPolicy(bytes32 policyId) external view returns (Policy memory) {
        if (_policies[policyId].policyId == bytes32(0)) revert PolicyNotFound(policyId);
        return _policies[policyId];
    }

    /**
     * @notice Get total policies count
     */
    function getTotalPolicies() external view returns (uint256) {
        return _allPolicyIds.length;
    }

    /**
     * @notice Set registry interface addresses (for upgrades)
     */
    function setRegistries(address _deviceRegistry, address _didRegistry) external onlyRole(DEFAULT_ADMIN_ROLE) {
        if (_deviceRegistry == address(0) || _didRegistry == address(0)) revert InvalidConfiguration();
        deviceRegistry = IDeviceRegistry(_deviceRegistry);
        didRegistry = IDIDRegistry(_didRegistry);
    }
}
