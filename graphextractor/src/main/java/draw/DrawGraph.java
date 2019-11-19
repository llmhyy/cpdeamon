package draw;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.gson.Gson;

import config.Config;
import graph.extractor.GraphExtractorImp;
import model.DataNode;
import model.graph.EdgesModel;
import model.graph.SampleModel;
import util.ASTtoDOT;
import util.FileUtil;

public class DrawGraph {
	GraphExtractorImp graphExtractor = new GraphExtractorImp();
	ExtractorForDOT extractor = new ExtractorForDOT();
    public static void main(String [] args) {
    	 DrawGraph draw =new DrawGraph();
    	 Map<Integer, DataNode> treeMap = new HashMap<>();
		 GraphExtractorImp graphExtractor=new GraphExtractorImp();
		 CompilationUnit unit= graphExtractor.processJavaFile(new File(Config.SOURCEFILEPATH));
		 graphExtractor.getBasedASTMap(unit, 0, treeMap);
		 draw.writeASTDot(treeMap);
		 draw.writeNextEdgesDot(treeMap);
	}
	public void writeNextEdgesDot(Map<Integer, DataNode> treeMap) {
		EdgesModel edgesModel = graphExtractor.extraEdges(treeMap);
		String string = ASTtoDOT.ASTtoDotParser(extractor.extraNextEdgesforDot(edgesModel, treeMap));
		FileUtil.writeFile(Config.DOTFILEOUTPATH + "nexttoken.dot", string);
	}

	public void writeGraphGson(SampleModel sampleModel) {
		Gson gson = new Gson();
		String jsonObject = gson.toJson(sampleModel);
		FileUtil.writeFile(Config.RESULTOUTFILEPATH, jsonObject);
	}

	public void writeASTDot(Map<Integer, DataNode> treeMap) {
		String dotString = ASTtoDOT.ASTtoDotParser(extractor.extraChildEdgesForDot(treeMap));
		FileUtil.writeFile(Config.DOTFILEOUTPATH + "ast.dot", dotString);
	}

	// print tree structure
	public void travelTreeMap(Map<Integer, DataNode> treeMap) {
		for (Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
			int nodeId = entry.getKey();
			DataNode dataNode = entry.getValue();
			String nodeType = dataNode.nodeType.substring(dataNode.nodeType.lastIndexOf(".") + 1);
			System.out.print(nodeId + nodeType + "_" + dataNode.node + "\n");
		}
	}

	// print leaf node
	public void travelLeafNode(Map<Integer, DataNode> treeMap) {
		for (Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
			int nodeId = entry.getKey();
			DataNode dataNode = entry.getValue();
			String nodeType = dataNode.nodeType.substring(dataNode.nodeType.lastIndexOf(".") + 1);
			if (dataNode.isLeaf == true)
				System.out.print(nodeId + nodeType + "_" + dataNode.node + "\n");
		}
	}
}
