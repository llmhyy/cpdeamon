package model.graph;

import java.util.HashMap;
import java.util.Map;

public class ContextGraphModel {

	EdgesModel Edges;
	Map<Integer, String> NodeLabels;
	Map<Integer, String> NodeTypes;

	public EdgesModel getEdges() {
		return Edges;
	}

	public void setEdges(EdgesModel edges) {
		this.Edges = edges;
	}

	public Map<Integer, String> getNodeLabels() {
		if(NodeTypes==null) {
			this.NodeTypes=new HashMap<>();
		}
		return NodeLabels;
	}

	public void setNodeLabels(Map<Integer, String> nodeLabels) {
		NodeLabels = nodeLabels;
	}

	public Map<Integer, String> getNodeTypes() {
		if(NodeLabels==null) {
			this.NodeLabels=new HashMap<Integer, String>();
		}
		return NodeTypes;
	}

	public void setNodeTypes(Map<Integer, String> nodeTypes) {
		NodeTypes = nodeTypes;
	}
	
}
