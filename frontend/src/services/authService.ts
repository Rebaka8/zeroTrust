import api from './api';
import { AuthResponse } from '../types';
import { BrowserProvider } from 'ethers';

export const authService = {
  async login(usernameOrEmail: string, password: string):Promise<AuthResponse> {
    const res = await api.post<AuthResponse>('/auth/login', { usernameOrEmail, password });
    localStorage.setItem('zt_token', res.data.token);
    localStorage.setItem('zt_user', JSON.stringify(res.data));
    return res.data;
  },

  async register(username: string, email: string, password: string, walletAddress?: string): Promise<AuthResponse> {
    const res = await api.post<AuthResponse>('/auth/register', { username, email, password, walletAddress });
    localStorage.setItem('zt_token', res.data.token);
    localStorage.setItem('zt_user', JSON.stringify(res.data));
    return res.data;
  },

  async loginWithMetaMask(): Promise<AuthResponse> {
    if (typeof (window as any).ethereum === 'undefined') {
      throw new Error('MetaMask is not installed. Please install MetaMask to use Web3 login.');
    }

    const provider = new BrowserProvider((window as any).ethereum);
    const accounts = await provider.send('eth_requestAccounts', []);
    const address = accounts[0];

    // 1. Fetch challenge nonce & message from backend
    const nonceRes = await api.get<{ nonce: string; message: string }>('/auth/siwe/nonce', {
      params: { address }
    });

    const signer = await provider.getSigner();
    // 2. Request EIP-191 personal sign from MetaMask
    const signature = await signer.signMessage(nonceRes.data.message);

    // 3. Verify on backend and receive JWT
    const verifyRes = await api.post<AuthResponse>('/auth/siwe/verify', {
      address,
      message: nonceRes.data.message,
      signature
    });

    localStorage.setItem('zt_token', verifyRes.data.token);
    localStorage.setItem('zt_user', JSON.stringify(verifyRes.data));
    return verifyRes.data;
  },

  logout() {
    localStorage.removeItem('zt_token');
    localStorage.removeItem('zt_user');
  },

  getCurrentUser(): AuthResponse | null {
    const userStr = localStorage.getItem('zt_user');
    return userStr ? JSON.parse(userStr) : null;
  }
};
