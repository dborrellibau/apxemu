import React, { useCallback, useMemo } from 'react';
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
  // Convert hierarchical data to ReactFlow format
  const { nodes: initialNodes, edges: initialEdges } = useMemo(() => {
    if (!data || data.length === 0) {
      return { nodes: [], edges: [] };
    }
    return convertToReactFlow(data);
  }, [data]);

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  // Update nodes and edges when data changes
  React.useEffect(() => {
    setNodes(initialNodes);
    setEdges(initialEdges);
  }, [initialNodes, initialEdges, setNodes, setEdges]);

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
        onNodesChange={onNodesChange}
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