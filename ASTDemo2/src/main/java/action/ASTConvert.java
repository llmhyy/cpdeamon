package action;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;


/**
 * Eclipse AST demo 
 * @author SongXueZhi
 *
 */
public class ASTConvert {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ASTConvert test = new ASTConvert();
		try {
			test.processJavaFile(new File("./src/main/java/action/TestFile.java"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void processJavaFile(File file) throws Exception {
		//read Java File by relative path
	    String source = FileUtils.readFileToString(file);
	    Document document = new Document(source);
	    //parse file
	    ASTParser parser = ASTParser.newParser(AST.JLS10); //define java programming specification
	    parser.setKind(ASTParser.K_COMPILATION_UNIT); //type of parser : java file
	    parser.setSource(document.get().toCharArray());//param :char or java model
	    CompilationUnit unit = (CompilationUnit)parser.createAST(null);// a point for java file
	   
	    SimpleVisitor visitor=new SimpleVisitor();    
	    unit.accept(visitor);
	    
//	    Enables the recording of changes to this compilation unit and its descendants.	    	    
//	    unit.recordModifications();
//	    // to get the imports from the file
//	    List<ImportDeclaration> imports = unit.imports();
//	    for (ImportDeclaration i : imports) {
//	        System.out.println(i.getName().getFullyQualifiedName());
//	    }
//
//	    // to create a new import
//	    AST ast = unit.getAST();
//	    ImportDeclaration importELemment = ast.newImportDeclaration();
//	    String classToImport = "action.TestFile";
//	    
//	    importELemment.setName(ast.newName(classToImport));
//	    unit.imports().add(importELemment); // add import declaration at end
//
//	    // to save the changed 
//	    TextEdit edits = unit.rewrite(document, null);
//	    edits.apply(document);
//	    FileUtils.writeStringToFile(file, document.get());
//
//	    // to iterate through methods
//	    List<AbstractTypeDeclaration> types = unit.types();
//	    for (AbstractTypeDeclaration type : types) {
//	        if (type.getNodeType() == ASTNode.TYPE_DECLARATION) { //Node type constant indicating a node of type TypeDeclaration.
//
//	            List<BodyDeclaration> bodies = type.bodyDeclarations();
//	            for (BodyDeclaration body : bodies) {
//	                if (body.getNodeType() == ASTNode.METHOD_DECLARATION) {
//	                    MethodDeclaration method = (MethodDeclaration)body;
//	                    System.out.println("name: " + method.getName().getFullyQualifiedName());
//	                }
//	            }
//	        }
//	    }
//	    
	}
}
