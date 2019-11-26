package graph.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.core.util.VerificationInfo;
import org.eclipse.jface.text.Document;

import graph.extractor.Interface.GraphExtractorI;
import model.ast.DataNode;
import model.graph.ContextGraphModel;
import model.graph.EdgesModel;
import model.graph.SampleModel;
import model.graph.SymbolCandidateModel;
import model.intermediate.ClassCandidates;

public class GraphExtractorImp implements GraphExtractorI {
	public  int ID = 1; // used for mark node serial number，0 leave to <slot>

	@Override
	public EdgesModel extractEdges(Map<Integer, DataNode> treeMap) {
		EdgesModel edges = new EdgesModel();
		List<Integer[]> childEdgelist = new ArrayList<>();
		List<Integer[]> nextTokenList = new ArrayList<>();
		List<Integer> leafNodeList = new ArrayList<>();
		// travel treeMap add edge from now node to target
		for (Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
			int nodeId = entry.getKey(); // now node label
			DataNode dataNode = entry.getValue(); // now node

			List<Integer> childLabels = dataNode.childrenLables;// all child node of now node
			// add all child edges from now_node to child if have
			for (int childIdx : childLabels) {
				childEdgelist.add(new Integer[] { nodeId, childIdx });
			}
			// collect all leaf node to add nextToken edges
			if (dataNode.isLeaf) {
				leafNodeList.add(nodeId);
			}
		}
		// add nextToken edges
		for (int i = 0; i < leafNodeList.size() - 1; i++) {
			nextTokenList.add(new Integer[] { leafNodeList.get(i), leafNodeList.get(i + 1) });
		}
		edges.setNextToken(nextTokenList);
		edges.setChild(childEdgelist);
		return edges;
	}

	// in scene of that Edges had extract early
	public SampleModel extraGraphSample(EdgesModel edges) {
		SampleModel sampleModel = new SampleModel();
		ContextGraphModel contextGraph = new ContextGraphModel();
		contextGraph.setEdges(edges);
		sampleModel.setContextGraph(contextGraph);
		return sampleModel;
	}

	@Override
	public SampleModel extractGraphSample(Map<Integer, DataNode> treeMap) {
		ContextGraphModel contextGraph = extractContextGraphQuickly(treeMap);
		return new SampleModel(null, 0, contextGraph, null);
	}

	// get all graphsamples
	public List<SampleModel> extractGraphSamples(Map<Integer, DataNode> treeMap,String fileName) {
		List<SampleModel> samples=new ArrayList<>();
		ContextGraphModel contextGraph = extractContextGraphQuickly(treeMap);
		List<ClassCandidates> symbolCandidateModels = extractSymbolCandidates(contextGraph.getNodeTypes(),
				contextGraph.getNodeLabels());
		for(Iterator<ClassCandidates> iterable=symbolCandidateModels.iterator();iterable.hasNext();) {
			ContextGraphModel contextGraph2 = extractContextGraphQuickly(treeMap);
			ClassCandidates classCandidates=iterable.next();
			SymbolCandidateModel[] symbolCandidates=classCandidates.getSymbolCandidateModels();
			int slotTokenIdx = extractSlotTokenIdx(contextGraph2.getNodeTypes());
			System.out.print("\nslotTokenIdx:"+slotTokenIdx+"\n");
			replaceASTNodeForCandidates(contextGraph2, classCandidates.getSymbolNode());
			for (int i = 0; i < symbolCandidates.length; i++) {
				SymbolCandidateModel symbolCandidate=symbolCandidates[i];
				contextGraph2.getNodeLabels().put(symbolCandidate.getSymbolDummyNode(), symbolCandidate.getSymbolName());
			}
			samples.add(new SampleModel(fileName, slotTokenIdx, contextGraph2, symbolCandidates));	
		}
		return samples;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getBasedASTMap(ASTNode node, int label, Map<Integer, DataNode> Nodes) {
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
		ASTParser parser = ASTParser.newParser(AST.JLS8); // define java programming specification
		parser.setKind(ASTParser.K_COMPILATION_UNIT); // type of parser : java file
		parser.setSource(document.get().toCharArray());// param :char or java model
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);// a point for java file
		return unit;
	}

