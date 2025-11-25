import React from 'react';
import './HierarchicalRenderer.css';

/**
 * Simple hierarchical renderer for Containable objects (DeploymentUnits and ComponentFolders)
 * Uses recursive rendering to display nested structures
 */
const HierarchicalRenderer = ({ data }) => {
  
  // Render a single containable object (DeploymentUnit or ComponentFolder)
  const renderContainer = (container, level = 0) => {
    const isDeploymentUnit = container.entityType === 'DeploymentUnit';
    
    return (
      <div 
        key={`${container.entityType}-${container.id}-${level}`} 
        className={`container level-${level} ${isDeploymentUnit ? 'deployment-unit' : 'component-folder'}`}
      >
        {/* Container Header */}
        <div className="container-header">
          <div className="container-info">
            <span className="container-name">{container.name}</span>
            <span className={`container-type ${isDeploymentUnit ? 'du-type' : 'folder-type'}`}>
              {isDeploymentUnit ? container.deploymentUnitType : container.folderType}
            </span>
          </div>
          
          {/* Additional info for DeploymentUnits */}
          {isDeploymentUnit && (
            <div className="container-details">
              {container.uuaa && <span className="detail">UUAA: {container.uuaa}</span>}
              {container.code && <span className="detail">Code: {container.code}</span>}
              {container.className && <span className="detail">Class: {container.className}</span>}
            </div>
          )}
          
          {container.description && (
            <div className="container-description">{container.description}</div>
          )}
        </div>
        
        {/* Render Children if they exist */}
        {container.children && container.children.length > 0 && (
          <div className="children-container">
            {container.children.map((child, index) => renderContainer(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  // Main render
  if (!data || data.length === 0) {
    return (
      <div className="hierarchical-renderer empty">
        <div className="empty-message">
          <h4>No Architecture Data</h4>
          <p>Create some deployment units to see the architecture</p>
          <code>du-online "MyApp"</code>
        </div>
      </div>
    );
  }

  return (
    <div className="hierarchical-renderer">
      <div className="renderer-header">
        <h3>Architecture Structure</h3>
        <span className="item-count">{data.length} root item(s)</span>
      </div>
      
      <div className="containers-list">
        {data.map((container, index) => renderContainer(container, 0))}
      </div>
    </div>
  );
};

export default HierarchicalRenderer;