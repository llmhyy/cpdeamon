/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


/**
 *
 * @author Administrator
 */
public class FileReaderUntil {

    public   String  getFileCode(File file) throws IOException {
        try {
            FileInputStream in = new FileInputStream(file);
            Reader reader2 = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(reader2);
            String line;
            StringBuffer sb=new  StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line+"\n");
            }
            bufferedReader.close();
            return sb.toString();
        } catch (Exception e) {
             return null;
        }
        
    }
        public   String  getFileCode(File file,int startLine,int endLine) throws IOException {
        try {
            FileInputStream in = new FileInputStream(file);
            Reader reader2 = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(reader2);
            String line;
            StringBuffer sb=new  StringBuffer();
            int lineNo=0;
            while ((line = bufferedReader.readLine()) != null) {
                ++lineNo;
                if(startLine<=lineNo &&lineNo<=endLine){
                       sb.append(line+"\n");               
                }                
            }
            bufferedReader.close();
            return sb.toString();
        } catch (Exception e) {
             return null;
        }
        
    }

}
