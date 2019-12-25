package model.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ContextGraphModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4754012657509882142L;
	EdgesModel Edges;
	Map<Integer, String> NodeLabels;
	Map<Integer, String> NodeTypes;
    /**
     * a constructor
     * @param Edges
     * @param NodeLabels
     * @param NodeTypes
     */
	public ContextGraphModel(EdgesModel edges,Map<Integer, String> nodeLabels,Map<Integer, String> nodeTypes) {
     this.Edges=edges;
     this.NodeLabels=nodeLabels;
     this.NodeTypes=nodeTypes;  
	}

	public ContextGraphModel() {
       super();
	}

	public EdgesModel getEdges() {
		return Edges;
	}

	public void setEdges(EdgesModel edges) {
		this.Edges = edges;
	}

	public Map<Integer, String> getNodeLabels() {
		if (NodeTypes == null) {
			this.NodeTypes = new HashMap<>();
		}
		return NodeLabels;
	}

	public void setNodeLabels(Map<Integer, String> nodeLabels) {
		NodeLabels = nodeLabels;
	}

	public Map<Integer, String> getNodeTypes() {
		if (NodeLabels == null) {
			this.NodeLabels = new HashMap<Integer, String>();
		}
		return NodeTypes;
	}

	public void setNodeTypes(Map<Integer, String> nodeTypes) {
		NodeTypes = nodeTypes;
	}

}
