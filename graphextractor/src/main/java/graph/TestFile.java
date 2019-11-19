package graph;

import junit.framework.Assert;

public class TestFile {

    public void input(String value) {
        int a=2;
        int b=3;
        ++a;
        if(a>0) {
        	System.out.print(a);
        }else {
			 b=4;			
		}
        for(int i=0 ;i<b;i++) {
        	a=a-1;
        	String s=String.valueOf(a);
        	Assert.assertNotNull(s);
        }
    }
}
