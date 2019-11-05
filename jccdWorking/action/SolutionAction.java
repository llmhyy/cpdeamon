package edu.dhu.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
//import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;
import edu.dhu.common.Constant;
import edu.dhu.dao.ExamproblemDaoI;
import edu.dhu.dao.StudentexamdetailDaoI;
import edu.dhu.dao.StudentexaminfoDaoI;
import edu.dhu.dao.UserDaoI;
import edu.dhu.model.Exam;
import edu.dhu.model.Examproblems;
import edu.dhu.model.ExamStudent;
import edu.dhu.model.Log;
import edu.dhu.model.Problems;
import edu.dhu.model.Solution;
import edu.dhu.model.Studentexamdetail;
import edu.dhu.model.Studentexaminfo;
import edu.dhu.model.Users;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMClasses;
import edu.dhu.pageModel.PMSolution;
import edu.dhu.pageModel.PMUser;
import edu.dhu.pageModel.PMWrongAndCorrectIds;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.ClassesServiceI;
import edu.dhu.service.ExamServiceI;
import edu.dhu.service.ExamproblemServiceI;
import edu.dhu.service.LogServiceI;
import edu.dhu.service.ProblemsServiceI;
import edu.dhu.service.RedisServiceI;
import edu.dhu.service.SolutionServiceI;
import edu.dhu.service.StudentexamdetailServiceI;
import edu.dhu.service.SubmittedcodeServiceI;
import edu.dhu.util.CHZipUtils;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "solutionAction", results = { @Result(name = "takeAnExam", location = "/user/takeAnExam.jsp"),
		@Result(name = "exportExamSearchSolution", type = "stream", params = { "inputName", "fileInput",
				"contentDisposition", "attachment;filename=exportExamSearchSolution.xls", "contentType",
				"text/plain" }),
		@Result(name = "exportSearchCopy", type = "stream", params = { "inputName", "fileInput", "contentDisposition",
				"attachment;filename=exportSearchCopy.xls", "contentType", "text/plain" }) })
public class SolutionAction extends BaseAction implements ModelDriven<Solution> {
	private static final long serialVersionUID = -2457759901751938914L;
	// 记录日志
	private static final Logger logger = Logger.getLogger(SolutionAction.class);
	private static final String root_ZIP = ServletActionContext.getServletContext().getRealPath("/") + "OJtemp\\";
	Solution solution = new Solution();
	int pageSize, nowPage;
	String displaySequence;
	String studentNo, name, banji, searchTime, url, targeName;
	float similarity;
	int reason = 0; // 撤销提交的原因
	boolean isCopy = true;
	boolean isLast; // 是否要查询最后提交
	private InputStream fileInput;
	private String fileName;
	private UserDaoI userDao;
	private ExamproblemDaoI examproblemDao;
	private SolutionServiceI solutionService;
	private StudentexamdetailServiceI studentexamdetailService;
	private ExamServiceI examService;
	private ProblemsServiceI problemsService;
	private LogServiceI logService;
	private SubmittedcodeServiceI submittedcodeService;
	private ExamproblemServiceI examproblemService;
	private ProblemsServiceI problemsServiceI;
	private StudentexaminfoDaoI studentexaminfoDao;
	private StudentexamdetailDaoI studentexamdetailDao;
	private ClassesServiceI classesServiceI;
	private RedisServiceI redisService;
	private static Lock reentrantLock = new ReentrantLock();// 锁对象

	public RedisServiceI getRedisService() {
		return redisService;
	}

	@Autowired
	public void setRedisService(RedisServiceI redisService) {
		this.redisService = redisService;
	}

	@Autowired
	public void setClassesServiceI(ClassesServiceI classesServiceI) {
		this.classesServiceI = classesServiceI;
	}

	public ClassesServiceI getClassesServiceI() {
		return classesServiceI;
	}

	public StudentexamdetailDaoI getStudentexamdetailDao() {
		return studentexamdetailDao;
	}

	@Autowired
	public void setStudentexamdetailDao(StudentexamdetailDaoI studentexamdetailDao) {
		this.studentexamdetailDao = studentexamdetailDao;
	}

	// 给给个学生分配一个锁，让学生可以并行提交代码，但是每个学生只能线性提交代码
	private final static HashMap<Integer, ReentrantLock> lockMap = new HashMap<>();

	public StudentexaminfoDaoI getStudentexaminfoDao() {
		return studentexaminfoDao;
	}

	@Autowired
	public void setStudentexaminfoDao(StudentexaminfoDaoI studentexaminfoDao) {
		this.studentexaminfoDao = studentexaminfoDao;
	}

	public ProblemsServiceI getProblemsServiceI() {
		return problemsServiceI;
	}

