package edu.dhu.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.ModelDriven;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMExamCheck;
import edu.dhu.service.ExamServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "examCheckAction", results = { @Result(name = "examCheck", location = "/tch/problemAdd.jsp") })
public class ExamCheckAction extends BaseAction implements
		ModelDriven<PMExamCheck> {

	private static final long serialVersionUID = 5433881125564853181L;

	private PMExamCheck pMExamCheck = new PMExamCheck();

	@Override
	public PMExamCheck getModel() {
		return pMExamCheck;
	}

	private ExamServiceI examService;

	public ExamServiceI getExamService() {
		return examService;
	}

	@Autowired
	public void setExamService(ExamServiceI examService) {
		this.examService = examService;
	}

	public void examCheck() {
		// 返回前台的json数据
		Json j = new Json();
		if (examService.checkExamByUserIdAndExamId(pMExamCheck.getExamId(),
				pMExamCheck.getUserId())) {
			j.setSuccess(true);
			j.setMsg("您可以参加该场考试");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("您无法参加该场考试");
			super.writeJson(j);
		}
	}

}
