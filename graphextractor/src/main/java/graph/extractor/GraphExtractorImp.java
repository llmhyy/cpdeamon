package graph.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jface.text.Document;

import graph.extractor.Interface.GraphExtractorI;
import model.DataNode;
import model.graph.ContextGraphModel;
import model.graph.EdgesModel;
import model.graph.SampleModel;

public class GraphExtractorImp implements GraphExtractorI{
	public static int ID = 0; // 用来编号
	@Override
	public EdgesModel extraEdges(Map<Integer, DataNode> treeMap) {
		EdgesModel edges = new EdgesModel();
		List<Integer[]> childEdgelist = new ArrayList<>();
		List<Integer []> nextTokenList=new ArrayList<>();
		List<Integer>leafNodeList =new ArrayList<>();
		// travel treeMap add edge from now node to target
		for (Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
			int nodeId = entry.getKey(); //now node label
			DataNode dataNode = entry.getValue(); //now node 
			
			List<Integer> childLabels = dataNode.childrenLables;// all child node of now node
			// add all child edges from now_node to child if have
			for (int label : childLabels) {
				childEdgelist.add(new Integer[] { nodeId, label });
			}
			// collect all leaf node to add nextToken edges
			if(dataNode.isLeaf){
				leafNodeList.add(nodeId);
				}
		}
		// add nextToken edges
		for(int i=0;i<leafNodeList.size()-1;i++) {
			nextTokenList.add(new Integer[] {leafNodeList.get(i),leafNodeList.get(i+1)});
		}
		edges.setNextToken(nextTokenList);
		edges.setChild(childEdgelist);
		return edges;
	}

	@Override
	public SampleModel extraGraphSample(EdgesModel edges) {
		SampleModel sampleModel = new SampleModel();
		ContextGraphModel contextGraph = new ContextGraphModel();
		contextGraph.setEdges(edges);
		sampleModel.setContextGraph(contextGraph);
		return sampleModel;
	}

	@Override
	public void extraNodeLabels() {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public  void getBasedASTMap(ASTNode node, int label, Map<Integer, DataNode> Nodes) {
		// 先创建一个节点数据结构
		DataNode myNode = new DataNode();
		Nodes.put(label, myNode);
		myNode.label = label;
		myNode.node = node;
		myNode.nodeType = node.getClass().toString();
		List<?> listProperty = node.structuralPropertiesForType();
		boolean hasChildren = false;
		for (int i = 0; i < listProperty.size(); i++) {
			StructuralPropertyDescriptor propertyDescriptor = (StructuralPropertyDescriptor) listProperty.get(i);
			if (propertyDescriptor instanceof ChildListPropertyDescriptor) {// ASTNode列表
				ChildListPropertyDescriptor childListPropertyDescriptor = (ChildListPropertyDescriptor) propertyDescriptor;
				Object children = node.getStructuralProperty(childListPropertyDescriptor);

				List<ASTNode> childrenNodes = (List<ASTNode>) children;
				for (ASTNode childNode : childrenNodes) {
					// 获取所有节点
					if (childNode == null)
						continue;
					hasChildren = true;
					myNode.childrenNodes.add(childNode);
					myNode.childrenLables.add((++ID));
					getBasedASTMap(childNode, ID, Nodes);// 继续递归
					// System.out.println("childrenList: "+childNode+" "+childNode.getClass());
				}

			} else if (propertyDescriptor instanceof ChildPropertyDescriptor) {// 一个ASTNode
				ChildPropertyDescriptor childPropertyDescriptor = (ChildPropertyDescriptor) propertyDescriptor;
				Object child = node.getStructuralProperty(childPropertyDescriptor);
				ASTNode childNode = (ASTNode) child;
				if (childNode == null)
					continue;
				hasChildren = true;
				// 获取了这个节点
				myNode.childrenNodes.add(childNode);
				myNode.childrenLables.add((++ID));
				getBasedASTMap(childNode, ID, Nodes);// 继续递归

				// System.out.println("child: "+childNode +" "+childNode.getClass());
			}
		}
		if (hasChildren) {
			// 进行递归子节点
			myNode.isLeaf = false;
		} else {
			// 结束，是叶子结点
			myNode.isLeaf = true;
		}
	}

	@Override
	public CompilationUnit processJavaFile(File file) {
		// read Java File by relative path
		String source = null;
		try {
			source = FileUtils.readFileToString(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document document = new Document(source);
		// parse file
		ASTParser parser = ASTParser.newParser(AST.JLS10); // define java programming specification
		parser.setKind(ASTParser.K_COMPILATION_UNIT); // type of parser : java file
		parser.setSource(document.get().toCharArray());// param :char or java model
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);// a point for java file        
		return unit;		
	}

}
