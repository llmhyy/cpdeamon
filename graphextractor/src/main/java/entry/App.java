package entry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import config.Config;
import draw.DrawGraph;
import graph.extractor.Prune;
import graph.extractor.imp.GraphExtractorImp;
import graph.visitor.TypeVisitor;
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
			List<SampleModel> subSampleList=extractor2(file);
			if (subSampleList != null) {
				sampleModels.addAll(subSampleList);
			}	
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
			graphExtractor.getBasedASTMap(unit, 1, treeMap);  //get based AST map					
			 //extractor sample
			List<SampleModel> sampleModels=graphExtractor.extractGraphSamples(treeMap,fileName);			
			return sampleModels;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static  List<SampleModel> extractor2 ( File file) {
		
		
		 GraphExtractorImp graphExtractor=new GraphExtractorImp();
		 Prune prune =new Prune();
		try {
			String absolutePath=file.getAbsolutePath();
			String fileName=absolutePath.substring(absolutePath.lastIndexOf(Config.SUBIDX));
			CompilationUnit unit=graphExtractor.processJavaFile(file);
			Map<Integer, DataNode> treeMap = prune.getPrunedAST(unit);
			  //get based AST map					
			 //extractor sample
			if (treeMap.size()==0) {
				return null;
			}
			List<SampleModel> sampleModels=graphExtractor.extractGraphSamples(treeMap,fileName);			
			return sampleModels;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
}
