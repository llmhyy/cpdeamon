package graph;
/*
 * author sxz 2019 11 25
 * the interface expect provide tools to operation  variables in ast
 * */

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

import graph.visitor.VarVisitor;
import model.ast.Variable;

public class VarManager {
	
  public List<Variable> getVariableList(CompilationUnit unit) {
	VarVisitor visitor =new VarVisitor();
	unit.accept(visitor);
	return visitor.variableList;
}
	
}
