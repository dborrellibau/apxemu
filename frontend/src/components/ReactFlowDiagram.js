import React, { useCallback, useMemo, useState, useEffect, useRef } from 'react';
import ReactFlow, { 
  MiniMap, 
  Controls, 
  Background,
  useNodesState,
  useEdgesState,
  addEdge
} from 'reactflow';
import 'reactflow/dist/style.css';
import CustomNode from './CustomNode';
import { convertToReactFlow } from '../utils/dataConverter';
import './ReactFlowDiagram.css';

const nodeTypes = {
  customNode: CustomNode,
};

const ReactFlowDiagram = ({ data }) => {
  // Estado para posiciones persistidas en localStorage
  const [persistedPositions, setPersistedPositions] = useState({});
  const saveTimeoutRef = useRef(null);
  
  // Cargar posiciones guardadas al montar componente (solo una vez)
  useEffect(() => {
    try {
      const saved = localStorage.getItem('apx-diagram-positions');
      if (saved) {
        const positions = JSON.parse(saved);
        setPersistedPositions(positions);
        console.log('✅ Loaded', Object.keys(positions).length, 'saved positions');
      }
    } catch (error) {
      console.error('Failed to load positions:', error);
      setPersistedPositions({});
    }
  }, []);
  
  // Cleanup reactivo: eliminar posiciones de nodos que ya no existen
  useEffect(() => {
    // Skip cleanup if no data loaded yet
    // IMPORTANT: Don't delete localStorage here - during F5 refresh, data takes time to load
    // but localStorage already has saved positions that should be preserved
    if (!data || data.length === 0) {
      return;
    }
    
    // Skip if persistedPositions is empty (still loading from localStorage or nothing saved)
    if (Object.keys(persistedPositions).length === 0) {
      return;
    }
    
    // Caso 2: Con datos, limpiar huérfanos (nodos eliminados)
    const currentNodeNames = new Set();
    
    const extractNames = (containers) => {
      containers.forEach(container => {
        if (!container.deleted) {
          currentNodeNames.add(container.name);
          if (container.children) {
            extractNames(container.children);
          }
        }
      });
    };
    
    extractNames(data);
    
    // Filtrar solo posiciones de nodos que existen actualmente
    const validPositions = {};
    let foundOrphan = false;
    
    Object.entries(persistedPositions).forEach(([name, pos]) => {
      if (currentNodeNames.has(name)) {
        validPositions[name] = pos;
      } else {
        foundOrphan = true;
      }
    });
    
    // Only update if we found orphans
    if (foundOrphan) {
      console.log('🗑️ Removed', Object.keys(persistedPositions).length - Object.keys(validPositions).length, 'orphaned position(s)');
      setPersistedPositions(validPositions);
      
      if (Object.keys(validPositions).length > 0) {
        localStorage.setItem('apx-diagram-positions', JSON.stringify(validPositions));
      } else {
        localStorage.removeItem('apx-diagram-positions');
      }
    }
  }, [data, persistedPositions]);
  
  // Convert hierarchical data to ReactFlow format con merge de posiciones
  const { nodes: initialNodes, edges: initialEdges } = useMemo(() => {
    if (!data || data.length === 0) {
      return { nodes: [], edges: [] };
    }
    
    const { nodes, edges } = convertToReactFlow(data);
    
    // Read positions directly from localStorage to avoid race conditions with state
    let savedPositions = persistedPositions;
    if (Object.keys(savedPositions).length === 0) {
      try {
        const saved = localStorage.getItem('apx-diagram-positions');
        if (saved) {
          savedPositions = JSON.parse(saved);
        }
      } catch (error) {
        console.error('Failed to read positions in useMemo:', error);
      }
    }
    
    // Merge saved positions: use persisted position if exists, otherwise use calculated
    const mergedNodes = nodes.map(node => ({
      ...node,
      position: savedPositions[node.data.name] || node.position
    }));
    
    return { nodes: mergedNodes, edges };
  }, [data, persistedPositions]);

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  // Update nodes and edges when data changes
  useEffect(() => {
    setNodes(initialNodes);
    setEdges(initialEdges);
  }, [initialNodes, initialEdges, setNodes, setEdges]);
  
  // Handler para cambios de nodos con debounce para guardar posiciones
  const handleNodesChange = useCallback((changes) => {
    // Aplicar cambios visuales inmediatamente (ReactFlow)
    onNodesChange(changes);
    
    // Filtrar solo cambios de posición
    const positionChanges = changes.filter(
      change => change.type === 'position' && change.position
    );
    
    if (positionChanges.length === 0) return;
    
    // Debounce: cancelar guardado anterior si existe
    if (saveTimeoutRef.current) {
      clearTimeout(saveTimeoutRef.current);
    }
    
    // Programar nuevo guardado después de 500ms sin movimientos
    saveTimeoutRef.current = setTimeout(() => {
      const updatedPositions = {};
      
      nodes.forEach(node => {
        if (node.data?.name) {
          updatedPositions[node.data.name] = {
            x: Math.round(node.position.x),
            y: Math.round(node.position.y)
          };
        }
      });
      
      setPersistedPositions(updatedPositions);
      
      try {
        localStorage.setItem('apx-diagram-positions', JSON.stringify(updatedPositions));
        console.log('💾 Saved', Object.keys(updatedPositions).length, 'positions');
      } catch (error) {
        console.error('Failed to save positions:', error);
      }
    }, 500);
  }, [nodes, onNodesChange]);
  
  // Cleanup: cancelar timeout al desmontar componente
  useEffect(() => {
    return () => {
      if (saveTimeoutRef.current) {
        clearTimeout(saveTimeoutRef.current);
      }
    };
  }, []);

  const onConnect = useCallback(
    (params) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  const onNodeClick = useCallback((event, node) => {
    console.log('Node clicked:', node.data);
    // Here we can add more interaction logic later
  }, []);

  if (!data || data.length === 0) {
    return (
      <div className="react-flow-container empty">
        <div className="empty-state">
          <h4>No Architecture Data</h4>
          <p>Create some deployment units to see the architecture</p>
          <code>du-online "MyApp"</code>
        </div>
      </div>
    );
  }

  return (
    <div className="react-flow-container">
      <div className="react-flow-header">
        <h3>Interactive Architecture</h3>
        <span className="item-count">{data.length} root item(s)</span>
      </div>
      
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={handleNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        onNodeClick={onNodeClick}
        nodeTypes={nodeTypes}
        fitView
        fitViewOptions={{
          padding: 0.2,
          minZoom: 0.1,
          maxZoom: 1.5
        }}
        defaultViewport={{ x: 0, y: 0, zoom: 0.8 }}

      >
        <Controls />
        <MiniMap 
          nodeStrokeColor="#6b7280"
          nodeColor="#374151" 
          nodeBorderRadius={4}
          pannable
          zoomable
          position="bottom-left"
          style={{
            backgroundColor: 'rgba(45, 45, 45, 0.95)'
          }}
        />
        <Background 
          variant="dots" 
          gap={24} 
          size={1.5} 
          color="rgba(107, 114, 128, 0.3)"
          style={{
            background: 'linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%)'
          }}
        />
      </ReactFlow>
    </div>
  );
};

export default ReactFlowDiagram;