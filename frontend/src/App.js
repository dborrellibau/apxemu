import React, { useState, useEffect } from 'react';
import Terminal from './components/Terminal';
import ReactFlowDiagram from './components/ReactFlowDiagram';
import HintPanel from './components/HintPanel';
import webSocketService from './services/WebSocketService'; // Import singleton instance
import './App.css';

function App() {
  const [diagramData, setDiagramData] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [showHints, setShowHints] = useState(() => {
    // Leer preferencia de localStorage, default: true
    const saved = localStorage.getItem('showHints');
    return saved !== 'false';
  });
  const [hints, setHints] = useState([]);

  const toggleHints = () => {
    const newValue = !showHints;
    setShowHints(newValue);
    localStorage.setItem('showHints', newValue.toString());
  };

  useEffect(() => {
    // Use the singleton instance directly
    webSocketService.onConnect = async () => {
      console.log('Connected to WebSocket');
      setIsConnected(true);
      
      // Load initial diagram data from REST endpoint
      try {
        console.log('Loading initial diagram data...');
        const response = await fetch('/api/architecture/units');
        
        if (response.ok) {
          const units = await response.json();
          console.log('Initial data loaded:', units.length, 'root units');
          setDiagramData(units);
        } else {
          console.error('Failed to load initial data:', response.status, response.statusText);
        }
      } catch (error) {
        console.error('Error loading initial diagram data:', error);
      }
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
        <h1>V-Ether</h1>
        
        <div className="header-controls">
          <button 
            className={`hint-toggle-btn ${showHints ? 'active' : ''}`}
            onClick={toggleHints}
            title={showHints ? 'Ocultar hints educativos' : 'Mostrar hints educativos'}
          >
            📚 Hints
          </button>
          
          <div className={`status ${isConnected ? 'connected' : 'disconnected'}`}>
            {isConnected ? '● Connected' : '● Disconnected'}
          </div>
        </div>
      </header>
      
      <div className="App-content">
        {/* Columna Izquierda: Hints + Terminal */}
        <div className="left-column">
          {showHints && (
            <div className="hints-section">
              <HintPanel hints={hints} />
            </div>
          )}
          
          <div className={`terminal-section ${showHints ? 'with-hints' : 'full-height'}`}>
            <Terminal wsService={webSocketService} isConnected={isConnected} />
          </div>
        </div>
        
        {/* Columna Derecha: Diagram */}
        <div className="right-column">
          <ReactFlowDiagram data={diagramData} />
        </div>
      </div>
    </div>
  );
}

export default App;