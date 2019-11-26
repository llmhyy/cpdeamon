package model.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class DataNode {
	public ASTNode node; //所代表的的AST节点
	public int label; //编号
	public List<Integer> childrenLables = new ArrayList<>(); //直接的子节点的编号
	public List<ASTNode> childrenNodes = new ArrayList<>(); //直接的子节点
	public boolean isLeaf = false; //是否是叶子节点
	public String nodeType = "unknown";
}
