/**
 * Utilities for converting hierarchical data to ReactFlow format
 */

/**
 * Convert hierarchical containable data to ReactFlow nodes and edges
 */
export const convertToReactFlow = (hierarchicalData) => {
  const nodes = [];
  const edges = [];
  let nodeId = 1;
  
  const processContainer = (container, parentId = null, level = 0, parentPath = '') => {
    // ETAPA 8: Skip deleted components
    if (container.deleted === true) {
      return null; // Don't process deleted containers
    }
    
    const currentNodeId = `node-${nodeId++}`;
    
    // Build full hierarchical path for unique identification
    const fullPath = parentPath ? `${parentPath}/${container.name}` : container.name;
    
    // Create node
    const node = {
      id: currentNodeId,
      type: 'customNode',
      position: calculatePosition(level, nodes.filter(n => n.position.y === level * 150).length),
      data: {
        id: container.id,
        name: container.name,
        fullPath: fullPath, // Unique path for position storage
        type: container.entityType,
        deploymentUnitType: container.deploymentUnitType,
        folderType: container.folderType,
        description: container.description,
        uuaa: container.uuaa,
        code: container.code,
        className: container.className,
        level: level,
        containerData: container // Keep original data for reference
      }
    };
    
    nodes.push(node);
    
    // Create edge from parent if exists
    if (parentId) {
      const parentContainer = nodes.find(n => n.id === parentId)?.data?.containerData;
      const edgeStyle = getEdgeStyle(parentContainer, container);
      
      edges.push({
        id: `edge-${parentId}-${currentNodeId}`,
        source: parentId,
        target: currentNodeId,
        type: 'smoothstep',
        animated: false,
        style: edgeStyle,
        markerEnd: {
          type: 'arrowclosed',
          color: edgeStyle.stroke,
          width: 20,
          height: 20,
        },
        labelStyle: {
          fontSize: '10px',
          fontWeight: '600',
        }
      });
    }
    
    // Process children recursively
    if (container.children && container.children.length > 0) {
      container.children.forEach(child => {
        processContainer(child, currentNodeId, level + 1, fullPath);
        // Note: deleted children will be filtered out automatically in processContainer
      });
    }
    
    return currentNodeId;
  };
  
  // Process all root containers
  hierarchicalData.forEach(container => {
    processContainer(container);
  });
  
  // Process dependencies - second pass to create dependency edges
  const processDependencies = (container) => {
    // ETAPA 8: Skip deleted components in dependency processing
    if (container.deleted === true) {
      return;
    }
    
    if (container.dependencyNames && container.dependencyNames.length > 0) {
      // Find the source node by name (unique identifier) instead of id
      // Important: We use name because ids can collide between DeploymentUnits and ComponentFolders
      const sourceNode = nodes.find(n => n.data.name === container.name);
      
      if (sourceNode) {
        container.dependencyNames.forEach(depName => {
          // Find target node by name (need to search in all containers)
          const targetNode = nodes.find(n => n.data.name === depName);
          
          if (targetNode) {
            // Check if it's a self-dependency (self-loop)
            const isSelfLoop = sourceNode.id === targetNode.id;
            
            edges.push({
              id: `dependency-${sourceNode.id}-${targetNode.id}`,
              source: sourceNode.id,
              target: targetNode.id,
              // For self-loops, go from right to top to create a visible loop above the node
              sourceHandle: isSelfLoop ? 'right' : undefined,
              targetHandle: undefined, // Use default top handle for target
              type: 'smoothstep',
              animated: false,
              style: {
                stroke: '#00ff00',  // Green for dependencies
                strokeWidth: 2,
                strokeDasharray: '5,5'  // Dashed line to distinguish from hierarchy
              },
              markerEnd: {
                type: 'arrowclosed',
                color: '#00ff00',
                width: 20,
                height: 20,
              },
              label: 'depends on',
              labelStyle: {
                fontSize: '10px',
                fontWeight: '600',
                fill: '#00ff00'
              },
              labelBgStyle: {
                fill: '#1a1a1a',
                fillOpacity: 0.8
              }
            });
          }
        });
      }
    }
    
    // Recursively process children
    if (container.children && container.children.length > 0) {
      container.children.forEach(child => {
        processDependencies(child);
      });
    }
  };
  
  // Process dependencies for all root containers
  hierarchicalData.forEach(container => {
    processDependencies(container);
  });
  
  return { nodes, edges };
};

/**
 * Calculate position for automatic layout
 */