	@Autowired
	public void setProblemsServiceI(ProblemsServiceI problemsServiceI) {
		this.problemsServiceI = problemsServiceI;
	}

	public ExamproblemServiceI getExamproblemService() {
		return examproblemService;
	}

	@Autowired
	public void setExamproblemService(ExamproblemServiceI examproblemService) {
		this.examproblemService = examproblemService;
	}

	public InputStream getFileInput() {
		return fileInput;
	}

	public void setFileInput(InputStream fileInput) {

		this.fileInput = fileInput;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setNowPage(int nowPage) {
		this.nowPage = nowPage;
	}

	public int getNowPage() {
		return nowPage;
	}

	public void setDisplaySequence(String displaySequence) {
		this.displaySequence = displaySequence;
	}

	public String getDisplaySequence() {
		return displaySequence;
	}

	public void setStudentNo(String studentNo) {
		this.studentNo = studentNo;
	}

	public String getStudentNo() {
		return studentNo;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTargeName() {
		return targeName;
	}

	public void setTargeName(String targeName) {
		this.targeName = targeName;
	}

	public void setBanji(String banji) {
		this.banji = banji;
	}

	public String getBanji() {
		return banji;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public float getSimilarity() {
		return similarity;
	}

	public void setReason(int reason) {
		this.reason = reason;
	}

	public int getReason() {
		return reason;
	}

	public void setIsCopy(boolean isCopy) {
		this.isCopy = isCopy;
	}

	public boolean getIsCopy() {
		return isCopy;
	}

	public void setSearchTime(String searchTime) {
		this.searchTime = searchTime;
	}

	public String getSearchTime() {
		return searchTime;
	}

	public void setIsLast(boolean isLast) {
		this.isLast = isLast;
	}

	public boolean getIsLast() {
		return isLast;
	}

	@Override
	public Solution getModel() {
		return solution;
	}

	public SolutionServiceI getSolutionService() {
		return solutionService;
	}

	@Autowired
	public void setSolutionService(SolutionServiceI solutionService) {
		this.solutionService = solutionService;
	}

	public StudentexamdetailServiceI getStudentexamdetailService() {
		return studentexamdetailService;
	}

	@Autowired
	public void setStudentexamdetailService(StudentexamdetailServiceI studentexamdetailService) {
		this.studentexamdetailService = studentexamdetailService;
	}

	public SubmittedcodeServiceI getSubmittedcodeService() {
		return submittedcodeService;
	}

	@Autowired
	public void setSubmittedcodeService(SubmittedcodeServiceI submittedcodeService) {
		this.submittedcodeService = submittedcodeService;
	}

	public ExamServiceI getExamService() {
		return examService;
	}

	@Autowired
	public void setExamService(ExamServiceI examService) {
		this.examService = examService;
	}

	@Autowired
	public void setUserDao(UserDaoI userDao) {
		this.userDao = userDao;
	}

	public UserDaoI getUserDao() {
		return userDao;
	}

	@Autowired
	public void setExamproblemDao(ExamproblemDaoI examproblemDao) {
		this.examproblemDao = examproblemDao;
	}

	public ExamproblemDaoI getExamproblemDao() {
		return examproblemDao;
	}

	@Autowired
	public void setLogService(LogServiceI logService) {
		this.logService = logService;
	}

	public LogServiceI getLogService() {
		return logService;
	}

	public ProblemsServiceI getProblemsService() {
		return problemsService;
	}

	@Autowired
	public void setProblemsService(ProblemsServiceI problemsService) {
		this.problemsService = problemsService;
	}

	/*
	 * 先判断LockMap中当前userid是否存在，若不存在则赋值加锁，存在就加上锁否则空的话直接赋值，加锁最后遍历找到userid对应的锁进行释放
	 */
	public synchronized Lock getUserLock() {
		if (lockMap.containsKey(solution.getUserid())) {
			return lockMap.get(solution.getUserid());
		} else {
			ReentrantLock lock = new ReentrantLock();
			lockMap.put(solution.getUserid(), lock);
			return lockMap.get(solution.getUserid());
		}
	}

	// 根据题目ID提交代码
	public void submitCodeById() {

		Json j = new Json();
		// 如果session断掉了
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		if (sessionInfo == null) {
			j.setSuccess(false);
			j.setMsg("必须先登录才能提交本题。");
			super.writeJson(j);
			return;
		}
		if (solution.getSourceCode().length() > 64000) {
			j.setSuccess(false);
			j.setMsg("代码长度不能超过64000个字符");
			super.writeJson(j);
			return;
		}

		String userName = sessionInfo.getLoginName();
		String key = "submitCode_" + userName;
		reentrantLock.lock();
		// redis分布式锁
		int expire = 30; // 超时时间
//		try {
//			// 获取锁失败就结束
//			if (!redisService.lock(key, expire)) {
//				j.setSuccess(false);
//				j.setMsg("提交代码不能过快,请稍后重试");
//				super.writeJson(j);
//				reentrantLock.unlock();
//				return;
//			}
//		} catch (Exception e) {
//			j.setSuccess(false);
//			j.setMsg("服务器错误,请重试");
//			super.writeJson(j);
//			reentrantLock.unlock();
//			return;
//		}

		try {

			//// // 返回前台的json数据
			// System.out.println(solution.getUserid() +
			//// "加锁了~~~~~~~~~~~~~~~~~~~~" + Calendar.getInstance().getTime());
			// Thread.sleep(20000);
			//
			// 设置消耗的时间elapsedTime
			Date now = new Date();

			// 设置提交时间
			solution.setSubmitTime(new Date());
			// 获取考试开始时间和结束时间
			Exam exam = examService.getExamById(solution.getExamId());

			Date startTime = exam.getStarttime();
			Date endTime = exam.getEndtime();
			// 如果考试开始之前提交代码
			if (now.getTime() < startTime.getTime()) {
				j.setSuccess(false);
				j.setMsg("考试还没开始，无法提交代码！");
				super.writeJson(j);
			}
			// 如果考试结束之后提交代码
			else if (now.getTime() > endTime.getTime() && sessionInfo.getRoleNames().equals("student")) {
				j.setSuccess(false);
				j.setMsg("考试已经结束，无法再提交代码！");
				super.writeJson(j);
			} else {
				// 根据problemID查找Problems
				Problems problem = problemsService.findProblemById(solution.getProblemId());
				// 根据userId+examId+problemId到studentExamDetail表查找
				Studentexamdetail studentexamdetail = studentexamdetailService.getStatusByUserIDexamIDproblemId(
						solution.getUserid(), solution.getExamId(), solution.getProblemId());

				// 提交本题之后再次提交代码
				if (studentexamdetail != null && studentexamdetail.isFinished()) {
					j.setSuccess(false);
					j.setMsg("已经过提交本题，不能再提交代码！");
					super.writeJson(j);
					return;
				}
				Examproblems examproblems = examproblemDao.getExamproblemsByExamIdAndProblemId(solution.getExamId(),
						solution.getProblemId());
				Date deadline = examproblems.getDeadline();
				Date submitTime = solution.getSubmitTime();
				if (deadline != null && submitTime.after(deadline) && sessionInfo.getRoleNames().equals("student")) {
					j.setSuccess(false);
					j.setMsg("提交时间已经晚于本题截止时间不能提交代码");
					super.writeJson(j);
					return;
				}
				// 更新Problems表的submit字段，增加1
				if (problem.getSubmit() == null) {
					problem.setSubmit(1);
				} else {
					problem.setSubmit(problem.getSubmit() + 1);
				}
				// 设置status字段为已提交状态
				solution.setStatus(Constant.CODE_WAIT);
				// 设置remark默认值
				solution.setRemark(Constant.DEFAULT_REMARK);
				// 设置正确的测试用例ids字断为默认值－1
				solution.setCorrectCaseIds(Constant.DEFAULT_CORRECTCASEIDS);

				Json json = submittedcodeService.submitCode(problem, solution, studentexamdetail, now, startTime,
						endTime);
				j.setMsg(json.getMsg());
				j.setSuccess(json.isSuccess());
				super.writeJson(j);

			}
		} catch (Exception e) {
			e.printStackTrace();
			String sOut = "";
			StackTraceElement[] trace = e.getStackTrace();
			for (StackTraceElement s : trace) {
				sOut += "\tat " + s + "\r\n";
			}
			// 异常信息最大记录19000个字符，数据库该字段最大为20K
			int count = sOut.length() > 19000 ? 19000 : sOut.length();
			sOut = sOut.substring(0, count - 1);
			int leng = e.getLocalizedMessage().length() > 1800 ? 1800 : e.getLocalizedMessage().length();
			String localMessage = "";
			if (e.getLocalizedMessage() != null) {
				localMessage = e.getLocalizedMessage().substring(0, leng - 1);
			}
			Log log = new Log();
			log.setType("代码提交");
			log.setOptime(new Date());
			log.setUserId(solution.getUserid());
			log.setUserType("student");
			log.setContent(sOut);
			log.setAbstractContent("学生id:" + solution.getUserid() + "考试id:" + solution.getExamId() + "题目id:"
					+ solution.getProblemId() + "\n" + localMessage);
			logService.WriteLog(log);

			// 返回前台的json数据
			j.setSuccess(false);
			j.setMsg("服务器内部发生错误，请报告管理员。");
			super.writeJson(j);
		} finally {
			reentrantLock.unlock();
//			redisService.unLock(key);
			// System.out.println(solution.getUserid() +
			// "解锁了！！！！！！！！！！！！！！！！！！！！！" + Calendar.getInstance().getTime());
		}
	}

	public void getExamStudentByTeacherId() // 查看本场考试本班的学生
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMClasses> classsList = classesServiceI.findClassInExam(solution.getExamId());
			List<PMUser> pmustudent = new ArrayList<PMUser>();
			int teacherId = solution.getId();// 将teacherId作为id传入
			for (int i = 0; i < classsList.size(); i++) {
				if (classsList.get(i).getTeacherId() == teacherId) {
					List<PMUser> pmu = classesServiceI.findClassStudentsById(classsList.get(i).getId());
					for (int k = 0; k < pmu.size(); k++) {
						Studentexaminfo sei = studentexaminfoDao.getStudentexaminfoByUserIdAnExamId(pmu.get(k).getId(),
								solution.getExamId());
						if (sei != null) {
							pmu.get(k).setScore(sei.getScore());
							pmu.get(k).setFirstLoginTime(sei.getFirstloginTime());
						}
					}
					pmustudent.addAll(pmu);
				}
			}
			j.setSuccess(true);
			j.setObj(pmustudent);
			j.setMsg("获取学生信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamStudentByexamId() //
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMClasses> classsList = classesServiceI.findClassInExam(solution.getExamId());
			List<PMUser> pmustudent = new ArrayList<PMUser>();

			for (int i = 0; i < classsList.size(); i++) {
				List<PMUser> pmu = classesServiceI.findClassStudentsById(classsList.get(i).getId());
				for (int k = 0; k < pmu.size(); k++) {
					Studentexaminfo sei = studentexaminfoDao.getStudentexaminfoByUserIdAnExamId(pmu.get(k).getId(),
							solution.getExamId());
					if (sei != null) {
						pmu.get(k).setScore(sei.getScore());
						pmu.get(k).setFirstLoginTime(sei.getFirstloginTime());
					}
				}
				pmustudent.addAll(pmu);
			}
			j.setSuccess(true);
			j.setObj(pmustudent);
			j.setMsg("获取学生信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamStudentByexamIdClassId() //
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMClasses> classsList = classesServiceI.findClassInExam(solution.getExamId());
			List<PMUser> pmustudent = new ArrayList<PMUser>();

			for (int i = 0; i < classsList.size(); i++) {
				if (classsList.get(i).getId() == solution.getId()) {
					List<PMUser> pmu = classesServiceI.findClassStudentsById(classsList.get(i).getId());
					for (int k = 0; k < pmu.size(); k++) {
						Studentexaminfo sei = studentexaminfoDao.getStudentexaminfoByUserIdAnExamId(pmu.get(k).getId(),
								solution.getExamId());
						if (sei != null) {
							pmu.get(k).setScore(sei.getScore());
							pmu.get(k).setFirstLoginTime(sei.getFirstloginTime());
						}
					}
					pmustudent.addAll(pmu);
				}
			}
			j.setSuccess(true);
			j.setObj(pmustudent);
			j.setMsg("获取学生信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamSubmitSolution() // 查看考试哦的提交情况
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMSolution> pList = solutionService.getExamSubmitSolution(isLast, solution.getExamId(), nowPage,
					pageSize, displaySequence, studentNo, name, banji, solution.getSimilarity(), searchTime);
			Exam exam = examService.getExamById(solution.getExamId());
			pList.get(0).setExamName(exam.getName());
			j.setSuccess(true);
			j.setObj(pList);
			j.setMsg("获取提交状况成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void editStudentScore() // 查看考试哦的提交情况
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			int num = studentexamdetailService.editStudentScore(solution);
			List<Studentexamdetail> stuexamDetailList = studentexamdetailService
					.getAllStudentexamdetailListByUserIdAndExamId(solution.getUserid(), solution.getExamId());
			float stuAllScore = 0;
			for (int i = 0; i < stuexamDetailList.size(); i++) {
				stuAllScore += stuexamDetailList.get(i).getScore();
			}
			int num2 = studentexaminfoDao.updateStudentScore(solution.getUserid(), solution.getExamId(), stuAllScore);
			if (num == 1 && num2 == 1) {
				j.setSuccess(true);
				j.setObj(stuAllScore);
				j.setMsg("修改成绩成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setObj(-1);
				j.setMsg("修改成绩失败");
				super.writeJson(j);
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void editStudentDetailFinished() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			boolean isOverSimilarity = false;
			Studentexamdetail stuexamd = studentexamdetailDao.get(Studentexamdetail.class, solution.getId());
			// Studentexamdetail为空说明还没有裁判过，则提示用户

			PMWrongAndCorrectIds pMWrongAndCorrectIds = new PMWrongAndCorrectIds();
			pMWrongAndCorrectIds.setUserId(stuexamd.getUserId());
			pMWrongAndCorrectIds.setExamId(stuexamd.getExamId());
			pMWrongAndCorrectIds.setProblemId(stuexamd.getProblemId());
			pMWrongAndCorrectIds.setSubmitType(solution.getStatus());
			Json json = solutionService.submitThisProblem(stuexamd, pMWrongAndCorrectIds, j, isOverSimilarity);
			j.setSuccess(json.isSuccess());
			j.setMsg(json.getMsg());
			super.writeJson(j);

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void editStudentexaminfoScore() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			int num = studentexaminfoDao.updateStudentScore(solution.getUserid(), solution.getExamId(),
					solution.getScore());
			if (num == 1) {
				j.setSuccess(true);
				j.setMsg("修改学生总分成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setObj(-1);
				j.setMsg("修改学生总分失败");
				super.writeJson(j);
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamSubmitSolutionScore() // 查看考试得分数
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMSolution> newList = new ArrayList<PMSolution>();

			Studentexaminfo stuexamInfo = studentexaminfoDao.getStudentexaminfoByUserIdAnExamId(solution.getUserid(),
					solution.getExamId());
			if (stuexamInfo != null) {

				List<Examproblems> pList = examproblemService.getProblemByExamId(solution.getExamId());// 获取到了problemId和总分还有题号
				for (int i = 0; i < pList.size(); i++) {
					Problems pro = problemsServiceI.findProblemById(pList.get(i).getProblemId());
					Solution pms = solutionService.getLastSolutionByUserIdExamIdProblemId(solution.getUserid(),
							solution.getExamId(), pList.get(i).getProblemId());
					Studentexamdetail stuexamdetail = studentexamdetailService.getStatusByUserIDexamIDproblemId(
							solution.getUserid(), solution.getExamId(), pList.get(i).getProblemId());

					PMSolution p = new PMSolution();
					p.setDisplaySequence(pList.get(i).getDisplaySequence());
					p.setScoreTotal(pList.get(i).getScore());
					p.setTitle(pro.getTitle());
					p.setProblemId(pList.get(i).getProblemId());
					p.setExamId(solution.getExamId());

					p.setStudentAllScore(stuexamInfo.getScore());

					if (pms != null) {
						p.setScore(stuexamdetail.getScore());
						p.setId(stuexamdetail.getId());
						p.setSubmited(stuexamdetail.isFinished());
					} else {
						p.setScore(0);
						p.setId(0);
						p.setSubmited(false);
						p.setFlag(true);
					}
					newList.add(p);
				}

				j.setSuccess(true);
				j.setObj(newList);
				j.setMsg("获取学生分数成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("查询无记录");
				super.writeJson(j);
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamUserName() // 查看考试得分数
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			PMSolution pms = new PMSolution();
			Users users = userDao.get(Users.class, solution.getUserid());
			Exam exam = examService.getExamById(solution.getExamId());
			if (users != null && exam != null) {
				pms.setChineseName(users.getChineseName());
				pms.setExamName(exam.getName());
				j.setSuccess(true);
				j.setObj(pms);
				j.setMsg("获取学生考试名称成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("考试信息或者学生信息不存在");
				super.writeJson(j);
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamSubmitSolutionCount() // 查看一场考试的solution总数
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			j.setSuccess(true);
			long size = solutionService.getExamSubmitSolutionCount(isLast, solution.getExamId(), displaySequence,
					studentNo, name, banji, solution.getSimilarity(), searchTime);
			j.setObj(size);
			j.setMsg("获取考试提交总数成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getSourceCoude() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			int id = solution.getId();
			Solution s = solutionService.getSourceCode(id);
			if (s != null) {
				j.setSuccess(true);
				j.setObj(s.getSourceCode());
				j.setMsg("获取源代码成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("获取源代码成功");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}

	}

	public String exportExamSearchSolution() {
		try {
			name = new String(name.getBytes("ISO-8859-1"), "UTF-8");
			displaySequence = new String(displaySequence.getBytes("ISO-8859-1"), "UTF-8");
			banji = new String(banji.getBytes("ISO-8859-1"), "UTF-8");
			studentNo = new String(studentNo.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMSolution> pList = solutionService.exportExamSearchSolution(isLast, solution.getExamId(),
					displaySequence, studentNo, name, banji, solution.getSimilarity(), searchTime);
			HSSFWorkbook wb = new HSSFWorkbook(); // 导出到excel文件中
			HSSFSheet sheet = wb.createSheet("表一");
			HSSFRow row = sheet.createRow(0);
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			HSSFCell cell = row.createCell((short) 0);
			cell.setCellValue("序号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 1);
			cell.setCellValue("提交时间");
			cell.setCellStyle(style);
			cell = row.createCell((short) 2);
			cell.setCellValue("学号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 3);
			cell.setCellValue("姓名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 4);
			cell.setCellValue("班级");
			cell.setCellStyle(style);
			cell = row.createCell((short) 5);
			cell.setCellValue("题号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 6);
			cell.setCellValue("标题");
			cell.setCellStyle(style);
			cell = row.createCell((short) 7);
			cell.setCellValue("状态");
			cell.setCellStyle(style);
			cell = row.createCell((short) 8);
			cell.setCellValue("分数");
			cell.setCellStyle(style);
			cell = row.createCell((short) 9);
			cell.setCellValue("相似度");
			cell.setCellStyle(style);
			cell = row.createCell((short) 10);
			cell.setCellValue("相似度阈");
			cell.setCellStyle(style);
			cell = row.createCell((short) 11);
			cell.setCellValue("语言");
			cell.setCellStyle(style);
			for (int i = 0; i < pList.size(); i++) {
				row = sheet.createRow(i + 1);
				PMSolution s = pList.get(i);
				row.createCell((short) 0).setCellValue((i + 1));
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = dateFormat.format(s.getSubmitTime());
				row.createCell((short) 1).setCellValue(date);
				row.createCell((short) 2).setCellValue(s.getStudentNo());
				row.createCell((short) 3).setCellValue(s.getChineseName());
				row.createCell((short) 4).setCellValue(s.getBanji());
				row.createCell((short) 5).setCellValue(s.getDisplaySequence());
				row.createCell((short) 6).setCellValue(s.getTitle());
				row.createCell((short) 7).setCellValue(s.getStatus());
				row.createCell((short) 8).setCellValue(s.getScore());
				if (s.getSimilarity() != -1)
					row.createCell((short) 9).setCellValue(s.getSimilarity());
				if (s.getSimilarityThreshold() != -1)
					row.createCell((short) 10).setCellValue(s.getSimilarityThreshold());
				row.createCell((short) 11).setCellValue(s.getLanguage());
			}
			try {
				String userId = String.valueOf(sessionInfo.getTeacherId());
				File dir = new File("C:\\OJtemp\\" + userId + "\\");
				if (!dir.exists() && !dir.isDirectory()) {
					dir.mkdirs(); // 创建用户目录
				}
				fileName = "exportExamSearchSolution.xls";
				FileOutputStream fout = new FileOutputStream("C:\\OJtemp\\" + userId + "\\" + fileName);
				wb.write(fout);
				fout.close();
				File file = new File("C:\\OJtemp\\" + userId + "\\" + fileName);
				fileInput = new FileInputStream(file);
				return "exportExamSearchSolution";
			} catch (Exception e) {

			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
		try {
			fileInput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block

		}
		return null;
	}

	public void exportExamCode() // 获得考试的最后代码
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			// 组合教师id_时间戳分割多教师文件夹
			String tearcherId = String.valueOf(sessionInfo.getTeacherId());
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			String dateNow = df.format(Calendar.getInstance().getTime());
			String tearcherFileId = tearcherId + "_" + dateNow;

			List<Solution> solutionList = new ArrayList<Solution>();
			List<ExamStudent> eaxmStudentList = new ArrayList<ExamStudent>();
			String codeType = "exportExamCode";// TODO用来标记文件夹，本次导出的类型
			int classId=Integer.parseInt(banji);
	        int examId=solution.getExamId();
			if ( classId!= 0) {
				// codeType="exportExamLastCode";
				if (isLast) {
					solutionList = solutionService.exportClassExamLastCode(examId,
							classId);
				} else {
					// 获取考试所有班级的考试数据
					solutionList = solutionService.exportClassExamCode(examId, classId);
				}
			} else {
				if (isLast) {
					solutionList = solutionService.exportExamLastCode(examId);

				} else {
					solutionList = solutionService.exportExamCode(examId);
				}
			}
			eaxmStudentList=solutionService.getExamStudentInfo(examId, classId);
			// 根据userId建立学生信息字典
			HashMap<Integer, ExamStudent> ExamStudentsMap = new HashMap<Integer, ExamStudent>();
			for (ExamStudent examstudent : eaxmStudentList) {
				ExamStudentsMap.put(examstudent.getId(), examstudent);
			}

			// 获取这场考试中所有题目对应的DisplaySequence（题目在考试中的编号）
			List<Examproblems> examproblemList = examproblemDao.getDisplaySequenceByExamId(examId);

			HashMap<Integer, Integer> examproblemMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < examproblemList.size(); i++) {
				examproblemMap.put(examproblemList.get(i).getProblemId(), examproblemList.get(i).getDisplaySequence());
			}

			int displaySequence;

			int userId;

			String dirPath = null;// 文件路径
			File file = null;// 文件名
			ExamStudent examStudent;
			for (Solution solution : solutionList) {
				userId = solution.getUserid();
				examStudent = ExamStudentsMap.get(userId);
				// 文件路径
				dirPath = tearcherFileId + "\\" + codeType + "\\" + examStudent.getClassName() + "\\"
						+ examStudent.getStudentNo() + "_" + examStudent.getStudentName();
				// 替代非法字符\ / : * ? " < > |
				dirPath = dirPath.replace("/", "_").replace(":", "_").replace("*", "_").replace("?", "_")
						.replace("\"", "_").replace("<", "_").replace(">", "_").replace("|", "_").replace(" ", "_");
				dirPath = root_ZIP + dirPath;
				// 文件
				displaySequence = examproblemMap.get(solution.getProblemId());
				String submitTime = df.format(solution.getSubmitTime());
				file = new File(
						dirPath + "\\" + displaySequence + "_" + submitTime + "_" + solution.getStatus() + ".c");
				try {
					if (!file.getParentFile().exists()) { // 如果父目录不存在，创建父目录

						file.getParentFile().mkdirs();
					}
					// 创建文件，代码输出至文件夹
					// FileOutputStream（file，boolean append）默认boolen false
					// 会覆盖原文件
					FileOutputStream out = new FileOutputStream(file);
					out.write(solution.getSourceCode().getBytes());
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
					j.setSuccess(false);
					j.setMsg("系统内部发生错误,请联系系统管理员");
					super.writeJson(j);
				}
			}
			// 文件整体压缩
			String sourceFolder = root_ZIP + tearcherFileId + "\\" + codeType;
			String zipFilePath = root_ZIP + tearcherFileId + "\\" + codeType + ".zip";
			CHZipUtils.zip(sourceFolder, zipFilePath);
			j.setSuccess(true);
			j.setMsg(tearcherFileId);
			super.writeJson(j);

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getCopy() // 获取抄袭的搜索结果
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			int examId = solution.getExamId();
			List<PMSolution> obj = solutionService.getCopyList(examId, displaySequence, studentNo, name, banji,
					solution.getSimilarity(), searchTime);
			if (obj != null) {
				j.setSuccess(true);
				j.setObj(obj);
				j.setMsg("获取抄袭情况成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("获取抄袭情况成功");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}

	}

	public String exportSearchCopy() // 导出查看抄袭情况的搜索结果
	{
		try {
			name = new String(name.getBytes("ISO-8859-1"), "UTF-8");
			displaySequence = new String(displaySequence.getBytes("ISO-8859-1"), "UTF-8");
			banji = new String(banji.getBytes("ISO-8859-1"), "UTF-8");
			studentNo = new String(studentNo.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMSolution> pList = solutionService.getCopyList(solution.getExamId(), displaySequence, studentNo, name,
					banji, solution.getSimilarity(), searchTime);
			HSSFWorkbook wb = new HSSFWorkbook(); // 导出到excel文件中
			HSSFSheet sheet = wb.createSheet("表一");
			HSSFRow row = sheet.createRow(0);
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			HSSFCell cell = row.createCell((short) 0);
			cell.setCellValue("序号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 1);
			cell.setCellValue("提交时间");
			cell.setCellStyle(style);
			cell = row.createCell((short) 2);
			cell.setCellValue("题号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 3);
			cell.setCellValue("标题");
			cell.setCellStyle(style);
			cell = row.createCell((short) 4);
			cell.setCellValue("学号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 5);
			cell.setCellValue("姓名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 6);
			cell.setCellValue("班级");
			cell.setCellStyle(style);
			cell = row.createCell((short) 7);
			cell.setCellValue("对象学号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 8);
			cell.setCellValue("对象姓名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 9);
			cell.setCellValue("相似度");
			cell.setCellStyle(style);
			cell = row.createCell((short) 10);
			cell.setCellValue("相似度阈");
			cell.setCellStyle(style);
			cell = row.createCell((short) 11);
			cell.setCellValue("是否提交");
			cell.setCellStyle(style);
			for (int i = 0; i < pList.size(); i++) {
				row = sheet.createRow(i + 1);
				PMSolution s = pList.get(i);
				row.createCell((short) 0).setCellValue((i + 1));
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = dateFormat.format(s.getSubmitTime());
				row.createCell((short) 1).setCellValue(date);
				row.createCell((short) 2).setCellValue(s.getDisplaySequence());
				row.createCell((short) 3).setCellValue(s.getTitle());
				row.createCell((short) 4).setCellValue(s.getStudentNo());
				row.createCell((short) 5).setCellValue(s.getChineseName());
				row.createCell((short) 6).setCellValue(s.getBanji());
				row.createCell((short) 7).setCellValue(s.getStudentNo2());
				row.createCell((short) 8).setCellValue(s.getChineseName2());
				if (s.getSimilarity() != -1)
					row.createCell((short) 9).setCellValue(s.getSimilarity());
				if (s.getSimilarityThreshold() != -1)
					row.createCell((short) 10).setCellValue(s.getSimilarityThreshold());
				if (s.getSubmited() == false)
					row.createCell((short) 11).setCellValue("");
				else
					row.createCell((short) 11).setCellValue("是");
				if (s.getEversubmit() != null && s.getEversubmit() == 1)
					row.createCell((short) 11).setCellValue("曾经");
			}
			try {
				String userId = String.valueOf(sessionInfo.getTeacherId());
				File dir = new File("C:\\OJtemp\\" + userId + "\\");
				if (!dir.exists() && !dir.isDirectory()) {
					dir.mkdirs(); // 创建用户目录
				}
				fileName = "exportSearchCopy.xls";
				FileOutputStream fout = new FileOutputStream("C:\\OJtemp\\" + userId + "\\" + fileName);
				wb.write(fout);
				fout.close();
				File file = new File("C:\\OJtemp\\" + userId + "\\" + fileName);
				fileInput = new FileInputStream(file);
				return "exportSearchCopy";
			} catch (Exception e) {

			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
		try {
			fileInput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block

		}
		return null;
	}

	// 对外暴露接口
	public void deleteTearchFile() {
		delFolder(root_ZIP + targeName);
	}

	public void deleteSubmit() // 撤销提交
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = solutionService.deleteSubmitBySolutionId(solution.getId(), reason, isCopy,
					solution.getRemark());
			if (result == true) {
				j.setSuccess(true);
				j.setMsg("撤销成功!");
				super.writeJson(j);
			} else {
				j.setSuccess(true);
				j.setMsg("撤销失败!");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getSolutionDetail() // 获取提交的详细信息
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMSolution> list = new ArrayList<PMSolution>();
			Solution s1 = solutionService.getSolutionById(solution.getId());
			PMSolution p1 = new PMSolution();
			p1.setSourceCode(s1.getSourceCode());
			p1.setSimilarity(s1.getSimilarity());
			int userId = s1.getUserid();
			Users user = userDao.get(Users.class, userId);
			if (user != null) {
				p1.setBanji(user.getBanji());
				p1.setChineseName(user.getChineseName());
				p1.setStudentNo(user.getStudentNo());
			}
			Solution s2 = solutionService.getSolutionById(solution.getSimilarId());
			PMSolution p2 = new PMSolution();
			p2.setSourceCode(s2.getSourceCode());
			p2.setSimilarity(s2.getSimilarity());
			userId = s2.getUserid();
			user = userDao.get(Users.class, userId);
			if (user != null) {
				p2.setBanji(user.getBanji());
				p2.setChineseName(user.getChineseName());
				p2.setStudentNo(user.getStudentNo());
			}
			list.add(p1);
			list.add(p2);
			j.setSuccess(true);
			j.setObj(list);
			j.setMsg("获取提交相似度信息成功!");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void isSubmited() // 查看solution在similarityWarning中是否为submited
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = solutionService.isSubmited(solution.getId());
			j.setSuccess(true);
			j.setObj(result);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getSolutionById() // 通过id获取solution
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			Solution result = solutionService.getSolutionById(solution.getId().intValue());
			j.setSuccess(true);
			j.setObj(result);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	// 删除文件夹
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 删除指定文件夹下所有文件
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	public void downloadZipFile() {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/zip");
		String fullFileName = root_ZIP + url + "\\exportExamCode.zip";
		InputStream in = null;
		OutputStream out = null;
		try {
			String filename = new String(root_ZIP.getBytes("UTF-8"), "ISO-8859-1");
			in = new FileInputStream(fullFileName);
			response.setHeader("Content-Disposition", "attachment;filename=exportExamCode.zip");
			response.setContentLength(in.available());
			out = response.getOutputStream();
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}

			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (in != null)
				try {
					in.close();
				} catch (Exception e2) {
				}
			if (out != null)
				try {
					out.close();
				} catch (Exception e3) {
				}
		} finally {
			delFolder(root_ZIP + url);
		}

	}

}
