package graph;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

import config.Config;
import draw.DrawGraph;
import graph.extractor.GraphExtractorImp;
import model.DataNode;
import model.graph.EdgesModel;
import model.graph.SampleModel;

/**
 * Eclipse AST demo
 * 
 * @author SongXueZhi
 *
 */
public class App {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DrawGraph draw=new DrawGraph();
		Map<Integer, DataNode> treeMap = new HashMap<>();
		 GraphExtractorImp graphExtractor=new GraphExtractorImp();
		try {
			CompilationUnit unit=graphExtractor.processJavaFile(new File(Config.SOURCEFILEPATH));
			graphExtractor.getBasedASTMap(unit, 0, treeMap);  //get based AST map
			
			// extractor a sample graph from AST map, the required component models are \
			// edges, contextGraph,sample symbol candidates
			EdgesModel edges=graphExtractor.extraEdges(treeMap); //extractor edges from base tree			
			//extractor sample
			SampleModel sampleModel=graphExtractor.extraGraphSample(edges); 
			String fileName=Config.SOURCEFILEPATH.substring(Config.SOURCEFILEPATH.lastIndexOf(Config.SUBIDX));
			sampleModel.setFilename(fileName);
			draw.writeGraphGson(sampleModel);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
