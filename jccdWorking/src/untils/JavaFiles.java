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
        getJavaFilesPathArray("D:\\database\\maven-master\\maven-artifact\\src\\main\\java\\org\\apache\\maven");
    }
    public static String[] getJavaFilesPathArray(String path) {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        List<String> javaFilesPath=new ArrayList<>();
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
                        if (fileName.substring(fileName.lastIndexOf(".") + 1).equals("java")) {
                            javaFilesPath.add(fileName);
                            fileNum++;
                        }
                    }
                }
            }
        } else {
        }
        String [] javaFiles=javaFilesPath.toArray(new String[0]);
        return javaFiles;       
    }
    public static File[] getJavaFilesArray(String path) {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        List<String> javaFilesPath=new ArrayList<>();
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
                        if (fileName.substring(fileName.lastIndexOf(".") + 1).equals("java")) {
                            javaFilesPath.add(fileName);
                            fileNum++;
                        }
                    }
                }
            }
        } else {;
        }
       File[] javaFiles=new File[fileNum];
        for (int i = 0; i < javaFilesPath.size(); i++) {
            String filePath = javaFilesPath.get(i);
            javaFiles[i]=new File(filePath);           
        }
        return javaFiles;       
    }
}
