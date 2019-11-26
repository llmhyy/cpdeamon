package graph;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class SimpleNameVisitor extends ASTVisitor {
    public String token;
	public boolean visit(SimpleName node) {
           token=node.getIdentifier();
           return true;
	}
}
