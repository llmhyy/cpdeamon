package graph.extractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import graph.visitor.TypeVisitor;
import model.ast.DataNode;

public class Prune {
	
	 public static int ID =0;
	 
	 public  Map<Integer, DataNode> getPrunedAST(CompilationUnit unit) {
		 Prune.ID=0;      //new tree reset node number
		 Map<Integer, DataNode> treeMap = new HashMap<>();
		 TypeVisitor visitor =new TypeVisitor();
			unit.accept(visitor);
			List<ASTNode> items=visitor.items;
			for (ASTNode astNode : items) {
				++Prune.ID;
				getBasedASTMap(astNode,Prune.ID, treeMap);
			}
			return treeMap;
	}
    @SuppressWarnings("unchecked")
	public void getBasedASTMap(ASTNode node, int label, Map<Integer, DataNode> Nodes) {
		// 先创建一个节点数据结构
		DataNode myNode = new DataNode();
		Nodes.put(label, myNode);
		myNode.label = label;
		myNode.node = node;
		myNode.nodeType = node.getClass().toString();
		List<?> listProperty = node.structuralPropertiesForType();
		boolean hasChildren = false;
		for (int i = 0; i < listProperty.size(); i++) {
			StructuralPropertyDescriptor propertyDescriptor = (StructuralPropertyDescriptor) listProperty.get(i);
			if (propertyDescriptor instanceof ChildListPropertyDescriptor) {// ASTNode列表
				ChildListPropertyDescriptor childListPropertyDescriptor = (ChildListPropertyDescriptor) propertyDescriptor;
				Object children = node.getStructuralProperty(childListPropertyDescriptor);

				List<ASTNode> childrenNodes = (List<ASTNode>) children;
				for (ASTNode childNode : childrenNodes) {
					// 获取所有节点
					if (childNode == null)
						continue;
					hasChildren = true;
					myNode.childrenNodes.add(childNode);
					myNode.childrenLables.add((++ID));
					getBasedASTMap(childNode, ID, Nodes);// 继续递归
					// System.out.println("childrenList: "+childNode+" "+childNode.getClass());
				}
			} else if (propertyDescriptor instanceof ChildPropertyDescriptor) {// 一个ASTNode
				ChildPropertyDescriptor childPropertyDescriptor = (ChildPropertyDescriptor) propertyDescriptor;
				Object child = node.getStructuralProperty(childPropertyDescriptor);
				ASTNode childNode = (ASTNode) child;
				if (childNode == null)
					continue;
				hasChildren = true;
				// 获取了这个节点
				myNode.childrenNodes.add(childNode);
				myNode.childrenLables.add((++ID));
				getBasedASTMap(childNode, ID, Nodes);// 继续递归
				// System.out.println("child: "+childNode +" "+childNode.getClass());
			}
		}
		if (hasChildren) {
			// 进行递归子节点
			myNode.isLeaf = false;
		} else {
			// 结束，是叶子结点
			myNode.isLeaf = true;
		}
	}

}
