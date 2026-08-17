import api from './api';
import { TrustBreakdown, TrustScore, AccessDecision, AccessPolicy } from '../types';

export const trustService = {
  async evaluateDeviceTrust(deviceId: string): Promise<TrustScore> {
    const res = await api.post<TrustScore>(`/trust/evaluate/${deviceId}`);
    return res.data;
  },

  async getTrustBreakdown(deviceId: string): Promise<TrustBreakdown> {
    const res = await api.get<TrustBreakdown>(`/trust/${deviceId}/breakdown`);
    return res.data;
  },

  async getTrustHistory(deviceId: string, page = 0, size = 20) {
    const res = await api.get(`/trust/${deviceId}/history`, { params: { page, size } });
    return res.data;
  },

  async getActivePolicies(): Promise<AccessPolicy[]> {
    const res = await api.get<AccessPolicy[]>('/policies/active');
    return res.data;
  },

  async createPolicy(policy: {
    policyName: string;
    description?: string;
    minimumTrustScore: number;
    requiredCredentialType?: string;
    allowedTopics: string;
    actionAllowed: string;
  }): Promise<AccessPolicy> {
    const res = await api.post<AccessPolicy>('/policies', policy);
    return res.data;
  },

  async evaluateAccess(data: {
    didUri: string;
    resource: string;
    action: string;
  }): Promise<AccessDecision> {
    const res = await api.post<AccessDecision>('/policies/evaluate', data);
    return res.data;
  }
};
