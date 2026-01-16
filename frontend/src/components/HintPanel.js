import React, { useRef, useEffect } from 'react';
import './HintPanel.css';

const HintPanel = ({ hints }) => {
  const scrollRef = useRef(null);

  useEffect(() => {
    if (scrollRef.current && hints.length > 0) {
      scrollRef.current.scrollTop = 0;
    }
  }, [hints]);

  return (
    <div className="hint-panel">
      <div className="hint-panel-header">
        <span className="hint-title">Educational Hints</span>
      </div>
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