	@Override
	public Map<Integer, String> extractNodeLabels(Map<Integer, DataNode> treeMap) {
		// TODO Auto-generated method stub
		return null;
	}

	// used in a scene of VarMisuse that use terminal token instead of terminal node
	public Map<Integer, String> extractNodeLabelsUseTerminalToken(Map<Integer, DataNode> treeMap) {
		Map<Integer, String> nodeLabels = new HashMap<Integer, String>();
		for (Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
			int idx = entry.getKey();
			DataNode dataNode = entry.getValue();
			// if non-terminal node set label equals node type
			String label = dataNode.nodeType.substring(dataNode.nodeType.lastIndexOf(".") + 1);
			// terminal node set token as label
			if (dataNode.isLeaf) {
				label = dataNode.node.toString();
			}
			nodeLabels.put(idx, label);
		}
		return nodeLabels;
	}

	@Override
	public ContextGraphModel extractContextGraph(Map<Integer, DataNode> treeMap) {
		// get Edges
		EdgesModel edges = extractEdges(treeMap);
		Map<Integer, String> nodeLabels = extractNodeLabelsUseTerminalToken(treeMap);
		// get node types
		// TO DO
		return new ContextGraphModel(edges, nodeLabels, null);
	}

	// Although we provide an interface to extract each component separately,
	// this means less efficiency
	// so we provide a composite method
	public ContextGraphModel extractContextGraphQuickly(Map<Integer, DataNode> treeMap) {
		EdgesModel edges = new EdgesModel();
		List<Integer[]> childEdgelist = new ArrayList<>();
		List<Integer[]> nextTokenList = new ArrayList<>();
		List<Integer> leafNodeList = new ArrayList<>();
		Map<String, String> variableTypes = new HashMap<>();
		Map<Integer, String> nodeLabels = new HashMap<Integer, String>();
		//put slot in first position
		nodeLabels.put(0, "<SLOT>");
		// travel treeMap add edge from now node to target
		System.out.print("ExtractContextGraph task---------------------\n");
		for (Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
			int nodeId = entry.getKey(); // now node label
			DataNode dataNode = entry.getValue(); // now node

			// 1.get Edges
			List<Integer> childLabels = dataNode.childrenLables;// all child node of now node
			// add all child edges from now_node to child if have
			for (int childIdx : childLabels) {
				childEdgelist.add(new Integer[] { nodeId, childIdx });
			}
			// collect all leaf node to add nextToken edges
			if (dataNode.isLeaf) {
				leafNodeList.add(nodeId);
			}

			// 2.collect NodeTypes
			// collect variableTypes by String split e.g. <a,int>
			// e.g. int a=1; and int a; declaration type is always last but one
			// then a=1 or a; can split by "=" or ";" we can get a which is token
			if (dataNode.node instanceof FieldDeclaration || dataNode.node instanceof VariableDeclarationStatement
					|| dataNode.node instanceof SingleVariableDeclaration) {
				String[] stringArray = dataNode.node.toString().split(" ");
				String type = stringArray[stringArray.length - 2];
				String token;
				String endPos = stringArray[stringArray.length - 1];
				if (endPos.contains("=")) {
					token = endPos.substring(0, endPos.indexOf("="));
				} else if (endPos.contains(";")) {
					token = endPos.substring(0, endPos.indexOf(";"));
				} else {
					token = endPos;
				}
				variableTypes.put(token, type);
			}

			// 3 collect nodeLabels that is a dictionary
			// if non-terminal node set label equals node type
			String label = dataNode.nodeType.substring(dataNode.nodeType.lastIndexOf(".") + 1);
			// terminal node set token as label
			if (dataNode.isLeaf) {
				label = dataNode.node.toString();
			}
			nodeLabels.put(nodeId, label);
		}

		// finally,get Node type and nextTokenList from leafNodeList
		Map<Integer, String> nodeType = new HashMap<>();
		System.out.print("NodeTypes:---------------------\n");
		String token = treeMap.get(leafNodeList.get(0)).node.toString();
		if (variableTypes.get(token) != null) {
			nodeType.put(leafNodeList.get(0), variableTypes.get(token));
			System.out.print(leafNodeList.get(0) + "_" + token + "_" + variableTypes.get(token) + "\n");
		}
		for (int i = 0; i < leafNodeList.size() - 1; i++) {
			// add nextToken edges
			nextTokenList.add(new Integer[] { leafNodeList.get(i), leafNodeList.get(i + 1) });
			// get Node type
			int label = leafNodeList.get(i + 1);
			String token2 = treeMap.get(label).node.toString();
			if (variableTypes.get(token2) != null) {
				nodeType.put(label, variableTypes.get(token2));
			}
		}

		// combine the components to get context\
		System.out.print("set edges --------------------\n");
		edges.setNextToken(nextTokenList);
		edges.setChild(childEdgelist);
		ContextGraphModel contextGraph = new ContextGraphModel();
		contextGraph.setEdges(edges);
		System.out.print("set nodeType --------------------\n");
		contextGraph.setNodeTypes(nodeType);
		System.out.print("set nodeLabels --------------------\n");
		contextGraph.setNodeLabels(nodeLabels);
		System.out.print("ExtractContextGraph task end---------\n");
		return contextGraph;
	}

