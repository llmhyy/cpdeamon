package graph.visitor;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class TypeVisitor extends ASTVisitor {
    public List<ASTNode> items=new ArrayList<>();
    
	public boolean visit(FieldDeclaration node) {
		items.add(node);
	return true;
	}
	
    
	public boolean visit(MethodDeclaration node) {
		items.add(node);
	return true;
	}
	
}
