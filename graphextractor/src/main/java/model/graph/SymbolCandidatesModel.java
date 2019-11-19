package model.graph;

public class SymbolCandidatesModel {

	int SymbolDummyNode;
	String SymbolName;	
	boolean IsCorrect=false;
	
	public int getSymbolDummyNode() {
		return SymbolDummyNode;
	}
	public void setSymbolDummyNode(int symbolDummyNode) {
		SymbolDummyNode = symbolDummyNode;
	}
	public String getSymbolName() {
		return SymbolName;
	}
	public void setSymbolName(String symbolName) {
		SymbolName = symbolName;
	}
	public boolean isIsCorrect() {
		return IsCorrect;
	}
	public void setIsCorrect(boolean isCorrect) {
		IsCorrect = isCorrect;
	}
}
