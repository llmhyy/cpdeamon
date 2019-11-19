package model.graph;

import java.util.ArrayList;
import java.util.List;

public class EdgesModel {
List<Integer []> Child;
List<Integer []> NextToken;
List<Integer []> LastUse;
List<Integer []> LastWrite;
List<Integer []> LastLexicalUse;
List<Integer []> ComputedFrom;
List<Integer []> GuardedByNegation;
List<Integer []> GuardedBy;
List<Integer []> FormalArgName;
List<Integer []> ReturnsTo;



public List<Integer[]> getChild() {
	if (Child==null) {
		this.Child=new ArrayList<>();
	}
	return Child;
}

public void setChild(List<Integer[]> child) {
	Child = child;
}

public List<Integer[]> getNextToken() {
	if(NextToken==null) {
		this.NextToken=new ArrayList<>();
	}
	return NextToken;
}

public void setNextToken(List<Integer[]> nextToken) {
	NextToken = nextToken;
}

public List<Integer[]> getLastUse() {
	if(LastUse==null) {
	this.LastUse=new ArrayList<>();
	}
	return LastUse;
}

public void setLastUse(List<Integer[]> lastUse) {
	LastUse = lastUse;
}

public List<Integer[]> getLastWrite() {
	if (LastWrite==null) {
		LastWrite=new ArrayList<>();
	}
	return LastWrite;
}

public void setLastWrite(List<Integer[]> lastWrite) {
	LastWrite = lastWrite;
}

public List<Integer[]> getLastLexicalUse() {
	if(LastLexicalUse==null) {
		LastLexicalUse=new ArrayList<>();
	}
	return LastLexicalUse;
}

public void setLastLexicalUse(List<Integer[]> lastLexicalUse) {
	LastLexicalUse = lastLexicalUse;
}

public List<Integer[]> getComputedFrom() {
	if(ComputedFrom==null) {
	 this.ComputedFrom=new ArrayList<>();
	}
	return ComputedFrom;
}

public void setComputedFrom(List<Integer[]> computedFrom) {
	ComputedFrom = computedFrom;
}

public List<Integer[]> getGuardedByNegation() {
	if (GuardedByNegation==null) {
		this.GuardedByNegation=new ArrayList<>();
	}
	return GuardedByNegation;
}

public void setGuardedByNegation(List<Integer[]> guardedByNegation) {
	GuardedByNegation = guardedByNegation;
}

public List<Integer[]> getGuardedBy() {
	if (GuardedBy==null) {
		this.GuardedBy=new ArrayList<>();
	}
	return GuardedBy;
}

public void setGuardedBy(List<Integer[]> guardedBy) {
	GuardedBy = guardedBy;
}

public List<Integer[]> getFormalArgName() {
	if(FormalArgName==null) {
		this.FormalArgName=new ArrayList<>();
	}
	return FormalArgName;
}

public void setFormalArgName(List<Integer[]> formalArgName) {
	FormalArgName = formalArgName;
}

public List<Integer[]> getReturnsTo() {
	if(ReturnsTo==null) {
		this.ReturnsTo=new ArrayList<>();
	}
	return ReturnsTo;
}

public void setReturnsTo(List<Integer[]> returnsTo) {
	ReturnsTo = returnsTo;
}

}
