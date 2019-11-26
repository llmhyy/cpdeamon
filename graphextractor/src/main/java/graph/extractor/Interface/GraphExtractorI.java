package graph.extractor.Interface;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import model.ast.DataNode;
import model.graph.ContextGraphModel;
import model.graph.EdgesModel;
import model.graph.SampleModel;
import model.graph.SymbolCandidateModel;
import model.intermediate.ClassCandidates;

public interface GraphExtractorI {
	// extra Child and nextToken edges
	EdgesModel extractEdges(Map<Integer, DataNode> treeMap);

	// extractor a sample graph from AST map, the required component models are \
	// filename,contextGraph,slotDummyNode, symbolCandidates,SlotTokenIdx
	SampleModel extractGraphSample(Map<Integer, DataNode> treeMap);

	// extract ContextGraph which contains Edges,nodeLabels,nodeTypes
	ContextGraphModel extractContextGraph(Map<Integer, DataNode> treeMap);

	// input is the CompilationUnit root ， label is 0
	void getBasedASTMap(ASTNode node, int label, Map<Integer, DataNode> Nodes);

	// process a java file to compilationUnit
	CompilationUnit processJavaFile(File file);

	// extract node labels such as [Integer,String ].e.g. [0,"CompilationUnit"]
	Map<Integer, String> extractNodeLabels(Map<Integer, DataNode> treeMap);

	// extract node types such as [int ,String ] e.g. [0,int]
	Map<Integer, String> extractNodeTypes(List<Integer[]> nextTokenEdges, Map<String, String> variableTypesMap,
			Map<Integer, DataNode> treeMap);

	// extract SymbolCandidates
	List<ClassCandidates> extractSymbolCandidates(Map<Integer, String> nodeTypes,
			Map<Integer, String> nodeLabels);

	// extract slotTokenIdx
	//get slotToken from variable node
	int extractSlotTokenIdx(Map<Integer, String> nodeTypes);
	
	//replace node and edges for varMisuse slot
	void replaceASTNodeForCandidates(ContextGraphModel contextGraph,int SymbolNode);
}