	public Map<String, String> extractVariableTypes(Map<Integer, DataNode> treeMap) {
		Map<String, String> variableTypes = new HashMap<>();
		for (Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
			DataNode dataNode = entry.getValue();
			if (dataNode.node instanceof FieldDeclaration || dataNode.node instanceof VariableDeclarationStatement
					|| dataNode.node instanceof SingleVariableDeclaration) {
				String[] stringArray = dataNode.node.toString().split(" ");
				String type = stringArray[stringArray.length - 2];
				String token;
				String endPos = stringArray[stringArray.length - 1];
				if (endPos.contains("=")) {
					token = endPos.substring(0, endPos.indexOf("="));
				} else if (endPos.contains(";")) {
					token = endPos.substring(0, endPos.indexOf(";"));
				} else {
					token = endPos;
				}
				variableTypes.put(token, type);
			}
		}
		return variableTypes;
	}

	@Override
	public Map<Integer, String> extractNodeTypes(List<Integer[]> nextTokenEdges, Map<String, String> variableTypesMap,
			Map<Integer, DataNode> treeMap) {
		// TODO for separately interface
		return null;
	}
	@Override
	public List<ClassCandidates> extractSymbolCandidates(Map<Integer, String> nodeTypes,
			Map<Integer, String> nodeLabels) {
		List<ClassCandidates> result = new ArrayList<>();
		// grouping by type
		Map<String, List<Integer>> typeNodes = new HashMap<String, List<Integer>>();		
		for (Map.Entry<Integer, String> entry : nodeTypes.entrySet()) {

			String type = entry.getValue();
			Integer node = entry.getKey();
			// if type contains add node to list
			if (typeNodes.containsKey(type)) {
				typeNodes.get(type).add(node);
			} else {
				// else new type list put into map
				List<Integer> nodes = new ArrayList<>();
				nodes.add(node);
				typeNodes.put(type, nodes);
			}
		}
		
		// then typeNodes have all type and type node list
		// get Candidates
		//The size of the collection of candidate arrays is equal to the number of token types
		//i.g.How many number of token types exist, how many candidate arrays can be extracted	
		
		for (Map.Entry<String, List<Integer>> entry : typeNodes.entrySet()) {
			int y = nodeLabels.size();
			// get a list for a type
			List<Integer> labelsIdx = entry.getValue();
			if (labelsIdx.size()<2) {
				continue;
			}
			// random a position as the slot
			int nodeIdx = new Random().nextInt(labelsIdx.size());
			int node = labelsIdx.get(nodeIdx);
			ClassCandidates candidates=new ClassCandidates();
			candidates.setSymbolNode(node);			
			// this node as current candidate in slot
			//Tokens of slot are placed at the first of the array as valid candidates
			SymbolCandidateModel symbolCandidateModel = new SymbolCandidateModel();
			symbolCandidateModel.setSymbolDummyNode(y);
			String currentSymbolName= nodeLabels.get(node);
			symbolCandidateModel.setSymbolName(currentSymbolName);
			symbolCandidateModel.setIsCorrect(true);

			//Select other token candidates of the same type
			List<SymbolCandidateModel> symbolCandidateModels=new ArrayList<>();
			symbolCandidateModels.add(symbolCandidateModel);
			// others same type node as false candidate
			List<String> lables = new ArrayList<>(); //detect duplicate label String
			for (int i = 0; i < labelsIdx.size(); i++) {
				String falseSymbolDummyNode=nodeLabels.get(labelsIdx.get(i));
					if (i==nodeIdx || currentSymbolName.equals(falseSymbolDummyNode)) {
						continue;
					}
					if (!lables.contains(falseSymbolDummyNode)) {
						 lables.add(falseSymbolDummyNode);
					}else {
						continue;
					}	
					//varMisuse task limit 5 size of candidateArray in one sample 
					if (symbolCandidateModels.size()==5) {
						break;
					}
					symbolCandidateModel = new SymbolCandidateModel();
					symbolCandidateModel.setSymbolDummyNode(++y);
					symbolCandidateModel.setSymbolName(nodeLabels.get(labelsIdx.get(i)));
					symbolCandidateModel.setIsCorrect(false);
					symbolCandidateModels.add(symbolCandidateModel);
			}
			SymbolCandidateModel [] symbolCandidateArray=new SymbolCandidateModel[symbolCandidateModels.size()];
			if(symbolCandidateModels.size()>1) {
				candidates.setSymbolCandidateModels(symbolCandidateModels.toArray(symbolCandidateArray));
			    result.add(candidates);
			}
		}
		return result;
	}

