public class TestFileOne {
    public int factorial(int n) {
        if (n == 0) {
            return 1;
        } else {
            return n * factorial(n - 1);
        }
    }

    public int gcdOne(int a, int b) {
        while (b != 0) {
            if (a > b) {
                a = a - b;
            } else {
                b = b - a;
            }
        }
        return a;
    }
    public int gcdOne2(int a, int b) {
        while (b != 0) {
            if (a > b) {
                a = a - b;
            } else {
               b = b - a;
            }
        }   
        return a;
    }
    public int mul(int a, int b) {
        int n = 0;
        for (int i = 0; i < b; i++) {
            n += a;
        }
        float c= (float)b;
        return n;
    }
}
