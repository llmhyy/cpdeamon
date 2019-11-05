package edu.dhu.action;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.ModelDriven;
import edu.dhu.model.Studentexamdetail;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMGradeProblem;
import edu.dhu.service.StudentexamdetailServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "gradeProblemAction", results = { @Result(name = "gradeProblemAction", location = "/user/takeAnExam.jsp") })
public class GradeProblemAction extends BaseAction implements
		ModelDriven<PMGradeProblem> {

	private static final long serialVersionUID = 8868861839193799029L;

	// 记录日志
	private static final Logger logger = Logger
			.getLogger(GradeProblemAction.class);

	private StudentexamdetailServiceI studentexamdetailService;
	PMGradeProblem pMGradeProblem = new PMGradeProblem();

	public StudentexamdetailServiceI getStudentexamdetailService() {
		return studentexamdetailService;
	}

	@Autowired
	public void setStudentexamdetailService(
			StudentexamdetailServiceI studentexamdetailService) {
		this.studentexamdetailService = studentexamdetailService;
	}

	@Override
	public PMGradeProblem getModel() {
		return pMGradeProblem;
	}

	// 接受exam id,user id problem id
	public void gradeProblemAction() {
		// 根据exam ID,user ID,problem ID获取该用户提交该题的详细信息
		Studentexamdetail studentexamdetail = studentexamdetailService
				.getStatusByUserIDexamIDproblemId(pMGradeProblem.getUserId(),
						pMGradeProblem.getExamId(),
						pMGradeProblem.getProblemId());
		// 返回前台的json数据
		Json j = new Json();
		j.setSuccess(true);
		logger.info("exam ID为：" + studentexamdetail.getExamId() + " 学生ID为： "
				+ studentexamdetail.getUserId() + "problem ID为 "
				+ studentexamdetail.getProblemId() + " 的代码的评判状态为： "
				+ studentexamdetail.getStatus());

		/*
		 * switch (studentexamdetail.getStatus()) { // 用户提交代码之后，代码评判状态为已提交 case
		 * Constant.CODE_WAIT : j.setMsg(Constant.CODE_WAIT);
		 * j.setObj(studentexamdetail);
		 * 
		 * super.writeJson(j);
		 * 
		 * // 用户提交代码之后，代码评判状态为编译错误 case Constant.CODE_PE :
		 * j.setMsg(Constant.CODE_PE); j.setObj(studentexamdetail);
		 * 
		 * super.writeJson(j);
		 * 
		 * // 用户提交代码之后，代码评判状态为超时 case Constant.CODE_TO :
		 * j.setMsg(Constant.CODE_TO); j.setObj(studentexamdetail);
		 * 
		 * super.writeJson(j);
		 * 
		 * // 用户提交代码之后，代码评判状态为超出内存 case Constant.CODE_OM :
		 * j.setMsg(Constant.CODE_OM); j.setObj(studentexamdetail);
		 * 
		 * super.writeJson(j);
		 * 
		 * // 用户提交代码之后，代码评判状态为错误答案 case Constant.CODE_WA :
		 * j.setMsg(Constant.CODE_WA); j.setObj(studentexamdetail);
		 * 
		 * super.writeJson(j);
		 * 
		 * // 用户提交代码之后，代码评判状态为正确答案 case Constant.CODE_AC :
		 * j.setMsg(Constant.CODE_AC); j.setObj(studentexamdetail);
		 * super.writeJson(j);
		 * 
		 * // 默认用户提交代码之后，代码评判状态为已提交 default: j.setMsg(Constant.CODE_WAIT);
		 * j.setObj(studentexamdetail);
		 * 
		 * super.writeJson(j); }
		 */
	}

}
