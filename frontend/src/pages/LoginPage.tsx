import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Lock, Shield, ArrowRight, Wallet, CheckCircle2, AlertCircle } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { login, loginMetaMask } = useAuth();

  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Admin@123456');
  const [loading, setLoading] = useState(false);
  const [loadingMetaMask, setLoadingMetaMask] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handlePasswordLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);
      await login(username, password);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid credentials or connection error');
    } finally {
      setLoading(false);
    }
  };

  const handleMetaMaskLogin = async () => {
    try {
      setLoadingMetaMask(true);
      setError(null);
      await loginMetaMask();
    } catch (err: any) {
      setError(err.message || 'MetaMask SIWE authentication failed');
    } finally {
      setLoadingMetaMask(false);
    }
  };

  return (
    <div className="min-h-screen w-screen bg-[#0B0F19] flex items-center justify-center p-4 selection:bg-indigo-500 selection:text-white">
      {/* Background Glows */}
      <div className="absolute top-1/4 left-1/3 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/3 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md glass-panel-glow p-8 rounded-2xl relative z-10 space-y-6">
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="w-14 h-14 mx-auto rounded-2xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 p-0.5 shadow-xl shadow-indigo-500/30">
            <div className="w-full h-full bg-[#0B0F19] rounded-[14px] flex items-center justify-center">
              <Lock className="w-7 h-7 text-indigo-400" />
            </div>
          </div>
          <h1 className="text-xl font-extrabold text-white tracking-wide">
            ZERO TRUST <span className="text-indigo-400">IoT</span>
          </h1>
          <p className="text-xs text-gray-400">
            Decentralized Identity & Blockchain Security Framework
          </p>
        </div>

        {error && (
          <div className="p-3 rounded-lg bg-rose-950/50 border border-rose-500/40 text-rose-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Web3 MetaMask Login Button */}
        <button
          onClick={handleMetaMaskLogin}
          disabled={loadingMetaMask}
          className="w-full py-3 px-4 rounded-xl bg-[#1E202C] hover:bg-[#282B3B] border border-amber-500/30 text-amber-300 text-xs font-bold transition-all shadow-lg flex items-center justify-center gap-3 disabled:opacity-50"
        >
          {loadingMetaMask ? (
            <div className="w-4 h-4 border-2 border-amber-400 border-t-transparent rounded-full animate-spin" />
          ) : (
            <Wallet className="w-4 h-4 text-amber-400" />
          )}
          <span>Sign In with Ethereum (SIWE / MetaMask)</span>
        </button>

        {/* Divider */}
        <div className="flex items-center gap-3">
          <div className="flex-1 h-px bg-gray-800" />
          <span className="text-[11px] font-mono text-gray-500 uppercase">or standard login</span>
          <div className="flex-1 h-px bg-gray-800" />
        </div>

        {/* Username/Password Form */}
        <form onSubmit={handlePasswordLogin} className="space-y-4 text-xs">
          <div>
            <label className="text-gray-300 font-medium block mb-1">Operator ID / Username</label>
            <input
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full bg-gray-900/90 border border-gray-800 rounded-lg px-3 py-2.5 text-gray-200 focus:outline-none focus:border-indigo-500 transition-colors"
            />
          </div>

          <div>
            <label className="text-gray-300 font-medium block mb-1">Security Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full bg-gray-900/90 border border-gray-800 rounded-lg px-3 py-2.5 text-gray-200 focus:outline-none focus:border-indigo-500 transition-colors"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-lg shadow-lg shadow-indigo-600/30 transition-all flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {loading ? (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <span>Access Security Command Center</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* Demo Credential Note */}
        <div className="p-3 bg-gray-900/60 rounded-lg border border-gray-800/80 text-[11px] text-gray-400 space-y-1">
          <div className="font-semibold text-gray-300 flex items-center gap-1">
            <CheckCircle2 className="w-3.5 h-3.5 text-indigo-400" />
            <span>Pre-Configured Demo Credentials:</span>
          </div>
          <p className="font-mono text-[10px] text-gray-400">
            Username: <span className="text-indigo-300 font-bold">admin</span> | Password: <span className="text-indigo-300 font-bold">Admin@123456</span>
          </p>
        </div>
      </div>
    </div>
  );
};
