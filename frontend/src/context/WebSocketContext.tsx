import React, { createContext, useContext, useEffect, useState } from 'react';
import { wsService } from '../services/websocketService';
import { Device, TelemetryData, SecurityAlert, AccessDecision, TrustScore } from '../types';

interface WebSocketContextType {
  lastTelemetry: TelemetryData | null;
  lastDeviceUpdate: Device | null;
  lastAlert: SecurityAlert | null;
  lastDecision: AccessDecision | null;
  lastTrustScore: TrustScore | null;
}

const WebSocketContext = createContext<WebSocketContextType>({
  lastTelemetry: null,
  lastDeviceUpdate: null,
  lastAlert: null,
  lastDecision: null,
  lastTrustScore: null,
});

export const WebSocketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [lastTelemetry, setLastTelemetry] = useState<TelemetryData | null>(null);
  const [lastDeviceUpdate, setLastDeviceUpdate] = useState<Device | null>(null);
  const [lastAlert, setLastAlert] = useState<SecurityAlert | null>(null);
  const [lastDecision, setLastDecision] = useState<AccessDecision | null>(null);
  const [lastTrustScore, setLastTrustScore] = useState<TrustScore | null>(null);

  useEffect(() => {
    wsService.connect();

    const unsubTelemetry = wsService.subscribe('/topic/telemetry', (data: TelemetryData) => {
      setLastTelemetry(data);
    });

    const unsubDevices = wsService.subscribe('/topic/devices', (data: Device) => {
      setLastDeviceUpdate(data);
    });

    const unsubAlerts = wsService.subscribe('/topic/alerts', (data: SecurityAlert) => {
      setLastAlert(data);
    });

    const unsubDecisions = wsService.subscribe('/topic/decisions', (data: AccessDecision) => {
      setLastDecision(data);
    });

    const unsubTrust = wsService.subscribe('/topic/trust', (data: TrustScore) => {
      setLastTrustScore(data);
    });

    return () => {
      unsubTelemetry();
      unsubDevices();
      unsubAlerts();
      unsubDecisions();
      unsubTrust();
      wsService.disconnect();
    };
  }, []);

  return (
    <WebSocketContext.Provider
      value={{
        lastTelemetry,
        lastDeviceUpdate,
        lastAlert,
        lastDecision,
        lastTrustScore,
      }}
    >
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => useContext(WebSocketContext);
