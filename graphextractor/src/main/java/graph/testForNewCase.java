package graph;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.ValidationEvent;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import config.Config;
import graph.extractor.Prune;
import graph.extractor.imp.GraphExtractorImp;
import graph.visitor.SimpleVisitor;
import model.ast.DataNode;
import model.ast.Variable;
import util.FileUtil;

public class testForNewCase {

	public static void main(String[] args) {
		GraphExtractorImp extractor=new GraphExtractorImp();
		File[] files=FileUtil.getTestJavaFilesArray(Config.SOURCEFILEPATH);
		for(int i=0;i<files.length;i++) {
			CompilationUnit unit=extractor.processJavaFile(files[i]);
//			VarManager varManager=new VarManager();
//			List<Variable> list=varManager.getVariableList(unit);
//			for (Variable variable : list) {
//				System.out.println(variable.getType()+"_"+variable.getToken()+"_"+variable.getIsFiled());
//			}
		testExtractorVariableTypes(unit);
//			SimpleVisitor bVisitor=new SimpleVisitor();
//			unit.accept(bVisitor);
		}							
	}
    public static  void  testExtractorVariableTypes(CompilationUnit unit) {
    	Map<Integer,Variable> varMap =new HashMap<>();
    	
    	Prune prune =new Prune();
    	Map<Integer, DataNode> treeMap=prune.getPrunedAST(unit);    	
    	for(Map.Entry<Integer, DataNode> entry : treeMap.entrySet()) {
    		 int key =entry.getKey();
    		 DataNode astNode=entry.getValue();   		 
    		 ASTNode node =astNode.node;    
//    		 if (node instanceof FieldDeclaration ) {
//    			 for (Object obj : ((FieldDeclaration) node).fragments()) {
//    				  	Variable variable=new Variable(true); //setIsFiled
//    					VariableDeclarationFragment v = (VariableDeclarationFragment) obj;
//    					variable.setToken(v.getName().toString());
//    					variable.setType(((FieldDeclaration) node).getType().toString()); 					
//    					varMap.put(key, variable);
//    				}
//			}
//    		 if (node instanceof VariableDeclarationStatement ) {
//    			 for (Object obj : ((VariableDeclarationStatement) node).fragments()) {
//    				  	Variable variable=new Variable(false);
//    					VariableDeclarationFragment v = (VariableDeclarationFragment) obj;
//    					variable.setToken(v.getName().toString());
//    					variable.setType(((VariableDeclarationStatement) node).getType().toString()); 					
//    					varMap.put(key, variable);
//    				}
//			}
//    		 if (node instanceof SingleVariableDeclaration ) {
//    			Variable variable=new Variable(false);
//				variable.setToken(((SingleVariableDeclaration) node).getName().toString());
//				variable.setType(((SingleVariableDeclaration) node).getType().toString()); 					
//				varMap.put(key, variable);
//			}
//			 2.collect NodeTypes
			String type ="UNK";
			String var="";
			if (node instanceof VariableDeclarationFragment) {
			  ASTNode parent=node.getParent();
			  if (parent instanceof FieldDeclaration) {
				 type =((FieldDeclaration)parent).getType().toString();
				 
			}else {
				 type =((VariableDeclarationStatement)parent).getType().toString();
			}
			   var=((SimpleName)treeMap.get(++key).node).getFullyQualifiedName();
			   System.out.println(type+"_"+var); 
			}
			
			if (node instanceof SingleVariableDeclaration) {
				type=((SingleVariableDeclaration)node).getType().toString();
				var=((SimpleName)treeMap.get(key+2).node).getFullyQualifiedName();
				System.out.println(type+"_"+var); 
			}
    	}
	}
}
