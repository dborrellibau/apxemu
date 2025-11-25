import React, { useState, useEffect } from 'react';
import Terminal from './components/Terminal';
import ReactFlowDiagram from './components/ReactFlowDiagram';
import webSocketService from './services/WebSocketService'; // Import singleton instance
import './App.css';

function App() {
  const [diagramData, setDiagramData] = useState([]);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    // Use the singleton instance directly
    webSocketService.onConnect = () => {
      console.log('Connected to WebSocket');
      setIsConnected(true);
    };

    webSocketService.onDisconnect = () => {
      console.log('Disconnected from WebSocket');
      setIsConnected(false);
    };

    webSocketService.onDiagramUpdate = (data) => {
      console.log('Diagram update received:', data);
      setDiagramData(data.units || []);
    };

    webSocketService.connect();

    return () => {
      // Don't disconnect on unmount, let the singleton handle its lifecycle
      console.log('App component unmounting');
    };
  }, []);

  return (
    <div className="App">
      <header className="App-header">
        <h1>APX CLI Banking Education Emulator</h1>
        <div className={`status ${isConnected ? 'connected' : 'disconnected'}`}>
          {isConnected ? '● Connected' : '● Disconnected'}
        </div>
      </header>
      
      <div className="App-content">
        <div className="terminal-panel">
          <Terminal wsService={webSocketService} isConnected={isConnected} />
        </div>
        
        <div className="diagram-panel">
          <ReactFlowDiagram data={diagramData} />
        </div>
      </div>
    </div>
  );
}

export default App;