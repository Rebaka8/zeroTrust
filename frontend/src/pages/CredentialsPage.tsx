import React, { useState } from 'react';
import { didVcService } from '../services/didVcService';
import { W3CDidDocument, VcVerificationResult } from '../types';
import {
  Fingerprint,
  Search,
  ShieldCheck,
  CheckCircle2,
  XCircle,
  FileCheck2,
  Key,
  ShieldAlert,
  Code
} from 'lucide-react';

export const CredentialsPage: React.FC = () => {
  // DID Resolver Playground
  const [resolveDidInput, setResolveDidInput] = useState('did:zt:issuer:0x8626f6940e2eb28930efb4cef49b2d1f2c9c1199');
  const [resolvedDoc, setResolvedDoc] = useState<W3CDidDocument | null>(null);
  const [loadingResolve, setLoadingResolve] = useState(false);
  const [resolveError, setResolveError] = useState<string | null>(null);

  // VC Verification Studio
  const [vcPayloadInput, setVcPayloadInput] = useState(`{
  "@context": [
    "https://www.w3.org/2018/credentials/v1",
    "https://schema.org"
  ],
  "id": "urn:uuid:f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "type": ["VerifiableCredential", "IoTDeviceAttestation"],
  "issuer": "did:zt:issuer:0x8626f6940e2eb28930efb4cef49b2d1f2c9c1199",
  "issuanceDate": "2026-08-17T10:00:00Z",
  "credentialSubject": {
    "id": "did:zt:dev:0x71c671d1133650711720f29c6991063357540776",
    "firmwareHash": "0xe3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "trustTier": "TIER_1_CRITICAL"
  },
  "proof": {
    "type": "Ed25519Signature2020",
    "created": "2026-08-17T10:00:00Z",
    "verificationMethod": "did:zt:issuer:0x8626f6940e2eb28930efb4cef49b2d1f2c9c1199#keys-1",
    "proofPurpose": "assertionMethod",
    "proofValue": "z3hGyvC4W7...validEd25519SignatureMultibase"
  }
}`);
  const [vcResult, setVcResult] = useState<VcVerificationResult | null>(null);
  const [verifying, setVerifying] = useState(false);

  const handleResolve = async () => {
    if (!resolveDidInput) return;
    try {
      setLoadingResolve(true);
      setResolveError(null);
      const doc = await didVcService.resolveDid(resolveDidInput);
      setResolvedDoc(doc);
    } catch (e: any) {
      setResolveError(e.response?.data?.message || 'Universal DID Resolution failed');
      setResolvedDoc(null);
    } finally {
      setLoadingResolve(false);
    }
  };

  const handleVerifyVc = async () => {
    try {
      setVerifying(true);
      const result = await didVcService.verifyCredential(vcPayloadInput);
      setVcResult(result);
    } catch (e: any) {
      setVcResult({
        valid: false,
        subjectDid: 'Unknown',
        issuerDid: 'Unknown',
        credentialType: 'Invalid',
        isExpired: false,
        isRevoked: false,
        signatureValid: false,
        firmwareIntegrityMatched: false,
        verificationSummary: 'Cryptographic parse failure: ' + (e.response?.data?.message || e.message),
        checkedAt: new Date().toISOString()
      });
    } finally {
      setVerifying(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="glass-panel p-5 rounded-xl flex items-center justify-between">
        <div>
          <h2 className="text-base font-bold text-gray-100 flex items-center gap-2">
            <Fingerprint className="w-5 h-5 text-indigo-400" />
            W3C Decentralized Identifiers (DID) & Verifiable Credentials (VC) Hub
          </h2>
          <p className="text-xs text-gray-400 mt-1">
            Universal DID Resolver with JSON-LD parser and 5-stage cryptographic hardware attestation engine.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Universal DID Resolver */}
        <div className="glass-panel p-5 rounded-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-3">
              <Key className="w-4 h-4 text-cyan-400" />
              <h3 className="text-sm font-bold text-gray-100">Universal W3C DID Resolver</h3>
            </div>
            <p className="text-xs text-gray-400 mb-4">
              Resolves any <code className="text-indigo-300 font-mono">did:zt:*</code> identifier to its canonical JSON-LD Document anchored on the blockchain.
            </p>

            <div className="flex items-center gap-2 mb-4">
              <input
                type="text"
                value={resolveDidInput}
                onChange={(e) => setResolveDidInput(e.target.value)}
                placeholder="did:zt:dev:0x..."
                className="flex-1 bg-gray-900 border border-gray-800 rounded-lg px-3 py-2 text-xs font-mono text-gray-200 focus:outline-none focus:border-indigo-500"
              />
              <button
                onClick={handleResolve}
                disabled={loadingResolve}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-xs font-semibold rounded-lg shadow-md shadow-indigo-600/20 transition-all flex items-center gap-1.5"
              >
                {loadingResolve ? (
                  <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  <Search className="w-3.5 h-3.5" />
                )}
                <span>Resolve</span>
              </button>
            </div>

            {resolveError && (
              <div className="p-3 rounded-lg bg-rose-950/40 border border-rose-500/40 text-rose-400 text-xs mb-4 flex items-center gap-2">
                <ShieldAlert className="w-4 h-4 flex-shrink-0" />
                <span>{resolveError}</span>
              </div>
            )}

            {resolvedDoc && (
              <div className="space-y-3">
                <div className="flex items-center justify-between text-xs p-2.5 rounded bg-gray-900 border border-gray-800">
                  <span className="text-gray-400">DID Status:</span>
                  <span className="font-mono text-emerald-400 font-bold flex items-center gap-1">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    Active & Anchored
                  </span>
                </div>

                <div className="p-3 rounded bg-gray-950 border border-gray-800 text-[11px] font-mono text-cyan-300 max-h-80 overflow-y-auto">
                  <pre>{JSON.stringify(resolvedDoc, null, 2)}</pre>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Verifiable Credential Verification Studio */}
        <div className="glass-panel p-5 rounded-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-3">
              <FileCheck2 className="w-4 h-4 text-emerald-400" />
              <h3 className="text-sm font-bold text-gray-100">Verifiable Credential Verification Studio</h3>
            </div>
            <p className="text-xs text-gray-400 mb-4">
              Cryptographically verifies W3C VC proofs, hardware firmware digests, and revocation state.
            </p>

            <div className="space-y-3 mb-4">
              <label className="text-xs text-gray-400 block font-mono">VC JSON-LD Payload to Verify:</label>
              <textarea
                rows={9}
                value={vcPayloadInput}
                onChange={(e) => setVcPayloadInput(e.target.value)}
                className="w-full bg-gray-950 border border-gray-800 rounded-lg p-3 text-[11px] font-mono text-gray-300 focus:outline-none focus:border-indigo-500"
              />

              <button
                onClick={handleVerifyVc}
                disabled={verifying}
                className="w-full py-2 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-xs font-semibold rounded-lg shadow-md shadow-emerald-600/20 transition-all flex items-center justify-center gap-2"
              >
                {verifying ? (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  <ShieldCheck className="w-4 h-4" />
                )}
                <span>Run 5-Stage Cryptographic Verification</span>
              </button>
            </div>

            {vcResult && (
              <div className={`p-4 rounded-lg border ${
                vcResult.valid
                  ? 'bg-emerald-950/30 border-emerald-500/40 text-emerald-300'
                  : 'bg-rose-950/30 border-rose-500/40 text-rose-300'
              }`}>
                <div className="flex items-center gap-2 mb-2 font-bold text-xs">
                  {vcResult.valid ? (
                    <>
                      <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      <span>Attestation Valid: Cryptographic Signature & Attestation Verified</span>
                    </>
                  ) : (
                    <>
                      <XCircle className="w-4 h-4 text-rose-400" />
                      <span>Attestation Failed: Cryptographic Anomaly Detected</span>
                    </>
                  )}
                </div>
                <p className="text-xs opacity-90">{vcResult.verificationSummary}</p>

                <div className="mt-3 grid grid-cols-2 gap-2 text-[11px] font-mono pt-2 border-t border-gray-800">
                  <div>Signature: {vcResult.signatureValid ? '✓ Valid' : '✗ Invalid'}</div>
                  <div>Firmware: {vcResult.firmwareIntegrityMatched ? '✓ Match' : '✗ Mismatch'}</div>
                  <div>Expired: {vcResult.isExpired ? 'Yes' : 'No'}</div>
                  <div>Revoked: {vcResult.isRevoked ? 'Yes' : 'No'}</div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
