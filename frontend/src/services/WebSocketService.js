import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    // Prevent multiple instances
    if (WebSocketService.instance) {
      return WebSocketService.instance;
    }
    
    this.client = null;
    this.connected = false;
    this.connecting = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000;
    this.sessionId = 'session-' + Math.random().toString(36).substr(2, 9); // Generate once and keep
    this.onConnect = null;
    this.onDisconnect = null;
    this.onCommandResponse = null;
    this.onDiagramUpdate = null;
    
    // Store singleton instance
    WebSocketService.instance = this;
  }

  connect() {
    if (this.connecting || this.connected) {
      console.log('WebSocket already connecting or connected');
      return;
    }
    
    this.connecting = true;
    
    try {
      // Disconnect existing client if any
      if (this.client) {
        this.client.deactivate();
      }
      
      this.client = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        debug: () => {}, // Disable debug logging
        reconnectDelay: this.reconnectInterval,
        onConnect: (frame) => {
          console.log('Connected to WebSocket:', frame);
          this.connected = true;
          this.connecting = false;
          this.reconnectAttempts = 0;
          if (this.onConnect) this.onConnect();
          
          // Subscribe to command responses
          this.client.subscribe('/topic/responses', (message) => {
            const response = JSON.parse(message.body);
            if (this.onCommandResponse) {
              this.onCommandResponse(response);
            }
          });
          
          // Subscribe to diagram updates
          this.client.subscribe('/topic/diagram-updates', (message) => {
            const data = JSON.parse(message.body);
            if (this.onDiagramUpdate) {
              this.onDiagramUpdate(data);
            }
          });
        },
        onStompError: (error) => {
          console.error('WebSocket connection error:', error);
          this.connected = false;
          this.connecting = false;
          if (this.onDisconnect) this.onDisconnect();
          this.attemptReconnect();
        },
        onWebSocketClose: () => {
          console.log('WebSocket connection closed');
          this.connected = false;
          this.connecting = false;
          if (this.onDisconnect) this.onDisconnect();
          this.attemptReconnect();
        }
      });
      
      this.client.activate();
    } catch (error) {
      console.error('Error creating WebSocket connection:', error);
      this.connecting = false;
      this.attemptReconnect();
    }
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts && !this.connecting) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      setTimeout(() => {
        this.connect();
      }, this.reconnectInterval);
    } else {
      console.error('Max reconnection attempts reached');
    }
  }

  disconnect() {
    if (this.client && this.connected) {
      this.client.deactivate();
      console.log('Disconnected from WebSocket');
      this.connected = false;
      if (this.onDisconnect) this.onDisconnect();
    }
  }

  sendCommand(command, args = []) {
    if (!this.client || !this.connected) {
      console.warn('WebSocket not connected, attempting to reconnect...');
      this.connect();
      // Queue the command to be sent after connection
      setTimeout(() => {
        if (this.client && this.connected) {
          this.sendCommand(command, args);
        } else {
          console.error('Failed to reconnect WebSocket for command:', command);
        }
      }, 1000);
      return;
    }
    
    try {
      const request = {
        sessionId: this.sessionId, // Use consistent session ID
        command: command,
        args: args
      };
      
      this.client.publish({
        destination: '/app/command',
        body: JSON.stringify(request)
      });
    } catch (error) {
      console.error('Error sending command:', error);
      this.connected = false;
      this.connect();
    }
  }
}

// Reset singleton instance when module is hot-reloaded
if (module.hot) {
  module.hot.accept(() => {
    WebSocketService.instance = null;
  });
}

// Static instance property
WebSocketService.instance = null;

// Create and export a singleton instance
const webSocketServiceInstance = new WebSocketService();
export default webSocketServiceInstance;