import { expect } from "chai";
import { ethers } from "hardhat";
import {
  DIDRegistry,
  DeviceRegistry,
  AccessControlManager,
  ImmutableAuditLog,
} from "../typechain-types";
import { SignerWithAddress } from "@nomicfoundation/hardhat-ethers/signers";

describe("🛡️ Zero Trust IoT Security Framework - Smart Contracts Suite", function () {
  let didRegistry: DIDRegistry;
  let deviceRegistry: DeviceRegistry;
  let accessControl: AccessControlManager;
  let auditLog: ImmutableAuditLog;

  let owner: SignerWithAddress;
  let operator: SignerWithAddress;
  let unauthorizedUser: SignerWithAddress;

  const sampleDid = "did:zt:dev:0x1A4F98E4B72C8120391A";
  const sampleCid = "bafybeigdyrzt5sfp7udm7hu76uh7y26nf3efuylqabf3oclgtqy55fbzdi";
  const samplePubKey = "z6MkuT4X89J897vY6LqK2xPzW902";
  const sampleMac = "00:1A:2B:3C:4D:5E";
  const sampleFirmwareHash = ethers.keccak256(ethers.toUtf8Bytes("FIRMWARE_SICAM_V2.4.1_APPROVED"));

  beforeEach(async function () {
    [owner, operator, unauthorizedUser] = await ethers.getSigners();

    // 1. Deploy DIDRegistry
    const DIDRegistryFactory = await ethers.getContractFactory("DIDRegistry");
    didRegistry = await DIDRegistryFactory.deploy();
    await didRegistry.waitForDeployment();

    // 2. Deploy DeviceRegistry
    const DeviceRegistryFactory = await ethers.getContractFactory("DeviceRegistry");
    deviceRegistry = await DeviceRegistryFactory.deploy();
    await deviceRegistry.waitForDeployment();

    // 3. Deploy AccessControlManager
    const AccessControlFactory = await ethers.getContractFactory("AccessControlManager");
    accessControl = await AccessControlFactory.deploy(
      await deviceRegistry.getAddress(),
      await didRegistry.getAddress()
    );
    await accessControl.waitForDeployment();

    // 4. Deploy ImmutableAuditLog
    const AuditLogFactory = await ethers.getContractFactory("ImmutableAuditLog");
    auditLog = await AuditLogFactory.deploy();
    await auditLog.waitForDeployment();

    // Configure permissions
    await deviceRegistry.setOperatorAuthorization(operator.address, true);
    await deviceRegistry.setOperatorAuthorization(await accessControl.getAddress(), true);
    await auditLog.setLoggerAuthorization(operator.address, true);
    await auditLog.setLoggerAuthorization(await accessControl.getAddress(), true);

    const OPERATOR_ROLE = await accessControl.OPERATOR_ROLE();
    const ORACLE_ROLE = await accessControl.ORACLE_ROLE();
    await accessControl.grantRole(OPERATOR_ROLE, operator.address);
    await accessControl.grantRole(ORACLE_ROLE, operator.address);
  });

  // =================================================================
  // 1. DIDRegistry Tests
  // =================================================================
  describe("1. W3C DID Registry Contract", function () {
    it("Should successfully register a new W3C DID", async function () {
      await expect(didRegistry.registerDID(sampleDid, sampleCid, samplePubKey))
        .to.emit(didRegistry, "DIDCreated")
        .withArgs(sampleDid, owner.address, sampleCid, samplePubKey, (val: any) => val > 0);

      expect(await didRegistry.isDIDActive(sampleDid)).to.be.true;

      const record = await didRegistry.resolveDID(sampleDid);
      expect(record.controller).to.equal(owner.address);
      expect(record.documentCid).to.equal(sampleCid);
      expect(record.publicKeyMultibase).to.equal(samplePubKey);
      expect(record.isRevoked).to.be.false;
    });

    it("Should reject duplicate DID registration", async function () {
      await didRegistry.registerDID(sampleDid, sampleCid, samplePubKey);
      await expect(
        didRegistry.registerDID(sampleDid, sampleCid, samplePubKey)
      ).to.be.revertedWithCustomError(didRegistry, "DIDAlreadyExists");
    });

    it("Should allow controller to update DID Document CID", async function () {
      await didRegistry.registerDID(sampleDid, sampleCid, samplePubKey);
      const newCid = "bafybeicnewcid999999999999999999999999999999999999999";
      const newKey = "z6MkuT4XNEWKEY999";

      await expect(didRegistry.updateDID(sampleDid, newCid, newKey))
        .to.emit(didRegistry, "DIDUpdated");

      const record = await didRegistry.resolveDID(sampleDid);
      expect(record.documentCid).to.equal(newCid);
      expect(record.publicKeyMultibase).to.equal(newKey);
    });

    it("Should revoke DID and prevent further updates", async function () {
      await didRegistry.registerDID(sampleDid, sampleCid, samplePubKey);
      await expect(didRegistry.revokeDID(sampleDid, "Private Key Compromise Detected"))
        .to.emit(didRegistry, "DIDRevoked");

      expect(await didRegistry.isDIDActive(sampleDid)).to.be.false;

      await expect(
        didRegistry.updateDID(sampleDid, "anyCid", "anyKey")
      ).to.be.revertedWithCustomError(didRegistry, "DIDIsRevoked");
    });
  });

  // =================================================================
  // 2. DeviceRegistry Tests
  // =================================================================
  describe("2. Device Registry & Lifecycle Contract", function () {
    beforeEach(async function () {
      await didRegistry.registerDID(sampleDid, sampleCid, samplePubKey);
    });

    it("Should register a new IoT device with firmware hash", async function () {
      await expect(
        deviceRegistry.registerDevice(
          sampleDid,
          "Smart Grid Substation Alpha",
          "Siemens-SICAM-A8000",
          sampleMac,
          sampleFirmwareHash,
          sampleCid
        )
      ).to.emit(deviceRegistry, "DeviceRegistered");

      expect(await deviceRegistry.isDeviceOperational(sampleDid)).to.be.true;
      expect(await deviceRegistry.getTotalDevices()).to.equal(1);
    });

    it("Should prevent duplicate MAC address registration", async function () {
      await deviceRegistry.registerDevice(
        sampleDid,
        "Device 1",
        "Model X",
        sampleMac,
        sampleFirmwareHash,
        sampleCid
      );

      const did2 = "did:zt:dev:0x99999999999999999999";
      await expect(
        deviceRegistry.registerDevice(
          did2,
          "Device 2",
          "Model Y",
          sampleMac,
          sampleFirmwareHash,
          sampleCid
        )
      ).to.be.revertedWithCustomError(deviceRegistry, "MacAlreadyRegistered");
    });

    it("Should isolate and quarantine compromised device", async function () {
      await deviceRegistry.registerDevice(
        sampleDid,
        "Device 1",
        "Model X",
        sampleMac,
        sampleFirmwareHash,
        sampleCid
      );

      await expect(
        deviceRegistry.connect(operator).quarantineDevice(sampleDid, 24, "Replay Attack Detected")
      )
        .to.emit(deviceRegistry, "DeviceQuarantined")
        .withArgs(sampleDid, 24, "Replay Attack Detected", (val: any) => val > 0);

      expect(await deviceRegistry.isDeviceOperational(sampleDid)).to.be.false;

      // Restore device after verification
      await expect(
        deviceRegistry.connect(operator).restoreDevice(sampleDid, "Re-attestation Successful")
      ).to.emit(deviceRegistry, "DeviceStatusChanged");

      expect(await deviceRegistry.isDeviceOperational(sampleDid)).to.be.true;
    });
  });

  // =================================================================
  // 3. AccessControlManager (Zero Trust PDP) Tests
  // =================================================================
  describe("3. Access Control & Zero Trust Policy Engine", function () {
    let policyId: string;

    beforeEach(async function () {
      await didRegistry.registerDID(sampleDid, sampleCid, samplePubKey);
      await deviceRegistry.registerDevice(
        sampleDid,
        "Smart Grid Substation Alpha",
        "Siemens-SICAM-A8000",
        sampleMac,
        sampleFirmwareHash,
        sampleCid
      );

      const tx = await accessControl.connect(operator).createPolicy(
        "Critical Grid Actuation Policy",
        85,
        "IndustrialGatewayAuthorization",
        "iot/grid/control",
        "EXECUTE"
      );
      const receipt = await tx.wait();
      // Extract policyId from event
      const event = receipt?.logs[0];
      const parsed = accessControl.interface.parseLog({
        topics: event?.topics as string[],
        data: event?.data as string,
      });
      policyId = parsed?.args.policyId;
    });

    it("Should GRANT access when trust score satisfies policy threshold (95 >= 85)", async function () {
      const result = await accessControl.connect(operator).evaluateAccess.staticCall(
        sampleDid,
        policyId,
        95
      );
      expect(result.granted).to.be.true;
      expect(result.reason).to.equal("Access Granted under Zero Trust Policy");
    });

    it("Should DENY access when trust score falls below threshold (72 < 85)", async function () {
      const result = await accessControl.connect(operator).evaluateAccess.staticCall(
        sampleDid,
        policyId,
        72
      );
      expect(result.granted).to.be.false;
      expect(result.reason).to.equal("Trust score falls below required threshold");
    });

    it("Should DENY access if device is quarantined even with high trust score", async function () {
      await deviceRegistry.connect(operator).quarantineDevice(sampleDid, 15, "Firmware Tampering");
      const result = await accessControl.connect(operator).evaluateAccess.staticCall(
        sampleDid,
        policyId,
        99
      );
      expect(result.granted).to.be.false;
      expect(result.reason).to.equal("Device is quarantined or suspended");
    });

    it("Should DENY access if DID is revoked", async function () {
      await didRegistry.revokeDID(sampleDid, "Key Revocation");
      const result = await accessControl.connect(operator).evaluateAccess.staticCall(
        sampleDid,
        policyId,
        99
      );
      expect(result.granted).to.be.false;
      expect(result.reason).to.equal("Device DID is inactive or revoked");
    });
  });

  // =================================================================
  // 4. ImmutableAuditLog Tests
  // =================================================================
  describe("4. Immutable Audit Ledger Contract", function () {
    it("Should log a security event and retrieve it", async function () {
      const payloadHash = ethers.keccak256(ethers.toUtf8Bytes("REPLAY_PAYLOAD_DETAILS_2026"));

      await expect(
        auditLog.connect(operator).logEvent(
          sampleDid,
          "REPLAY_ATTACK_BLOCKED",
          4, // CRITICAL
          sampleCid,
          payloadHash
        )
      ).to.emit(auditLog, "SecurityEventLogged");

      expect(await auditLog.getTotalLogs()).to.equal(1);
      const entry = await auditLog.getAuditEntry(0);
      expect(entry.didUri).to.equal(sampleDid);
      expect(entry.eventType).to.equal("REPLAY_ATTACK_BLOCKED");
      expect(entry.severity).to.equal(4);
    });

    it("Should reject unauthorized loggers", async function () {
      const payloadHash = ethers.keccak256(ethers.toUtf8Bytes("PAYLOAD"));
      await expect(
        auditLog.connect(unauthorizedUser).logEvent(sampleDid, "EVENT", 0, sampleCid, payloadHash)
      ).to.be.revertedWithCustomError(auditLog, "UnauthorizedLogger");
    });

    it("Should record Merkle root batch audit commits", async function () {
      const merkleRoot = ethers.keccak256(ethers.toUtf8Bytes("BATCH_MERKLE_ROOT_100_ENTRIES"));
      await expect(
        auditLog.connect(operator).commitBatchAudit(0, 100, merkleRoot)
      ).to.emit(auditLog, "BatchAuditCommitted");
    });
  });
});
