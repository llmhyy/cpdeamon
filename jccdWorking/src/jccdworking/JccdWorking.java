/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jccdworking;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import diffcode.Diffcode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import main.FileReaderUntil;
import model.SimilarityInstance;
import org.eposoft.jccd.comparators.ast.AcceptFileNames;
import org.eposoft.jccd.comparators.ast.java.AcceptNumberTypeNames;
import org.eposoft.jccd.comparators.ast.java.AcceptStringLiterals;
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
import org.eposoft.jccd.preprocessors.java.*;
import untils.JavaFiles;

/**
 *
 * @author Administrator
 */
public class JccdWorking {

    final static float sameScore = 1.0f;
    final static String JAVAFILE_DIRECTORY_STRING = "C:\\Users\\Administrator\\Documents\\NetBeansProjects\\diffcode\\src\\test";
    final static String OUTPUTFILE_STRING = "D:\\clonepair\\incubator.txt";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        File[] copyFiles = JavaFiles.getJavaFilesArray(JAVAFILE_DIRECTORY_STRING);
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
//        detector.addOperator(new GeneralizeMethodDeclarationNames());//消除方法名称
        detector.addOperator(new GeneralizeVariableNames());//移除变量名
        detector.addOperator(new CompleteToBlock());//补全if else
        detector.addOperator(new GeneralizeMethodArgumentTypes());//删除方方法参数类型
        detector.addOperator(new GeneralizeMethodReturnTypes()); //删除方法的返回类型
        detector.addOperator(new GeneralizeVariableDeclarationTypes());//移除变量声明类型
//        detector.addOperator(new GeneralizeClassDeclarationNames());//移除类名
//        detector.addOperator(new NumberLiteralToDouble());//忽略数字的表示97.0=97f=a
//        detector.addOperator(new AcceptFileNames());//忽略文件名
//       detector.addOperator(new CollapseCastExpressions());//移除强类型转换
//        detector.addOperator(new AcceptNumberTypeNames());//移除数字类型
//        detector.addOperator(new AcceptStringLiterals());//移除魔法文本
//        detector.addOperator(new RemoveEmptyMethods());//移除接口声明，空方法

        jCCDWork(detector.process());

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
            int num=0;
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
                            if (jInstance.getStopPos() - jInstance.getStartPos() >= 4 && kInstance.getStopPos()-kInstance.getStartPos()>=4) {
                                if (sameScore != diff.getSimilarity(jCode, false, kCode, false)) {
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
                                    
                                }
                                // System.out.println(++num);
                            }
                           
                        }
                    }
                }
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
}
