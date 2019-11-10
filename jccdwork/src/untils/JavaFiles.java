/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package untils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class JavaFiles {

    public static void main(String[] args) {
       File[] a =getJavaFilesArray("/Users/knightsong/Documents/database/completed-compile/maven-master",false);
       File[] b=getJavaFilesArray("/Users/knightsong/Documents/database/completed-compile/maven-master",true);
       String [] c=getJavaFilesPathArray("/Users/knightsong/Documents/database/completed-compile/maven-master",false);
       File[] d=getTestJavaFilesArray("/Users/knightsong/Documents/database/completed-compile/maven-master");
        System.out.println(a.length+"_"+b.length+"_"+c.length+"_"+(b.length-a.length)+"_"+d.length);
    }

    public static String[] getJavaFilesPathArray(String path,boolean isIncludeTestFile) {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        List<String> javaFilesPath = new ArrayList<>();
        LinkedList<File> list = new LinkedList<>();
        if (file.exists()) {
            if (null == file.listFiles()) {
                return null;
            }
            list.addAll(Arrays.asList(file.listFiles()));
            while (!list.isEmpty()) {
                File[] files = list.removeFirst().listFiles();
                if (null == files) {
                    continue;
                }
                for (File f : files) {
                    if (f.isDirectory()) {
                        list.add(f);
                        folderNum++;
                    } else {
                        String fileName = f.getAbsolutePath();
                        if (isIncludeTestFile == false && containTestFile(fileName)) {
                            continue;
                        }
                        if (fileName.substring(fileName.lastIndexOf(".") + 1).equals("java")) {
                            javaFilesPath.add(fileName);
                            fileNum++;
                        }
                    }
                }
            }
        } else {
        }
        System.out.println(fileNum);
        String[] javaFiles = javaFilesPath.toArray(new String[0]);
        return javaFiles;
    }

    /**
     *
     * @param path directory
     * @param isIncludeTestFile Whether to cull the test folder in your project
     * @return
     */
    public static File[] getJavaFilesArray(String path, boolean isIncludeTestFile) {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        List<String> javaFilesPath = new ArrayList<>();
        LinkedList<File> list = new LinkedList<>();
        if (file.exists()) {
            if (null == file.listFiles()) {
                return null;
            }
            list.addAll(Arrays.asList(file.listFiles()));
            while (!list.isEmpty()) {
                File[] files = list.removeFirst().listFiles();
                if (null == files) {
                    continue;
                }
                for (File f : files) {
                    if (f.isDirectory()) {
                        list.add(f);
                        folderNum++;
                    } else {
                        String fileName = f.getAbsolutePath();
                        if (isIncludeTestFile == false && containTestFile(fileName)) {
                            continue;
                        }
                        if (fileName.substring(fileName.lastIndexOf(".") + 1).equals("java")) {
                            javaFilesPath.add(fileName);
                            fileNum++;
                        }

                    }
                }
            }
        } else {
        }
        File[] javaFiles = new File[fileNum];
        for (int i = 0; i < javaFilesPath.size(); i++) {
            String filePath = javaFilesPath.get(i);
            javaFiles[i] = new File(filePath);
        }
        return javaFiles;
    }

    private static boolean containTestFile(String fileName) {
        if (fileName.toLowerCase().contains("test")) {
            return true;
        } else {
            return false;
        }
    }
    public static File[] getTestJavaFilesArray(String path) {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        List<String> javaFilesPath = new ArrayList<>();
        LinkedList<File> list = new LinkedList<>();
        if (file.exists()) {
            if (null == file.listFiles()) {
                return null;
            }
            list.addAll(Arrays.asList(file.listFiles()));
            while (!list.isEmpty()) {
                File[] files = list.removeFirst().listFiles();
                if (null == files) {
                    continue;
                }
                for (File f : files) {
                    if (f.isDirectory()) {
                        list.add(f);
                        folderNum++;
                    } else {
                        String fileName = f.getAbsolutePath();
                        if (containTestFile(fileName)&&fileName.substring(fileName.lastIndexOf(".") + 1).equals("java")) {
                            javaFilesPath.add(fileName);
                            fileNum++;
                        }

                    }
                }
            }
        } else {
        }
        File[] javaFiles = new File[fileNum];
        for (int i = 0; i < javaFilesPath.size(); i++) {
            String filePath = javaFilesPath.get(i);
            javaFiles[i] = new File(filePath);
        }
        return javaFiles;
    }
}
