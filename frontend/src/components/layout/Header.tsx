import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Shield, Radio, LogOut, Wallet, CheckCircle2 } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle?: string;
  onOpenRegisterModal?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ title, subtitle, onOpenRegisterModal }) => {
  const { user, logout } = useAuth();

  const truncateAddress = (addr?: string) => {
    if (!addr) return '';
    return `${addr.substring(0, 6)}...${addr.substring(addr.length - 4)}`;
  };

  return (
    <header className="h-16 border-b border-gray-800/80 bg-[#0B0F19]/80 backdrop-blur-md px-6 flex items-center justify-between flex-shrink-0 z-10">
      {/* Title & Path */}
      <div>
        <h1 className="text-base font-bold text-gray-100 flex items-center gap-2">
          {title}
        </h1>
        {subtitle && <p className="text-xs text-gray-400 font-normal">{subtitle}</p>}
      </div>

      {/* Right Controls */}
      <div className="flex items-center gap-3">
        {/* Real-Time WebSocket Link Status */}
        <div className="hidden sm:flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-950/40 border border-emerald-500/30 text-emerald-400 text-xs font-mono">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          <span>LIVE STOMP WS</span>
        </div>

        {/* Action Button if provided */}
        {onOpenRegisterModal && (
          <button
            onClick={onOpenRegisterModal}
            className="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/20 transition-all flex items-center gap-1.5"
          >
            <Shield className="w-3.5 h-3.5" />
            <span>Onboard Device</span>
          </button>
        )}

        {/* User / Wallet Profile */}
        <div className="flex items-center gap-2 pl-3 border-l border-gray-800">
          <div className="flex items-center gap-2 bg-gray-900/80 border border-gray-800 px-3 py-1 rounded-lg">
            {user?.walletAddress ? (
              <>
                <Wallet className="w-3.5 h-3.5 text-amber-400" />
                <span className="text-xs font-mono font-medium text-gray-200">
                  {truncateAddress(user.walletAddress)}
                </span>
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" title="MetaMask Connected" />
              </>
            ) : (
              <>
                <div className="w-6 h-6 rounded-full bg-indigo-600/30 border border-indigo-500/40 flex items-center justify-center text-xs font-bold text-indigo-300">
                  {user?.username ? user.username[0].toUpperCase() : 'U'}
                </div>
                <span className="text-xs font-medium text-gray-200">
                  {user?.username || 'Operator'}
                </span>
              </>
            )}
          </div>

          <button
            onClick={logout}
            title="Sign Out"
            className="p-1.5 rounded-lg text-gray-400 hover:text-rose-400 hover:bg-gray-800/80 transition-colors"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
