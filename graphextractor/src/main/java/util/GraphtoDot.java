package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.soap.Node;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import model.varmisuse.GraphTMPModel;


public class GraphtoDot {
	public static void main(String[] args) throws IOException {
		getASTDotByGraph("/Users/knightsong/Desktop/benchmarkdotnet.2.json");
		getNextTokenDot("/Users/knightsong/Desktop/benchmarkdotnet.2.json");
		getNodeTypes("/Users/knightsong/Desktop/benchmarkdotnet.2.json");
		
	}
	public static void getASTDotByGraph(String pathName) throws IOException {
		String jsonString=FileUtils.readFileToString(new File(pathName));
		Gson gson = new Gson();
		GraphTMPModel Json = gson.fromJson(jsonString,
				GraphTMPModel.class);
		List<Integer []> childList =Json.Child;
		Map<Integer, String> nodeLables=Json.NodeLabels;
		List<String []> edges=new ArrayList<String[]>();
		for(Integer [] childEdge:childList) {
			int source=childEdge[0];
			int target=childEdge[1];
			edges.add(new String[] {source+"_"+nodeLables.get(source),target+"_"+nodeLables.get(target)});
		}
		String dot=ASTtoDOT.ASTtoDotParser(edges);
		FileUtil.writeFile("/Users/knightsong/Desktop/graph_AST.dot", dot);
	}
	
	public static  void getNextTokenDot(String pathName) throws IOException {
		String jsonString=FileUtils.readFileToString(new File(pathName));
		Gson gson = new Gson();
		GraphTMPModel Json = gson.fromJson(jsonString,
				GraphTMPModel.class);
		List<Integer []> nextTokenList=Json.NextToken;
		Map<Integer, String> nodeLables=Json.NodeLabels;
		List<String []> edges=new ArrayList<String[]>();
		for(Integer[] nextChild:nextTokenList) {
			int source=nextChild[0];
			int target=nextChild[1];
			edges.add(new String[] {source+"_"+nodeLables.get(source),target+"_"+nodeLables.get(target)});
		}
		String dot=ASTtoDOT.ASTtoDotParser(edges);
		FileUtil.writeFile("/Users/knightsong/Desktop/graph_nexttoken.dot", dot);
	}
	//read a gson to look up node type
	public static void getNodeTypes(String pathName) throws IOException {
		String jsonString=FileUtils.readFileToString(new File(pathName));
		Gson gson = new Gson();
		GraphTMPModel Json = gson.fromJson(jsonString,
				GraphTMPModel.class);
		Map<Integer, String> NodeType=Json.NodeTypes;
		for(Map.Entry<Integer, String> entry:NodeType.entrySet()) {
			System.out.print(entry.getValue()+"\n");
		}
	}
	
	// write a dot form existing edge
	public static void getNextTokenDot2(List<String []> edges) {
		String dot=ASTtoDOT.ASTtoDotParser(edges);
		FileUtil.writeFile("/Users/knightsong/Desktop/graph_nexttoken.dot", dot);
	}
	
}
