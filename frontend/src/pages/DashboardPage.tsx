import React, { useEffect, useState } from 'react';
import { auditService } from '../services/auditService';
import { DashboardStats, TelemetryData } from '../types';
import { useWebSocket } from '../context/WebSocketContext';
import { StatusBadge } from '../components/common/StatusBadge';
import { TrustGauge } from '../components/common/TrustGauge';
import { AlertItem } from '../components/common/AlertItem';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  AreaChart,
  Area
} from 'recharts';
import {
  Cpu,
  ShieldCheck,
  ShieldAlert,
  Radio,
  Activity,
  Layers,
  ArrowUpRight,
  TrendingUp,
  AlertCircle,
  Zap
} from 'lucide-react';

export const DashboardPage: React.FC<{ onNavigateTo: (tab: string) => void }> = ({ onNavigateTo }) => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [telemetryHistory, setTelemetryHistory] = useState<{ time: string; temp: number; cpu: number; packets: number }[]>([]);

  const { lastTelemetry, lastAlert } = useWebSocket();

  useEffect(() => {
    loadDashboard();
  }, []);

  // Update real-time charts upon incoming WebSocket telemetry
  useEffect(() => {
    if (lastTelemetry) {
      const timeStr = new Date(lastTelemetry.recordedAt).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });

      setTelemetryHistory((prev) => {
        const next = [
          ...prev,
          {
            time: timeStr,
            temp: lastTelemetry.temperature ? Number(lastTelemetry.temperature) : 25,
            cpu: lastTelemetry.cpuUtilization ? Number(lastTelemetry.cpuUtilization) : 20,
            packets: lastTelemetry.packetRate || 10
          }
        ];
        return next.slice(-20); // Keep latest 20 telemetry ticks
      });
    }
  }, [lastTelemetry]);

  // Prepend incoming live alert
  useEffect(() => {
    if (lastAlert && stats) {
      setStats((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          recentAlerts: [lastAlert, ...prev.recentAlerts.slice(0, 5)],
          activeAlertsCount: prev.activeAlertsCount + 1
        };
      });
    }
  }, [lastAlert]);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await auditService.getDashboardStats();
      setStats(data);

      // Seed initial dummy telemetry curve if empty
      const initialTimeline = Array.from({ length: 12 }, (_, i) => {
        const d = new Date(Date.now() - (12 - i) * 5000);
        return {
          time: d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
          temp: 24 + Math.sin(i) * 3,
          cpu: 15 + Math.cos(i) * 8,
          packets: 12 + Math.floor(Math.random() * 8)
        };
      });
      setTelemetryHistory(initialTimeline);
    } catch (e: any) {
      console.error('Failed to load dashboard stats:', e);
      setError(e?.response?.data?.message || 'Failed to load telemetry and fleet metrics from backend.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
          <span className="text-xs font-mono text-gray-400">Loading Zero Trust Security Mesh...</span>
        </div>
      </div>
    );
  }

  if (error || !stats) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="glass-panel p-8 rounded-2xl max-w-md w-full text-center space-y-4 border border-rose-500/30">
          <div className="w-12 h-12 mx-auto rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-400">
            <AlertCircle className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-white">Connection or Session Error</h3>
            <p className="text-xs text-gray-400 mt-1">
              {error || 'Unable to load dashboard metrics from backend.'}
            </p>
          </div>
          <div className="flex items-center justify-center gap-3 pt-2">
            <button
              onClick={loadDashboard}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs font-semibold transition-colors"
            >
              Retry Connection
            </button>
            <button
              onClick={() => {
                localStorage.removeItem('zt_token');
                localStorage.removeItem('zt_user');
                window.dispatchEvent(new Event('auth:unauthorized'));
              }}
              className="px-4 py-2 bg-gray-800 hover:bg-gray-700 text-gray-300 rounded-lg text-xs font-semibold transition-colors border border-gray-700"
            >
              Sign In Again
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Metric Cards Banner */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {/* Total Devices */}
        <div className="glass-panel p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-400">Managed IoT Fleet</p>
            <h3 className="text-2xl font-extrabold text-white font-mono mt-1">
              {stats.totalDevices}
            </h3>
            <div className="flex items-center gap-1.5 mt-2 text-[11px] text-emerald-400">
              <TrendingUp className="w-3.5 h-3.5" />
              <span>{stats.activeDevices} Active Nodes</span>
            </div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-indigo-950/60 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
            <Cpu className="w-6 h-6" />
          </div>
        </div>

        {/* Fleet Average Trust Score */}
        <div className="glass-panel p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-400">Fleet Trust Score T(t)</p>
            <h3 className="text-2xl font-extrabold text-white font-mono mt-1">
              {stats.averageTrustScore}
              <span className="text-xs text-gray-500 font-normal"> / 100</span>
            </h3>
            <div className="flex items-center gap-1.5 mt-2 text-[11px] text-indigo-400">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>Continuous Attestation</span>
            </div>
          </div>
          <div className="w-14 h-14 flex items-center justify-center">
            <TrustGauge score={stats.averageTrustScore} size="sm" showLabel={false} />
          </div>
        </div>

        {/* Zero Trust Decision Latency */}
        <div className="glass-panel p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-400">PDP Decision Latency</p>
            <h3 className="text-2xl font-extrabold text-white font-mono mt-1">
              {stats.averageDecisionLatencyMs || 14.2}
              <span className="text-xs text-gray-500 font-normal"> ms</span>
            </h3>
            <div className="flex items-center gap-1.5 mt-2 text-[11px] text-amber-400">
              <Zap className="w-3.5 h-3.5" />
              <span>&lt; 20ms Real-Time SLA</span>
            </div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-amber-950/60 border border-amber-500/30 flex items-center justify-center text-amber-400">
            <Zap className="w-6 h-6" />
          </div>
        </div>

        {/* Quarantined Nodes */}
        <div className="glass-panel p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-400">Autonomous Quarantine</p>
            <h3 className="text-2xl font-extrabold text-white font-mono mt-1">
              {stats.quarantinedDevices}
            </h3>
            <div className="flex items-center gap-1.5 mt-2 text-[11px] text-rose-400">
              <ShieldAlert className="w-3.5 h-3.5" />
              <span>{stats.criticalAlertsCount} Critical Violations</span>
            </div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-rose-950/60 border border-rose-500/30 flex items-center justify-center text-rose-400">
            <ShieldAlert className="w-6 h-6" />
          </div>
        </div>

        {/* Blockchain Anchors */}
        <div className="glass-panel p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-400">On-Chain Anchors</p>
            <h3 className="text-2xl font-extrabold text-white font-mono mt-1">
              {stats.totalOnChainTxCount}
            </h3>
            <div className="flex items-center gap-1.5 mt-2 text-[11px] text-cyan-400">
              <Layers className="w-3.5 h-3.5" />
              <span>Immutable Ledger</span>
            </div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-cyan-950/60 border border-cyan-500/30 flex items-center justify-center text-cyan-400">
            <Layers className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Real-Time Telemetry Graph & Threat Feed */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Real-Time Telemetry Ingestion Chart (2 Cols) */}
        <div className="lg:col-span-2 glass-panel p-5 rounded-xl flex flex-col justify-between">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-bold text-gray-100 flex items-center gap-2">
                <Activity className="w-4 h-4 text-indigo-400" />
                Live Ingested Telemetry Signals (MQTT / WebSockets)
              </h2>
              <p className="text-xs text-gray-400">
                Continuous hardware sensor & packet frequency telemetry streaming
              </p>
            </div>
            <div className="flex items-center gap-3 text-xs font-mono">
              <div className="flex items-center gap-1.5 text-indigo-400">
                <span className="w-2.5 h-2.5 rounded-full bg-indigo-500" />
                <span>CPU %</span>
              </div>
              <div className="flex items-center gap-1.5 text-cyan-400">
                <span className="w-2.5 h-2.5 rounded-full bg-cyan-400" />
                <span>Temp (°C)</span>
              </div>
            </div>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={telemetryHistory}>
                <defs>
                  <linearGradient id="cpuGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366F1" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#6366F1" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="tempGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#00D8FF" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#00D8FF" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1F2937" />
                <XAxis dataKey="time" stroke="#6B7280" tick={{ fontSize: 10, fill: '#9CA3AF' }} />
                <YAxis stroke="#6B7280" tick={{ fontSize: 10, fill: '#9CA3AF' }} domain={[0, 100]} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#111827',
                    borderColor: '#374151',
                    borderRadius: '8px',
                    fontSize: '12px',
                    color: '#F3F4F6'
                  }}
                />
                <Area type="monotone" dataKey="cpu" stroke="#6366F1" strokeWidth={2} fillOpacity={1} fill="url(#cpuGrad)" name="CPU Usage (%)" />
                <Area type="monotone" dataKey="temp" stroke="#00D8FF" strokeWidth={2} fillOpacity={1} fill="url(#tempGrad)" name="Temperature (°C)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Live Threat Alert Feed (1 Col) */}
        <div className="glass-panel p-5 rounded-xl flex flex-col justify-between">
          <div className="flex items-center justify-between mb-3 border-b border-gray-800 pb-3">
            <div>
              <h2 className="text-sm font-bold text-gray-100 flex items-center gap-2">
                <ShieldAlert className="w-4 h-4 text-rose-400" />
                Active Security Threat Feed
              </h2>
              <p className="text-[11px] text-gray-400">Zero Trust Anomaly Detection Stream</p>
            </div>
            <button
              onClick={() => onNavigateTo('simulation')}
              className="text-[11px] text-indigo-400 hover:text-indigo-300 font-semibold flex items-center gap-1"
            >
              <span>Simulate</span>
              <ArrowUpRight className="w-3 h-3" />
            </button>
          </div>

          <div className="space-y-2.5 overflow-y-auto max-h-64 pr-1">
            {stats.recentAlerts && stats.recentAlerts.length > 0 ? (
              stats.recentAlerts.map((alert) => (
                <AlertItem key={alert.id} alert={alert} />
              ))
            ) : (
              <div className="p-6 text-center text-gray-500 text-xs">
                <ShieldCheck className="w-8 h-8 mx-auto text-emerald-500/40 mb-2" />
                No active threats detected. All IoT nodes attested.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Top IoT Devices & Quick Actions */}
      <div className="glass-panel p-5 rounded-xl">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-sm font-bold text-gray-100">Monitored Fleet Nodes</h2>
            <p className="text-xs text-gray-400">Live hardware identity, W3C DIDs, and trust attestation levels</p>
          </div>
          <button
            onClick={() => onNavigateTo('devices')}
            className="px-3 py-1 rounded-lg bg-gray-800 hover:bg-gray-700 text-xs font-semibold text-gray-200 transition-all flex items-center gap-1.5"
          >
            <span>View All Fleet</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-gray-900/60 text-gray-400 uppercase font-mono text-[10px] border-b border-gray-800">
              <tr>
                <th className="py-2.5 px-3">Device Name</th>
                <th className="py-2.5 px-3">W3C DID URI</th>
                <th className="py-2.5 px-3">Hardware Model</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3 text-center">Trust Score T(t)</th>
                <th className="py-2.5 px-3 text-right">Heartbeat</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800/60">
              {stats.topDevices && stats.topDevices.length > 0 ? (
                stats.topDevices.map((device) => (
                  <tr key={device.id} className="hover:bg-gray-800/30 transition-colors">
                    <td className="py-3 px-3 font-semibold text-gray-200">
                      {device.deviceName}
                    </td>
                    <td className="py-3 px-3 font-mono text-indigo-400 text-[11px] truncate max-w-[200px]">
                      {device.didUri}
                    </td>
                    <td className="py-3 px-3 text-gray-400">
                      {device.hardwareModel}
                    </td>
                    <td className="py-3 px-3">
                      <StatusBadge status={device.status} />
                    </td>
                    <td className="py-3 px-3 text-center">
                      <span className={`font-mono font-bold text-xs ${
                        device.currentTrustScore >= 80 ? 'text-emerald-400' :
                        device.currentTrustScore >= 60 ? 'text-amber-400' :
                        device.currentTrustScore >= 35 ? 'text-orange-400' : 'text-rose-400'
                      }`}>
                        {device.currentTrustScore}/100
                      </span>
                    </td>
                    <td className="py-3 px-3 text-right font-mono text-gray-500 text-[11px]">
                      {device.lastHeartbeat ? new Date(device.lastHeartbeat).toLocaleTimeString() : 'Online'}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="py-6 text-center text-gray-500">
                    No devices onboarded yet. Click "Onboard Device" or use the Simulation Arena to provision virtual nodes.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
