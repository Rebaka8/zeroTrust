import api from './api';
import { AuditLog, BlockchainTransaction, DashboardStats } from '../types';

export const auditService = {
  async getDashboardStats(): Promise<DashboardStats> {
    const res = await api.get<DashboardStats>('/dashboard/stats');
    return res.data;
  },

  async getAuditLogs(page = 0, size = 20) {
    const res = await api.get('/audit/logs', { params: { page, size } });
    return res.data;
  },

  async getRecentAuditLogs(): Promise<AuditLog[]> {
    const res = await api.get<AuditLog[]>('/audit/logs/recent');
    return res.data;
  },

  async getBlockchainTransactions(page = 0, size = 20) {
    const res = await api.get('/audit/transactions', { params: { page, size } });
    return res.data;
  },

  async getLatestBlockNumber(): Promise<number> {
    const res = await api.get<{ latestBlockNumber: number }>('/audit/block-number');
    return res.data.latestBlockNumber;
  }
};
