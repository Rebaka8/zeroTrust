import React, { useEffect, useState } from 'react';
import { simulationService } from '../services/simulationService';
import { deviceService } from '../services/deviceService';
import { SimulationScenario, AttackSimulationResult, AttackType, Device } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';
import { TrustGauge } from '../components/common/TrustGauge';
import {
  Flame,
  ShieldAlert,
  ShieldCheck,
  Zap,
  Server,
  Radio,
  FileWarning,
  Activity,
  Play,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Sparkles
} from 'lucide-react';

export const SimulationPage: React.FC = () => {
  const [scenarios, setScenarios] = useState<SimulationScenario[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedAttack, setSelectedAttack] = useState<AttackType>('FIRMWARE_MODIFICATION');
  const [selectedDeviceId, setSelectedDeviceId] = useState<string>('');
  const [intensity, setIntensity] = useState<number>(8);

  const [loadingScenarios, setLoadingScenarios] = useState(true);
  const [executing, setExecuting] = useState(false);
  const [provisioning, setProvisioning] = useState(false);
  const [simulationResult, setSimulationResult] = useState<AttackSimulationResult | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoadingScenarios(true);
      const [scenariosData, devRes] = await Promise.all([
        simulationService.getScenarios(),
        deviceService.getAllDevices(0, 50)
      ]);
      setScenarios(scenariosData);
      setDevices(devRes.content);
      if (devRes.content.length > 0) {
        setSelectedDeviceId(devRes.content[0].id);
      }
    } catch (e) {
      console.error('Failed to load simulation arena data:', e);
    } finally {
      setLoadingScenarios(false);
    }
  };

  const handleExecuteAttack = async () => {
    try {
      setExecuting(true);
      setSimulationResult(null);
      const res = await simulationService.executeAttack(
        selectedAttack,
        selectedDeviceId || undefined,
        intensity
      );
      setSimulationResult(res);
      // Reload devices to reflect new trust scores or quarantine state
      const devRes = await deviceService.getAllDevices(0, 50);
      setDevices(devRes.content);
    } catch (err: any) {
      alert('Attack simulation failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setExecuting(false);
    }
  };

  const handleProvisionFleet = async () => {
    try {
      setProvisioning(true);
      await simulationService.provisionVirtualFleet();
      await loadData();
      alert('Virtual IoT Fleet provisioned successfully! (4 Smart Grid, HVAC, Actuator, Camera nodes created)');
    } catch (e: any) {
      alert('Provisioning failed: ' + (e.response?.data?.message || e.message));
    } finally {
      setProvisioning(false);
    }
  };

  const getAttackIcon = (type: AttackType) => {
    switch (type) {
      case 'TAMPER_PAYLOAD':
        return <Activity className="w-5 h-5 text-indigo-400" />;
      case 'REPLAY_ATTACK':
        return <RefreshCw className="w-5 h-5 text-amber-400" />;
      case 'FIRMWARE_MODIFICATION':
        return <FileWarning className="w-5 h-5 text-rose-400" />;
      case 'SYBIL_DID_INJECTION':
        return <Server className="w-5 h-5 text-purple-400" />;
      case 'PACKET_FLOODING':
        return <Zap className="w-5 h-5 text-cyan-400" />;
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="glass-panel p-5 rounded-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-base font-bold text-gray-100 flex items-center gap-2">
            <Flame className="w-5 h-5 text-rose-500 animate-pulse" />
            Adversarial Cyberattack Simulation & Zero Trust Defense Arena
          </h2>
          <p className="text-xs text-gray-400 mt-1">
            Simulate realistic attacks against enrolled IoT nodes to demonstrate dynamic risk penalties, attestation checks, and autonomous quarantine.
          </p>
        </div>

        <button
          onClick={handleProvisionFleet}
          disabled={provisioning}
          className="px-3.5 py-2 bg-gradient-to-r from-indigo-600 to-cyan-500 hover:from-indigo-500 hover:to-cyan-400 disabled:opacity-50 text-white text-xs font-semibold rounded-lg shadow-lg shadow-indigo-500/20 transition-all flex items-center gap-2 flex-shrink-0"
        >
          {provisioning ? (
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
          ) : (
            <Sparkles className="w-4 h-4" />
          )}
          <span>Provision 4 Virtual IoT Nodes</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Attack Scenario Selection (2 Cols) */}
        <div className="lg:col-span-2 space-y-4">
          <h3 className="text-sm font-bold text-gray-100 flex items-center gap-2">
            <ShieldAlert className="w-4 h-4 text-indigo-400" />
            Select Cyberattack Vector to Simulate
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {scenarios.map((scenario) => {
              const isSelected = selectedAttack === scenario.attackType;
              return (
                <div
                  key={scenario.attackType}
                  onClick={() => setSelectedAttack(scenario.attackType)}
                  className={`p-4 rounded-xl border cursor-pointer transition-all duration-200 ${
                    isSelected
                      ? 'bg-indigo-950/40 border-indigo-500/60 shadow-lg shadow-indigo-500/10'
                      : 'glass-panel hover:bg-gray-800/40 border-gray-800'
                  }`}
                >
                  <div className="flex items-center gap-3 mb-2">
                    <div className={`p-2 rounded-lg ${isSelected ? 'bg-indigo-600/30' : 'bg-gray-800'}`}>
                      {getAttackIcon(scenario.attackType)}
                    </div>
                    <h4 className="text-xs font-bold text-gray-100">{scenario.name}</h4>
                  </div>
                  <p className="text-xs text-gray-400 mb-3">{scenario.description}</p>
                  <div className="text-[11px] text-gray-400 pt-2 border-t border-gray-800/60">
                    <span className="text-indigo-300 font-semibold block mb-0.5">Mitigation:</span>
                    {scenario.mitigationMechanism}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Attack Execution Launcher (1 Col) */}
        <div className="glass-panel p-5 rounded-xl flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-gray-100 flex items-center gap-2 mb-3">
              <Play className="w-4 h-4 text-rose-400" />
              Attack Simulation Parameters
            </h3>

            <div className="space-y-4 text-xs">
              <div>
                <label className="text-gray-400 font-medium block mb-1">Target IoT Device Node</label>
                <select
                  value={selectedDeviceId}
                  onChange={(e) => setSelectedDeviceId(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-800 rounded-lg p-2 text-gray-200 focus:outline-none focus:border-indigo-500"
                >
                  {devices.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.deviceName} (Score: {d.currentTrustScore}, {d.status})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <div className="flex justify-between text-gray-400 font-medium mb-1">
                  <span>Attack Intensity / Volume</span>
                  <span className="font-mono text-indigo-400 font-bold">{intensity} / 10</span>
                </div>
                <input
                  type="range"
                  min="1"
                  max="10"
                  value={intensity}
                  onChange={(e) => setIntensity(Number(e.target.value))}
                  className="w-full accent-indigo-500 bg-gray-800"
                />
              </div>

              <button
                onClick={handleExecuteAttack}
                disabled={executing || devices.length === 0}
                className="w-full py-3 bg-gradient-to-r from-rose-600 to-orange-600 hover:from-rose-500 hover:to-orange-500 disabled:opacity-50 text-white text-xs font-bold rounded-lg shadow-lg shadow-rose-600/30 transition-all flex items-center justify-center gap-2"
              >
                {executing ? (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  <Flame className="w-4 h-4" />
                )}
                <span>Launch Adversarial Simulation</span>
              </button>
            </div>
          </div>

          <p className="text-[11px] text-gray-500 mt-4 text-center">
            Zero Trust engine will monitor telemetry, execute attestation checks, and automatically quarantine if risk exceeds tolerance.
          </p>
        </div>
      </div>

      {/* Real-Time Defense Execution Report */}
      {simulationResult && (
        <div className="glass-panel-glow p-6 rounded-xl space-y-4 animate-in fade-in duration-300">
          <div className="flex items-center justify-between pb-3 border-b border-gray-800">
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-emerald-400" />
              <h3 className="text-sm font-bold text-gray-100">
                Zero Trust Defense Execution Report — {simulationResult.attackType}
              </h3>
            </div>
            <span className="text-xs font-mono text-gray-400">
              Simulation ID: {simulationResult.simulationId.substring(0, 8)}
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {/* Pre vs Post Trust Score */}
            <div className="bg-gray-900/80 p-4 rounded-xl border border-gray-800 flex flex-col items-center justify-center">
              <span className="text-xs text-gray-400 mb-2">Trust Score Dynamic Shift</span>
              <div className="flex items-center gap-3">
                <span className="text-lg font-mono font-bold text-emerald-400">
                  {simulationResult.preAttackTrustScore}
                </span>
                <span className="text-gray-500">➔</span>
                <span className={`text-xl font-mono font-extrabold ${
                  simulationResult.postAttackTrustScore < 35 ? 'text-rose-400' : 'text-amber-400'
                }`}>
                  {simulationResult.postAttackTrustScore}
                </span>
              </div>
              <span className="text-[10px] text-rose-400 font-mono mt-1">
                -{simulationResult.preAttackTrustScore - simulationResult.postAttackTrustScore} pts penalty
              </span>
            </div>

            {/* Quarantine Status */}
            <div className="bg-gray-900/80 p-4 rounded-xl border border-gray-800 flex flex-col items-center justify-center">
              <span className="text-xs text-gray-400 mb-2">Autonomous Quarantine</span>
              {simulationResult.automatedQuarantineTriggered ? (
                <div className="text-center">
                  <StatusBadge status="QUARANTINED" />
                  <span className="text-[10px] text-rose-400 block mt-1">Node Fully Isolated</span>
                </div>
              ) : (
                <div className="text-center">
                  <StatusBadge status="ACTIVE" />
                  <span className="text-[10px] text-emerald-400 block mt-1">Under Restricted Watch</span>
                </div>
              )}
            </div>

            {/* PDP Decision */}
            <div className="bg-gray-900/80 p-4 rounded-xl border border-gray-800 flex flex-col items-center justify-center">
              <span className="text-xs text-gray-400 mb-2">PDP Access Policy Decision</span>
              <StatusBadge status={simulationResult.pdpDecision} />
            </div>

            {/* Defense Status */}
            <div className="bg-gray-900/80 p-4 rounded-xl border border-gray-800 flex flex-col items-center justify-center">
              <span className="text-xs text-gray-400 mb-2">Threat Mitigation</span>
              <span className="text-xs font-bold text-emerald-400 flex items-center gap-1">
                <CheckCircle2 className="w-4 h-4" />
                Defended & Logged
              </span>
            </div>
          </div>

          {/* Defense Summary */}
          <div className="p-4 rounded-lg bg-gray-950/80 border border-gray-800 text-xs">
            <h4 className="font-bold text-indigo-300 mb-1">Defense Analysis Summary:</h4>
            <p className="text-gray-300">{simulationResult.defenseSummary}</p>
          </div>

          {/* Anomaly Indicators */}
          {simulationResult.anomalyIndicators && simulationResult.anomalyIndicators.length > 0 && (
            <div className="p-4 rounded-lg bg-rose-950/20 border border-rose-500/30 text-xs">
              <h4 className="font-bold text-rose-400 mb-1.5 flex items-center gap-1.5">
                <AlertTriangle className="w-4 h-4" />
                Detected Anomaly Indicators:
              </h4>
              <ul className="list-disc list-inside space-y-1 text-gray-300 font-mono text-[11px]">
                {simulationResult.anomalyIndicators.map((ind, idx) => (
                  <li key={idx}>{ind}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
