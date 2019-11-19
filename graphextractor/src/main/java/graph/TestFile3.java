package graph;

import java.util.List;
import java.util.ArrayList;


public class TestFile3 {

	static int classVar = 23;

    static void Main(String[] args) {
        String foo = args[0];
        int baz = - 42;
        int intVar = args.length;

        intVar = intVar + classVar;
        
        if (foo.startsWith("foobar")) {
            int l = foo.length() + 10;
        }
        else
        {
            int bar = foo.indexOf('-');
            bar = foo.indexOf('-', 2);
        }
    }

}
