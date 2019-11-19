package graph;

public class TestFile2 {
	
	static int  thresholdIdx = 2;
    static void Main(String[] args) {
        String source = args[0];
        String subsource;
        if (source.length() > 2)
        {
             subsource = source.substring(thresholdIdx);
        }
        else {
            subsource = source;
        }           
    }
}
