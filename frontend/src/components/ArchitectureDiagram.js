import React, { useMemo } from 'react';
import ReactFlow, { 
  Background, 
  Controls, 
  MiniMap,
  useNodesState,
  useEdgesState
} from 'reactflow';
import 'reactflow/dist/style.css';
import './ArchitectureDiagram.css';

const ArchitectureDiagram = ({ data }) => {
  const { nodes, edges } = useMemo(() => {
    if (!data || data.length === 0) {
      return { nodes: [], edges: [] };
    }

    const getNodeColor = (unit) => {
      const type = unit.deploymentUnitType || unit.entityType;
      switch (type) {
        case 'DU_ONLINE':
          return 'linear-gradient(145deg, #2d5aa0, #1e3f72)'; // Blue for main services
        case 'DTO':
          return 'linear-gradient(145deg, #a02d5a, #721e3f)'; // Red for DTOs
        case 'LIB':
        case 'LIB_IMPL':
          return 'linear-gradient(145deg, #5aa02d, #3f721e)'; // Green for Libraries
        case 'TRX':
          return 'linear-gradient(145deg, #a0772d, #72551e)'; // Orange for Transactions
        case 'DU_LIB':
          return 'linear-gradient(145deg, #6a5acd, #483d8b)'; // Purple for DU-LIB containers
        case 'ComponentFolder':
          return 'linear-gradient(145deg, #666, #444)'; // Gray for folders
        default:
          return 'linear-gradient(145deg, #666, #444)';
      }
    };

    const getTooltipText = (unit) => {
      let tooltip = `${unit.name} (${unit.deploymentUnitType || unit.entityType})`;
      if (unit.uuaa) tooltip += `\\nUUAA: ${unit.uuaa}`;
      if (unit.code) tooltip += `\\nCode: ${unit.code}`;
      if (unit.className) tooltip += `\\nClass: ${unit.className}`;
      if (unit.description) tooltip += `\\nDescription: ${unit.description}`;
      if (unit.createdAt) tooltip += `\\nCreated: ${new Date(unit.createdAt).toLocaleDateString()}`;
      return tooltip;
    };

    // Create nodes from deployment units with hierarchical structure
    const generatedNodes = data.map((unit, index) => {
      const position = {
        x: (index % 3) * 300 + 100,
        y: Math.floor(index / 3) * 200 + 100
      };

      const unitType = unit.deploymentUnitType || unit.entityType;

      return {
        id: unit.id.toString(),
        type: 'default',
        position,
        data: {
          label: (
            <div className="deployment-unit-node" title={getTooltipText(unit)}>
              <div className="unit-header">
                <div className="unit-name">{unit.name}</div>
                <div className="unit-type">{unitType}</div>
              </div>
              <div className="unit-folders">
                {(() => {
                  // For DU_LIB and other containers, show children as nested items
                  if (unit.children && unit.children.length > 0) {
                    return (
                      <div className="children-container">
                        <div className="folder-header">contains</div>
                        {unit.children.map((child, childIndex) => {
                          const childType = child.deploymentUnitType || child.entityType;
                          return (
                            <div key={`child-${child.id}-${childIndex}`} className="nested-object">
                              <div className="object-name">{child.name}</div>
                              <div className="object-type">{childType}</div>
                              {child.className && (
                                <div className="object-class">{child.className}</div>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    );
                  }

                  // For units with traditional component folders, maintain backward compatibility
                  if (unit.componentFolders && unit.componentFolders.length > 0) {
                    const groupedFolders = {};
                    unit.componentFolders.forEach(folder => {
                      const type = folder.type.toLowerCase();
                      if (!groupedFolders[type]) {
                        groupedFolders[type] = [];
                      }
                      groupedFolders[type].push(folder);
                    });

                    let folderTypes = [];
                    if (unitType === 'DU_ONLINE') {
                      folderTypes = ['dto', 'library', 'transactions'];
                    }
                    
                    return folderTypes.map(type => (
                      <div key={type} className={`folder ${type}`}>
                        <div className="folder-header">{type.toUpperCase()}</div>
                        {groupedFolders[type] && groupedFolders[type].map((folder, folderIndex) => (
                          <div key={`${folder.type}-${folder.id}-${folderIndex}`} className="folder-container">
                            {folder.containedUnits && folder.containedUnits.length > 0 ? (
                              <div className="contained-objects">
                                {folder.containedUnits.map((contained, containedIndex) => (
                                  <div key={`contained-${contained.id}-${containedIndex}`} className="contained-object">
                                    <div className="object-name">{contained.name}</div>
                                    {contained.className && (
                                      <div className="object-class">{contained.className}</div>
                                    )}
                                    <div className="object-type">{contained.type}</div>
                                  </div>
                                ))}
                              </div>
                            ) : (
                              <div className="empty-folder">No objects</div>
                            )}
                          </div>
                        ))}
                      </div>
                    ));
                  }

                  return null;
                })()}
              </div>
              {/* Custom Tooltip */}
              <div className="custom-tooltip">
                <div className="tooltip-header">
                  <strong>{unit.name}</strong>
                  <span className="tooltip-type">{unitType}</span>
                </div>
                <div className="tooltip-content">
                  {unit.uuaa && (
                    <div className="tooltip-field">
                      <strong>UUAA:</strong> {unit.uuaa}
                    </div>
                  )}
                  {unit.code && (
                    <div className="tooltip-field">
                      <strong>Code:</strong> {unit.code}
                    </div>
                  )}
                  {unit.className && (
                    <div className="tooltip-field">
                      <strong>Class:</strong> {unit.className}
                    </div>
                  )}
                  {unit.description && (
                    <div className="tooltip-field">
                      <strong>Description:</strong> {unit.description}
                    </div>
                  )}
                  {unit.createdAt && (
                    <div className="tooltip-field">
                      <strong>Created:</strong> {new Date(unit.createdAt).toLocaleDateString()}
                    </div>
                  )}
                  {unit.children && unit.children.length > 0 && (
                    <div className="tooltip-field">
                      <strong>Contains:</strong>
                      <div className="children-details">
                        {unit.children.map(child => (
                          <div key={`tooltip-${child.id}`} className="child-detail">
                            <span className="child-name">{child.name}</span>
                            <span className="child-type">({child.deploymentUnitType || child.entityType})</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )
        },
        style: {
          background: getNodeColor(unit),
          border: '2px solid #555',
          borderRadius: '8px',
          color: '#fff',
          fontSize: '12px',
          width: 180,
          padding: 0
        }
      };
    });

    // Create edges from dependencies
    const generatedEdges = [];
    data.forEach(unit => {
      if (unit.dependencyNames && unit.dependencyNames.length > 0) {
        unit.dependencyNames.forEach(depName => {
          const targetUnit = data.find(u => u.name === depName);
          if (targetUnit) {
            generatedEdges.push({
              id: `${unit.id}-${targetUnit.id}`,
              source: unit.id.toString(),
              target: targetUnit.id.toString(),
              type: 'smoothstep',
              style: {
                stroke: '#00ff00',
                strokeWidth: 2
              },
              markerEnd: {
                type: 'arrowclosed',
                color: '#00ff00'
              }
            });
          }
        });
      }
    });

    return { nodes: generatedNodes, edges: generatedEdges };
  }, [data]);

  const [flowNodes, setNodes, onNodesChange] = useNodesState(nodes);
  const [flowEdges, setEdges, onEdgesChange] = useEdgesState(edges);

  // Update nodes and edges when data changes
  React.useEffect(() => {
    setNodes(nodes);
    setEdges(edges);
  }, [nodes, edges, setNodes, setEdges]);

  return (
    <div className="architecture-diagram">
      <div className="diagram-header">
        <h3>Architecture Diagram</h3>
        <div className="legend">
          <div className="legend-item">
            <div className="legend-color du-online"></div>
            <span>DU-Online</span>
          </div>
          <div className="legend-item">
            <div className="legend-color du-lib"></div>
            <span>DU-Library</span>
          </div>
          <div className="legend-item">
            <div className="legend-color dto"></div>
            <span>DTO</span>
          </div>
          <div className="legend-item">
            <div className="legend-color lib"></div>
            <span>Library</span>
          </div>
          <div className="legend-item">
            <div className="legend-color trx"></div>
            <span>Transaction</span>
          </div>
        </div>
      </div>
      
      <div className="diagram-content">
        {flowNodes.length === 0 ? (
          <div className="empty-diagram">
            <div className="empty-message">
              <h4>No deployment units created</h4>
              <p>Use the terminal to create banking components:</p>
              <code>apx init</code>
            </div>
          </div>
        ) : (
          <ReactFlow
            nodes={flowNodes}
            edges={flowEdges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            fitView
            attributionPosition="bottom-left"
          >
            <Background color="#333" gap={20} />
            <Controls />
            <MiniMap 
              nodeColor="#00ff00"
              maskColor="rgba(0, 0, 0, 0.6)"
              style={{ backgroundColor: '#1a1a1a' }}
            />
          </ReactFlow>
        )}
      </div>
    </div>
  );
};

export default ArchitectureDiagram;