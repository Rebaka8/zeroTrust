import api from './api';
import { W3CDidDocument, VerifiableCredential, VcVerificationResult } from '../types';

export const didVcService = {
  async resolveDid(didUri: string): Promise<W3CDidDocument> {
    const res = await api.get<W3CDidDocument>(`/did/resolve/${encodeURIComponent(didUri)}`);
    return res.data;
  },

  async getAllDids(page = 0, size = 20) {
    const res = await api.get('/did', { params: { page, size } });
    return res.data;
  },

  async rotateKey(didUri: string, newPublicKeyMultibase: string) {
    const res = await api.post(`/did/${encodeURIComponent(didUri)}/rotate-key`, {
      newPublicKeyMultibase
    });
    return res.data;
  },

  async revokeDid(didUri: string, reason = 'Security decommission') {
    const res = await api.post(`/did/${encodeURIComponent(didUri)}/revoke`, null, {
      params: { reason }
    });
    return res.data;
  },

  async issueCredential(data: {
    deviceId: string;
    credentialType?: string;
    validityDays?: number;
    trustTier?: string;
    capabilities?: string[];
  }): Promise<VerifiableCredential> {
    const res = await api.post<VerifiableCredential>('/vc/issue', data);
    return res.data;
  },

  async verifyCredential(vcPayload: string): Promise<VcVerificationResult> {
    const res = await api.post<VcVerificationResult>('/vc/verify', { vcPayload });
    return res.data;
  },

  async getDeviceCredentials(deviceId: string): Promise<VerifiableCredential[]> {
    const res = await api.get<VerifiableCredential[]>(`/vc/device/${deviceId}`);
    return res.data;
  },

  async revokeCredential(credentialId: string, reason = 'Attestation invalidated'): Promise<VerifiableCredential> {
    const res = await api.post<VerifiableCredential>(`/vc/${credentialId}/revoke`, null, {
      params: { reason }
    });
    return res.data;
  }
};
