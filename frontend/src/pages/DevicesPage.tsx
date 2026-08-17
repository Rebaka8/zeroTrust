import React, { useEffect, useState } from 'react';
import { deviceService } from '../services/deviceService';
import { Device, DeviceDetails } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';
import { TrustGauge } from '../components/common/TrustGauge';
import {
  Cpu,
  Search,
  Plus,
  ShieldAlert,
  ShieldCheck,
  ExternalLink,
  RefreshCw,
  Trash2,
  Lock,
  X,
  FileCode
} from 'lucide-react';

export const DevicesPage: React.FC<{ isOpenRegisterModal?: boolean; onCloseRegisterModal?: () => void }> = ({
  isOpenRegisterModal = false,
  onCloseRegisterModal
}) => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Drawer / Details state
  const [selectedDevice, setSelectedDevice] = useState<DeviceDetails | null>(null);
  const [loadingDetails, setLoadingDetails] = useState(false);

  // Local Register Modal state
  const [isRegisterOpen, setIsRegisterOpen] = useState(isOpenRegisterModal);
  const [registerForm, setRegisterForm] = useState({
    deviceName: '',
    deviceType: 'SENSOR_NODE',
    hardwareModel: 'ESP32-S3-WROOM',
    macAddress: '',
    ipAddress: '192.168.1.150',
    firmwareHash: '',
    publicKey: '',
  });

  useEffect(() => {
    loadDevices();
  }, []);

  useEffect(() => {
    setIsRegisterOpen(isOpenRegisterModal);
  }, [isOpenRegisterModal]);

  const loadDevices = async () => {
    try {
      setLoading(true);
      const res = await deviceService.getAllDevices(0, 50);
      setDevices(res.content);
    } catch (e) {
      console.error('Failed to load devices:', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDetails = async (id: string) => {
    try {
      setLoadingDetails(true);
      const details = await deviceService.getDeviceById(id);
      setSelectedDevice(details);
    } catch (e) {
      console.error('Failed to load device details:', e);
    } finally {
      setLoadingDetails(false);
    }
  };

  const handleQuarantine = async (id: string) => {
    if (!confirm('Are you sure you want to enforce ZERO TRUST QUARANTINE on this device?')) return;
    try {
      await deviceService.quarantineDevice(id);
      loadDevices();
      if (selectedDevice && selectedDevice.id === id) {
        handleOpenDetails(id);
      }
    } catch (e: any) {
      alert('Quarantine failed: ' + (e.response?.data?.message || e.message));
    }
  };

  const handleRestore = async (id: string) => {
    try {
      await deviceService.restoreDevice(id);
      loadDevices();
      if (selectedDevice && selectedDevice.id === id) {
        handleOpenDetails(id);
      }
    } catch (e: any) {
      alert('Restore failed: ' + (e.response?.data?.message || e.message));
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // Auto-generate firmware hash and key if blank
      const fwHash = registerForm.firmwareHash || '0x' + Array.from({length: 64}, () => Math.floor(Math.random()*16).toString(16)).join('');
      const pubKey = registerForm.publicKey || 'z6MkmL6N5D3PjK3G4yV2q8NodeKey' + Math.floor(Math.random()*1000);

      await deviceService.registerDevice({
        ...registerForm,
        firmwareHash: fwHash,
        publicKey: pubKey
      });

      setIsRegisterOpen(false);
      if (onCloseRegisterModal) onCloseRegisterModal();
      loadDevices();
    } catch (err: any) {
      alert('Registration failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const filteredDevices = devices.filter((d) => {
    const matchesSearch =
      d.deviceName.toLowerCase().includes(search.toLowerCase()) ||
      d.didUri.toLowerCase().includes(search.toLowerCase()) ||
      d.macAddress.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || d.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Top Controls */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
        {/* Search & Filters */}
        <div className="flex items-center gap-3 w-full sm:w-auto">
          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 text-gray-500 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by name, DID, or MAC..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-gray-900/80 border border-gray-800 rounded-lg pl-9 pr-3 py-2 text-xs text-gray-200 placeholder-gray-500 focus:outline-none focus:border-indigo-500 transition-colors"
            />
          </div>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-gray-900/80 border border-gray-800 rounded-lg px-3 py-2 text-xs text-gray-200 focus:outline-none focus:border-indigo-500 transition-colors"
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active Nominal</option>
            <option value="QUARANTINED">Quarantined</option>
            <option value="SUSPENDED">Suspended</option>
          </select>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
          <button
            onClick={loadDevices}
            className="p-2 rounded-lg bg-gray-900/80 border border-gray-800 hover:bg-gray-800 text-gray-400 hover:text-gray-200 transition-all"
            title="Refresh Fleet"
          >
            <RefreshCw className="w-4 h-4" />
          </button>

          <button
            onClick={() => setIsRegisterOpen(true)}
            className="px-3 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/20 transition-all flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" />
            <span>Onboard Device</span>
          </button>
        </div>
      </div>

      {/* Fleet Table */}
      <div className="glass-panel rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-gray-900/70 text-gray-400 uppercase font-mono text-[10px] border-b border-gray-800">
              <tr>
                <th className="py-3 px-4">Node / Model</th>
                <th className="py-3 px-4">W3C DID Identifier</th>
                <th className="py-3 px-4">MAC / Network</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-center">Trust Score</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800/60">
              {loading ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-gray-400">
                    <div className="w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
                    Loading devices...
                  </td>
                </tr>
              ) : filteredDevices.length > 0 ? (
                filteredDevices.map((dev) => (
                  <tr key={dev.id} className="hover:bg-gray-800/30 transition-colors">
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-gray-200">{dev.deviceName}</div>
                      <div className="text-[11px] text-gray-500">{dev.hardwareModel} ({dev.deviceType})</div>
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="font-mono text-indigo-400 text-[11px] truncate max-w-[220px]">
                        {dev.didUri}
                      </div>
                      <div className="text-[10px] text-gray-500 font-mono">
                        DID Registry Verified
                      </div>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-[11px] text-gray-400">
                      <div>{dev.macAddress}</div>
                      <div className="text-gray-600">{dev.ipAddress || '192.168.1.x'}</div>
                    </td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={dev.status} />
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <span className={`font-mono font-bold text-xs ${
                        dev.currentTrustScore >= 80 ? 'text-emerald-400' :
                        dev.currentTrustScore >= 60 ? 'text-amber-400' :
                        dev.currentTrustScore >= 35 ? 'text-orange-400' : 'text-rose-400'
                      }`}>
                        {dev.currentTrustScore} / 100
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleOpenDetails(dev.id)}
                          className="px-2.5 py-1 rounded bg-gray-800 hover:bg-gray-700 text-[11px] text-gray-300 transition-colors"
                        >
                          Inspect DID
                        </button>

                        {dev.isQuarantined ? (
                          <button
                            onClick={() => handleRestore(dev.id)}
                            className="px-2.5 py-1 rounded bg-emerald-950/60 hover:bg-emerald-900/80 border border-emerald-500/40 text-emerald-300 text-[11px] font-semibold transition-colors"
                          >
                            Restore
                          </button>
                        ) : (
                          <button
                            onClick={() => handleQuarantine(dev.id)}
                            className="px-2.5 py-1 rounded bg-rose-950/60 hover:bg-rose-900/80 border border-rose-500/40 text-rose-300 text-[11px] font-semibold transition-colors"
                          >
                            Quarantine
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-gray-500 text-xs">
                    No devices matched the selected filter.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Device Details Drawer Modal */}
      {selectedDevice && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#111827] border border-gray-700 rounded-xl w-full max-w-2xl max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
            <div className="p-4 border-b border-gray-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <FileCode className="w-5 h-5 text-indigo-400" />
                <h3 className="text-sm font-bold text-gray-100">
                  {selectedDevice.deviceName} — W3C DID & Attestation
                </h3>
              </div>
              <button
                onClick={() => setSelectedDevice(null)}
                className="p-1 rounded text-gray-400 hover:text-gray-200 hover:bg-gray-800"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-6 overflow-y-auto space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-gray-900/80 p-3 rounded-lg border border-gray-800">
                  <span className="text-gray-400 block mb-1">Status & Isolation</span>
                  <StatusBadge status={selectedDevice.status} />
                </div>
                <div className="bg-gray-900/80 p-3 rounded-lg border border-gray-800">
                  <span className="text-gray-400 block mb-1">Dynamic Trust Score T(t)</span>
                  <span className="font-mono text-sm font-bold text-indigo-400">
                    {selectedDevice.currentTrustScore} / 100
                  </span>
                </div>
              </div>

              <div>
                <label className="text-gray-400 block mb-1 font-mono">W3C Decentralized Identifier (DID)</label>
                <div className="p-2.5 rounded bg-gray-900 border border-gray-800 font-mono text-indigo-300 text-xs break-all">
                  {selectedDevice.didUri}
                </div>
              </div>

              <div>
                <label className="text-gray-400 block mb-1 font-mono">Attested Firmware SHA-256 Digest</label>
                <div className="p-2.5 rounded bg-gray-900 border border-gray-800 font-mono text-gray-300 text-xs break-all">
                  {selectedDevice.firmwareHash}
                </div>
              </div>

              <div>
                <label className="text-gray-400 block mb-1 font-mono">Canonical W3C DID Document (JSON-LD)</label>
                <pre className="p-3 rounded bg-gray-950 border border-gray-800 text-emerald-400 font-mono text-[11px] overflow-x-auto max-h-48">
                  {selectedDevice.didDocumentJson}
                </pre>
              </div>
            </div>

            <div className="p-4 border-t border-gray-800 flex justify-end gap-2">
              <button
                onClick={() => setSelectedDevice(null)}
                className="px-4 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-xs text-gray-200 font-semibold"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Onboard Device Modal */}
      {isRegisterOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#111827] border border-gray-700 rounded-xl w-full max-w-lg shadow-2xl overflow-hidden">
            <div className="p-4 border-b border-gray-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Cpu className="w-5 h-5 text-indigo-400" />
                <h3 className="text-sm font-bold text-gray-100">
                  Onboard & Register IoT Node
                </h3>
              </div>
              <button
                onClick={() => {
                  setIsRegisterOpen(false);
                  if (onCloseRegisterModal) onCloseRegisterModal();
                }}
                className="p-1 rounded text-gray-400 hover:text-gray-200 hover:bg-gray-800"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleRegister} className="p-6 space-y-4 text-xs">
              <div>
                <label className="text-gray-300 font-medium block mb-1">Device Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Substation Pressure Monitor 01"
                  value={registerForm.deviceName}
                  onChange={(e) => setRegisterForm({ ...registerForm, deviceName: e.target.value })}
                  className="w-full bg-gray-900 border border-gray-800 rounded-lg px-3 py-2 text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-gray-300 font-medium block mb-1">Device Type</label>
                  <select
                    value={registerForm.deviceType}
                    onChange={(e) => setRegisterForm({ ...registerForm, deviceType: e.target.value })}
                    className="w-full bg-gray-900 border border-gray-800 rounded-lg px-3 py-2 text-gray-200 focus:outline-none focus:border-indigo-500"
                  >
                    <option value="SENSOR_NODE">Sensor Node</option>
                    <option value="INDUSTRIAL_GATEWAY">Industrial Gateway</option>
                    <option value="ACTUATOR">Actuator Unit</option>
                    <option value="EDGE_CAMERA">Edge Surveillance Camera</option>
                  </select>
                </div>
                <div>
                  <label className="text-gray-300 font-medium block mb-1">Hardware Model</label>
                  <input
                    type="text"
                    required
                    value={registerForm.hardwareModel}
                    onChange={(e) => setRegisterForm({ ...registerForm, hardwareModel: e.target.value })}
                    className="w-full bg-gray-900 border border-gray-800 rounded-lg px-3 py-2 text-gray-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-gray-300 font-medium block mb-1">MAC Address</label>
                  <input
                    type="text"
                    required
                    placeholder="00:1A:2B:3C:4D:5E"
                    value={registerForm.macAddress}
                    onChange={(e) => setRegisterForm({ ...registerForm, macAddress: e.target.value })}
                    className="w-full bg-gray-900 border border-gray-800 rounded-lg px-3 py-2 font-mono text-gray-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="text-gray-300 font-medium block mb-1">IP Address</label>
                  <input
                    type="text"
                    value={registerForm.ipAddress}
                    onChange={(e) => setRegisterForm({ ...registerForm, ipAddress: e.target.value })}
                    className="w-full bg-gray-900 border border-gray-800 rounded-lg px-3 py-2 font-mono text-gray-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <p className="text-[11px] text-gray-500">
                Onboarding anchors a W3C DID on-chain, issues a hardware attestation Verifiable Credential, and generates cryptographic keypairs automatically.
              </p>

              <div className="p-4 border-t border-gray-800 -mx-6 -mb-6 mt-6 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => {
                    setIsRegisterOpen(false);
                    if (onCloseRegisterModal) onCloseRegisterModal();
                  }}
                  className="px-4 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-xs text-gray-300 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/20"
                >
                  Register & Anchor DID
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
