import React from 'react';
import { DeviceStatus, RiskLevel, DecisionType, AlertSeverity } from '../../types';

export const StatusBadge: React.FC<{
  status?: DeviceStatus | RiskLevel | DecisionType | AlertSeverity | string;
  type?: 'status' | 'risk' | 'decision' | 'severity';
}> = ({ status, type = 'status' }) => {
  if (!status) return null;

  let bg = 'bg-gray-800/80 text-gray-300 border-gray-700';

  if (status === 'ACTIVE' || status === 'LOW_NOMINAL' || status === 'PERMIT_FULL' || status === 'LOW') {
    bg = 'bg-emerald-950/60 text-emerald-400 border-emerald-500/40 shadow-emerald-500/10 shadow-sm';
  } else if (status === 'MEDIUM_ELEVATED' || status === 'PERMIT_RESTRICTED' || status === 'MEDIUM') {
    bg = 'bg-amber-950/60 text-amber-400 border-amber-500/40 shadow-amber-500/10 shadow-sm';
  } else if (status === 'HIGH_SUSPICIOUS' || status === 'CHALLENGE_REAUTH' || status === 'HIGH') {
    bg = 'bg-orange-950/60 text-orange-400 border-orange-500/40 shadow-orange-500/10 shadow-sm';
  } else if (status === 'QUARANTINED' || status === 'CRITICAL_COMPROMISED' || status === 'DENY_QUARANTINE' || status === 'CRITICAL') {
    bg = 'bg-rose-950/60 text-rose-400 border-rose-500/50 shadow-rose-500/20 shadow-sm animate-pulse-slow';
  } else if (status === 'SUSPENDED' || status === 'DECOMMISSIONED') {
    bg = 'bg-gray-800 text-gray-400 border-gray-700';
  }

  const formatText = (text: string) => {
    return text.replace(/_/g, ' ');
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${bg}`}>
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-80" />
      {formatText(status)}
    </span>
  );
};
