package edu.dhu.action;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.common.Constant;
import edu.dhu.model.Exam;
import edu.dhu.model.Examproblems;
import edu.dhu.model.Problemtestcases;
import edu.dhu.model.Solution;
import edu.dhu.model.Studentexamdetail;
import edu.dhu.model.Wrongcases;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMWrongAndCorrect;
import edu.dhu.pageModel.PMWrongAndCorrectIds;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.ExamServiceI;
import edu.dhu.service.ExamproblemServiceI;
import edu.dhu.service.GradeProblemServiceI;
import edu.dhu.service.ProblemtestcasesServiceI;
import edu.dhu.service.SolutionServiceI;
import edu.dhu.service.StudentexamdetailServiceI;
import edu.dhu.service.WrongcasesServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "wrongAndRightCasesAction", results = { @Result(name = "getAllWrongAndRightCases", location = "/user/takeAnExam.jsp") })
public class WrongAndCorrectCasesAction extends BaseAction implements
		ModelDriven<PMWrongAndCorrectIds> {

	private static final long serialVersionUID = 1157593811335042059L;
	// 记录日志
	private static final Logger logger = Logger
			.getLogger(WrongAndCorrectCasesAction.class);

	private SolutionServiceI solutionService;
	private WrongcasesServiceI wrongcasesService;
	private ProblemtestcasesServiceI problemtestcasesService;
	private GradeProblemServiceI gradeProblemService;
	private StudentexamdetailServiceI studentexamdetailService;
	private ExamproblemServiceI examproblemService;
	private ExamServiceI examService;

	PMWrongAndCorrectIds pMWrongAndCorrectIds = new PMWrongAndCorrectIds();

	@Override
	public PMWrongAndCorrectIds getModel() {
		return pMWrongAndCorrectIds;
	}

	public SolutionServiceI getSolutionService() {
		return solutionService;
	}

	@Autowired
	public void setSolutionService(SolutionServiceI solutionService) {
		this.solutionService = solutionService;
	}

	public WrongcasesServiceI getWrongcasesService() {
		return wrongcasesService;
	}

	@Autowired
	public void setWrongcasesService(WrongcasesServiceI wrongcasesService) {
		this.wrongcasesService = wrongcasesService;
	}

	public ProblemtestcasesServiceI getProblemtestcasesService() {
		return problemtestcasesService;
	}

	@Autowired
	public void setProblemtestcasesService(
			ProblemtestcasesServiceI problemtestcasesService) {
		this.problemtestcasesService = problemtestcasesService;
	}

	public GradeProblemServiceI getGradeProblemService() {
		return gradeProblemService;
	}

	@Autowired
	public void setGradeProblemService(GradeProblemServiceI gradeProblemService) {
		this.gradeProblemService = gradeProblemService;
	}

	public StudentexamdetailServiceI getStudentexamdetailService() {
		return studentexamdetailService;
	}

	@Autowired
	public void setStudentexamdetailService(
			StudentexamdetailServiceI studentexamdetailService) {
		this.studentexamdetailService = studentexamdetailService;
	}

	public ExamproblemServiceI getExamproblemService() {
		return examproblemService;
	}

	@Autowired
	public void setExamproblemService(ExamproblemServiceI examproblemService) {
		this.examproblemService = examproblemService;
	}

	public ExamServiceI getExamService() {
		return examService;
	}

	@Autowired
	public void setExamService(ExamServiceI examService) {
		this.examService = examService;
	}

	// 根据examID查看是否允许获取提示
	public void canGetHint() {
		// 返回前台的json数据
		Json j = new Json();
		// 根据examId查找Exam
		Exam exam = examService.getExamById(pMWrongAndCorrectIds.getExamId());
		if (exam.getCanGetHint()) {
			j.setSuccess(true);
			j.setMsg("该场考试允许获取提示");
		} else {
			j.setSuccess(false);
			j.setMsg("该场考试不允许获取提示");
		}
		super.writeJson(j);
	}

	// 根据wrongcasesId 查找错误输出和对应的Wrongcases
	// 根据problemtestcasesId查找对应的Problemtestcases

	public void getHint() {
		// 返回前台的json数据
		Json j = new Json();
		// 根据userID，examID，problemID在Studentexamdetail中查找获取studentexamdetail记录
		Studentexamdetail studentexamdetail = studentexamdetailService
				.getStatusByUserIDexamIDproblemId(
						pMWrongAndCorrectIds.getUserId(),
						pMWrongAndCorrectIds.getExamId(),
						pMWrongAndCorrectIds.getProblemId());

		Solution s = null;
		if (studentexamdetail.getSolutionId() != null) {
			s = solutionService.getSolutionById(studentexamdetail
					.getSolutionId());
		} else {
			// 根据userId,examId,problemId以及Studentexamdetail的status在solution中查找ID值最大的solution
			s = solutionService
					.getLastSolutionByUserIdExamIdProblemIdAndStatus(
							pMWrongAndCorrectIds.getUserId(),
							pMWrongAndCorrectIds.getExamId(),
							pMWrongAndCorrectIds.getProblemId(),
							studentexamdetail.getStatus());
		}

		// 根据examID，problemID在examproblems中获取该题的总分数
		Examproblems examproblems = examproblemService
				.getExamproblemsByExamIdAndProblemId(
						pMWrongAndCorrectIds.getExamId(),
						pMWrongAndCorrectIds.getProblemId());
		// 获取该题的总分数
		float problemScore = examproblems.getScore();
		// 获取hintCases
		String hintCases = studentexamdetail.getHintCases();
		// 获取提示的测试用例ID
		int theTestCaseId = pMWrongAndCorrectIds.getProblemtestcasesId();
		// 获取分数
		float score = s.getScore();
		// 该学生没有请求过测试用例
		if (hintCases.equals(new String(Constant.DEFAULT_HINTCASE))) {
			studentexamdetail.setHintCases("" + theTestCaseId);
			// 重新计算得分
			score = (float) (score - problemScore * 0.02);
			if(score<0)
				score=0;
			studentexamdetail.setScore(score);
			studentexamdetailService.updateStudentexamdetail(studentexamdetail);
		} else {
			// 如果请求的测试用例ID在studentExamDetail.hintCases里不存在
			String[] hintCasesArr = hintCases.split(",");
			boolean flag = false;
			for (int i = 0; i < hintCasesArr.length; i++) {
				if (hintCasesArr[i].equals(new String("" + theTestCaseId))) {
					flag = true;
				}
			}
			if (flag == false) {
				// 如果不存在，则将该测试用例ID加入到studentExamDetail.hintCases字段中，并更新hintCases字段
				studentexamdetail.setHintCases(hintCases + "," + theTestCaseId);
				// 重新计算得分
				score = (float) (score - problemScore * 0.02);
				if(score<0)
					score=0;
				studentexamdetail.setScore(score);
				studentexamdetailService
						.updateStudentexamdetail(studentexamdetail);
			}
		}
		// 保存分数
		if(score<0)
			score=0;
		s.setScore(score);
		solutionService.updateSolution(s);
		// 获取正确和错误的输出
		Problemtestcases problemtestcases = problemtestcasesService
				.getProblemtestcasesById(pMWrongAndCorrectIds
						.getProblemtestcasesId());
		Wrongcases wrongcases = wrongcasesService
				.getWrongcasesById(pMWrongAndCorrectIds.getWrongcasesId());

		PMWrongAndCorrect pMWrongAndCorrect = new PMWrongAndCorrect();
		if (problemtestcases != null && wrongcases != null) {
			pMWrongAndCorrect.setProblemtestcases(problemtestcases);
			pMWrongAndCorrect.setWrongcases(wrongcases);
			pMWrongAndCorrect.setScore(score);
			logger.info("获取wrongcasesId" + wrongcases.getId()
					+ ";problemtestcasesId为" + problemtestcases.getId()
					+ "获取正确的测试用成功");
			j.setSuccess(true);
			j.setObj(pMWrongAndCorrect);
			j.setMsg("获取正确的测试用成功");
			super.writeJson(j);
		} else {
			logger.info("获取wrongcasesId" + wrongcases.getId()
					+ ";problemtestcasesId为" + problemtestcases.getId()
					+ "获取正确的测试用成功");
			j.setSuccess(true);
			j.setObj(null);
			j.setMsg("获取正确的测试用失败");
			super.writeJson(j);
		}
	}

	// 获取学生提交的该题的所有错误的测试用例ID和正确的测试用例ID
	public void getAllWrongAndRightCases() {
		// 根据userID，examID，problemID在studentexamdetail表中查找该条记录
		Studentexamdetail studentexamdetail = studentexamdetailService
				.getStatusByUserIDexamIDproblemId(
						pMWrongAndCorrectIds.getUserId(),
						pMWrongAndCorrectIds.getExamId(),
						pMWrongAndCorrectIds.getProblemId());
		// 返回前台的json数据
		Json j = new Json();
		// 如果没有不存在记录
		if (studentexamdetail != null) {
			Solution s = null;
			if (studentexamdetail.getSolutionId() != null) {
				s = solutionService.getSolutionById(studentexamdetail
						.getSolutionId());
			} else {
				// 根据userId,examId,problemId以及Studentexamdetail的status在solution中查找ID值最大的solution
				s = solutionService
						.getLastSolutionByUserIdExamIdProblemIdAndStatus(
								pMWrongAndCorrectIds.getUserId(),
								pMWrongAndCorrectIds.getExamId(),
								pMWrongAndCorrectIds.getProblemId(),
								studentexamdetail.getStatus());
			}
			float score;
			// 如果solution不为空,则代表用户提交了该题
			if (s != null) {
				if (s.getScore() > 0) {
					score = s.getScore();
				} else {
					// 获取该题的所得的分数情况
					score = gradeProblemService.gradeProblemBySolution(s);
				}
				// 根据solutionID查询该题所有的正确的测试用例
				String[] correctCaseIds = solutionService.getCorrectCaseIds(s
						.getId());
				// 根据solutionID查询该题所有的错误的测试用例
				List<Wrongcases> wrongcases = wrongcasesService
						.getWrongcasesBySolutionID(s.getId());

				PMWrongAndCorrectIds wrongAndCorrectIds = new PMWrongAndCorrectIds();
				wrongAndCorrectIds.setExamId(pMWrongAndCorrectIds.getExamId());
				wrongAndCorrectIds.setUserId(pMWrongAndCorrectIds.getUserId());
				wrongAndCorrectIds.setProblemId(pMWrongAndCorrectIds
						.getProblemId());
				wrongAndCorrectIds.setSolutionId(s.getId());
				wrongAndCorrectIds.setCorrectCaseIds(correctCaseIds);
				wrongAndCorrectIds.setWrongcases(wrongcases);
				wrongAndCorrectIds.setStatus(studentexamdetail.getStatus());
				// 如果分数出来了，则设置分数，返回页面
				if (score >= 0) {
					wrongAndCorrectIds.setScore(score);
				}
				// 设置提交次数
				wrongAndCorrectIds.setSubmit(studentexamdetail.getSubmit());
				// 设置remark
				if (s.getRemark() != null
						&& !s.getRemark().equals(new String(""))) {
					wrongAndCorrectIds
							.setRemark(switchStatusToRemark(studentexamdetail
									.getStatus())
									+ "\n具体信息如下:\n"
									+ s.getRemark());
				} else {
					wrongAndCorrectIds
							.setRemark(switchStatusToRemark(studentexamdetail
									.getStatus()));
				}

//				logger.info("查询solution ID为:" + s.getId()
//						+ "的提交的题目的正确和错误测试用例成功");
				j.setSuccess(true);
				j.setObj(wrongAndCorrectIds);
				j.setMsg("查询所有的正确和错误的测试用例成功");
				super.writeJson(j);
			} else {
				PMWrongAndCorrectIds wrongAndCorrectIds = new PMWrongAndCorrectIds();
				wrongAndCorrectIds.setStatus("");
				wrongAndCorrectIds.setSubmit(0);
				wrongAndCorrectIds.setScore(-1);
				wrongAndCorrectIds.setCorrectCaseIds(null);
				wrongAndCorrectIds.setWrongcases(null);
				wrongAndCorrectIds.setRemark("");
				j.setSuccess(true);
				j.setMsg("正确和错误的测试用例为空");
				super.writeJson(j);
			}
		} else {
			PMWrongAndCorrectIds wrongAndCorrectIds = new PMWrongAndCorrectIds();
			wrongAndCorrectIds.setStatus("");
			wrongAndCorrectIds.setSubmit(0);
			wrongAndCorrectIds.setScore(-1);
			wrongAndCorrectIds.setCorrectCaseIds(null);
			wrongAndCorrectIds.setWrongcases(null);
			wrongAndCorrectIds.setRemark("");
			j.setSuccess(true);
			j.setMsg("正确和错误的测试用例为空");
			super.writeJson(j);
		}
	}

	public void getWrongTestCaseBySolutionId() // 通过solutionId获取错误测试用例
	{
		int solutionId = pMWrongAndCorrectIds.getSolutionId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			j.setSuccess(true);
			List<Wrongcases> caseList = wrongcasesService
					.getWrongcasesBySolutionID(solutionId);
			j.setObj(caseList);
			j.setMsg("获取错误测试用例成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getProblemTestCasesById() // 根据id获取测试用例
	{
		int problemId = pMWrongAndCorrectIds.getProblemId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<Problemtestcases> testCases = problemtestcasesService
					.getProblemtestcasesByProblemId(problemId);
			j.setSuccess(true);
			j.setObj(testCases);
			j.setMsg("获取测试用例成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public String switchStatusToRemark(String status) {
		switch (status) {
		case Constant.CODE_WAIT:
			return Constant.REMARK_WAIT;
		case Constant.CODE_QUEUE:
			return Constant.REMARK_QUEUE;
		case Constant.CODE_CE:
			return Constant.REMARK_CE;
		case Constant.CODE_TLE:
			return Constant.REMARK_TLE;
		case Constant.CODE_RE:
			return Constant.REMARK_RE;
		case Constant.CODE_WA:
			return Constant.REMARK_WA;
		case Constant.CODE_PE:
			return Constant.REMARK_PE;
		case Constant.CODE_OLE:
			return Constant.REMARK_OLE;
		case Constant.CODE_AC:
			return Constant.REMARK_AC;
		default:
			return "";
		}
	}
}
