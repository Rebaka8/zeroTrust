import React from 'react';
import {
  LayoutDashboard,
  Cpu,
  Fingerprint,
  ShieldCheck,
  Flame,
  FileCode2,
  Lock
} from 'lucide-react';

interface SidebarProps {
  currentTab: string;
  onSelectTab: (tab: string) => void;
  activeAlertsCount?: number;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentTab,
  onSelectTab,
  activeAlertsCount = 0
}) => {
  const navItems = [
    { id: 'dashboard', label: 'Executive Dashboard', icon: LayoutDashboard },
    { id: 'devices', label: 'IoT Fleet Manager', icon: Cpu },
    { id: 'credentials', label: 'DIDs & Credentials', icon: Fingerprint },
    { id: 'trust', label: 'Risk Engine & ABAC', icon: ShieldCheck },
    { id: 'simulation', label: 'Attack Simulator', icon: Flame, badge: 'Live Arena' },
    { id: 'dataset', label: 'Dataset Benchmarks', icon: FileCode2, badge: 'Kaggle ML' },
    { id: 'audit', label: 'Blockchain Audit Ledger', icon: Lock },
  ];

  return (
    <aside className="w-64 bg-[#0E1322] border-r border-gray-800/80 flex flex-col justify-between flex-shrink-0 z-20">
      {/* Brand Header */}
      <div>
        <div className="p-5 border-b border-gray-800/80 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 p-0.5 shadow-lg shadow-indigo-500/20">
            <div className="w-full h-full bg-[#0B0F19] rounded-[10px] flex items-center justify-center">
              <Lock className="w-5 h-5 text-indigo-400" />
            </div>
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-extrabold text-sm tracking-wider text-transparent bg-clip-text bg-gradient-to-r from-white via-indigo-200 to-cyan-400">
                ZERO TRUST
              </span>
              <span className="text-[10px] px-1.5 py-0.5 bg-indigo-950 text-indigo-400 border border-indigo-500/30 rounded font-mono font-bold">
                IoT
              </span>
            </div>
            <p className="text-[11px] text-gray-500 font-mono tracking-tight">
              Web3 Identity Framework
            </p>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="p-3 space-y-1.5">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all duration-200 ${
                  isActive
                    ? 'bg-indigo-600/15 text-indigo-400 border border-indigo-500/40 shadow-sm shadow-indigo-500/10'
                    : 'text-gray-400 hover:text-gray-200 hover:bg-gray-800/50'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Icon className={`w-4 h-4 ${isActive ? 'text-indigo-400' : 'text-gray-400'}`} />
                  <span>{item.label}</span>
                </div>

                {item.badge && (
                  <span className="text-[10px] px-1.5 py-0.5 bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded-full font-mono font-semibold">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Footer Security Badge */}
      <div className="p-4 border-t border-gray-800/80">
        <div className="p-3 rounded-lg bg-gray-900/80 border border-gray-800">
          <div className="flex items-center justify-between text-xs">
            <span className="text-gray-400">Threat Alerts</span>
            <span className={`px-2 py-0.5 rounded-full font-mono text-[11px] font-bold ${
              activeAlertsCount > 0
                ? 'bg-rose-500/20 text-rose-400 border border-rose-500/40 animate-pulse'
                : 'bg-emerald-500/20 text-emerald-400'
            }`}>
              {activeAlertsCount} Active
            </span>
          </div>
          <div className="mt-2 text-[11px] text-gray-500 flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-emerald-500 inline-block animate-ping" />
            <span>Consensus Engine: Active</span>
          </div>
        </div>
      </div>
    </aside>
  );
};
