package graph.visitor;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import model.ast.Variable;


public class VarVisitor extends ASTVisitor {
	public List<Variable>  variableList =new ArrayList<>();
	
	@Override
	public boolean visit(FieldDeclaration node) {
		for (Object obj : node.fragments()) {
			  	Variable variable=new Variable(true); //setIsFiled
				VariableDeclarationFragment v = (VariableDeclarationFragment) obj;
				variable.setToken(v.getName().toString());
				variable.setType(node.getType().toString());
				variableList.add(variable);
		}
		return true;
	}
	public boolean visit(VariableDeclarationStatement node) {
		for (Object obj : node.fragments()) {
		  	Variable variable=new Variable(false); //setIsFiled
			VariableDeclarationFragment v = (VariableDeclarationFragment) obj;
			variable.setToken(v.getName().toString());
			variable.setType(node.getType().toString());
			variableList.add(variable);
		}
		return true;
	}
	
	public boolean visit(SingleVariableDeclaration node) {
		Variable variable=new Variable(false); //setIsFiled
		variable.setToken(node.getName().toString());
		variable.setType(node.getType().toString());
		variableList.add(variable);
		return true;
	}
 }

