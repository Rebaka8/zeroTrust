import React, { useEffect, useState, useRef } from 'react';
import { datasetService } from '../services/datasetService';
import { deviceService } from '../services/deviceService';
import { DatasetPreset, DatasetBenchmarkResult, Device } from '../types';
import {
  Upload,
  Database,
  FileSpreadsheet,
  CheckCircle2,
  AlertTriangle,
  Play,
  Activity,
  Zap,
  ShieldCheck,
  ShieldAlert,
  Layers,
  Sparkles,
  Info,
  Clock
} from 'lucide-react';

export const DatasetBenchmarkPage: React.FC = () => {
  const [presets, setPresets] = useState<DatasetPreset[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedPreset, setSelectedPreset] = useState<string>('iot-23-smart-meter');
  const [selectedDeviceId, setSelectedDeviceId] = useState<string>('');

  // Custom CSV upload state
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [dragActive, setDragActive] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [loading, setLoading] = useState(true);
  const [executing, setExecuting] = useState(false);
  const [benchmarkResult, setBenchmarkResult] = useState<DatasetBenchmarkResult | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [presetList, devRes] = await Promise.all([
        datasetService.getPresets(),
        deviceService.getAllDevices(0, 50)
      ]);
      setPresets(presetList);
      setDevices(devRes.content);
      if (devRes.content.length > 0) {
        setSelectedDeviceId(devRes.content[0].id);
      }
    } catch (e) {
      console.error('Failed to load dataset studio data:', e);
    } finally {
      setLoading(false);
    }
  };

  const handleFileDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      if (file.name.endsWith('.csv')) {
        setSelectedFile(file);
      } else {
        alert('Please select a valid .csv file from Kaggle or your local storage.');
      }
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleRunPreset = async () => {
    try {
      setExecuting(true);
      setBenchmarkResult(null);
      const res = await datasetService.replayPreset(selectedPreset, selectedDeviceId || undefined);
      setBenchmarkResult(res);
      // Reload device states
      const devRes = await deviceService.getAllDevices(0, 50);
      setDevices(devRes.content);
    } catch (err: any) {
      alert('Preset replay failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setExecuting(false);
    }
  };

  const handleRunCustomCsv = async () => {
    if (!selectedFile) {
      alert('Please upload or drag & drop a .csv file first.');
      return;
    }

    try {
      setExecuting(true);
      setBenchmarkResult(null);
      const res = await datasetService.uploadAndReplayCustomCsv(selectedFile, selectedDeviceId || undefined);
      setBenchmarkResult(res);
      // Reload device states
      const devRes = await deviceService.getAllDevices(0, 50);
      setDevices(devRes.content);
    } catch (err: any) {
      alert('Custom CSV evaluation failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setExecuting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="glass-panel p-5 rounded-xl flex items-center justify-between">
        <div>
          <h2 className="text-base font-bold text-gray-100 flex items-center gap-2">
            <Database className="w-5 h-5 text-indigo-400" />
            Kaggle IoT Dataset Replay & Academic Benchmarking Studio
          </h2>
          <p className="text-xs text-gray-400 mt-1">
            Test the Zero Trust Risk Engine with real-world IoT datasets (IoT-23, CIC-IoT-2023) or upload your own Kaggle CSV to compute research-grade confusion matrix metrics.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Option 1: Upload Your Own Kaggle CSV */}
        <div className="glass-panel p-5 rounded-xl flex flex-col justify-between space-y-4">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <Upload className="w-4 h-4 text-cyan-400" />
              <h3 className="text-sm font-bold text-gray-100">1. Upload Custom Kaggle CSV Dataset</h3>
            </div>
            <p className="text-xs text-gray-400 mb-3">
              Drag & drop any IoT CSV file from Kaggle. Supported columns: <code className="text-indigo-300 font-mono">timestamp, temperature, cpu, packet_rate, label, attack_type</code>.
            </p>

            {/* Drag and Drop Zone */}
            <div
              onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
              onDragLeave={() => setDragActive(false)}
              onDrop={handleFileDrop}
              onClick={() => fileInputRef.current?.click()}
              className={`p-6 border-2 border-dashed rounded-xl flex flex-col items-center justify-center cursor-pointer transition-all ${
                dragActive
                  ? 'border-indigo-400 bg-indigo-950/40'
                  : 'border-gray-800 bg-gray-900/60 hover:bg-gray-800/40 hover:border-gray-700'
              }`}
            >
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv"
                onChange={handleFileChange}
                className="hidden"
              />
              <FileSpreadsheet className="w-8 h-8 text-indigo-400 mb-2" />
              {selectedFile ? (
                <div className="text-center">
                  <span className="text-xs font-bold text-emerald-400 block">{selectedFile.name}</span>
                  <span className="text-[11px] text-gray-500 font-mono">{(selectedFile.size / 1024).toFixed(1)} KB</span>
                </div>
              ) : (
                <div className="text-center text-xs text-gray-400">
                  <span className="font-semibold text-gray-200 block">Click to Browse or Drag & Drop</span>
                  <span>Any Kaggle .csv dataset</span>
                </div>
              )}
            </div>
          </div>

          <div className="space-y-3">
            <div className="text-xs">
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

            <button
              onClick={handleRunCustomCsv}
              disabled={executing || !selectedFile}
              className="w-full py-2.5 bg-gradient-to-r from-cyan-600 to-indigo-600 hover:from-cyan-500 hover:to-indigo-500 disabled:opacity-50 text-white text-xs font-bold rounded-lg shadow-lg shadow-cyan-600/20 transition-all flex items-center justify-center gap-2"
            >
              {executing ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                <Play className="w-4 h-4" />
              )}
              <span>Run Custom Dataset Benchmark</span>
            </button>
          </div>
        </div>

        {/* Option 2: Pre-bundled Kaggle Presets */}
        <div className="glass-panel p-5 rounded-xl flex flex-col justify-between space-y-4">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <Sparkles className="w-4 h-4 text-indigo-400" />
              <h3 className="text-sm font-bold text-gray-100">2. Built-in Kaggle Dataset Presets</h3>
            </div>
            <p className="text-xs text-gray-400 mb-3">
              One-click instant testing using verified Kaggle network traffic & attack captures.
            </p>

            <div className="space-y-3">
              {presets.map((preset) => {
                const isSelected = selectedPreset === preset.id;
                return (
                  <div
                    key={preset.id}
                    onClick={() => { setSelectedPreset(preset.id); setSelectedFile(null); }}
                    className={`p-3.5 rounded-xl border cursor-pointer transition-all ${
                      isSelected
                        ? 'bg-indigo-950/40 border-indigo-500/60 shadow-md shadow-indigo-500/10'
                        : 'bg-gray-900/60 border-gray-800 hover:bg-gray-800/40'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <h4 className="text-xs font-bold text-gray-100">{preset.name}</h4>
                      <span className="text-[10px] text-indigo-400 font-mono px-2 py-0.5 rounded bg-indigo-950 border border-indigo-500/30">
                        {preset.source}
                      </span>
                    </div>
                    <p className="text-xs text-gray-400 mb-2">{preset.description}</p>
                    <div className="text-[10px] font-mono text-gray-400">
                      <span className="text-indigo-300 font-semibold">Attacks Included: </span>
                      {preset.attackTypes}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          <button
            onClick={handleRunPreset}
            disabled={executing}
            className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-xs font-bold rounded-lg shadow-lg shadow-indigo-600/30 transition-all flex items-center justify-center gap-2"
          >
            {executing ? (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Play className="w-4 h-4" />
            )}
            <span>Replay & Evaluate Selected Preset</span>
          </button>
        </div>
      </div>

      {/* Real-Time Academic Performance & Confusion Matrix Results */}
      {benchmarkResult && (
        <div className="glass-panel-glow p-6 rounded-xl space-y-6 animate-in fade-in duration-300">
          <div className="flex items-center justify-between pb-3 border-b border-gray-800">
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-emerald-400" />
              <h3 className="text-sm font-bold text-gray-100">
                Evaluation Benchmark Results — {benchmarkResult.datasetName}
              </h3>
            </div>
            <span className="text-xs font-mono text-emerald-400 font-bold px-3 py-1 rounded-full bg-emerald-950/60 border border-emerald-500/30">
              Evaluated via Zero Trust Mathematical Engine
            </span>
          </div>

          {/* Metric Cards Banner */}
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
            <div className="bg-gray-900/80 p-3.5 rounded-xl border border-gray-800 text-center">
              <span className="text-[11px] text-gray-400 block mb-1">Detection Accuracy</span>
              <span className="text-xl font-extrabold font-mono text-emerald-400">
                {benchmarkResult.accuracyPercentage}%
              </span>
            </div>

            <div className="bg-gray-900/80 p-3.5 rounded-xl border border-gray-800 text-center">
              <span className="text-[11px] text-gray-400 block mb-1">Precision</span>
              <span className="text-xl font-extrabold font-mono text-indigo-400">
                {benchmarkResult.precisionPercentage}%
              </span>
            </div>

            <div className="bg-gray-900/80 p-3.5 rounded-xl border border-gray-800 text-center">
              <span className="text-[11px] text-gray-400 block mb-1">Recall (Sensitivity)</span>
              <span className="text-xl font-extrabold font-mono text-cyan-400">
                {benchmarkResult.recallPercentage}%
              </span>
            </div>

            <div className="bg-gray-900/80 p-3.5 rounded-xl border border-gray-800 text-center">
              <span className="text-[11px] text-gray-400 block mb-1">F1-Score</span>
              <span className="text-xl font-extrabold font-mono text-amber-400">
                {benchmarkResult.f1ScorePercentage}%
              </span>
            </div>

            <div className="bg-gray-900/80 p-3.5 rounded-xl border border-gray-800 text-center">
              <span className="text-[11px] text-gray-400 block mb-1">Avg Detection Latency</span>
              <span className="text-xl font-extrabold font-mono text-gray-200">
                {benchmarkResult.averageDetectionLatencyMs} <span className="text-xs text-gray-400">ms</span>
              </span>
            </div>
          </div>

          {/* Confusion Matrix Table & Breakdown */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            {/* Confusion Matrix */}
            <div className="bg-gray-950/80 p-4 rounded-xl border border-gray-800 space-y-3">
              <h4 className="font-bold text-gray-200 flex items-center gap-2">
                <Layers className="w-4 h-4 text-indigo-400" />
                Zero Trust Confusion Matrix
              </h4>
              <div className="grid grid-cols-2 gap-2 text-center font-mono">
                <div className="bg-emerald-950/40 p-3 rounded-lg border border-emerald-500/40">
                  <span className="text-emerald-400 font-bold block text-sm">{benchmarkResult.truePositives}</span>
                  <span className="text-[10px] text-gray-400">True Positives (Attacks Blocked)</span>
                </div>
                <div className="bg-gray-900 p-3 rounded-lg border border-gray-800">
                  <span className="text-gray-300 font-bold block text-sm">{benchmarkResult.falsePositives}</span>
                  <span className="text-[10px] text-gray-400">False Positives (Nominal Flagged)</span>
                </div>
                <div className="bg-gray-900 p-3 rounded-lg border border-gray-800">
                  <span className="text-gray-300 font-bold block text-sm">{benchmarkResult.falseNegatives}</span>
                  <span className="text-[10px] text-gray-400">False Negatives (Missed)</span>
                </div>
                <div className="bg-cyan-950/40 p-3 rounded-lg border border-cyan-500/40">
                  <span className="text-cyan-400 font-bold block text-sm">{benchmarkResult.trueNegatives}</span>
                  <span className="text-[10px] text-gray-400">True Negatives (Nominal Passed)</span>
                </div>
              </div>
            </div>

            {/* Threat Signatures & Summary */}
            <div className="bg-gray-950/80 p-4 rounded-xl border border-gray-800 space-y-3">
              <h4 className="font-bold text-gray-200 flex items-center gap-2">
                <ShieldAlert className="w-4 h-4 text-rose-400" />
                Detected Adversarial Attack Waves
              </h4>
              <div className="flex flex-wrap gap-1.5">
                {benchmarkResult.detectedThreatSignatures && benchmarkResult.detectedThreatSignatures.length > 0 ? (
                  benchmarkResult.detectedThreatSignatures.map((sig, i) => (
                    <span key={i} className="px-2.5 py-1 rounded bg-rose-950/60 border border-rose-500/40 text-rose-300 font-mono text-[11px]">
                      ⚠ {sig}
                    </span>
                  ))
                ) : (
                  <span className="text-gray-500 text-xs">No active malicious signatures in this stream</span>
                )}
              </div>

              <div className="pt-2 border-t border-gray-800 text-[11px] text-gray-400">
                <p>{benchmarkResult.defenseEvaluationSummary}</p>
                <span className="text-indigo-400 block mt-1 font-semibold">
                  Autonomous Quarantines Enforced: {benchmarkResult.automaticQuarantinesEnforced}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
