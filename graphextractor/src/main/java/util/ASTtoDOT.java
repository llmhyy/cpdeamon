package util;

import java.util.List;


public class ASTtoDOT {
	@SuppressWarnings("unchecked")
	public static String ASTtoDotParser(List<?> edges) {
		StringBuffer str = new StringBuffer("digraph \"DirectedGraph\" {\n");
		boolean spiltflag=false;
		// name
		str.append("graph [label = \"" +"ast grah" + "\", labelloc=t, concentrate = true];\n");
		for (Object[] k : (List<Object []>)edges) {
			Object pHashcode = k[0];
			Object hashcode = k[1];
		    String source=String.valueOf(pHashcode);
		    String target =String.valueOf(hashcode);
		    if (source.equals("1000maxline") && target.equals("1000maxline")) {
				spiltflag=true;
				continue;
			}
		    if (spiltflag) {
		    	str.append("\"" + source + "\" -> \"" +target + "\"\n");
			}else {
				str.append("\"" + source + "\" -> \"" +target + "\"[color=green]\n");
			}			
		}
		str.append("}\n");
		return str.toString();
	}
	
}
