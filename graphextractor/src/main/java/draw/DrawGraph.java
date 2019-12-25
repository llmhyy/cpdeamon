package draw;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.core.dom.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import config.Config;
import graph.extractor.imp.GraphExtractorImp;
import model.ast.DataNode;
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
		 graphExtractor.getBasedASTMap(unit, 1, treeMap);
		 draw.writeASTDot(treeMap);
		 draw.writeNextEdgesDot(treeMap);
	}
	public void writeNextEdgesDot(Map<Integer, DataNode> treeMap) {
		EdgesModel edgesModel = graphExtractor.extractEdges(treeMap);
		String string = ASTtoDOT.ASTtoDotParser(extractor.extractNextEdgesforDot(edgesModel, treeMap));
		FileUtil.writeFile(Config.DOTFILEOUTPATH + "nexttoken.dot", string);
	}

	public void writeGraphGson(List<SampleModel> sampleModels) {
		Gson gson = new Gson();
	     SampleModel[]  jsonArray = (SampleModel[]) sampleModels.toArray(new SampleModel[0]);
	     String gsonObject=gson.toJson(jsonArray).toString();
		FileUtil.writeFile(Config.RESULTOUTFILEPATH+Config.GRAPHFILENAME,gsonObject);
		//FileUtil.zip(Config.RESULTOUTFILEPATH);
	}
 
	public void writeASTDot(Map<Integer, DataNode> treeMap) {
		String dotString = ASTtoDOT.ASTtoDotParser(extractor.extractChildEdgesForDot(treeMap));
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
