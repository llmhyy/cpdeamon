package edu.dhu.action;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.ModelDriven;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMProblemTestCaseAdd;
import edu.dhu.service.ProblemAddServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "problemAddAction", results = { @Result(name = "addProblem", location = "/tch/problemAdd.jsp") })
public class ProblemAddAction extends BaseAction implements
		ModelDriven<PMProblemTestCaseAdd> {

	private static final long serialVersionUID = 2409782743160267729L;

	// 记录日志
	private static final Logger logger = Logger
			.getLogger(ProblemAddAction.class);

	PMProblemTestCaseAdd pMProblemTestCaseAdd = new PMProblemTestCaseAdd();

	@Override
	public PMProblemTestCaseAdd getModel() {
		return pMProblemTestCaseAdd;
	}

	private ProblemAddServiceI problemAddService;

	public ProblemAddServiceI getProblemAddService() {
		return problemAddService;
	}

	@Autowired
	public void setProblemAddService(ProblemAddServiceI problemAddService) {
		this.problemAddService = problemAddService;
	}

	// 向题库中添加题目
	public void addProblem() {
		// 默认设置题目的提交次数为0
		pMProblemTestCaseAdd.setSubmit(0);
		// 默认设置题目的解决次数为0
		pMProblemTestCaseAdd.setSolved(0);

		// 返回前台的json数据
		Json j = new Json();
		// 模拟设置章节ID
		String res = problemAddService.addProblem(pMProblemTestCaseAdd);
		if (res == null) // 没有错误信息
		{
			logger.info("题目名为: " + pMProblemTestCaseAdd.getTitle() + "的题目增加成功。");
			j.setSuccess(true);
			j.setMsg("题目增加成功");
			super.writeJson(j);
		} else {
			logger.info("题目名为: " + pMProblemTestCaseAdd.getTitle() + "的题目添加失败。");
			j.setSuccess(false);
			j.setMsg("题目添加失败!<br>" + res);
			super.writeJson(j);
		}
	}

}
