import api from './api';
import { Device, DeviceDetails } from '../types';

export const deviceService = {
  async getAllDevices(page = 0, size = 20): Promise<{ content: Device[]; totalPages: number; totalElements: number }> {
    const res = await api.get('/devices', { params: { page, size } });
    return res.data;
  },

  async getActiveDevices(): Promise<Device[]> {
    const res = await api.get<Device[]>('/devices/active');
    return res.data;
  },

  async getDeviceById(id: string): Promise<DeviceDetails> {
    const res = await api.get<DeviceDetails>(`/devices/${id}`);
    return res.data;
  },

  async registerDevice(data: {
    deviceName: string;
    deviceType: string;
    hardwareModel: string;
    macAddress: string;
    ipAddress?: string;
    firmwareHash: string;
    publicKey: string;
    metadataCid?: string;
  }): Promise<Device> {
    const res = await api.post<Device>('/devices', data);
    return res.data;
  },

  async quarantineDevice(id: string, reason = 'Operator Intervention'): Promise<Device> {
    const res = await api.post<Device>(`/devices/${id}/quarantine`, null, {
      params: { reason }
    });
    return res.data;
  },

  async restoreDevice(id: string, reason = 'Attestation Re-verified'): Promise<Device> {
    const res = await api.post<Device>(`/devices/${id}/restore`, null, {
      params: { reason }
    });
    return res.data;
  },

  async deleteDevice(id: string): Promise<void> {
    await api.delete(`/devices/${id}`);
  }
};
