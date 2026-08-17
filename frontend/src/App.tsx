import React, { useState } from 'react';
import { useAuth } from './context/AuthContext';
import { Layout } from './components/layout/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { DevicesPage } from './pages/DevicesPage';
import { CredentialsPage } from './pages/CredentialsPage';
import { TrustEnginePage } from './pages/TrustEnginePage';
import { SimulationPage } from './pages/SimulationPage';
import { DatasetBenchmarkPage } from './pages/DatasetBenchmarkPage';
import { AuditLedgerPage } from './pages/AuditLedgerPage';
import { LoginPage } from './pages/LoginPage';

export const AppContent: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [currentTab, setCurrentTab] = useState('dashboard');
  const [isOpenRegisterModal, setIsOpenRegisterModal] = useState(false);

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  const getPageInfo = () => {
    switch (currentTab) {
      case 'dashboard':
        return { title: 'Executive Overview', subtitle: 'Live IoT telemetry, security threat feeds, and fleet metrics' };
      case 'devices':
        return { title: 'IoT Fleet Manager', subtitle: 'Hardware inventory, W3C DID identifiers, and quarantine isolation' };
      case 'credentials':
        return { title: 'Decentralized Identifiers & Attestations', subtitle: 'Universal W3C DID Resolver & Verifiable Credential verification studio' };
      case 'trust':
        return { title: 'Dynamic Risk Engine & ABAC', subtitle: 'Mathematical T(t) formulation and Policy Decision Point (PDP) sandbox' };
      case 'simulation':
        return { title: 'Adversarial Cyberattack Simulator', subtitle: '5 attack vectors to test Zero Trust defenses and automated quarantine' };
      case 'dataset':
        return { title: 'Kaggle Dataset Replay & Benchmarks', subtitle: 'Upload custom Kaggle CSVs or run IoT-23 / CIC-IoT-2023 confusion matrix evaluations' };
      case 'audit':
        return { title: 'Blockchain Ledger & Audit Trail', subtitle: 'Cryptographic SHA-256 hash chains and Smart Contract transactions' };
      default:
        return { title: 'Zero Trust Command Center', subtitle: 'IoT Security Framework' };
    }
  };

  const info = getPageInfo();

  return (
    <Layout
      currentTab={currentTab}
      onSelectTab={setCurrentTab}
      title={info.title}
      subtitle={info.subtitle}
      onOpenRegisterModal={() => setIsOpenRegisterModal(true)}
    >
      {currentTab === 'dashboard' && <DashboardPage onNavigateTo={setCurrentTab} />}
      {currentTab === 'devices' && (
        <DevicesPage
          isOpenRegisterModal={isOpenRegisterModal}
          onCloseRegisterModal={() => setIsOpenRegisterModal(false)}
        />
      )}
      {currentTab === 'credentials' && <CredentialsPage />}
      {currentTab === 'trust' && <TrustEnginePage />}
      {currentTab === 'simulation' && <SimulationPage />}
      {currentTab === 'dataset' && <DatasetBenchmarkPage />}
      {currentTab === 'audit' && <AuditLedgerPage />}
    </Layout>
  );
};

export default function App() {
  return <AppContent />;
}
