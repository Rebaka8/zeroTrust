import React from 'react';
import { Sidebar } from './Sidebar';
import { Header } from './Header';

interface LayoutProps {
  children: React.ReactNode;
  currentTab: string;
  onSelectTab: (tab: string) => void;
  title: string;
  subtitle?: string;
  activeAlertsCount?: number;
  onOpenRegisterModal?: () => void;
}

export const Layout: React.FC<LayoutProps> = ({
  children,
  currentTab,
  onSelectTab,
  title,
  subtitle,
  activeAlertsCount,
  onOpenRegisterModal
}) => {
  return (
    <div className="flex h-screen w-screen bg-[#0B0F19] text-gray-100 overflow-hidden font-sans">
      <Sidebar
        currentTab={currentTab}
        onSelectTab={onSelectTab}
        activeAlertsCount={activeAlertsCount}
      />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Header
          title={title}
          subtitle={subtitle}
          onOpenRegisterModal={onOpenRegisterModal}
        />
        <main className="flex-1 overflow-y-auto p-6 bg-[#0B0F19]/50">
          {children}
        </main>
      </div>
    </div>
  );
};
