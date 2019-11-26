package graph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

import config.Config;
import draw.DrawGraph;
import graph.extractor.GraphExtractorImp;
import model.ast.DataNode;
import model.graph.SampleModel;
import util.FileUtil;

/**
 * java graph extractor for VarMisuse
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
		File[] files=FileUtil.getTestJavaFilesArray(Config.SOURCEFILEPATH);
		List<SampleModel> sampleModels=new ArrayList<>();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			sampleModels.addAll(extractor(file));
		}
		draw.writeGraphGson(sampleModels);	
	}
	public static  List<SampleModel> extractor ( File file) {
	
		Map<Integer, DataNode> treeMap = new HashMap<>();
		 GraphExtractorImp graphExtractor=new GraphExtractorImp();
		try {
			String absolutePath=file.getAbsolutePath();
			String fileName=absolutePath.substring(absolutePath.lastIndexOf(Config.SUBIDX));
			CompilationUnit unit=graphExtractor.processJavaFile(file);
			//root start from 5 number, as  reserved location for candidate node 
			graphExtractor.getBasedASTMap(unit, 1, treeMap);  //get based AST map					
			 //extractor sample
			List<SampleModel> sampleModels=graphExtractor.extractGraphSamples(treeMap,fileName);			
			return sampleModels;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
