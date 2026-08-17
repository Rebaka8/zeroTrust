import api from './api';
import { DatasetBenchmarkResult, DatasetPreset } from '../types';

export const datasetService = {
  async getPresets(): Promise<DatasetPreset[]> {
    const res = await api.get<DatasetPreset[]>('/dataset/presets');
    return res.data;
  },

  async replayPreset(presetId: string, targetDeviceId?: string): Promise<DatasetBenchmarkResult> {
    const res = await api.post<DatasetBenchmarkResult>(`/dataset/replay-preset/${presetId}`, null, {
      params: targetDeviceId ? { targetDeviceId } : {}
    });
    return res.data;
  },

  async uploadAndReplayCustomCsv(file: File, targetDeviceId?: string): Promise<DatasetBenchmarkResult> {
    const formData = new FormData();
    formData.append('file', file);

    const res = await api.post<DatasetBenchmarkResult>('/dataset/upload-replay', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      params: targetDeviceId ? { targetDeviceId } : {}
    });
    return res.data;
  }
};
