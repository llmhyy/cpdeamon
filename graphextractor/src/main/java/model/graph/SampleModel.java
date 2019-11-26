package model.graph;

public class SampleModel {

	String filename;
	int slotTokenIdx;
	ContextGraphModel ContextGraph;
	final int SlotDummyNode = 0;
	SymbolCandidateModel[] SymbolCandidates;
    
	public SampleModel() {
		
	}
	public SampleModel(String filename,int slotTokenIdx,ContextGraphModel ContextGraph,SymbolCandidateModel[] SymbolCandidates) {
    this.filename=filename;
    this.slotTokenIdx=slotTokenIdx;
    this.ContextGraph=ContextGraph;
    this.SymbolCandidates=SymbolCandidates;
	}


	public SymbolCandidateModel[] getSymbolCandidates() {
		return SymbolCandidates;
	}
	public void setSymbolCandidates(SymbolCandidateModel[] symbolCandidates) {
		SymbolCandidates = symbolCandidates;
	}
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getSlotTokenIdx() {
		return slotTokenIdx;
	}

	public void setSlotTokenIdx(int slotTokenIdx) {
		this.slotTokenIdx = slotTokenIdx;
	}

	public ContextGraphModel getContextGraph() {
		return ContextGraph;
	}

	public void setContextGraph(ContextGraphModel contextGraph) {
		this.ContextGraph = contextGraph;
	}

	public int getSlotDummyNode() {
		return SlotDummyNode;
	}
}
