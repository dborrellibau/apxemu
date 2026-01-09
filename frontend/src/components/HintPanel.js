import React, { useRef, useEffect } from 'react';
import './HintPanel.css';

const HintPanel = ({ hints }) => {
  const scrollRef = useRef(null);

  // Auto-scroll al último hint (al inicio de la lista)
  useEffect(() => {
    if (scrollRef.current && hints.length > 0) {
      scrollRef.current.scrollTop = 0;
    }
  }, [hints]);

  return (
    <div className="hint-panel">
      {/* Header */}
      <div className="hint-panel-header">
        <span className="hint-icon">💡</span>
        <span className="hint-title">Hints Educativos</span>
        {hints.length > 0 && (
          <span className="hint-badge">{hints.length}</span>
        )}
      </div>

      {/* Contenido */}
      <div className="hint-panel-content" ref={scrollRef}>
        {hints.length === 0 ? (
          <div className="hint-empty-state">
            <span className="empty-icon">📚</span>
            <p>Los hints educativos aparecerán aquí mientras usas comandos en la terminal</p>
          </div>
        ) : (
          <div className="hint-list">
            {hints.map(hint => (
              <div key={hint.id} className="hint-card">
                <div className="hint-content">{hint.content}</div>
                <div className="hint-timestamp">
                  {hint.timestamp.toLocaleTimeString('es-AR', {
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit'
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default HintPanel;
