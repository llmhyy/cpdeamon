/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jccdworking;

import org.eposoft.jccd.preprocessors.java.GeneralizeVariableNames;
import org.eposoft.jccd.preprocessors.java.GeneralizeVariableDeclarationTypes;
import org.eposoft.jccd.preprocessors.java.GeneralizeArrayInitializers;
import org.eposoft.jccd.preprocessors.java.CompleteToBlock;
import org.eposoft.jccd.preprocessors.java.GeneralizeMethodCallNames;
import org.eposoft.jccd.preprocessors.java.GeneralizeMethodArgumentTypes;
import org.eposoft.jccd.preprocessors.java.GeneralizeMethodReturnTypes;
import diffcode.Diffcode;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import main.FileReaderUntil;
import model.SimilarityInstance;
import org.eposoft.jccd.comparators.ast.AcceptFileNames;
import org.eposoft.jccd.comparators.ast.java.AcceptNumberTypeNames;
import org.eposoft.jccd.comparators.ast.java.NumberLiteralToDouble;
import org.eposoft.jccd.data.ASourceUnit;
import org.eposoft.jccd.data.JCCDFile;
import org.eposoft.jccd.data.SimilarityGroup;
import org.eposoft.jccd.data.SimilarityGroupManager;
import org.eposoft.jccd.data.SourceUnitPosition;
import org.eposoft.jccd.data.ast.ANode;
import org.eposoft.jccd.data.ast.NodeTypes;
import org.eposoft.jccd.detectors.APipeline;
import static org.eposoft.jccd.detectors.APipeline.getFirstNodePosition;
import static org.eposoft.jccd.detectors.APipeline.getLastNodePosition;
import org.eposoft.jccd.detectors.ASTDetector;
import org.eposoft.jccd.preprocessors.java.CollapseCastExpressions;
import org.eposoft.jccd.preprocessors.java.GeneralizeMethodDeclarationNames;
import untils.JavaFiles;

/**
 *
 * @author Administrator
 */
public class JccdWorking {

