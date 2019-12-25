package model.ast;

public class Variable {
String token ;
String Type;
String methodSignature;
Boolean isFiled;
public Variable(boolean isFiled) {
	this.isFiled=isFiled;
}
public String getToken() {
	return token;
}
public void setToken(String token) {
	this.token = token;
}
public String getType() {
	return Type;
}
public void setType(String type) {
	Type = type;
}
public String getMethodSignature() {
	return methodSignature;
}
public void setMethodSignature(String methodSignature) {
	this.methodSignature = methodSignature;
}
public Boolean getIsFiled() {
	return isFiled;
}
public void setIsFiled(Boolean isFiled) {
	this.isFiled = isFiled;
}
}
