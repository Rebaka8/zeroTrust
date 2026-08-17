import React, { useEffect, useState } from 'react';
import { trustService } from '../services/trustService';
import { deviceService } from '../services/deviceService';
import { TrustBreakdown, Device, AccessDecision } from '../types';
import { TrustGauge } from '../components/common/TrustGauge';
import { StatusBadge } from '../components/common/StatusBadge';
import {
  ShieldCheck,
  Cpu,
  Zap,
  Activity,
  Lock,
  Layers,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  Play
} from 'lucide-react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid
} from 'recharts';

export const TrustEnginePage: React.FC = () => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedDeviceId, setSelectedDeviceId] = useState<string>('');
  const [breakdown, setBreakdown] = useState<TrustBreakdown | null>(null);
  const [loadingBreakdown, setLoadingBreakdown] = useState(false);

  // PDP Sandbox state
  const [pdpForm, setPdpForm] = useState({
    didUri: '',
    resource: 'iot/smartgrid/control',
    action: 'WRITE'
  });
  const [pdpResult, setPdpResult] = useState<AccessDecision | null>(null);
  const [evaluatingPdp, setEvaluatingPdp] = useState(false);

  useEffect(() => {
    loadDevices();
  }, []);

  const loadDevices = async () => {
    try {
      const res = await deviceService.getAllDevices(0, 50);
      setDevices(res.content);
      if (res.content.length > 0) {
        setSelectedDeviceId(res.content[0].id);
        loadBreakdown(res.content[0].id);
        setPdpForm((prev) => ({ ...prev, didUri: res.content[0].didUri }));
      }
    } catch (e) {
      console.error('Failed to load devices:', e);
    }
  };

  const loadBreakdown = async (deviceId: string) => {
    try {
      setLoadingBreakdown(true);
      const data = await trustService.getTrustBreakdown(deviceId);
      setBreakdown(data);
    } catch (e) {
      console.error('Failed to load trust breakdown:', e);
    } finally {
      setLoadingBreakdown(false);
    }
  };

  const handleDeviceChange = (id: string) => {
    setSelectedDeviceId(id);
    loadBreakdown(id);
    const dev = devices.find((d) => d.id === id);
    if (dev) {
      setPdpForm((prev) => ({ ...prev, didUri: dev.didUri }));
    }
  };

  const handleEvaluatePdp = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setEvaluatingPdp(true);
      const res = await trustService.evaluateAccess(pdpForm);
      setPdpResult(res);
      // Reload breakdown after recalculation
      if (selectedDeviceId) loadBreakdown(selectedDeviceId);
    } catch (err: any) {
      alert('PDP Evaluation failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setEvaluatingPdp(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Mathematical Trust Formula Banner */}
      <div className="glass-panel-glow p-5 rounded-xl">
        <div className="flex items-center gap-2 mb-2">
          <Sparkles className="w-5 h-5 text-indigo-400" />
          <h2 className="text-sm font-bold text-gray-100 uppercase tracking-wider font-mono">
            Mathematical Dynamic Trust Formulation T(t)
          </h2>
        </div>
        <div className="p-3 bg-gray-950/80 rounded-lg border border-indigo-500/30 overflow-x-auto text-center font-mono text-sm text-cyan-300">
          T(t) = [ 0.30 · C(t) + 0.25 · B(t) + 0.25 · F(t) + 0.20 · N(t) ] · e^(-0.0005 · Δt) - P(t)
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 mt-3 text-[11px] font-mono text-gray-400 text-center">
          <div className="bg-gray-900/60 p-2 rounded border border-gray-800">
            <span className="text-indigo-400 block font-bold">C(t) 30%</span>
            <span>Crypto / DID</span>
          </div>
          <div className="bg-gray-900/60 p-2 rounded border border-gray-800">
            <span className="text-cyan-400 block font-bold">B(t) 25%</span>
            <span>Behavioral</span>
          </div>
          <div className="bg-gray-900/60 p-2 rounded border border-gray-800">
            <span className="text-emerald-400 block font-bold">F(t) 25%</span>
            <span>Firmware Digest</span>
          </div>
          <div className="bg-gray-900/60 p-2 rounded border border-gray-800">
            <span className="text-amber-400 block font-bold">N(t) 20%</span>
            <span>Network Rate</span>
          </div>
          <div className="bg-gray-900/60 p-2 rounded border border-gray-800">
            <span className="text-rose-400 block font-bold">P(t) Penalty</span>
            <span>Threat Deduction</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Device Breakdown Explorer (2 Cols) */}
        <div className="lg:col-span-2 glass-panel p-5 rounded-xl space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-gray-100 flex items-center gap-2">
              <Zap className="w-4 h-4 text-amber-400" />
              Live Device Multi-Factor Score Breakdown
            </h3>
            <select
              value={selectedDeviceId}
              onChange={(e) => handleDeviceChange(e.target.value)}
              className="bg-gray-900 border border-gray-800 rounded-lg px-3 py-1.5 text-xs text-gray-200 focus:outline-none focus:border-indigo-500"
            >
              {devices.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.deviceName} ({d.currentTrustScore}/100)
                </option>
              ))}
            </select>
          </div>

          {loadingBreakdown || !breakdown ? (
            <div className="py-12 text-center text-gray-500">
              <div className="w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
              Calculating dynamic trust vectors...
            </div>
          ) : (
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 bg-gray-900/70 p-4 rounded-xl border border-gray-800">
                <div className="flex flex-col items-center justify-center">
                  <TrustGauge score={breakdown.currentTrustScore} size="md" />
                </div>
                <div className="sm:col-span-2 space-y-2 text-xs">
                  <div className="flex items-center justify-between pb-1 border-b border-gray-800">
                    <span className="text-gray-400">Target Node:</span>
                    <span className="font-semibold text-gray-200">{breakdown.deviceName}</span>
                  </div>
                  <div className="flex items-center justify-between pb-1 border-b border-gray-800">
                    <span className="text-gray-400">Risk Tier:</span>
                    <StatusBadge status={breakdown.riskLevel} />
                  </div>
                  <div className="flex items-center justify-between pb-1 border-b border-gray-800">
                    <span className="text-gray-400">Decay Elapsed:</span>
                    <span className="font-mono text-cyan-400">{breakdown.elapsedSecondsSinceHeartbeat} seconds</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Penalty Deduction P(t):</span>
                    <span className="font-mono text-rose-400 font-bold">-{breakdown.penaltyScore} pts</span>
                  </div>
                </div>
              </div>

              {/* Subscore progress bars */}
              <div className="space-y-2.5 text-xs">
                <div>
                  <div className="flex justify-between text-[11px] mb-1 font-mono">
                    <span className="text-indigo-400">C(t) Cryptographic Identity (Weight: 30%)</span>
                    <span>{breakdown.cryptoIdentityScore}/100</span>
                  </div>
                  <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
                    <div className="h-full bg-indigo-500 rounded-full transition-all duration-500" style={{ width: `${breakdown.cryptoIdentityScore}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-[11px] mb-1 font-mono">
                    <span className="text-cyan-400">B(t) Behavioral & Environmental (Weight: 25%)</span>
                    <span>{breakdown.behavioralScore}/100</span>
                  </div>
                  <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
                    <div className="h-full bg-cyan-400 rounded-full transition-all duration-500" style={{ width: `${breakdown.behavioralScore}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-[11px] mb-1 font-mono">
                    <span className="text-emerald-400">F(t) Firmware Attestation (Weight: 25%)</span>
                    <span>{breakdown.firmwareScore}/100</span>
                  </div>
                  <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
                    <div className="h-full bg-emerald-400 rounded-full transition-all duration-500" style={{ width: `${breakdown.firmwareScore}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-[11px] mb-1 font-mono">
                    <span className="text-amber-400">N(t) Network Packet Rate (Weight: 20%)</span>
                    <span>{breakdown.networkScore}/100</span>
                  </div>
                  <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
                    <div className="h-full bg-amber-400 rounded-full transition-all duration-500" style={{ width: `${breakdown.networkScore}%` }} />
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* PDP Access Decision Sandbox (1 Col) */}
        <div className="glass-panel p-5 rounded-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-3">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <h3 className="text-sm font-bold text-gray-100">Zero Trust PDP Decision Sandbox</h3>
            </div>
            <p className="text-xs text-gray-400 mb-4">
              Real-time Attribute-Based Access Control (ABAC) evaluation against on-chain policies.
            </p>

            <form onSubmit={handleEvaluatePdp} className="space-y-3 text-xs">
              <div>
                <label className="text-gray-400 font-mono block mb-1">Subject DID</label>
                <input
                  type="text"
                  required
                  value={pdpForm.didUri}
                  onChange={(e) => setPdpForm({ ...pdpForm, didUri: e.target.value })}
                  className="w-full bg-gray-900 border border-gray-800 rounded-lg p-2 font-mono text-[11px] text-indigo-300 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="text-gray-400 font-mono block mb-1">Requested Resource / Topic</label>
                <input
                  type="text"
                  required
                  value={pdpForm.resource}
                  onChange={(e) => setPdpForm({ ...pdpForm, resource: e.target.value })}
                  className="w-full bg-gray-900 border border-gray-800 rounded-lg p-2 font-mono text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="text-gray-400 font-mono block mb-1">Requested Action</label>
                <select
                  value={pdpForm.action}
                  onChange={(e) => setPdpForm({ ...pdpForm, action: e.target.value })}
                  className="w-full bg-gray-900 border border-gray-800 rounded-lg p-2 text-gray-200 focus:outline-none focus:border-indigo-500"
                >
                  <option value="READ">READ (Telemetry)</option>
                  <option value="WRITE">WRITE (Actuation / Config)</option>
                  <option value="EXECUTE">EXECUTE (Firmware / Reboot)</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>

              <button
                type="submit"
                disabled={evaluatingPdp}
                className="w-full py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold rounded-lg shadow-md shadow-indigo-600/20 transition-all flex items-center justify-center gap-2"
              >
                {evaluatingPdp ? (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  <Play className="w-4 h-4" />
                )}
                <span>Evaluate Access Decision</span>
              </button>
            </form>

            {pdpResult && (
              <div className={`mt-4 p-3.5 rounded-lg border text-xs ${
                pdpResult.granted
                  ? 'bg-emerald-950/30 border-emerald-500/40'
                  : 'bg-rose-950/30 border-rose-500/40'
              }`}>
                <div className="flex items-center justify-between mb-1.5">
                  <span className="font-bold text-gray-200">PDP Decision:</span>
                  <StatusBadge status={pdpResult.decision} />
                </div>
                <div className="text-xs text-gray-300 mb-2">
                  {pdpResult.reason}
                </div>
                <div className="text-[11px] font-mono text-gray-400 pt-2 border-t border-gray-800 flex justify-between">
                  <span>Trust at Request: {pdpResult.currentTrustScore}/100</span>
                  <span>Granted: {pdpResult.granted ? 'YES' : 'NO'}</span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
