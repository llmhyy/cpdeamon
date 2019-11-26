package graph;





import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;


public class FieldVisitor extends ASTVisitor {
	@Override
	public boolean visit(FieldDeclaration  node) {
		// TODO Auto-generated method stub
		String fieldDeclaration=node.toString();
		String[] stringArray = node.toString().split(" ");
		String type = stringArray[stringArray.length - 2];
		String token;
		String endPos = stringArray[stringArray.length - 1];
		if (endPos.contains("=")) {
			token = endPos.substring(0, endPos.indexOf("="));
		} else if (endPos.contains(";")) {
			token = endPos.substring(0, endPos.indexOf(";"));
		} else {
			token = endPos;
		}
		System.out.print(token+"_"+type);
		return true;
	}
   
	 
 }

