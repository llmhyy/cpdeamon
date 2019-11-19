package util;

import java.util.List;


public class ASTtoDOT {
	@SuppressWarnings("unchecked")
	public static String ASTtoDotParser(List<?> edges) {
		StringBuffer str = new StringBuffer("digraph \"DirectedGraph\" {\n");
		// name
		str.append("graph [label = \"" +"ast grah" + "\", labelloc=t, concentrate = true];\n");
		for (Object[] k : (List<Object []>)edges) {
			Object pHashcode = k[0];
			Object hashcode = k[1];
			str.append("\"" + String.valueOf(pHashcode) + "\" -> \"" + String.valueOf(hashcode) + "\"\n");
		}
		str.append("}\n");
		return str.toString();
	}
}
