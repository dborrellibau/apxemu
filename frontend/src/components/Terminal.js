import React, { useState, useEffect, useRef } from 'react';
import './Terminal.css';

const Terminal = ({ wsService, isConnected }) => {
  const [history, setHistory] = useState([]);
  const [currentCommand, setCurrentCommand] = useState('');
  const [commandHistory, setCommandHistory] = useState([]);
  const [historyIndex, setHistoryIndex] = useState(-1);
  const [currentPrompt, setCurrentPrompt] = useState('vether> ');
  const [isFormMode, setIsFormMode] = useState(false);
  const [currentFormPromptText, setCurrentFormPromptText] = useState(null);
  const terminalRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    // Add welcome message
    setHistory([
      { type: 'info', content: 'Welcome to V-Ether' },
      { type: 'info', content: 'Type "apx help" for available commands' },
      { type: 'prompt', content: '' }
    ]);
  }, []);

  useEffect(() => {
    if (wsService) {
      wsService.onCommandResponse = (response) => {
        handleCommandResponse(response);
      };
    }
  }, [wsService]);

  useEffect(() => {
    // Auto-scroll to bottom
    if (terminalRef.current) {
      terminalRef.current.scrollTop = terminalRef.current.scrollHeight;
    }
    
    // Focus input
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, [history]);

  const handleCommandResponse = (response) => {
    const newEntries = [];
    
    // Update current prompt if provided
    if (response.prompt) {
      setCurrentPrompt(response.prompt);
    }
    
    if (response.type === 'MENU') {
      // Modo menú: no estamos en formulario
      setIsFormMode(false);
      setCurrentFormPromptText(null);
  
      newEntries.push({
        type: 'info',
        content: response.message
      });
      
      if (response.output && response.output.length > 0) {
        response.output.forEach(line => {
          newEntries.push({
            type: 'menu',
            content: line
          });
        });
      }
      
      // Calculate max option number dynamically from response
      const maxOption = response.data && response.data.maxOption 
        ? response.data.maxOption 
        : (response.output ? response.output.length : 3); // Fallback to 3 if not provided
  
      newEntries.push({
        type: 'info',
        content: `Enter selection (1-${maxOption} or type name):`
      });
  
    } else if (response.type === 'FORM') {
      // Modo formulario activo
      setIsFormMode(true);
      setCurrentFormPromptText(response.message || '');
  
      newEntries.push({
        type: 'form',
        content: response.message
      });
  
    } else if (response.output && response.output.length > 0) {
      // Respuesta normal (lista)
      setIsFormMode(false);
      setCurrentFormPromptText(null);
  
      response.output.forEach(line => {
        newEntries.push({
          type: response.type.toLowerCase(),
          content: line
        });
      });
  
    } else {
      // SUCCESS / ERROR simples
      setIsFormMode(false);
      setCurrentFormPromptText(null);
  
      newEntries.push({
        type: response.success ? 'success' : 'error',
        content: response.message
      });
    }
    
    // Add new prompt
    newEntries.push({ type: 'prompt', content: '' });
    
    // Append response to existing history (preserving all commands)
    setHistory(prev => [...prev, ...newEntries]);
  };

  const executeCommand = (command) => {
    // Always add the command to the display history first
    setHistory(prev => [
      ...prev.slice(0, -1), // Remove the empty prompt
      { type: 'command', content: `${currentPrompt}${command}` }, // Add the command
      // Note: response will be added when handleCommandResponse is called
    ]);
  
    // Update command history for navigation
    setCommandHistory(prev => [command, ...prev.slice(0, 99)]); // Keep last 100 commands
    setHistoryIndex(-1);
  
    const trimmedInput = command.trim();
  
    // === VALIDACIONES SOLO EN MODO FORMULARIO ===
    if (isFormMode && currentFormPromptText) {
      const promptLower = currentFormPromptText.toLowerCase();
  
      // 1) Deployment Unit Name: una sola palabra (sin espacios)
      if (promptLower.includes('deployment unit name') && trimmedInput.includes(' ')) {
        setHistory(prev => [
          ...prev,
          { type: 'error', content: 'Deployment Unit Name must be a single word (no spaces). Try again:' },
          { type: 'prompt', content: '' }
        ]);
        return;
      }
  
      // 2) Description: mínimo 5 caracteres totales
      if (promptLower.includes('description') && trimmedInput.length < 5) {
        setHistory(prev => [
          ...prev,
          { type: 'error', content: 'Description must be at least 5 characters. Try again:' },
          { type: 'prompt', content: '' }
        ]);
        return;
      }
  
      // En modo formulario: enviar el input completo, sin split
      if (wsService && isConnected) {
        try {
          wsService.sendCommand(trimmedInput, []); // backend toma el input crudo
        } catch (error) {
          console.error('Error sending command:', error);
          setHistory(prev => [...prev, 
            { type: 'error', content: 'Connection error. Trying to reconnect...' },
            { type: 'prompt', content: '' }
          ]);
        }
      } else {
        setHistory(prev => [...prev, 
          { type: 'error', content: 'Not connected to server. Attempting to reconnect...' },
          { type: 'prompt', content: '' }
        ]);
        if (wsService) {
          wsService.connect();
        }
      }
      return; // IMPORTANTE: no seguir a la parte de comandos normales
    }
  
    // === MODO COMANDO NORMAL (no formulario) ===
    const parts = trimmedInput.split(' ');
    const cmd = parts[0];
    const args = parts.slice(1);
  
    if (wsService && isConnected) {
      try {
        wsService.sendCommand(cmd, args);
      } catch (error) {
        console.error('Error sending command:', error);
        setHistory(prev => [...prev, 
          { type: 'error', content: 'Connection error. Trying to reconnect...' },
          { type: 'prompt', content: '' }
        ]);
      }
    } else {
      setHistory(prev => [...prev, 
        { type: 'error', content: 'Not connected to server. Attempting to reconnect...' },
        { type: 'prompt', content: '' }
      ]);
      if (wsService) {
        wsService.connect();
      }
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      executeCommand(currentCommand);
      setCurrentCommand('');
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      if (historyIndex < commandHistory.length - 1) {
        const newIndex = historyIndex + 1;
        setHistoryIndex(newIndex);
        setCurrentCommand(commandHistory[newIndex] || '');
      }
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (historyIndex > -1) {
        const newIndex = historyIndex - 1;
        setHistoryIndex(newIndex);
        setCurrentCommand(newIndex === -1 ? '' : commandHistory[newIndex] || '');
      }
    }
  };

  const handleTerminalClick = () => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };

  const renderHistoryEntry = (entry, index) => {
    const className = `terminal-line ${entry.type}`;
    
    if (entry.type === 'prompt') {
      return (
        <div key={index} className="terminal-input-line">
          <span className="prompt">{currentPrompt}</span>
          <input
            ref={inputRef}
            type="text"
            value={currentCommand}
            onChange={(e) => setCurrentCommand(e.target.value)}
            onKeyDown={handleKeyPress}
            className="terminal-input"
            autoFocus
            spellCheck="false"
          />
        </div>
      );
    }
    
    return (
      <div key={index} className={className}>
        {entry.content}
      </div>
    );
  };

  return (
    <div className="terminal" onClick={handleTerminalClick}>
      <div className="terminal-header">
        <div className="terminal-title">V-Ether Terminal</div>
        <div className={`connection-indicator ${isConnected ? 'connected' : 'disconnected'}`}>
          {isConnected ? 'Connected' : 'Disconnected'}
        </div>
      </div>
      
      <div className="terminal-body" ref={terminalRef}>
        <div className="terminal-content">
          {history.map((entry, index) => renderHistoryEntry(entry, index))}
        </div>
      </div>
    </div>
  );
};

export default Terminal;