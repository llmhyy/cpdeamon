package edu.dhu.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMProblemsStatus;
import edu.dhu.service.StudentexamdetailServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "problemStatusAction", results = { @Result(name = "addProblem", location = "/tch/problemAdd.jsp") })
public class ProblemStatusAction extends BaseAction implements
		ModelDriven<PMProblemsStatus> {

	private static final long serialVersionUID = 1278720662226674571L;

	private StudentexamdetailServiceI studentexamdetailService;

	PMProblemsStatus pMProblemsStatus = new PMProblemsStatus();

	@Override
	public PMProblemsStatus getModel() {
		return pMProblemsStatus;
	}

	public StudentexamdetailServiceI getStudentexamdetailService() {
		return studentexamdetailService;
	}

	@Autowired
	public void setStudentexamdetailService(
			StudentexamdetailServiceI studentexamdetailService) {
		this.studentexamdetailService = studentexamdetailService;
	}

	public void getProblemsStatusByIds() {
		// 返回前台的json数据
		Json j = new Json();
		PMProblemsStatus problemsStatus = studentexamdetailService
				.getProblemsStatusArrByIds(pMProblemsStatus);
		if (problemsStatus != null) {
			j.setSuccess(true);
			j.setMsg("根据ID数组查找状态成功。");
			j.setObj(problemsStatus);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("根据ID数组查找状态失败。");
			super.writeJson(j);
		}
	}

}
