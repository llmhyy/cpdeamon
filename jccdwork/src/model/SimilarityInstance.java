/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

/**
 *
 * @author Administrator
 */
public class SimilarityInstance {
    private  String fileName;
    private   int startPos;
    private  int  stopPos;

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the startPos
     */
    public int getStartPos() {
        return startPos;
    }

    /**
     * @param startPos the startPos to set
     */
    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    /**
     * @return the stopPos
     */
    public int getStopPos() {
        return stopPos;
    }

    /**
     * @param stopPos the stopPos to set
     */
    public void setStopPos(int stopPos) {
        this.stopPos = stopPos;
    }
    
}