    final static double SAMESCORE = 1.0d;
    static String JAVAFILE_DIRECTORY_STRING = "D:\\BaiduNetdiskDownload\\camel-master\\";
    static String OUTPUTFILE_STRING = "D:\\clonepair\\camel.txt";
    static int LINEFILTERNUM = 3;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
//        test code
//        args=new String[4];
//        args[0]="jccd";
//        args[1]="5";
//        args[2]="/Users/knightsong/Documents/database/completed-compile/maven-master";
//        args[3]="/Users/knightsong/Documents/clonepair/out";
        //Reasonable judgment of parameters
        if (args.length < 2) {
            System.out.println("At least two parameters are required,\nsuch as [TaskName](javafilecount or jccd) [LineFilterNumber] [InputDirectory] [OutFile],\n[LineFilterNumber] [OutFile] only used in jccd task,all task are not included test file by default");
            return;
        }
        //javafilecount
        if (args[0].equalsIgnoreCase(Tasks.JAVAFILECOUNTTASK.getTaskName())) {
            JavaFiles.getJavaFilesPathArray(new File(args[1]).toString(), false);
            return;
        }
        if (args[0].equalsIgnoreCase(Tasks.JCCDTASK.getTaskName())) {
            if (args.length < 4) {
                System.out.println("jccd task need four pamares");
                return;
            }
            try {
                LINEFILTERNUM = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("jccd task second pamare must a number");
                e.printStackTrace();
                return;
            }

            JAVAFILE_DIRECTORY_STRING = args[2];
            OUTPUTFILE_STRING = args[3];
            File[] copyFiles = JavaFiles.getJavaFilesArray(JAVAFILE_DIRECTORY_STRING, false);
            JCCDFile[] jccdFiles = new JCCDFile[copyFiles.length];
            System.out.println("当前需要处理的任务" + "_" + copyFiles.length);
            for (int i = 0; i < copyFiles.length; i++) {
                File file1 = copyFiles[i];
                if (file1.isFile()) {
                    jccdFiles[i] = new JCCDFile(file1);
                }
            }
            APipeline detector = new ASTDetector();
            detector.setSourceFiles(jccdFiles);
            detector.addOperator(new GeneralizeArrayInitializers());
            detector.addOperator(new GeneralizeMethodCallNames());
            detector.addOperator(new GeneralizeMethodDeclarationNames());//消除方法名称
            detector.addOperator(new GeneralizeVariableNames());//移除变量名
            detector.addOperator(new CompleteToBlock());//补全if else
            detector.addOperator(new GeneralizeMethodArgumentTypes());//删除方方法参数类型
            detector.addOperator(new GeneralizeMethodReturnTypes()); //删除方法的返回类型
            detector.addOperator(new GeneralizeVariableDeclarationTypes());//移除变量声明类型
//        detector.addOperator(new GeneralizeClassDeclarationNames());//移除类名
            detector.addOperator(new NumberLiteralToDouble());//忽略数字的表示97.0=97f=a
            detector.addOperator(new AcceptFileNames());//忽略文件名
            detector.addOperator(new CollapseCastExpressions());//移除强类型转换
            detector.addOperator(new AcceptNumberTypeNames());//移除数字类型
//        detector.addOperator(new AcceptStringLiterals());//移除魔法文本
//        detector.addOperator(new RemoveEmptyMethods());//移除接口声明，空方法
            jCCDWork(detector.process());
        } else {
            System.out.println("unsupport task");
        }
    }

    @SuppressWarnings("empty-statement")
    public static void jCCDWork(
            final SimilarityGroupManager groupContainer) {
        diffcode.Diffcode diff = new Diffcode();
        // output similarity groups
        SimilarityGroup[] simGroups = groupContainer.getSimilarityGroups();

        if (null == simGroups) {
            simGroups = new SimilarityGroup[0];
        }
        FileReaderUntil fileReader = new FileReaderUntil();
        try {
            File file = new File(OUTPUTFILE_STRING);
            if (!file.exists()) {
                file.createNewFile();
            }
            int num = 0;
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file), "gbk");
            BufferedWriter out = new BufferedWriter(write);
            if ((null != simGroups) && (0 < simGroups.length)) {
                for (int i = 0; i < simGroups.length; i++) {
                    List<SimilarityInstance> groupList = new ArrayList<>();
                    final ASourceUnit[] nodes = simGroups[i].getNodes(); //获取group节点
                    for (int j = 0; j < nodes.length; j++) {
                        SimilarityInstance instance = new SimilarityInstance();
                        final SourceUnitPosition minPos = getFirstNodePosition((ANode) nodes[j]);
                        final SourceUnitPosition maxPos = getLastNodePosition((ANode) nodes[j]);

                        ANode fileNode = (ANode) nodes[j];
                        while (fileNode.getType() != NodeTypes.FILE.getType()) {   //获取文件名
                            fileNode = fileNode.getParent();
                        }
                        String fileName = fileNode.getText();
                        // System.out.print(nodes[j].getRange());
                        instance.setFileName(fileName);
                        int startLine = minPos.getLine();
                        instance.setStartPos(startLine);
                        int stopLine = maxPos.getLine();
                        instance.setStopPos(stopLine);
                        groupList.add(instance);
                    }
                    for (int j = 0; j < groupList.size(); j++) {
                        for (int k = j + 1; k < groupList.size(); k++) {
                            SimilarityInstance jInstance = groupList.get(j);
                            SimilarityInstance kInstance = groupList.get(k);
                            String jCode = fileReader.getFileCode(new File(jInstance.getFileName()), jInstance.getStartPos(), jInstance.getStopPos());
                            String kCode = fileReader.getFileCode(new File(kInstance.getFileName()), kInstance.getStartPos(), kInstance.getStopPos());
                            if (jInstance.getStopPos() - jInstance.getStartPos() > LINEFILTERNUM && kInstance.getStopPos() - kInstance.getStartPos() > LINEFILTERNUM) {

                                if (SAMESCORE > diff.getSimilarity(jCode, false, kCode, false)) {
                                    //过滤行数小的
                                    out.write(jInstance.getFileName());
                                    out.write("\t");
                                    out.write(String.valueOf(jInstance.getStartPos()));
                                    out.write("\t");
                                    out.write(String.valueOf(jInstance.getStopPos()));
                                    out.write("\n");
                                    out.write(kInstance.getFileName());
                                    out.write("\t");
                                    out.write(String.valueOf((int) kInstance.getStartPos()));
                                    out.write("\t");
                                    out.write(String.valueOf((int) kInstance.getStopPos()));
                                    out.write("\n");
                                    out.write("========================");
                                    out.write("\n");
                                    ++num;
                                }
                            }
                            // System.out.println(++num);
                        }
                    }
                }
                System.out.println("total clone pairs:" + num);
            } else {
                System.out.println("No similar nodes found.");
            }
            out.close();
        } catch (Exception e) {
        }

    }

    //求阶乘法
    public static int doFactorial(int n) {
        if (n < 0) {
            return -1;//传入的数据不合法
        }
        if (n == 0) {
            return 1;
        } else if (n == 1) {//递归结束的条件
            return 1;
        } else {
            return n * doFactorial(n - 1);
        }
    }

    public enum Tasks {
        JAVAFILECOUNTTASK("javafilecount", 1), JCCDTASK("jccd", 0);
        private String taskName;
        private int taskNo;

        private Tasks(String taskName, int taskNo) {
            this.taskName = taskName;
            this.taskNo = taskNo;
        }

        /**
         * @return the taskName
         */
        public String getTaskName() {
            return taskName;
        }

        /**
         * @param taskName the taskName to set
         */
        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        /**
         * @return the taskNo
         */
        public int getTaskNo() {
            return taskNo;
        }

        /**
         * @param taskNo the taskNo to set
         */
        public void setTaskNo(int taskNo) {
            this.taskNo = taskNo;
        }

    }
}