	@Override
	public int extractSlotTokenIdx(Map<Integer, String> nodeTypes) {
		// TODO Auto-generated method stub
		int idx = new Random().nextInt(nodeTypes.size());
		int x=0;
		int result = 0;
		for (Integer key : nodeTypes.keySet()) {
			++x;
			if (x == idx) {
				result = key;
				break;
			}
		}
		return result;
	}

	@Override
	public void replaceASTNodeForCandidates(ContextGraphModel contextGraph,int SymbolNode) {
		
		// TODO Auto-generated method stub
		System.out.print("start repleace symbolNode");
		List<Integer[]> childEdges=contextGraph.getEdges().getChild();
		//replace child node
		for (Iterator<Integer[]> iterator = childEdges.iterator(); iterator.hasNext();) {
			Integer[] edges =iterator.next();
			for (int i = 0; i < edges.length; i++) {
				if (edges[i]==SymbolNode) {
					System.out.print("replace child");
					edges[i]=0;
				}
			}
		}
		contextGraph.getEdges().setChild(childEdges);;
		//replace next token node
		List<Integer[]> nextTokenEdges=contextGraph.getEdges().getNextToken();
		for (Iterator<Integer[]> iterator = nextTokenEdges.iterator(); iterator.hasNext();) {
			Integer[] edges = (Integer[]) iterator.next();
			for (int i = 0; i < edges.length; i++) {
				if (edges[i]==SymbolNode) {
					System.out.print("replace nextToken");
					edges[i]=0;
				}
			}
		}
		contextGraph.getEdges().setNextToken(nextTokenEdges);
	}

}
