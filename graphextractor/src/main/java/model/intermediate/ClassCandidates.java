package model.intermediate;

import model.graph.SymbolCandidateModel;

public class ClassCandidates {
int SymbolNode=0;//location that needs instead of <Slot>
SymbolCandidateModel[] symbolCandidateModels;
public int getSymbolNode() {
	return SymbolNode;
}
public void setSymbolNode(int symbolNode) {
	SymbolNode = symbolNode;
}
public SymbolCandidateModel[] getSymbolCandidateModels() {
	return symbolCandidateModels;
}
public void setSymbolCandidateModels(SymbolCandidateModel[] symbolCandidateModels) {
	this.symbolCandidateModels = symbolCandidateModels;
}
}
