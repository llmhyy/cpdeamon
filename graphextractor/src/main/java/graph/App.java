package graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cfg.bytecode.BlockGraph;
import cfg.bytecode.BlockNode;
import cfg.bytecode.CFG;
import cfg.bytecode.CFGNode;
import cfg.cfgcoverage.CFGInstance;
import cfg.model.ClassLocation;
import cfg.utils.InstrumentationUtils;
import util.ASTtoDOT;

public class App {
	public static void main(String[] args) {
		Extractor extractor = new Extractor();
		String classPath = "bceltest.WS.Main(String[] args)";
		String file_name = "/Users/knightsong/Desktop/WS.class";
		ClassLocation classLocation = InstrumentationUtils.getClassLocation(classPath);
		CFGInstance cfgInstance = extractor.findCfg(classLocation, file_name);
		CFG cfg = cfgInstance.getCfg();
		drawCFDGraph(cfg);
		BlockGraph blockGraph=BlockGraph.createBlockGraph(cfg);
		drawBlockGraph(blockGraph);
	}
	
	public static void  drawCFDGraph(CFG cfg) {
		List<CFGNode> cfgNodes = cfg.getNodeList();
		List<String[]> cfgGraph = new ArrayList<String[]>();
		for (CFGNode cfgNode : cfgNodes) {
			List<CFGNode> childNodes = cfgNode.getChildren();
			String source = cfgNode.getIdx() + "_" + cfgNode.getDisplayString().split(", b")[0];
			for (int i = 0; i < childNodes.size(); i++) {
				String target = childNodes.get(i).getIdx() + "_" + childNodes.get(i).getDisplayString().split(", b")[0];
				cfgGraph.add(new String[] { source, target });
			}
		}
		String txt = ASTtoDOT.ASTtoDotParser(cfgGraph);
		try {
			FileUtils.write(new File("/Users/knightsong/Desktop/cfgDot.dot"), txt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void  drawBlockGraph(BlockGraph blockGrap) {
		List<BlockNode> blockNodes = blockGrap.getList();
		List<String[]> cfgGraph = new ArrayList<String[]>();
		int j=0;
		for (BlockNode cfgNode : blockNodes) {
			j++;
			List<BlockNode> childNodes = cfgNode.getChildren();
			String source =j+"_"+cfgNode.getContents().toString();
			for (int i = 0; i < childNodes.size(); i++) {
				j++;
				String target = j+"_"+cfgNode.getContents().toString();
				cfgGraph.add(new String[] { source, target });
			}
		}
		String txt = ASTtoDOT.ASTtoDotParser(cfgGraph);
		try {
			FileUtils.write(new File("/Users/knightsong/Desktop/cfgblockDot.dot"), txt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
