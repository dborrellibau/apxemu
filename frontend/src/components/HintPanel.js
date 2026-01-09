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
        <span className="hint-title">V-Ether Educational Hints</span>
        {hints.length > 0 && (
          <span className="hint-badge">{hints.length}</span>
        )}
      </div>

      {/* Contenido */}
      <div className="hint-panel-content" ref={scrollRef}>
        {hints.length === 0 ? (
          <div className="hint-empty-state">
            <p>Educational hints will appear here as you use terminal commands</p>
          </div>
        ) : (
          <div className="hint-list">
            {hints.map(hint => (
              <div key={hint.id} className="hint-card">
                <div className="hint-content">{hint.content}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default HintPanel;
