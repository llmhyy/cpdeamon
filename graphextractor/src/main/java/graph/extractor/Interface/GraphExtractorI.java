package graph.extractor.Interface;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import model.DataNode;
import model.graph.EdgesModel;
import model.graph.SampleModel;

public interface GraphExtractorI {

	EdgesModel extraEdges(Map<Integer, DataNode> treeMap);
	SampleModel extraGraphSample(EdgesModel edges);
	void extraNodeLabels();
	// 输入的是CompilationUnit根节点， label为0
	void getBasedASTMap(ASTNode node, int label, Map<Integer, DataNode> Nodes);
	CompilationUnit processJavaFile(File file);
}
