import React, { useEffect, useState } from 'react';
import { auditService } from '../services/auditService';
import { AuditLog, BlockchainTransaction } from '../types';
import {
  FileCode2,
  Layers,
  ShieldCheck,
  CheckCircle2,
  ExternalLink,
  RefreshCw,
  Hash,
  Clock
} from 'lucide-react';

export const AuditLedgerPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'audit' | 'blockchain'>('blockchain');
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [txs, setTxs] = useState<BlockchainTransaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [logsRes, txsRes] = await Promise.all([
        auditService.getAuditLogs(0, 30),
        auditService.getBlockchainTransactions(0, 30)
      ]);
      setLogs(logsRes.content);
      setTxs(txsRes.content);
    } catch (e) {
      console.error('Failed to load audit records:', e);
    } finally {
      setLoading(false);
    }
  };

  const truncate = (str?: string, front = 8, back = 6) => {
    if (!str) return 'N/A';
    if (str.length <= front + back) return str;
    return `${str.substring(0, front)}...${str.substring(str.length - back)}`;
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="glass-panel p-5 rounded-xl flex items-center justify-between">
        <div>
          <h2 className="text-base font-bold text-gray-100 flex items-center gap-2">
            <Layers className="w-5 h-5 text-cyan-400" />
            Immutable Audit Trail & Blockchain Ledger
          </h2>
          <p className="text-xs text-gray-400 mt-1">
            Tamper-evident system actions and Ethereum/Hardhat smart contract transactions with SHA-256 hash chaining.
          </p>
        </div>

        <button
          onClick={loadData}
          className="p-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 transition-colors"
          title="Refresh Ledger"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Tab Switcher */}
      <div className="flex items-center gap-2 border-b border-gray-800 pb-2">
        <button
          onClick={() => setActiveTab('blockchain')}
          className={`px-4 py-2 rounded-lg text-xs font-bold transition-all flex items-center gap-2 ${
            activeTab === 'blockchain'
              ? 'bg-cyan-950/60 text-cyan-300 border border-cyan-500/40'
              : 'text-gray-400 hover:text-gray-200'
          }`}
        >
          <Layers className="w-4 h-4" />
          <span>Smart Contract Transactions ({txs.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('audit')}
          className={`px-4 py-2 rounded-lg text-xs font-bold transition-all flex items-center gap-2 ${
            activeTab === 'audit'
              ? 'bg-indigo-950/60 text-indigo-300 border border-indigo-500/40'
              : 'text-gray-400 hover:text-gray-200'
          }`}
        >
          <FileCode2 className="w-4 h-4" />
          <span>Cryptographic Audit Log ({logs.length})</span>
        </button>
      </div>

      {/* Content Table */}
      <div className="glass-panel rounded-xl overflow-hidden">
        {activeTab === 'blockchain' ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-900/70 text-gray-400 uppercase font-mono text-[10px] border-b border-gray-800">
                <tr>
                  <th className="py-3 px-4">Transaction Hash</th>
                  <th className="py-3 px-4">Contract / Function</th>
                  <th className="py-3 px-4">Block #</th>
                  <th className="py-3 px-4">From / To Address</th>
                  <th className="py-3 px-4 text-center">Gas Used</th>
                  <th className="py-3 px-4 text-right">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800/60 font-mono text-[11px]">
                {loading ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-gray-400">Loading blockchain blocks...</td>
                  </tr>
                ) : txs.length > 0 ? (
                  txs.map((tx) => (
                    <tr key={tx.id} className="hover:bg-gray-800/30 transition-colors">
                      <td className="py-3 px-4 text-cyan-400 font-semibold flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                        <span>{truncate(tx.txHash, 10, 8)}</span>
                      </td>
                      <td className="py-3 px-4 text-gray-300">
                        <span className="font-semibold text-indigo-300">{tx.contractName}</span>
                        <span className="text-gray-500 block text-[10px]">{tx.functionName}()</span>
                      </td>
                      <td className="py-3 px-4 text-amber-400 font-bold">
                        #{tx.blockNumber || 1}
                      </td>
                      <td className="py-3 px-4 text-gray-400">
                        <div>From: {truncate(tx.fromAddress, 6, 4)}</div>
                        <div className="text-gray-600">To: {truncate(tx.toAddress, 6, 4)}</div>
                      </td>
                      <td className="py-3 px-4 text-center text-gray-300">
                        {tx.gasUsed ? tx.gasUsed.toLocaleString() : '21,000'}
                      </td>
                      <td className="py-3 px-4 text-right text-gray-500">
                        {new Date(tx.minedAt).toLocaleTimeString()}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-gray-500 font-sans text-xs">
                      No on-chain transactions recorded yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-900/70 text-gray-400 uppercase font-mono text-[10px] border-b border-gray-800">
                <tr>
                  <th className="py-3 px-4">Action Type</th>
                  <th className="py-3 px-4">Operator / User</th>
                  <th className="py-3 px-4">Entity</th>
                  <th className="py-3 px-4">Details</th>
                  <th className="py-3 px-4">SHA-256 Integrity Hash</th>
                  <th className="py-3 px-4 text-right">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800/60 text-[11px]">
                {loading ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-gray-400">Loading audit trail...</td>
                  </tr>
                ) : logs.length > 0 ? (
                  logs.map((log) => (
                    <tr key={log.id} className="hover:bg-gray-800/30 transition-colors">
                      <td className="py-3 px-4 font-semibold text-indigo-400 font-mono">
                        {log.actionType}
                      </td>
                      <td className="py-3 px-4 text-gray-300">
                        {log.username}
                      </td>
                      <td className="py-3 px-4 text-gray-400">
                        {log.targetEntity}
                      </td>
                      <td className="py-3 px-4 text-gray-300 max-w-xs truncate">
                        {log.details}
                      </td>
                      <td className="py-3 px-4 font-mono text-gray-500 text-[10px]">
                        {truncate(log.integrityHash, 8, 8)}
                      </td>
                      <td className="py-3 px-4 text-right font-mono text-gray-500">
                        {new Date(log.createdAt).toLocaleTimeString()}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-gray-500 text-xs">
                      No audit log records found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
