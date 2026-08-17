import React, { createContext, useContext, useState, useEffect } from 'react';
import { AuthResponse } from '../types';
import { authService } from '../services/authService';

interface AuthContextType {
  user: AuthResponse | null;
  isAuthenticated: boolean;
  login: (usernameOrEmail: string, pass: string) => Promise<void>;
  loginMetaMask: () => Promise<void>;
  register: (u: string, e: string, p: string, w?: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthResponse | null>(null);

  useEffect(() => {
    const saved = authService.getCurrentUser();
    if (saved) {
      setUser(saved);
    }
  }, []);

  const login = async (usernameOrEmail: string, pass: string) => {
    const auth = await authService.login(usernameOrEmail, pass);
    setUser(auth);
  };

  const loginMetaMask = async () => {
    const auth = await authService.loginWithMetaMask();
    setUser(auth);
  };

  const register = async (u: string, e: string, p: string, w?: string) => {
    const auth = await authService.register(u, e, p, w);
    setUser(auth);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        login,
        loginMetaMask,
        register,
        logout
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
