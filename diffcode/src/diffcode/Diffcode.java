/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diffcode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import main.FileReaderUntil;
import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;

/**
 *
 * @author Administrator
 */
public class Diffcode {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
//        File file = new File("C:\\Users\\Administrator\\Documents\\NetBeansProjects\\diffcode\\src\\test\\Solution.java");
//        File file2 = new File("C:\\Users\\Administrator\\Documents\\NetBeansProjects\\diffcode\\src\\test\\Solution2.java");
////        String code1 = FileReaderUntil.getFileCode(file, 21, 29);
////        String code2 = FileReaderUntil.getFileCode(file2, 21, 29);
//        Diffcode diff = new Diffcode();
////        double b = diff.getSimilarity(code1, false, code2, false);
//        System.out.println(b);

    }

    public double getSimilarity(String code1, boolean code1Processed, String code2, boolean code2Processed) {
        int sameLines = 0;
        int diffLines = 0;
        HashMap map1 = new HashMap();
        HashMap map2 = new HashMap();
        List<Difference> result;
        List<Integer> sameList1 = new ArrayList<Integer>();
        List<Integer> sameList2 = new ArrayList<Integer>();
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        if (!code1Processed) {
            code1 = getPreProcessedCode(code1);
        }
        if (!code2Processed) {
            code2 = getPreProcessedCode(code2);
        }
        StringTokenizer analysis = new StringTokenizer(code1, ";\n");
        StringTokenizer analysis2 = new StringTokenizer(code2, ";\n");
        while (analysis.hasMoreTokens()) {
            String str = analysis.nextToken();
            if (str.trim().equals("")) {
                continue;
            }
            list1.add(str.replaceAll(" ", "") + "\n");
            // System.out.println(str.replaceAll(" ", ""));
        }
        while (analysis2.hasMoreTokens()) {
            String str = analysis2.nextToken();
            if (str.trim().equals("")) {
                continue;
            }
            list2.add(str.replaceAll(" ", "") + "\n");
        }
        Diff<String> d = new Diff<String>(list1, list2);
        result = d.diff();
        /* System.out.println(result.size()); */
        for (int i = 0; i < result.size(); i++) {
            /* System.out.println(result.get(i).toString()); */
            Difference diff = result.get(i);
            for (int j = diff.getDeletedStart(); j <= diff.getDeletedEnd(); j++) {
                map1.put(j + 1, j + 1);
            }// for
            for (int k = diff.getAddedStart(); k <= diff.getAddedEnd(); k++) {
                map2.put(k + 1, k + 1);
            }// for
        }// for
        for (int i = 1; i <= list1.size(); i++) {
            if (map1.containsKey(i) == false) {
                sameList1.add(i);
                /* System.out.print(i+","); */
            }
        }
        for (int i = 1; i <= list2.size(); i++) {
            if (map2.containsKey(i) == false) {
                sameList2.add(i);
                /* System.out.print(i+","); */
            }
        }
        diffLines = map1.size() + map2.size();
        sameLines = list1.size() + list2.size() - diffLines;
        double similarity = sameLines
                / ((double) diffLines + (double) sameLines);
        System.out.println(similarity);
        /* System.out.println(diffLines+"\n"+sameLines+"\n"+similarity); */
        return similarity;
    }

    private String getPreProcessedCode(String codeString) {
        String code = "";
//        try {
        StringBuffer buf = new StringBuffer(codeString);
        code = DelComments.delComments(buf.toString());
        code=code.replaceAll("\r\n|\r|\n|\\s*", "");
        System.out.println(code);
        //删除变量名
//            // System.out.println(code);
//            int pos1 = 0, pos2 = 0;
//            int len = code.length();
//            boolean isString = false;
//            StringBuffer ret = new StringBuffer();
//            while (pos1 < len) {
//                pos2++;
//                if (isString) {
//                    if (pos2 < len - 1) {
//                        if (code.substring(pos2, pos2 + 1).equals("\"")
//                                && !code.subSequence(pos2 - 1, pos2).equals(
//                                        "\\")) {
//                            isString = false;
//                            ret.append(delVariables(code.substring(pos1,
//                                    pos2 + 1)));
//                            pos1 = pos2 + 1;
//                        }
//                    } else {
//                        break;
//                    }
//                } else {
//                    if (pos2 < len - 1) {
//                        if (code.substring(pos2, pos2 + 1).equals("\"")) {
//                            isString = true;
//                            ret.append(delVariables(code.substring(pos1, pos2)));
//                            pos1 = pos2;
//                        }
//                    } else {
//                        ret.append(delVariables(code.substring(pos1,
//                                code.length())));
//                        break;
//                    }
//                }
//            }
//            code = ret.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return code;
    }

    private String delVariables(String code) {
        String keyWords = "and|asm|auto|bad_cast|bad_typeid|bool|break|case|catch|char|class|const|const_cast"
                + "|continue|default|delete|do|double|dynamic_cast|else|enum|except|explicit|extern|false|finally|float|for"
                + "|friend|goto|if|inline|int|long|mutable|namespace|new|operator|or|private|protected|public|register|reinterpret_cast"
                + "|return|short|signed|sizeof|static|static_cast|struct|switch|template|this|throw|true|try|type_info|typedef"
                + "|typeid|typename|union|unsigned|using|virtual|void|volatile|wchar_t|while";
        HashSet<String> keyWordSet = new HashSet<String>();
        String list[] = keyWords.split("\\|");
        for (String keyword : list) {
            keyWordSet.add(keyword);
        }
        code = "   " + code + "  ";
        int pos1 = 0, pos2 = 0;
        int len = code.length();
        boolean isVariables = false;
        StringBuffer ret = new StringBuffer();
        while (pos1 < len) {
            pos2++;
            if (isVariables) {
                if (code.substring(pos2, pos2 + 2)
                        .replaceAll("[0-9a-zA-Z_][^a-zA-Z_]", "").equals("")) {
                    isVariables = false;
                    String vv = code.substring(pos1, pos2 + 1);
                    if (keyWordSet.contains(vv)) {
                        ret.append(vv);
                    }
                    pos1 = pos2 + 1;
                }
            } else {
                if (code.substring(pos2, pos2 + 2)
                        .replaceAll("[^\\._a-zA-Z][_a-zA-Z]", "").equals("")) {
                    isVariables = true;
                    ret.append(code.substring(pos1, pos2 + 1));
                    pos1 = pos2 + 1;
                }
            }
            if (pos2 == len - 2) {
                break;
            }
        }

        return ret.toString().trim();
    }
}
