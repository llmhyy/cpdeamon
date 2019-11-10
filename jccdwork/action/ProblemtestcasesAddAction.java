package edu.dhu.action;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.ModelDriven;
import edu.dhu.model.Problemtestcases;
import edu.dhu.pageModel.Json;
import edu.dhu.service.ProblemtestcasesAddServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "problemtestcasesAddAction", results = { @Result(name = "problemtestcasesAdd", location = "/tch/problemAdd.jsp") })
public class ProblemtestcasesAddAction extends BaseAction implements
		ModelDriven<Problemtestcases> {

	private static final long serialVersionUID = 5202997524486802343L;

	// 记录日志
	private static final Logger logger = Logger
			.getLogger(ProblemtestcasesAddAction.class);

	private ProblemtestcasesAddServiceI ProblemtestcasesAddService;

	public ProblemtestcasesAddServiceI getProblemtestcasesAddService() {
		return ProblemtestcasesAddService;
	}

	@Autowired
	public void setProblemtestcasesAddService(
			ProblemtestcasesAddServiceI problemtestcasesAddService) {
		ProblemtestcasesAddService = problemtestcasesAddService;
	}

	Problemtestcases problemtestcases = new Problemtestcases();

	@Override
	public Problemtestcases getModel() {
		return problemtestcases;
	}

	public void problemtestcasesAdd() {
		// 返回前台的json数据
		Json j = new Json();
		String res = ProblemtestcasesAddService
				.problemtestcasesAdd(problemtestcases);
		if (res == null) {
			logger.info("题目ID为:" + problemtestcases.getProblemId()
					+ " 测试用例ID为: " + problemtestcases.getId() + "的测试用例增加成功。");
			j.setSuccess(true);
			j.setMsg("测试用例增加成功");
			super.writeJson(j);
		} else {
			logger.info("题目ID为:" + problemtestcases.getProblemId()
					+ " 测试用例ID为: " + problemtestcases.getId() + "的测试用例增加失败。");
			j.setSuccess(false);
			j.setMsg("测试用例增加失败<br>" + res);
			super.writeJson(j);
		}

	}

}
