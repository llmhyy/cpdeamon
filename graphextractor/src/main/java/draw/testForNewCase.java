package draw;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;


import config.Config;
import graph.FieldVisitor;
import graph.extractor.GraphExtractorImp;
import util.FileUtil;

public class testForNewCase {

	public static void main(String[] args) {
		GraphExtractorImp extractor=new GraphExtractorImp();
		File[] files=FileUtil.getTestJavaFilesArray(Config.SOURCEFILEPATH);
		for(int i=0;i<files.length;i++) {
			System.out.print(files[i].getAbsolutePath());
			CompilationUnit unit=extractor.processJavaFile(files[i]);
			FieldVisitor fieldVisitor=new FieldVisitor();
			unit.accept(fieldVisitor);
		}							
	}
}
