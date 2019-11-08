package edu.dhu.action;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;
import edu.dhu.model.Log;
import edu.dhu.model.Solution;
import edu.dhu.model.Studentexamdetail;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMWrongAndCorrectIds;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.CheckSimilarityServiceI;
import edu.dhu.service.ExamServiceI;
import edu.dhu.service.ExamproblemServiceI;
import edu.dhu.service.LogServiceI;
import edu.dhu.service.ProblemsServiceI;
import edu.dhu.service.RedisServiceI;
import edu.dhu.service.SimilaritywarningServiceI;
import edu.dhu.service.SolutionServiceI;
import edu.dhu.service.StudentexamdetailServiceI;
import edu.dhu.service.SubmittedcodeServiceI;
import edu.dhu.service.UserServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "submitThisProblemAction", results = { @Result(name = "getAllWrongAndRightCases", location = "/user/takeAnExam.jsp") })
public class SubmitThisProblemAction extends BaseAction implements
		ModelDriven<PMWrongAndCorrectIds> {

	private static final long serialVersionUID = -2185834336935466790L;

	// 记录日志
	private static final Logger logger = Logger
			.getLogger(SubmitThisProblemAction.class);
    private UserServiceI userService;
	private ProblemsServiceI problemsService;
	private SubmittedcodeServiceI submittedcodeService;
	private CheckSimilarityServiceI checkSimilarityService;
	private ExamServiceI examService;
	private SolutionServiceI solutionService;
	private SimilaritywarningServiceI similaritywarningService;
	private StudentexamdetailServiceI studentexamdetailService;
	private ExamproblemServiceI examproblemService;
	private LogServiceI logService;
	private RedisServiceI redisService;

	private static Lock lock = new ReentrantLock();// 锁对象

	PMWrongAndCorrectIds pMWrongAndCorrectIds = new PMWrongAndCorrectIds();

	public ExamproblemServiceI getExamproblemService() {
		return examproblemService;
	}
	@Autowired
	public UserServiceI getUserService() {
		return userService;
	}

	public void setUserService(UserServiceI userService) {
		this.userService = userService;
	}
	@Autowired
	public void setExamproblemService(ExamproblemServiceI examproblemService) {
		this.examproblemService = examproblemService;
	}

	@Autowired
	public void setLogService(LogServiceI logService) {
		this.logService = logService;
	}

	public LogServiceI getLogService() {
		return logService;
	}

	@Override
	public PMWrongAndCorrectIds getModel() {
		return pMWrongAndCorrectIds;
	}

	public CheckSimilarityServiceI getCheckSimilarityService() {
		return checkSimilarityService;
	}

	@Autowired
	public void setCheckSimilarityService(
			CheckSimilarityServiceI checkSimilarityService) {
		this.checkSimilarityService = checkSimilarityService;
	}

	public ExamServiceI getExamService() {
		return examService;
	}

	@Autowired
	public void setExamService(ExamServiceI examService) {
		this.examService = examService;
	}

	public SolutionServiceI getSolutionService() {
		return solutionService;
	}

	@Autowired
	public void setSolutionService(SolutionServiceI solutionService) {
		this.solutionService = solutionService;
	}

	public SubmittedcodeServiceI getSubmittedcodeService() {
		return submittedcodeService;
	}

	@Autowired
	public void setSubmittedcodeService(
			SubmittedcodeServiceI submittedcodeService) {
		this.submittedcodeService = submittedcodeService;
	}
	public RedisServiceI getRedisService() {
		return redisService;
	}
	@Autowired
	public void setRedisService(RedisServiceI redisService) {
		this.redisService = redisService;
	}
	public ProblemsServiceI getProblemsService() {
		return problemsService;
	}

	@Autowired
	public void setProblemsService(ProblemsServiceI problemsService) {
		this.problemsService = problemsService;
	}

	public SimilaritywarningServiceI getSimilaritywarningService() {
		return similaritywarningService;
	}

	@Autowired
	public void setSimilaritywarningService(
			SimilaritywarningServiceI similaritywarningService) {
		this.similaritywarningService = similaritywarningService;
	}

	public StudentexamdetailServiceI getStudentexamdetailService() {
		return studentexamdetailService;
	}

	@Autowired
	public void setStudentexamdetailService(
			StudentexamdetailServiceI studentexamdetailService) {
		this.studentexamdetailService = studentexamdetailService;
	}

	// 根据examID，solutionID，problemID获取用户最新的solution
	public void getLastSolution() {
		Solution solution = solutionService
				.getLastSolutionByUserIdExamIdProblemId(
						pMWrongAndCorrectIds.getUserId(),
						pMWrongAndCorrectIds.getExamId(),
						pMWrongAndCorrectIds.getProblemId());
		// 返回前台的json数据
		Json j = new Json();
		if (solution != null) {
			j.setSuccess(true);
			j.setObj(solution);
			j.setMsg("获取之前的代码成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("本题之前没有提交过");
			super.writeJson(j);
		}
	}

	// 判断是否能够点击提交本题按钮
	public void isSubmitThisProblem() {
		// 返回前台的json数据
		Json j = new Json();
		// 根据userId,examId,problemId在solution中查找最新的solution
		Solution solution = solutionService
				.getLastSolutionByUserIdExamIdProblemId(
						pMWrongAndCorrectIds.getUserId(),
						pMWrongAndCorrectIds.getExamId(),
						pMWrongAndCorrectIds.getProblemId());
		if (solution == null) {
			j.setSuccess(false);
			j.setMsg("本题没有提交过代码，不可以提交本题。");
			super.writeJson(j);
		} else {
			// 根据userid，examID，problemID在studentexamtail表中查找
			Studentexamdetail studentexamdetail = studentexamdetailService
					.getStatusByUserIDexamIDproblemId(
							pMWrongAndCorrectIds.getUserId(),
							pMWrongAndCorrectIds.getExamId(),
							pMWrongAndCorrectIds.getProblemId());
			if (studentexamdetail.isFinished()) {
				j.setSuccess(false);
				j.setMsg("不能重复提交");
				super.writeJson(j);
			} else {
				j.setSuccess(true);
				j.setMsg("没有提交过本题，可以提交");
				super.writeJson(j);
			}
		}
	}
	 //用户提交本题
	@Transactional(isolation=Isolation.SERIALIZABLE)
	public void submitThisProblem() {
		String key=null;
			// 返回前台的json数据
			Json j = new Json();
			// 如果session断掉了
			Map<String, Object> session = ActionContext.getContext()
					.getSession();
			SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
			if (sessionInfo == null) {
				j.setSuccess(false);
				j.setMsg("必须先登录才能提交本题。");
				super.writeJson(j);
				return;
			}	
			String userName=sessionInfo.getLoginName();
			 key = "submitThisProblem_" + userName;
			 
			lock.lock();// 获得锁	
			// redis分布式锁
			int expire = 30; // 超时时间
//			try {
//				// 获取锁失败就结束
//				if (!redisService.lock(key, expire)) {
//					j.setSuccess(false);
//					j.setMsg("提交本题不能过快,请稍后重试");
//					super.writeJson(j);
//					lock.unlock();
//					return;
//				}
//			} catch (Exception e) {
//				j.setSuccess(false);
//				j.setMsg("服务器错误,请重试");
//				super.writeJson(j);
//				lock.unlock();
//				return;
//			}
		try {	
			//System.out.println("加锁了"+Calendar.getInstance().getTime());
			//Thread.sleep(20000);
			boolean isOverSimilarity = false;
			// 根据userID，examID，problemID在Studentexamdetail中查找获取studentexamdetail记录
			Studentexamdetail studentexamdetail = studentexamdetailService
					.getStatusByUserIDexamIDproblemId(
							pMWrongAndCorrectIds.getUserId(),
							pMWrongAndCorrectIds.getExamId(),
							pMWrongAndCorrectIds.getProblemId());
			// Studentexamdetail为空说明还没有裁判过，则提示用户
			if (studentexamdetail == null) {
				j.setSuccess(false);
				j.setMsg("必须先提交代码才能提交本题。");
				super.writeJson(j);
			}
			else if (studentexamdetail.isFinished()) {
				j.setSuccess(false);
				j.setMsg("不能重复提交");
				super.writeJson(j);
			} 
			else {
				Json json = solutionService.submitThisProblem(
						studentexamdetail, pMWrongAndCorrectIds, j,
						isOverSimilarity);
				j.setSuccess(json.isSuccess());
				j.setMsg(json.getMsg());
				super.writeJson(j);
			}
			// }
		} catch (Exception e) {
			String sOut = "";
			StackTraceElement[] trace = e.getStackTrace();
			for (StackTraceElement s : trace) {
				sOut += "\tat " + s + "\r\n";
			}
			// 异常信息最大记录19000个字符，数据库该字段最大为20K
			int count = sOut.length() > 19000 ? 19000 : sOut.length();
			sOut = sOut.substring(0, count - 1);
			int leng = e.getLocalizedMessage().length() > 1800 ? 1800 : e
					.getLocalizedMessage().length();
			String localMessage = "";
			if (e.getLocalizedMessage() != null) {
				localMessage = e.getLocalizedMessage().substring(0, leng - 1);
			}
			Log log = new Log();
			log.setType("代码提交");
			log.setOptime(new Date());
			log.setUserId(pMWrongAndCorrectIds.getUserId());
			log.setUserType("student");
			log.setContent(sOut);
			log.setAbstractContent("学生id:" + pMWrongAndCorrectIds.getUserId()
					+ "考试id:" + pMWrongAndCorrectIds.getExamId() + "题目id:"
					+ pMWrongAndCorrectIds.getProblemId() + "\n" + localMessage);
			logService.WriteLog(log);

			// 返回前台的json数据
			j = new Json();
			j.setSuccess(false);
			j.setMsg("服务器内部发生错误，请报告管理员。");
			super.writeJson(j);
		} finally {
//			redisService.unLock(key);
			lock.unlock();// 释放锁
			
		}
	}
}
