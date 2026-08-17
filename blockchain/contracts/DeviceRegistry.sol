// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

/**
 * @title DeviceRegistry
 * @dev On-chain IoT device lifecycle, hardware attestation, and firmware integrity registry.
 */
contract DeviceRegistry is Ownable, Pausable, ReentrancyGuard {

    enum DeviceStatus {
        PROVISIONED,    // 0: Registered but awaiting initial attestation
        ACTIVE,         // 1: Normal operational state
        SUSPENDED,      // 2: Temporarily paused by operator
        QUARANTINED,    // 3: Isolated due to Zero Trust policy violation or anomaly
        DECOMMISSIONED  // 4: Permanently retired
    }

    struct Device {
        string didUri;            // Associated W3C DID URI
        string deviceName;        // Human-readable identifier
        string hardwareModel;     // Manufacturer hardware model
        string macAddress;        // Physical MAC address
        bytes32 firmwareHash;     // SHA-256 Digest of approved firmware
        address owner;            // Owner / Gateway controller address
        DeviceStatus status;      // Current lifecycle status
        uint256 registeredAt;     // Timestamp of on-chain registration
        uint256 lastAttestedAt;   // Timestamp of last successful hardware attestation
        string metadataCid;       // IPFS CID of extended telemetry & device profile
    }

    // Mapping from DID URI to Device struct
    mapping(string => Device) private _devices;
    
    // Mapping from DID URI to existence flag
    mapping(string => bool) private _deviceExists;

    // Mapping from MAC address hash to DID URI (enforces unique physical MACs)
    mapping(bytes32 => string) private _macToDid;

    // List of all registered DID URIs
    string[] private _allDeviceDids;

    // Authorized Oracles / Zero Trust Policy Decision Point (PDP) backends
    mapping(address => bool) public authorizedOperators;

    // Events
    event DeviceRegistered(
        string indexed didUri,
        address indexed owner,
        string deviceName,
        string macAddress,
        bytes32 firmwareHash,
        uint256 timestamp
    );

    event FirmwareUpdated(
        string indexed didUri,
        bytes32 oldFirmwareHash,
        bytes32 newFirmwareHash,
        uint256 timestamp
    );

    event DeviceStatusChanged(
        string indexed didUri,
        DeviceStatus previousStatus,
        DeviceStatus newStatus,
        uint256 timestamp,
        string reason
    );

    event DeviceQuarantined(
        string indexed didUri,
        uint256 trustScore,
        string reason,
        uint256 timestamp
    );

    event DeviceAttested(
        string indexed didUri,
        bytes32 verifiedFirmwareHash,
        uint256 timestamp
    );

    event OperatorAuthorizationChanged(address indexed operator, bool authorized);

    // Custom Errors
    error DeviceAlreadyExists(string didUri);
    error DeviceNotFound(string didUri);
    error MacAlreadyRegistered(string macAddress);
    error UnauthorizedAccess(address caller);
    error InvalidDeviceStatus(DeviceStatus currentStatus);
    error InvalidInput();

    modifier onlyDeviceOwnerOrAdmin(string memory didUri) {
        if (!_deviceExists[didUri]) revert DeviceNotFound(didUri);
        if (_devices[didUri].owner != msg.sender && owner() != msg.sender && !authorizedOperators[msg.sender]) {
            revert UnauthorizedAccess(msg.sender);
        }
        _;
    }

    modifier onlyAuthorizedOperator() {
        if (msg.sender != owner() && !authorizedOperators[msg.sender]) {
            revert UnauthorizedAccess(msg.sender);
        }
        _;
    }

    constructor() Ownable(msg.sender) {
        authorizedOperators[msg.sender] = true;
    }

    /**
     * @notice Set operator authorization (for Spring Boot PDP / Zero Trust Engine)
     */
    function setOperatorAuthorization(address operator, bool authorized) external onlyOwner {
        if (operator == address(0)) revert InvalidInput();
        authorizedOperators[operator] = authorized;
        emit OperatorAuthorizationChanged(operator, authorized);
    }

    /**
     * @notice Register a new IoT device
     */
    function registerDevice(
        string calldata didUri,
        string calldata deviceName,
        string calldata hardwareModel,
        string calldata macAddress,
        bytes32 firmwareHash,
        string calldata metadataCid
    ) external whenNotPaused nonReentrant {
        if (bytes(didUri).length == 0 || bytes(macAddress).length == 0 || firmwareHash == bytes32(0)) {
            revert InvalidInput();
        }
        if (_deviceExists[didUri]) revert DeviceAlreadyExists(didUri);

        bytes32 macHash = keccak256(abi.encodePacked(macAddress));
        if (bytes(_macToDid[macHash]).length != 0) revert MacAlreadyRegistered(macAddress);

        _devices[didUri] = Device({
            didUri: didUri,
            deviceName: deviceName,
            hardwareModel: hardwareModel,
            macAddress: macAddress,
            firmwareHash: firmwareHash,
            owner: msg.sender,
            status: DeviceStatus.ACTIVE,
            registeredAt: block.timestamp,
            lastAttestedAt: block.timestamp,
            metadataCid: metadataCid
        });

        _deviceExists[didUri] = true;
        _macToDid[macHash] = didUri;
        _allDeviceDids.push(didUri);

        emit DeviceRegistered(didUri, msg.sender, deviceName, macAddress, firmwareHash, block.timestamp);
    }

    /**
     * @notice Zero Trust Dynamic Quarantine Trigger (called automatically by PDP engine)
     */
    function quarantineDevice(
        string calldata didUri,
        uint256 trustScore,
        string calldata reason
    ) external onlyAuthorizedOperator {
        if (!_deviceExists[didUri]) revert DeviceNotFound(didUri);

        DeviceStatus prev = _devices[didUri].status;
        _devices[didUri].status = DeviceStatus.QUARANTINED;

        emit DeviceStatusChanged(didUri, prev, DeviceStatus.QUARANTINED, block.timestamp, reason);
        emit DeviceQuarantined(didUri, trustScore, reason, block.timestamp);
    }

    /**
     * @notice Restore device from quarantine after verified re-attestation
     */
    function restoreDevice(
        string calldata didUri,
        string calldata reason
    ) external onlyAuthorizedOperator {
        if (!_deviceExists[didUri]) revert DeviceNotFound(didUri);

        DeviceStatus prev = _devices[didUri].status;
        _devices[didUri].status = DeviceStatus.ACTIVE;
        _devices[didUri].lastAttestedAt = block.timestamp;

        emit DeviceStatusChanged(didUri, prev, DeviceStatus.ACTIVE, block.timestamp, reason);
    }

    /**
     * @notice Update firmware hash following verified FOTA (Firmware Over-The-Air) update
     */
    function updateFirmwareHash(
        string calldata didUri,
        bytes32 newFirmwareHash
    ) external onlyDeviceOwnerOrAdmin(didUri) {
        if (newFirmwareHash == bytes32(0)) revert InvalidInput();

        bytes32 oldHash = _devices[didUri].firmwareHash;
        _devices[didUri].firmwareHash = newFirmwareHash;
        _devices[didUri].lastAttestedAt = block.timestamp;

        emit FirmwareUpdated(didUri, oldHash, newFirmwareHash, block.timestamp);
    }

    /**
     * @notice Record periodic hardware & cryptographic attestation
     */
    function recordAttestation(
        string calldata didUri,
        bytes32 reportedFirmwareHash
    ) external onlyAuthorizedOperator {
        if (!_deviceExists[didUri]) revert DeviceNotFound(didUri);
        if (_devices[didUri].firmwareHash != reportedFirmwareHash) {
            revert InvalidInput();
        }

        _devices[didUri].lastAttestedAt = block.timestamp;
        emit DeviceAttested(didUri, reportedFirmwareHash, block.timestamp);
    }

    /**
     * @notice Retrieve device specifications and status
     */
    function getDevice(string calldata didUri)
        external
        view
        returns (
            string memory deviceName,
            string memory hardwareModel,
            string memory macAddress,
            bytes32 firmwareHash,
            address owner,
            DeviceStatus status,
            uint256 registeredAt,
            uint256 lastAttestedAt,
            string memory metadataCid
        )
    {
        if (!_deviceExists[didUri]) revert DeviceNotFound(didUri);
        Device memory d = _devices[didUri];
        return (
            d.deviceName,
            d.hardwareModel,
            d.macAddress,
            d.firmwareHash,
            d.owner,
            d.status,
            d.registeredAt,
            d.lastAttestedAt,
            d.metadataCid
        );
    }

    /**
     * @notice Check if a device is active and trusted (not quarantined or suspended)
     */
    function isDeviceOperational(string calldata didUri) external view returns (bool) {
        return _deviceExists[didUri] && _devices[didUri].status == DeviceStatus.ACTIVE;
    }

    /**
     * @notice Get total registered devices count
     */
    function getTotalDevices() external view returns (uint256) {
        return _allDeviceDids.length;
    }

    /**
     * @notice Get all registered device DID URIs
     */
    function getAllDeviceDids() external view returns (string[] memory) {
        return _allDeviceDids;
    }
}