const calculatePosition = (level, indexAtLevel) => {
  const LEVEL_HEIGHT = 150;
  const HORIZONTAL_SPACING = 250;
  
  return {
    x: indexAtLevel * HORIZONTAL_SPACING,
    y: level * LEVEL_HEIGHT
  };
};

/**
 * Get enhanced edge style and color based on container relationship
 */
const getEdgeStyle = (sourceContainer, targetContainer) => {
  const baseStyle = {
    strokeWidth: 2,
    stroke: getEdgeColor(targetContainer.entityType, targetContainer.folderType),
  };
  
  // Add special styling for different relationships
  if (sourceContainer.entityType === 'DeploymentUnit' && targetContainer.entityType === 'ComponentFolder') {
    return {
      ...baseStyle,
      strokeWidth: 3,
      strokeDasharray: '0', // Solid line for main relationships
    };
  }
  
  return baseStyle;
};

/**
 * Get edge color based on target container type - optimized for dark theme
 */
const getEdgeColor = (entityType, folderType) => {
  if (entityType === 'DeploymentUnit') {
    return '#60a5fa'; // Brighter blue for dark background
  }
  
  if (entityType === 'ComponentFolder') {
    switch (folderType?.toLowerCase()) {
      case 'library':
        return '#34d399'; // Brighter green for library
      case 'transactions':
        return '#f87171'; // Brighter red for transactions
      case 'dto':
        return '#a78bfa'; // Brighter purple for DTOs
      default:
        return '#9ca3af'; // Light gray for unknown
    }
  }
  
  return '#9ca3af'; // Default light gray
};

/**
 * Get node style based on container type with modern banking aesthetic
 */
export const getNodeStyle = (data) => {
  const isDeploymentUnit = data.type === 'DeploymentUnit';
  const isComponentFolder = data.type === 'ComponentFolder';
  
  // Color scheme for banking application
  const colorSchemes = {
    DeploymentUnit: {
      primary: '#1e40af',    // Deep blue
      secondary: '#dbeafe',   // Light blue
      border: '#1e40af',
      shadow: 'rgba(30, 64, 175, 0.3)'
    },
    ComponentFolder: {
      library: {
        primary: '#059669',    // Green
        secondary: '#d1fae5',   
        border: '#059669',
        shadow: 'rgba(5, 150, 105, 0.3)'
      },
      transactions: {
        primary: '#dc2626',    // Red
        secondary: '#fee2e2',   
        border: '#dc2626',
        shadow: 'rgba(220, 38, 38, 0.3)'
      },
      dto: {
        primary: '#7c3aed',    // Purple
        secondary: '#ede9fe',   
        border: '#7c3aed',
        shadow: 'rgba(124, 58, 237, 0.3)'
      },
      default: {
        primary: '#6b7280',    // Gray
        secondary: '#f3f4f6',   
        border: '#6b7280',
        shadow: 'rgba(107, 114, 128, 0.3)'
      }
    }
  };
  
  let scheme;
  if (isDeploymentUnit) {
    scheme = colorSchemes.DeploymentUnit;
  } else if (isComponentFolder) {
    const folderType = data.folderType?.toLowerCase() || 'default';
    scheme = colorSchemes.ComponentFolder[folderType] || colorSchemes.ComponentFolder.default;
  } else {
    scheme = colorSchemes.ComponentFolder.default;
  }
  
  return {
    background: `linear-gradient(145deg, ${scheme.secondary}, #ffffff)`,
    border: `2px solid ${scheme.border}`,
    borderRadius: isDeploymentUnit ? '12px' : '8px',
    padding: isDeploymentUnit ? '16px' : '12px',
    minWidth: isDeploymentUnit ? '200px' : '160px',
    minHeight: isDeploymentUnit ? '80px' : '60px',
    color: '#1f2937',
    fontSize: '14px',
    fontFamily: '"Inter", "Segoe UI", system-ui, -apple-system, sans-serif',
    fontWeight: '500',
    boxShadow: `
      0 4px 6px -1px ${scheme.shadow},
      0 2px 4px -1px rgba(0, 0, 0, 0.06),
      inset 0 1px 0 rgba(255, 255, 255, 0.1)
    `,
    transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
    position: 'relative',
    cursor: 'pointer'
  };
};

/**
 * Get display text for different types
 */
export const getTypeDisplayText = (data) => {
  if (data.type === 'DeploymentUnit') {
    return data.deploymentUnitType?.replace('_', '-') || 'DU';
  }
  if (data.type === 'ComponentFolder') {
    return data.folderType?.toLowerCase() || 'folder';
  }
  return data.type;
};