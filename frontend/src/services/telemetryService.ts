import api from './api';
import { TelemetryData } from '../types';

export const telemetryService = {
  async ingestTelemetry(data: {
    didUri: string;
    temperature?: number;
    humidity?: number;
    voltage?: number;
    cpuUtilization?: number;
    memoryUsage?: number;
    packetRate?: number;
    payloadSignature?: string;
    metadata?: string;
  }): Promise<TelemetryData> {
    const res = await api.post<TelemetryData>('/telemetry/ingest', data);
    return res.data;
  },

  async getRecentTelemetry(deviceId: string): Promise<TelemetryData[]> {
    const res = await api.get<TelemetryData[]>(`/telemetry/device/${deviceId}/recent`);
    return res.data;
  }
};
