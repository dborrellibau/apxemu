import React, { useState } from 'react';
import { Handle, Position } from 'reactflow';
import { getNodeStyle, getTypeDisplayText } from '../utils/dataConverter';

const CustomNode = ({ data }) => {
  const [isHovered, setIsHovered] = useState(false);
  const style = getNodeStyle(data);
  const typeText = getTypeDisplayText(data);
  const isDeploymentUnit = data.type === 'DeploymentUnit';
  
  // Enhanced style with hover effects
  const enhancedStyle = {
    ...style,
    transform: isHovered ? 'translateY(-2px) scale(1.02)' : 'translateY(0) scale(1)',
    boxShadow: isHovered 
      ? `${style.boxShadow}, 0 8px 25px -5px rgba(0, 0, 0, 0.1)`
      : style.boxShadow
  };

  // Get icon based on type
  const getIcon = () => {
    if (isDeploymentUnit) {
      return '🏗️'; // Building/construction icon for deployment units
    }
    const folderType = data.folderType?.toLowerCase();
    switch (folderType) {
      case 'library': return '📚';
      case 'transactions': return '💼';
      case 'dto': return '📦';
      default: return '📁';
    }
  };
  
  return (
    <div 
      style={enhancedStyle}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {/* Top connection handle */}
      <Handle 
        type="target" 
        position={Position.Top} 
        style={{ 
          background: style.border?.split(' ')[2] || '#555',
          width: '10px',
          height: '10px',
          border: '2px solid white'
        }} 
      />
      
      {/* Left connection handle for self-loops */}
      <Handle 
        type="target" 
        position={Position.Left}
        id="left"
        style={{ 
          background: style.border?.split(' ')[2] || '#555',
          width: '10px',
          height: '10px',
          border: '2px solid white'
        }} 
      />
      
      {/* Right connection handle for self-loops */}
      <Handle 
        type="source" 
        position={Position.Right}
        id="right"
        style={{ 
          background: style.border?.split(' ')[2] || '#555',
          width: '10px',
          height: '10px',
          border: '2px solid white'
        }} 
      />
      
      {/* Header section */}
      <div style={{ 
        display: 'flex',
        alignItems: 'center',
        marginBottom: isDeploymentUnit ? '8px' : '6px',
        gap: '8px'
      }}>
        <span style={{ fontSize: '18px' }}>{getIcon()}</span>
        <div style={{ flex: 1 }}>
          <div style={{ 
            fontWeight: isDeploymentUnit ? '700' : '600', 
            fontSize: isDeploymentUnit ? '16px' : '14px',
            marginBottom: '2px',
            color: '#111827',
            lineHeight: '1.3'
          }}>
            {data.name}
          </div>
          
          <div style={{ 
            fontSize: '11px', 
            color: '#6b7280',
            fontWeight: '500',
            textTransform: 'uppercase',
            letterSpacing: '0.5px'
          }}>
            {typeText}
          </div>
        </div>
      </div>
      
      {/* Details section */}
      <div style={{ 
        fontSize: '11px',
        color: '#4b5563',
        lineHeight: '1.4'
      }}>
        {/* UUAA and Code in same line for space efficiency */}
        {(data.uuaa || data.code) && (
          <div style={{ 
            display: 'flex', 
            gap: '12px',
            marginBottom: '4px'
          }}>
            {data.uuaa && (
              <span style={{ 
                background: 'rgba(59, 130, 246, 0.1)',
                padding: '2px 6px',
                borderRadius: '4px',
                fontWeight: '600'
              }}>
                UUAA: {data.uuaa}
              </span>
            )}
            {data.code && (
              <span style={{ 
                background: 'rgba(107, 114, 128, 0.1)',
                padding: '2px 6px',
                borderRadius: '4px',
                fontWeight: '600'
              }}>
                {data.code}
              </span>
            )}
          </div>
        )}
        
        {data.className && (
          <div style={{ 
            fontStyle: 'italic',
            fontWeight: '500',
            color: '#374151',
            marginBottom: '4px'
          }}>
            {data.className}
          </div>
        )}
        
        {data.description && (
          <div style={{ 
            fontSize: '10px',
            color: '#9ca3af',
            fontStyle: 'italic',
            marginTop: '6px',
            maxWidth: '100%',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            lineHeight: '1.3'
          }}>
            {data.description}
          </div>
        )}
      </div>
      
      {/* Bottom connection handle */}
      <Handle 
        type="source" 
        position={Position.Bottom} 
        style={{ 
          background: style.border?.split(' ')[2] || '#555',
          width: '10px',
          height: '10px',
          border: '2px solid white'
        }} 
      />
    </div>
  );
};

export default CustomNode;