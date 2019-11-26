package model.varmisuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphTMPModel {
public List<Integer []> Child =new ArrayList<Integer[]>();
public List<Integer []> NextToken =new ArrayList<>();
public Map<Integer, String> NodeLabels =new HashMap<Integer, String>();
public Map<Integer, String>NodeTypes=new HashMap<>();
public List<Integer[]> getChild() {
	return Child;
}
public void setChild(List<Integer[]> child) {
	Child = child;
}
public List<Integer[]> getNextToken() {
	return NextToken;
}
public void setNextToken(List<Integer[]> nextToken) {
	NextToken = nextToken;
}
public Map<Integer, String> getNodeTypes() {
	return NodeTypes;
}
public void setNodeTypes(Map<Integer, String> nodeTypes) {
	NodeTypes = nodeTypes;
}
public Map<Integer, String> getNodeLabels() {
	return NodeLabels;
}
public void setNodeLabels(Map<Integer, String> nodeLabels) {
	NodeLabels = nodeLabels;
}
}
