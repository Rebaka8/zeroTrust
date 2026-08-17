import React from 'react';
import { SecurityAlert } from '../../types';
import { ShieldAlert, AlertTriangle, Info, AlertCircle } from 'lucide-react';

export const AlertItem: React.FC<{ alert: SecurityAlert }> = ({ alert }) => {
  const getIcon = () => {
    switch (alert.severity) {
      case 'CRITICAL':
        return <ShieldAlert className="w-5 h-5 text-rose-400" />;
      case 'HIGH':
        return <AlertTriangle className="w-5 h-5 text-orange-400" />;
      case 'MEDIUM':
        return <AlertCircle className="w-5 h-5 text-amber-400" />;
      default:
        return <Info className="w-5 h-5 text-blue-400" />;
    }
  };

  const getBorderColor = () => {
    switch (alert.severity) {
      case 'CRITICAL':
        return 'border-l-rose-500 bg-rose-950/20';
      case 'HIGH':
        return 'border-l-orange-500 bg-orange-950/20';
      case 'MEDIUM':
        return 'border-l-amber-500 bg-amber-950/20';
      default:
        return 'border-l-blue-500 bg-blue-950/20';
    }
  };

  const formattedTime = new Date(alert.triggeredAt).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });

  return (
    <div className={`p-3 rounded-r-lg border border-gray-800 border-l-4 ${getBorderColor()} transition-all hover:bg-gray-800/40 flex items-start gap-3`}>
      <div className="mt-0.5 flex-shrink-0">{getIcon()}</div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between gap-2">
          <span className="text-xs font-semibold text-gray-200 truncate">
            {alert.alertType.replace(/_/g, ' ')}
          </span>
          <span className="text-[10px] text-gray-500 font-mono flex-shrink-0">
            {formattedTime}
          </span>
        </div>
        <p className="text-xs text-gray-400 mt-1 line-clamp-2">
          {alert.description}
        </p>
        <div className="flex items-center gap-2 mt-2">
          <span className="text-[11px] font-mono text-indigo-400">
            {alert.deviceName || 'System Node'}
          </span>
          {alert.didUri && (
            <span className="text-[10px] text-gray-500 font-mono truncate max-w-[150px]">
              {alert.didUri}
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
