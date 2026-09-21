export type DeviceStatus = 'ACTIVE' | 'QUARANTINED' | 'SUSPENDED' | 'DECOMMISSIONED';

export type RiskLevel = 'LOW_NOMINAL' | 'MEDIUM_ELEVATED' | 'HIGH_SUSPICIOUS' | 'CRITICAL_COMPROMISED';

export type DecisionType = 'PERMIT_FULL' | 'PERMIT_RESTRICTED' | 'CHALLENGE_REAUTH' | 'DENY_QUARANTINE';

export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type ActionAllowed = 'READ' | 'WRITE' | 'EXECUTE' | 'ADMIN';

export type AttackType =
  | 'TAMPER_PAYLOAD'
  | 'REPLAY_ATTACK'
  | 'FIRMWARE_MODIFICATION'
  | 'SYBIL_DID_INJECTION'
  | 'PACKET_FLOODING';

export interface User {
  id: string;
  username: string;
  email: string;
  walletAddress?: string;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  id: string;
  username: string;
  email: string;
  walletAddress?: string;
  roles: string[];
}

export interface Device {
  id: string;
  didUri: string;
  deviceName: string;
  deviceType: string;
  hardwareModel: string;
  macAddress: string;
  ipAddress?: string;
  firmwareHash: string;
  status: DeviceStatus;
  publicKey: string;
  isQuarantined: boolean;
  currentTrustScore: number;
  registeredAt: string;
  lastHeartbeat?: string;
}

export interface DeviceDetails extends Device {
  didDocumentJson: string;
  onChainTxHash?: string;
  recentTelemetry: TelemetryData[];
}

export interface TelemetryData {
  id: number;
  didUri: string;
  temperature?: number;
  humidity?: number;
  voltage?: number;
  cpuUtilization?: number;
  memoryUsage?: number;
  packetRate?: number;
  payloadSignature?: string;
  recordedAt: string;
}

export interface SecurityAlert {
  id: string;
  deviceId?: string;
  deviceName?: string;
  didUri?: string;
  alertType: string;
  severity: AlertSeverity;
  description: string;
  incidentPayload?: string;
  isResolved: boolean;
  triggeredAt: string;
}

export interface AuditLog {
  id: number;
  username: string;
  actionType: string;
  targetEntity: string;
  targetId?: string;
  ipAddress?: string;
  details: string;
  integrityHash: string;
  createdAt: string;
}

export interface BlockchainTransaction {
  id: string;
  txHash: string;
  contractName: string;
  functionName: string;
  blockNumber?: number;
  fromAddress: string;
  toAddress: string;
  gasUsed?: number;
  status: string;
  minedAt: string;
}

export interface DashboardStats {
  totalDevices: number;
  activeDevices: number;
  quarantinedDevices: number;
  suspendedDevices: number;
  averageTrustScore: number;
  averageDecisionLatencyMs?: number;
  activeAlertsCount: number;
  criticalAlertsCount: number;
  totalOnChainTxCount: number;
  topDevices: Device[];
  recentAlerts: SecurityAlert[];
  recentAuditLogs: AuditLog[];
  recentTransactions: BlockchainTransaction[];
}

export interface TrustScore {
  id: number;
  deviceId: string;
  deviceName: string;
  didUri: string;
  overallScore: number;
  cryptoIdentityScore: number;
  behavioralScore: number;
  firmwareScore: number;
  networkScore: number;
  penaltyScore: number;
  riskLevel: RiskLevel;
  evaluationReasons: string;
  reasonsList: string[];
  evaluatedAt: string;
}

export interface TrustBreakdown {
  deviceId: string;
  didUri: string;
  deviceName: string;
  currentTrustScore: number;
  riskLevel: RiskLevel;
  cryptoIdentityScore: number;
  behavioralScore: number;
  firmwareScore: number;
  networkScore: number;
  penaltyScore: number;
  cryptoWeight: number;
  behavioralWeight: number;
  firmwareWeight: number;
  networkWeight: number;
  decayLambda: number;
  elapsedSecondsSinceHeartbeat: number;
  contributingFactors: string[];
  historicalTimeline: TrustScore[];
  lastEvaluatedAt: string;
}

export interface AccessPolicy {
  id: string;
  policyName: string;
  description?: string;
  minimumTrustScore: number;
  requiredCredentialType?: string;
  allowedTopics: string;
  actionAllowed: ActionAllowed;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AccessDecision {
  requestId: string;
  didUri: string;
  resource: string;
  action: string;
  currentTrustScore: number;
  decision: DecisionType;
  granted: boolean;
  reason: string;
  evaluatedAt: string;
}

export interface W3CDidDocument {
  '@context': string[];
  id: string;
  verificationMethod: {
    id: string;
    type: string;
    controller: string;
    publicKeyMultibase?: string;
    publicKeyHex?: string;
  }[];
  authentication: string[];
  assertionMethod: string[];
  capabilityInvocation: string[];
  metadataCid?: string;
  deactivated: boolean;
}

export interface VerifiableCredential {
  id: string;
  deviceId: string;
  deviceName: string;
  credentialType: string;
  issuerDid: string;
  subjectDid: string;
  rawVcJwt: string;
  claimsJson: string;
  issuanceDate: string;
  expirationDate: string;
  isRevoked: boolean;
  createdAt: string;
}

export interface VcVerificationResult {
  valid: boolean;
  subjectDid: string;
  issuerDid: string;
  credentialType: string;
  trustTier?: string;
  capabilities?: string[];
  isExpired: boolean;
  isRevoked: boolean;
  signatureValid: boolean;
  firmwareIntegrityMatched: boolean;
  verificationSummary: string;
  checkedAt: string;
}

export interface SimulationScenario {
  attackType: AttackType;
  name: string;
  description: string;
  expectedDefenseOutcome: string;
  mitigationMechanism: string;
}

export interface AttackSimulationResult {
  simulationId: string;
  attackType: AttackType;
  targetDeviceId: string;
  targetDeviceName: string;
  targetDidUri: string;
  attackDetected: boolean;
  automatedQuarantineTriggered: boolean;
  pdpDecision: DecisionType;
  preAttackTrustScore: number;
  postAttackTrustScore: number;
  defenseSummary: string;
  anomalyIndicators: string[];
  generatedAlert?: SecurityAlert;
  updatedTrustScore?: TrustScore;
  defenseLatencyMs?: number;
  executedAt: string;
}

export interface DatasetPreset {
  id: string;
  name: string;
  source: string;
  description: string;
  attackTypes: string;
  sampleRowsCount: number;
}

export interface DatasetBenchmarkResult {
  datasetName: string;
  totalRowsProcessed: number;
  benignPacketsCount: number;
  maliciousPacketsCount: number;
  truePositives: number;
  falsePositives: number;
  trueNegatives: number;
  falseNegatives: number;
  accuracyPercentage: number;
  precisionPercentage: number;
  recallPercentage: number;
  f1ScorePercentage: number;
  averageDetectionLatencyMs: number;
  automaticQuarantinesEnforced: number;
  detectedThreatSignatures: string[];
  defenseEvaluationSummary: string;
}
