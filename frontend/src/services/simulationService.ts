import api from './api';
import { AttackSimulationResult, AttackType, SimulationScenario, Device } from '../types';

export const simulationService = {
  async getScenarios(): Promise<SimulationScenario[]> {
    const res = await api.get<SimulationScenario[]>('/simulation/scenarios');
    return res.data;
  },

  async executeAttack(attackType: AttackType, targetDeviceId?: string, intensity = 5): Promise<AttackSimulationResult> {
    const res = await api.post<AttackSimulationResult>('/simulation/attack', {
      attackType,
      targetDeviceId,
      intensity
    });
    return res.data;
  },

  async provisionVirtualFleet(): Promise<Device[]> {
    const res = await api.post<Device[]>('/simulation/provision-virtual-devices');
    return res.data;
  }
};
