import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  private client: Client | null = null;
  private connected = false;
  private listeners: Map<string, Set<(data: any) => void>> = new Map();

  connect() {
    if (this.client && this.connected) return;

    let wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8085/ws';
    // SockJS client requires http/https protocol for its handshake
    if (wsUrl.startsWith('ws://')) {
      wsUrl = wsUrl.replace('ws://', 'http://');
    } else if (wsUrl.startsWith('wss://')) {
      wsUrl = wsUrl.replace('wss://', 'https://');
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.connected = true;
        console.log('[WebSocket] Connected to Zero Trust Real-Time Broker');
        this.resubscribe();
      },
      onDisconnect: () => {
        this.connected = false;
        console.log('[WebSocket] Disconnected');
      },
      onStompError: (frame) => {
        console.error('[WebSocket] Broker error:', frame.headers['message']);
      }
    });

    this.client.activate();
  }

  subscribe(topic: string, callback: (data: any) => void): () => void {
    if (!this.listeners.has(topic)) {
      this.listeners.set(topic, new Set());
      if (this.connected && this.client) {
        this.client.subscribe(topic, (msg: IMessage) => {
          this.notifyTopic(topic, msg);
        });
      }
    }

    this.listeners.get(topic)!.add(callback);

    return () => {
      const topicListeners = this.listeners.get(topic);
      if (topicListeners) {
        topicListeners.delete(callback);
      }
    };
  }

  private resubscribe() {
    if (!this.client || !this.connected) return;

    for (const topic of this.listeners.keys()) {
      this.client.subscribe(topic, (msg: IMessage) => {
        this.notifyTopic(topic, msg);
      });
    }
  }

  private notifyTopic(topic: string, msg: IMessage) {
    try {
      const data = JSON.parse(msg.body);
      const topicListeners = this.listeners.get(topic);
      if (topicListeners) {
        topicListeners.forEach(cb => cb(data));
      }
    } catch (e) {
      console.warn('Failed to parse WebSocket message from topic:', topic);
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
    }
  }
}

export const wsService = new WebSocketService();
