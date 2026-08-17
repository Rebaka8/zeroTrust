import { ethers } from "hardhat";
import * as fs from "fs";
import * as path from "path";

async function main() {
  console.log("================================================================");
  console.log("🚀 Deploying Zero Trust IoT Smart Contracts Suite");
  console.log("================================================================");

  const [deployer] = await ethers.getSigners();
  console.log(`📍 Deployer Address: ${deployer.address}`);
  const balance = await ethers.provider.getBalance(deployer.address);
  console.log(`💰 Deployer Balance: ${ethers.formatEther(balance)} ETH`);

  // 1. Deploy DIDRegistry
  console.log("\n[1/4] Deploying DIDRegistry...");
  const DIDRegistryFactory = await ethers.getContractFactory("DIDRegistry");
  const didRegistry = await DIDRegistryFactory.deploy();
  await didRegistry.waitForDeployment();
  const didRegistryAddress = await didRegistry.getAddress();
  console.log(`✅ DIDRegistry deployed at: ${didRegistryAddress}`);

  // 2. Deploy DeviceRegistry
  console.log("\n[2/4] Deploying DeviceRegistry...");
  const DeviceRegistryFactory = await ethers.getContractFactory("DeviceRegistry");
  const deviceRegistry = await DeviceRegistryFactory.deploy();
  await deviceRegistry.waitForDeployment();
  const deviceRegistryAddress = await deviceRegistry.getAddress();
  console.log(`✅ DeviceRegistry deployed at: ${deviceRegistryAddress}`);

  // 3. Deploy AccessControlManager
  console.log("\n[3/4] Deploying AccessControlManager...");
  const AccessControlFactory = await ethers.getContractFactory("AccessControlManager");
  const accessControl = await AccessControlFactory.deploy(deviceRegistryAddress, didRegistryAddress);
  await accessControl.waitForDeployment();
  const accessControlAddress = await accessControl.getAddress();
  console.log(`✅ AccessControlManager deployed at: ${accessControlAddress}`);

  // 4. Deploy ImmutableAuditLog
  console.log("\n[4/4] Deploying ImmutableAuditLog...");
  const AuditLogFactory = await ethers.getContractFactory("ImmutableAuditLog");
  const auditLog = await AuditLogFactory.deploy();
  await auditLog.waitForDeployment();
  const auditLogAddress = await auditLog.getAddress();
  console.log(`✅ ImmutableAuditLog deployed at: ${auditLogAddress}`);

  // 5. Authorize AccessControl & Operator permissions
  console.log("\n[5/5] Configuring Cross-Contract Authorizations...");
  await deviceRegistry.setOperatorAuthorization(deployer.address, true);
  await deviceRegistry.setOperatorAuthorization(accessControlAddress, true);
  await auditLog.setLoggerAuthorization(deployer.address, true);
  await auditLog.setLoggerAuthorization(accessControlAddress, true);
  console.log("✅ Cross-contract permissions configured.");

  // 6. Seed Sample On-Chain Zero Trust Data
  console.log("\n[Seed] Seeding Initial Zero Trust DIDs and Devices...");
  const sampleDid = "did:zt:dev:0x1A4F98E4B72C8120391A";
  await didRegistry.registerDID(
    sampleDid,
    "bafybeigdyrzt5sfp7udm7hu76uh7y26nf3efuylqabf3oclgtqy55fbzdi",
    "z6MkuT4X89J897vY6LqK2xPzW902"
  );
  console.log(`   ✔ Registered DID: ${sampleDid}`);

  const firmwareHash = ethers.keccak256(ethers.toUtf8Bytes("FIRMWARE_SICAM_V2.4.1_APPROVED"));
  await deviceRegistry.registerDevice(
    sampleDid,
    "Smart Grid Substation Alpha",
    "Siemens-SICAM-A8000",
    "00:1A:2B:3C:4D:5E",
    firmwareHash,
    "bafybeihdwdcefgh4dqkjv67uzcmw7ojee6xedviomoolkW75519b1d"
  );
  console.log(`   ✔ Registered Device: Smart Grid Substation Alpha`);

  // Seed Access Policy
  const policyTx = await accessControl.createPolicy(
    "Critical Grid Actuation Policy",
    85,
    "IndustrialGatewayAuthorization",
    "iot/grid/control",
    "EXECUTE"
  );
  await policyTx.wait();
  console.log("   ✔ Created Access Policy: Critical Grid Actuation Policy (Min Trust Score: 85)");

  // Seed Audit Log
  const eventHash = ethers.keccak256(ethers.toUtf8Bytes("INITIAL_DEPLOYMENT_HEALTHCHECK"));
  await auditLog.logEvent(
    sampleDid,
    "SYSTEM_PROVISIONED",
    0, // INFO
    "bafybeigdyrzt5sfp7udm7hu76uh7y26nf3efuylqabf3oclgtqy55fbzdi",
    eventHash
  );
  console.log("   ✔ Seeded Initial Audit Log entry.");

  // 7. Export Deployed Addresses & Artifacts
  const deploymentInfo = {
    network: (await ethers.provider.getNetwork()).name,
    chainId: Number((await ethers.provider.getNetwork()).chainId),
    deployer: deployer.address,
    timestamp: new Date().toISOString(),
    contracts: {
      DIDRegistry: didRegistryAddress,
      DeviceRegistry: deviceRegistryAddress,
      AccessControlManager: accessControlAddress,
      ImmutableAuditLog: auditLogAddress,
    },
  };

  const outputDir = path.resolve(__dirname, "../deployments");
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }

  const outputPath = path.join(outputDir, "deployed-contracts.json");
  fs.writeFileSync(outputPath, JSON.stringify(deploymentInfo, null, 2));
  console.log(`\n📄 Deployment manifests saved to: ${outputPath}`);

  console.log("\n================================================================");
  console.log("🎉 ALL ZERO TRUST SMART CONTRACTS SUCCESSFULLY DEPLOYED!");
  console.log("================================================================");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("❌ Deployment failed:", error);
    process.exit(1);
  });
