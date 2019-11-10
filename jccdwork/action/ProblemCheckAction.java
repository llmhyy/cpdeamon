package edu.dhu.action;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.ModelDriven;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMProblemCheck;
import edu.dhu.service.ProblemCheckServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "problemCheckAction", results = { @Result(name = "problemCheck", location = "/tch/problemAdd.jsp") })
public class ProblemCheckAction extends BaseAction implements
		ModelDriven<PMProblemCheck> {

	private static final long serialVersionUID = 9176130051244463838L;

	// 记录日志
	private static final Logger logger = Logger
			.getLogger(ProblemCheckAction.class);

	private PMProblemCheck pMProblemCheck = new PMProblemCheck();

	@Override
	public PMProblemCheck getModel() {
		return pMProblemCheck;
	}

	private ProblemCheckServiceI problemCheckService;

	public ProblemCheckServiceI getProblemCheckService() {
		return problemCheckService;
	}

	@Autowired
	public void setProblemCheckService(ProblemCheckServiceI problemCheckService) {
		this.problemCheckService = problemCheckService;
	}

	public void getAnswer() {
		String result[] = problemCheckService.getAnswer(pMProblemCheck);
		// System.out.println(pMProblemCheck.getLanguage());
		// System.out.println(pMProblemCheck.getSourceCode());
		// System.out.println(pMProblemCheck.getTestIn()[0]);
		// 返回前台的json数据
		Json j = new Json();
		logger.info("程序执行完毕");
		j.setSuccess(true);
		j.setObj(result);
		super.writeJson(j);
	}

}
