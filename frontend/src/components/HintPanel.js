import React, { useRef, useEffect } from 'react';
import './HintPanel.css';

const HintPanel = ({ hints }) => {
  const scrollRef = useRef(null);

  useEffect(() => {
    if (scrollRef.current && hints.length > 0) {
      scrollRef.current.scrollTop = 0;
    }
  }, [hints]);

  /**
   * Detects if hint is in tutorial mode (starts with 🎓 emoji)
   */
  const isTutorialMode = (content) => {
    return content && content.trim().startsWith('🎓');
  };

  /**
   * Parses simple markdown syntax for tutorial hints:
   * - **text** → <strong>text</strong>
   * - `code` → <code>code</code>
   * - newlines preserved
   */
  const parseMarkdown = (text) => {
    if (!text) return '';
    
    let html = text;
    
    // Parse **bold**
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    
    // Parse `code`
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
    
    // Preserve line breaks
    html = html.replace(/\n/g, '<br/>');
    
    return html;
  };

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
            {hints.map(hint => {
              const isTutorial = isTutorialMode(hint.content);
              const parsedContent = isTutorial ? parseMarkdown(hint.content) : hint.content;
              
              return (
                <div 
                  key={hint.id} 
                  className={`hint-card ${isTutorial ? 'tutorial-mode' : ''}`}
                >
                  {isTutorial ? (
                    <div 
                      className="hint-content"
                      dangerouslySetInnerHTML={{ __html: parsedContent }}
                    />
                  ) : (
                    <div className="hint-content">{hint.content}</div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default HintPanel;
