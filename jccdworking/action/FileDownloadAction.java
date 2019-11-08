package edu.dhu.action;

import java.io.InputStream;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.opensymphony.xwork2.ActionSupport;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "fileDownloadAction", results = {
		@Result(name = "importClassStudentModel.csv", type = "stream", params = {
				"inputName", "fileInput", "contentDisposition",
				"attachment;filename=importClassStudentModel.csv",
				"contentType", "text/plain" }),
		@Result(name = "importClassStudentsInstruction.docx", type = "stream", params = {
				"inputName", "fileInput", "contentDisposition",
				"attachment;filename=importClassStudentsInstruction.docx",
				"contentType", "text/plain" }) })
public class FileDownloadAction extends ActionSupport {
	private InputStream fileInput;
	private String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getFileInput() {
		return ServletActionContext.getServletContext().getResourceAsStream(
				"file\\" + fileName);
	}

	public void setFileInput(InputStream fileInput) {
		this.fileInput = fileInput;
	}

	@Override
	public String execute() throws Exception {
		fileInput = ServletActionContext.getServletContext()
				.getResourceAsStream("file\\" + fileName);
		return fileName;
	}

}
