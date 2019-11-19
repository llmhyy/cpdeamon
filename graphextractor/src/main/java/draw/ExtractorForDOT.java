package draw;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.DataNode;
import model.graph.EdgesModel;

public class ExtractorForDOT {
	
	//extractor edges for visual AST,e.g. a node present as Idx_nodeTYpe 
	public List<String[]> extraChildEdgesForDot(Map<Integer, DataNode> treeMap) {
		List<String[]> childEdgelist = new ArrayList<>();
		for (Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
			int nodeId = entry.getKey();
			DataNode dataNode = entry.getValue();
			String nodeType = dataNode.nodeType.substring(dataNode.nodeType.lastIndexOf(".") + 1);
			List<Integer> childLabels = dataNode.childrenLables;
			for (int label : childLabels) {
				DataNode childNode=treeMap.get(label);	
				String childNodeType=childNode.nodeType.substring(childNode.nodeType.lastIndexOf(".")+1);
				if (childNode.isLeaf==true) {
					String token=childNode.node.toString();
					childEdgelist.add(new String[] {label+"_"+childNodeType,label+"_"+token});
				}
				childEdgelist.add(new String[] { nodeId+"_"+nodeType, label+"_"+childNodeType });
			}
		}
		return childEdgelist;
	}
	
	public List<String []> extraNextEdgesforDot(EdgesModel edges,Map<Integer, DataNode> treeMap) {
		List<String []>nextTokenStringList=new ArrayList<>();
		List<Integer []>nextTokenList=edges.getNextToken();
		for (Integer[] Idx : nextTokenList) {
			nextTokenStringList.add(new String[]{Idx[0]+"_"+treeMap.get(Idx[0]).node.toString(),Idx[1]+"_"+treeMap.get(Idx[1]).node.toString()});
		}
		return nextTokenStringList;
	}
}
