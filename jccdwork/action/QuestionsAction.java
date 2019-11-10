package edu.dhu.action;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMQuestions;
import edu.dhu.service.QuestionsServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "questionsAction", results = { @Result(name = "view", location = "/user/signStudent.jsp") })
public class QuestionsAction extends BaseAction implements
		ModelDriven<PMQuestions> {

	private static final Logger logger = Logger.getLogger(ChaptersAction.class);
	private QuestionsServiceI questionsServiceI;
	PMQuestions pmQuestions = new PMQuestions();

	@Override
	public PMQuestions getModel() {
		// TODO Auto-generated method stub
		return pmQuestions;
	}

	public QuestionsServiceI getQuestionsServiceI() {
		return questionsServiceI;
	}

	@Autowired
	public void setQuestionsServiceI(QuestionsServiceI questionsServiceI) {
		this.questionsServiceI = questionsServiceI;
	}

	public void findAllQuestions() {
		// 返回前台的json数据
		Json j = new Json();
		List<PMQuestions> pmq = questionsServiceI.findAllQuestions();
		logger.info("查询所有问题成功");
		j.setSuccess(true);
		j.setObj(pmq);
		super.writeJson(j);
	}

}
