package extractor;

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
public class App {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		App test = new App();
		try {
			test.processJavaFile(new File("./src/main/java/extractor/TestFile.java"));
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
	}
}
