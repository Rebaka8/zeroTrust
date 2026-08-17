// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

/**
 * @title DIDRegistry
 * @dev On-chain decentralized identity document registry and cryptographic revocation anchor.
 * Conforms to W3C Decentralized Identifiers (DIDs) v1.0 standard principles.
 */
contract DIDRegistry is Ownable, Pausable, ReentrancyGuard {

    struct DIDRecord {
        string didUri;             // e.g., "did:zt:dev:0x1A4F98E..."
        address controller;        // Controller Ethereum address
        string documentCid;        // IPFS CID of full W3C JSON-LD DID Document
        string publicKeyMultibase; // Public key representation (e.g. Ed25519/Secp256k1 multibase)
        uint256 createdAt;         // Unix timestamp of registration
        uint256 updatedAt;         // Unix timestamp of last update
        bool isRevoked;            // Cryptographic revocation flag
    }

    // Mapping from DID URI string to DIDRecord
    mapping(string => DIDRecord) private _didRecords;
    
    // Mapping from DID URI string to existence check
    mapping(string => bool) private _didExists;

    // Mapping from controller address to array of controlled DID URIs
    mapping(address => string[]) private _controllerDids;

    // Total active registered DIDs count
    uint256 public totalRegisteredDIDs;

    // Events
    event DIDCreated(
        string indexed didUri,
        address indexed controller,
        string documentCid,
        string publicKeyMultibase,
        uint256 timestamp
    );

    event DIDUpdated(
        string indexed didUri,
        address indexed controller,
        string newDocumentCid,
        string newPublicKeyMultibase,
        uint256 timestamp
    );

    event DIDRevoked(
        string indexed didUri,
        address indexed revoker,
        uint256 timestamp,
        string reason
    );

    event DIDControllerTransferred(
        string indexed didUri,
        address indexed previousController,
        address indexed newController,
        uint256 timestamp
    );

    // Custom Errors
    error DIDAlreadyExists(string didUri);
    error DIDNotFound(string didUri);
    error UnauthorizedController(address caller, string didUri);
    error DIDIsRevoked(string didUri);
    error InvalidParameters();

    modifier onlyController(string memory didUri) {
        if (!_didExists[didUri]) revert DIDNotFound(didUri);
        DIDRecord memory record = _didRecords[didUri];
        if (record.controller != msg.sender && owner() != msg.sender) {
            revert UnauthorizedController(msg.sender, didUri);
        }
        _;
    }

    constructor() Ownable(msg.sender) {}

    /**
     * @notice Register a new W3C Decentralized Identifier (DID)
     * @param didUri Unique identifier string (e.g., did:zt:dev:0x123...)
     * @param documentCid IPFS CID storing the off-chain JSON-LD DID document
     * @param publicKeyMultibase Multibase-encoded public verification key
     */
    function registerDID(
        string calldata didUri,
        string calldata documentCid,
        string calldata publicKeyMultibase
    ) external whenNotPaused nonReentrant {
        if (bytes(didUri).length == 0 || bytes(publicKeyMultibase).length == 0) {
            revert InvalidParameters();
        }
        if (_didExists[didUri]) {
            revert DIDAlreadyExists(didUri);
        }

        _didRecords[didUri] = DIDRecord({
            didUri: didUri,
            controller: msg.sender,
            documentCid: documentCid,
            publicKeyMultibase: publicKeyMultibase,
            createdAt: block.timestamp,
            updatedAt: block.timestamp,
            isRevoked: false
        });

        _didExists[didUri] = true;
        _controllerDids[msg.sender].push(didUri);
        totalRegisteredDIDs++;

        emit DIDCreated(didUri, msg.sender, documentCid, publicKeyMultibase, block.timestamp);
    }

    /**
     * @notice Update an existing DID Document CID and/or Public Key
     */
    function updateDID(
        string calldata didUri,
        string calldata newDocumentCid,
        string calldata newPublicKeyMultibase
    ) external whenNotPaused onlyController(didUri) {
        if (_didRecords[didUri].isRevoked) {
            revert DIDIsRevoked(didUri);
        }
        if (bytes(newPublicKeyMultibase).length == 0) {
            revert InvalidParameters();
        }

        _didRecords[didUri].documentCid = newDocumentCid;
        _didRecords[didUri].publicKeyMultibase = newPublicKeyMultibase;
        _didRecords[didUri].updatedAt = block.timestamp;

        emit DIDUpdated(didUri, msg.sender, newDocumentCid, newPublicKeyMultibase, block.timestamp);
    }

    /**
     * @notice Revoke a DID permanently (Zero Trust emergency isolation or key compromise)
     */
    function revokeDID(
        string calldata didUri,
        string calldata reason
    ) external onlyController(didUri) {
        if (_didRecords[didUri].isRevoked) {
            revert DIDIsRevoked(didUri);
        }

        _didRecords[didUri].isRevoked = true;
        _didRecords[didUri].updatedAt = block.timestamp;

        emit DIDRevoked(didUri, msg.sender, block.timestamp, reason);
    }

    /**
     * @notice Transfer DID controller rights to another Ethereum address
     */
    function transferDIDController(
        string calldata didUri,
        address newController
    ) external onlyController(didUri) {
        if (newController == address(0)) revert InvalidParameters();
        if (_didRecords[didUri].isRevoked) revert DIDIsRevoked(didUri);

        address prev = _didRecords[didUri].controller;
        _didRecords[didUri].controller = newController;
        _didRecords[didUri].updatedAt = block.timestamp;
        _controllerDids[newController].push(didUri);

        emit DIDControllerTransferred(didUri, prev, newController, block.timestamp);
    }

    /**
     * @notice Resolve and verify a DID document record
     */
    function resolveDID(string calldata didUri)
        external
        view
        returns (
            address controller,
            string memory documentCid,
            string memory publicKeyMultibase,
            uint256 createdAt,
            uint256 updatedAt,
            bool isRevoked
        )
    {
        if (!_didExists[didUri]) revert DIDNotFound(didUri);
        DIDRecord memory r = _didRecords[didUri];
        return (r.controller, r.documentCid, r.publicKeyMultibase, r.createdAt, r.updatedAt, r.isRevoked);
    }

    /**
     * @notice Check if a DID exists and is valid (not revoked)
     */
    function isDIDActive(string calldata didUri) external view returns (bool) {
        return _didExists[didUri] && !_didRecords[didUri].isRevoked;
    }

    /**
     * @notice Get all DIDs controlled by an address
     */
    function getDIDsByController(address controller) external view returns (string[] memory) {
        return _controllerDids[controller];
    }

    /**
     * @notice Emergency Pause / Unpause
     */
    function pause() external onlyOwner {
        _pause();
    }

    function unpause() external onlyOwner {
        _unpause();
    }
}
