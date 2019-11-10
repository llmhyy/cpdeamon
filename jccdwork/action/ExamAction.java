package edu.dhu.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;
//import com.sun.tools.internal.ws.wsdl.document.http.HTTPConstants;

import edu.dhu.cache.ClassesCacheManager;
import edu.dhu.cache.ClassstudentsCacheManager;
import edu.dhu.cache.ExamCacheManager;
import edu.dhu.cache.ExamLastSolutionStatusCacheManager;
import edu.dhu.cache.ExamScoreCacheManager;
import edu.dhu.cache.ExamproblemsCacheManager;
import edu.dhu.cache.StudentexamdetailCacheManager;
import edu.dhu.cache.StudentexaminfoCacheManager;
import edu.dhu.cache.UsersCacheManager;
import edu.dhu.cache.WSExamCacheManager;
import edu.dhu.dao.AdminusersDaoI;
import edu.dhu.dao.ClassesDaoI;
import edu.dhu.dao.ClassstudentsDaoI;
import edu.dhu.dao.ExamClassesDaoI;
import edu.dhu.dao.ExamproblemDaoI;
import edu.dhu.dao.SolutionDaoI;
import edu.dhu.dao.StudentexamdetailDaoI;
import edu.dhu.dao.StudentexaminfoDaoI;
import edu.dhu.dao.UserDaoI;
import edu.dhu.model.Adminusers;
import edu.dhu.model.Classstudents;
import edu.dhu.model.Exam;
import edu.dhu.model.Examclasses;
import edu.dhu.model.Examproblems;
import edu.dhu.model.Studentexamdetail;
import edu.dhu.model.Studentexaminfo;
import edu.dhu.model.Users;
import edu.dhu.pageModel.CookieInfo;
import edu.dhu.pageModel.DataGrid;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMClasses;
import edu.dhu.pageModel.PMExam;
import edu.dhu.pageModel.PMExamScore;
import edu.dhu.pageModel.PMExamScore2;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.ExamServiceI;
import edu.dhu.service.LogServiceI;
import edu.dhu.service.RedisServiceI;
import edu.dhu.service.UserServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "examAction", results = { @Result(name = "examList", location = "/front/user/examList.jsp"),
		@Result(name = "exportExamScores", type = "stream", params = { "inputName", "fileInput", "contentDisposition",
				"attachment;filename=exportExamScores.xls", "contentType", "text/plain" }),
		@Result(name = "exportExamScores2", type = "stream", params = { "inputName", "fileInput", "contentDisposition",
				"attachment;filename=exportExamScores2.xls", "contentType", "text/plain" }),
		@Result(name = "exportClassExamScoresExcel", type = "stream", params = { "inputName", "fileInput",
				"contentDisposition", "attachment;filename=exportClassExamScoresExcel.xls", "contentType",
				"text/plain" }),
		@Result(name = "exportClassExamScoresExcel2", type = "stream", params = { "inputName", "fileInput",
				"contentDisposition", "attachment;filename=exportClassExamScoresExcel2.xls", "contentType",
				"text/plain" }) })
public class ExamAction extends BaseAction implements ModelDriven<PMExam> {

	private static final long serialVersionUID = -4969428948695788009L;

	// 记录日志
	private static final Logger logger = Logger.getLogger(ExamAction.class);

	private ExamServiceI examService;
	private UserServiceI userService;
	private ExamClassesDaoI examclassesDao;
	int classId, examId, adminId;
	private AdminusersDaoI adminusersDao;
	private StudentexaminfoDaoI studentexaminfoDao;
	private UserDaoI userDao;
	private ClassesDaoI classesDao;
	private ClassstudentsDaoI classstudentsDao;
	private StudentexamdetailDaoI studentexamdetailDao;
	private ExamproblemDaoI examproblemDao;
	private InputStream fileInput;
	private String fileName;
	private String timeFrom, timeTo;
	private LogServiceI logService;
	private SolutionDaoI solutionDao;
	private RedisServiceI redisService;

	PMExam pMExam = new PMExam();

	@Override
	public PMExam getModel() {
		return pMExam;
	}

	public ExamServiceI getExamService() {
		return examService;
	}

	@Autowired
	public UserServiceI getUserService() {
		return userService;
	}

	public void setUserService(UserServiceI userService) {
		this.userService = userService;
	}

	@Autowired
	public void setExamService(ExamServiceI examService) {
		this.examService = examService;
	}

	// 返回的页面
	public String examList() {
		return "examList";
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getClassId() {
		return classId;
	}

	public void setExamId(int examId) {
		this.examId = examId;
	}

	public int getExamId() {
		return examId;
	}

	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}

	public int getAdminId() {
		return adminId;
	}

	public RedisServiceI getRedisService() {
		return redisService;
	}

	@Autowired
	public void setRedisService(RedisServiceI redisService) {
		this.redisService = redisService;
	}



	public ExamClassesDaoI getExamclassesDao() {
		return examclassesDao;
	}

	@Autowired
	public void setExamclassesDao(ExamClassesDaoI examclassesDao) {
		this.examclassesDao = examclassesDao;
	}
     
	public AdminusersDaoI getAdminusersDao() {
		return adminusersDao;
	}
	@Autowired
	public void setAdminusersDao(AdminusersDaoI adminusersDao) {
		this.adminusersDao = adminusersDao;
	}

	@Autowired
	public void setStudentexaminfoDao(StudentexaminfoDaoI studentexaminfoDao) {
		this.studentexaminfoDao = studentexaminfoDao;
	}

	public StudentexaminfoDaoI getStudentexaminfoDao() {
		return studentexaminfoDao;
	}

	@Autowired
	public void setUserDao(UserDaoI userDao) {
		this.userDao = userDao;
	}

	public UserDaoI getUserDao() {
		return userDao;
	}

	@Autowired
	public void setClassesDao(ClassesDaoI classesDao) {
		this.classesDao = classesDao;
	}

	public ClassesDaoI getClassesDao() {
		return classesDao;
	}

	@Autowired
	public void setClassstudentsDao(ClassstudentsDaoI classstudentsDao) {
		this.classstudentsDao = classstudentsDao;
	}

	public ClassstudentsDaoI getClassstudentsDao() {
		return classstudentsDao;
	}

	@Autowired
	public void setStudentexamdetailDao(StudentexamdetailDaoI studentexamdetailDao) {
		this.studentexamdetailDao = studentexamdetailDao;
	}

	public StudentexamdetailDaoI getStudentexamdetailDao() {
		return studentexamdetailDao;
	}

	@Autowired
	public void setExamproblemDao(ExamproblemDaoI examproblemDao) {
		this.examproblemDao = examproblemDao;
	}

	public ExamproblemDaoI getExamproblemDao() {
		return examproblemDao;
	}

	public InputStream getFileInput() {
		return fileInput;
	}

	public void setFileInput(InputStream fileInput) {
		this.fileInput = fileInput;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setTimeFrom(String timeFrom) {
		this.timeFrom = timeFrom;
	}

	public String getTimeFrom() {
		return timeFrom;
	}

	public void setTimeTo(String timeTo) {
		this.timeTo = timeTo;
	}

	public String getTimeTo() {
		return timeTo;
	}

	@Autowired
	public void setLogService(LogServiceI logService) {
		this.logService = logService;
	}

	public LogServiceI getLogService() {
		return logService;
	}

	@Autowired
	public void setSolutionDao(SolutionDaoI solutionDao) {
		this.solutionDao = solutionDao;
	}

	public SolutionDaoI getSolutionDao() {
		return solutionDao;
	}

	// 获取考试列表
	public void getExamList() {
		// 从session中获取登录的用户id
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		if (sessionInfo != null) {
			int id = sessionInfo.getUserId();
			this.pMExam.setStudentId(id);
		}

		// 返回前台的json数据
		Json j = new Json();

		// 获取分页数据
		DataGrid dataGrid = examService.dataGrid(pMExam);
		if (dataGrid != null) {
			j.setSuccess(true);
			j.setMsg("获取考试列表成功");
			j.setObj(dataGrid);
			logger.info("获取考试列表成功");
		} else {
			j.setSuccess(false);
			j.setMsg("获取考试列表失败");
			logger.info("获取考试列表失败");
		}
		super.writeJson(j);
	}

	// 根据examId获取本场考试的相关信息
	public void getExamById() {
		// 返回前台的json数据
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
		// 根据examID查询该场考试的信息,先从缓冲中获取该场考试的信息
		ExamCacheManager examCacheManager = ExamCacheManager.getInstance();
		Exam exam = (Exam) examCacheManager.getObject("theExamById" + pMExam.getId());
		if (exam == null) {
			exam = examService.getExamById(pMExam.getId());
			examCacheManager.putObject("theExamById" + pMExam.getId(), exam);
		}

		if (exam != null) {
			j.setSuccess(true);
			j.setMsg("获取本场考试信息成功");
			j.setObj(exam);
			logger.info("获取本场考试信息成功");
		} else {
			j.setSuccess(false);
			j.setMsg("获取本场考试信息失败");
			logger.info("获取本场考试信息失败");
		}
		super.writeJson(j);		
	}else{
		j.setSuccess(false);
		j.setMsg("请先登录!");
		super.writeJson(j);
	}
	}

	public void getExamsByClassId() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<Examclasses> examclasses = examclassesDao.getExamclassesByClassId(classId);
			List<PMExam> pexamList = new ArrayList<PMExam>();
			for (int i = 0; i < examclasses.size(); i++) {
				int examId = examclasses.get(i).getExamId();
				Exam exam = examService.getExamById(examId);
				PMExam pe = new PMExam();
				pe.setId(exam.getId());
				pe.setName(exam.getName());
				pe.setDescription(exam.getDescription());
				pe.setStarttime(exam.getStarttime());
				pe.setEndtime(exam.getEndtime());
				pe.setProblemNum(exam.getProblemNum());
				pe.setCanGetHint(exam.getCanGetHint());
				pe.setPartialScore(exam.getPartialScore());
				pe.setTeacherId(exam.getTeacherId());
				int teacherId = exam.getTeacherId();
				Adminusers teacher = adminusersDao.get(Adminusers.class, teacherId);
				pe.setTeacherName(teacher.getName());
				pe.setLanguage(exam.getLanguage());
				pexamList.add(pe);
			}
			j.setSuccess(true);
			j.setMsg("获取班级考试成功");
			j.setObj(pexamList);
			logger.info("获取班级考试成功");
			super.writeJson(j);

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamScoresByExamId() // 通过考试id获取所有成绩
	{
		// Map<String, Object> session =
		// ActionContext.getContext().getSession();
		// SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		// if (sessionInfo != null) {
		classId = 0;
		List<PMExamScore> pscoreList = getClassExamScoreExe();
		json.setSuccess(true);
		json.setMsg("获取考试成绩成功");
		json.setObj(pscoreList);
		logger.info("获取考试成绩成功");
		super.writeJson(json);
		/*
		 * } else { json.setSuccess(false); json.setMsg("请先登录!");
		 * super.writeJson(json); }
		 */
	}

	public void getExamScoresByExamId2() // 对考试题目数小于10的考试采用acm成绩的格式输出
	{
		// Map<String, Object> session =
		// ActionContext.getContext().getSession();
		// SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		// if (sessionInfo != null) {
		classId = 0;
		List<PMExamScore2> pscoreList = getClassExamScoreExe2();
		json.setSuccess(true);
		json.setMsg("获取考试成绩成功");
		json.setObj(pscoreList);
		logger.info("获取考试成绩成功");
		super.writeJson(json);
		/*
		 * } else { json.setSuccess(false); json.setMsg("请先登录!");
		 * super.writeJson(json); }
		 */

	}

	public void getClassScore() // 获取班级成绩
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		if (sessionInfo != null) {
			List<PMExamScore> pscoreList = getClassExamScoreExe();
			json.setSuccess(true);
			json.setMsg("获取班级成绩成功");
			json.setObj(pscoreList);
			logger.info("获取班级成绩成功");
			super.writeJson(json);
		} else {
			json.setSuccess(false);
			json.setMsg("请先登录!");
			super.writeJson(json);
		}
	}

	public void getClassScore2() // 使用acm的样式显示班级的成绩
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		if (sessionInfo != null) {
			try {

				List<PMExamScore2> pscoreList = getClassExamScoreExe2();
				json.setSuccess(true);
				json.setMsg("获取班级成绩成功");
				json.setObj(pscoreList);
				logger.info("获取班级成绩成功");
				super.writeJson(json);
			} catch (Exception e) {
				System.out.println(e.getMessage() + "\n" + e.getStackTrace());
			}

		} else {
			json.setSuccess(false);
			json.setMsg("请先登录!");
			super.writeJson(json);
		}
	}
	public void getStudentRank(){
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		if (sessionInfo != null) {
			try {		
           json=examService.getStudentRank(sessionInfo.getUserId(),examId);
           super.writeJson(json);
			} catch (Exception e) {
				System.out.println(e.getStackTrace());
			}

		} else {
			json.setSuccess(false);
			json.setMsg("请先登录!");
			super.writeJson(json);
		}
	}

	public List<PMExamScore> getClassExamScoreExe() // 获取班级的考试成绩列表
	{

		// 先从缓存中读取数据，如果缓存中有则从缓存中获取
		ExamScoreCacheManager cacheManage = ExamScoreCacheManager.getInstance(); // 获取缓存
		List<PMExamScore> pscoreList = (List<PMExamScore>) cacheManage.getObject(classId + "_" + examId);
		if (pscoreList != null) // 获取缓存得到了对象
		{
			return pscoreList;
		}

		// 缓存中没有PMExamScore数据
		// 1、获取学生考试信息：主要包括一场考试中的
		// a.AC题目
		// b.提交数
		// c.分数
		// d.排名分数
		// e.提交时间
		// f. 总共的花费时间
		StudentexaminfoCacheManager infoManager = StudentexaminfoCacheManager.getInstance();
		List<Studentexaminfo> scoreList = (List<Studentexaminfo>) infoManager.getObject(classId + "_" + examId);
		if (scoreList == null) {
			scoreList = studentexaminfoDao.getClassExamScore(examId, classId); // 所有info
			infoManager.putObject(classId + "_" + examId, scoreList); // 存入cache
		}

		// 2、获取学生考试详情，主要信息为
		// a. 题目的id
		// b. 提交的第多少次
		// c. 题目的批改状态
		// d. 题目分数所得分数
		// e. 花费的时间

		StudentexamdetailCacheManager detailManager = StudentexamdetailCacheManager.getInstance();
		List<Studentexamdetail> detailList = (List<Studentexamdetail>) detailManager.getObject(classId + "_" + examId);
		if (detailList == null) {
			detailList = studentexamdetailDao.getClassStudentexamdetail(examId, classId); // 所有detail信息
			detailManager.putObject(classId + "_" + examId, detailList);
		}

		// 3、 考试题目信息
		// a.考试的id
		// b.题目的id
		// c.题目的分数

		ExamproblemsCacheManager examproblemsManager = ExamproblemsCacheManager.getInstance();
		List<Examproblems> displaySequnceList = (List<Examproblems>) examproblemsManager.getObject(examId + "");
		if (displaySequnceList == null) {
			displaySequnceList = examproblemDao.getDisplaySequenceByExamId(examId); // 所有dispalysequence
			examproblemsManager.putObject(examId + "", displaySequnceList);
		}
		HashMap displaySequnceMap = new HashMap();
		for (int i = 0; i < displaySequnceList.size(); i++) {
			int problemId = displaySequnceList.get(i).getProblemId();// 获取problemId
			int displaySequnce = displaySequnceList.get(i).getDisplaySequence(); // 获取displaySequnce
			displaySequnceMap.put(problemId, displaySequnce);
		}

		// 用户信息
		UsersCacheManager userManager = UsersCacheManager.getInstance();
		List<Users> userList = (List<Users>) userManager.getObject("exam_" + examId);
		if (userList == null) {
			userList = userDao.getUsersByExamId(examId);
			userManager.putObject("exam_" + examId, userList);
		}
		HashMap userMap = new HashMap();
		for (int i = 0; i < userList.size(); i++) {
			int userId = userList.get(i).getId();
			userMap.put(userId, userList.get(i));
		}
		// 班级id和学生id对应关系
		ClassstudentsCacheManager studentsManager = ClassstudentsCacheManager.getInstance();
		List<Classstudents> classstudentList = (List<Classstudents>) studentsManager.getObject("exam_" + examId);
		if (classstudentList == null) {
			classstudentList = classstudentsDao.getClassStudentsByExamId(examId);
			studentsManager.putObject("exam_" + examId, classstudentList);
		}
		HashMap classstudentMap = new HashMap();
		for (int m = 0; m < classstudentList.size(); m++) {
			Classstudents student = classstudentList.get(m);
			classstudentMap.put(student.getUserId(), student.getClassId()); // 建立学生与班级的hash表
		}

		// 考试和班级对应关系
		ClassesCacheManager classesManager = ClassesCacheManager.getInstance();
		List<PMClasses> classes = (List<PMClasses>) classesManager.getObject("exam_" + examId);
		if (classes == null) {
			classes = classesDao.findClassInExam(examId);
			classesManager.putObject("exam_" + examId, classes);
		}
		HashMap classtMap = new HashMap(); // 建立班级的hash表
		for (int m = 0; m < classes.size(); m++) {
			int classId = classes.get(m).getId();
			classtMap.put(classId, classes.get(m).getName());
		}
		pscoreList = new ArrayList<PMExamScore>();
		Studentexaminfo score = null;
		Studentexamdetail detail = null;
		List<String> submited = null; // 已提交
		List<String> doing = null; // 正在做
		List<String> undo = null; // 未做
		int done[] = null; // 已提交的和正在做的
		int userId = 0;
		int submitNum = 0; // 提交数
		PMExamScore pscore = null;
		if (detailList != null) {
			// i控制scoreList的遍历、j控制detailList的遍历
			int i = 0, j = 0;
			while (true) {
				// 如果scoreList和detailList都遍历完成这退出
				if (i >= scoreList.size() && j >= detailList.size())
					break;
				// 如果scoreList未遍历完成则获取score,否则设置score为空
				if (i < scoreList.size())
					score = scoreList.get(i);
				else
					score = null;
				// 如果过detailList未遍历完则获取detail
				if (j < detailList.size())
					detail = detailList.get(j);
				if (j < detailList.size()) {
					if (score == null || score.getUserId().intValue() > detail.getUserId().intValue()) // 存在多余的detail，则创建一个PMExamScore
					{
						pscore = new PMExamScore();
						pscore.setRank(0);
						pscore.setScore(0);
						pscore.setSolve(0);
						submitNum = 0;
						userId = detail.getUserId();
						Users user = (Users) userMap.get(userId);
						if (user != null) {
							pscore.setUserId(user.getId().intValue());
							pscore.setStudentNo(user.getStudentNo());
							pscore.setChineseName(user.getChineseName());
							if (classstudentMap.get(userId) != null) {
								int classId = (Integer) classstudentMap.get(userId);
								if (classtMap.get(classId) != null)
									pscore.setBanji((String) classtMap.get(classId));
							}

						}
						submited = new ArrayList<String>(); // 已提交
						doing = new ArrayList<String>(); // 正在做
						undo = new ArrayList<String>(); // 未做
						done = new int[displaySequnceMap.size()];
						for (int k = 0; k < displaySequnceMap.size(); k++)
							// 0表示不存在
							done[k] = 0;
					}
					if (score != null && score.getUserId().intValue() == detail.getUserId().intValue()) {
						i++;
						pscore = new PMExamScore();
						pscore.setRank(score.getRank());
						pscore.setScore(score.getScore());
						if (score.getSolved() != null)
							pscore.setSolve(score.getSolved());
						else
							pscore.setSolve(0);
						submitNum = 0;
						userId = score.getUserId();
						Users user = (Users) userMap.get(userId);
						if (user != null) {
							pscore.setUserId(user.getId().intValue());
							pscore.setStudentNo(user.getStudentNo());
							pscore.setChineseName(user.getChineseName());
							if (classstudentMap.get(userId) != null) {
								int classId = (Integer) classstudentMap.get(userId);
								if (classtMap.get(classId) != null)
									pscore.setBanji((String) classtMap.get(classId));
							}

						}
						submited = new ArrayList<String>(); // 已提交
						doing = new ArrayList<String>(); // 正在做
						undo = new ArrayList<String>(); // 未做
						done = new int[displaySequnceMap.size()];
						for (int k = 0; k < displaySequnceMap.size(); k++)
							// 0表示不存在
							done[k] = 0;
					}
					if (score != null && score.getUserId().intValue() < detail.getUserId().intValue()) {
						i++;
						pscore = new PMExamScore();
						pscore.setRank(score.getRank());
						pscore.setScore(score.getScore());
						if (score.getSolved() != null)
							pscore.setSolve(score.getSolved());
						else
							pscore.setSolve(0);
						submitNum = 0;
						userId = score.getUserId();
						Users user = (Users) userMap.get(userId);
						if (user != null) {
							pscore.setUserId(user.getId().intValue());
							pscore.setStudentNo(user.getStudentNo());
							pscore.setChineseName(user.getChineseName());
							if (classstudentMap.get(userId) != null) {
								int classId = (Integer) classstudentMap.get(userId);
								if (classtMap.get(classId) != null)
									pscore.setBanji((String) classtMap.get(classId));
							}
						}
						submited = new ArrayList<String>(); // 已提交
						doing = new ArrayList<String>(); // 正在做
						undo = new ArrayList<String>(); // 未做
						done = new int[displaySequnceMap.size()];
						for (int k = 0; k < displaySequnceMap.size(); k++)
							// 0表示不存在
							done[k] = 0;
					}
					while (true) {
						if (j >= detailList.size())
							break;
						detail = detailList.get(j);
						if (detail == null || detail.getUserId().intValue() > userId)
							break;
						else {
							j++;
							if (detail.isFinished() == true) {
								if (displaySequnceMap.get(detail.getProblemId()) != null) {
									int displaySequnce = (Integer) displaySequnceMap.get(detail.getProblemId());
									submited.add(String.valueOf(displaySequnce));
									done[displaySequnce - 1] = 1;
								}
							} else {
								if (displaySequnceMap.get(detail.getProblemId()) != null) {
									int displaySequnce = (Integer) displaySequnceMap.get(detail.getProblemId());
									doing.add(String.valueOf(displaySequnce));
									done[displaySequnce - 1] = 1;
								}
								submitNum++; // 提交但未成功的次数
							}
						}
					}
					for (int k = 0; k < displaySequnceMap.size(); k++) {
						if (done[k] == 0)
							undo.add(String.valueOf(k + 1)); // 添加未做的
					}
					pscore.setSubmited(sortDisplaysequence(submited));
					pscore.setDoing(sortDisplaysequence(doing));
					pscore.setUndo(sortDisplaysequence(undo));
					if (score != null)
						pscore.setSubmit(score.getSubmit() + submitNum);
					else
						pscore.setSubmit(submitNum);

					pscoreList.add(pscore);
				}
				if (i < scoreList.size() && j >= detailList.size()) {
					score = scoreList.get(i);
					i++;
					pscore = new PMExamScore();
					pscore.setRank(score.getRank());
					pscore.setScore(score.getScore());
					if (score.getSolved() != null)
						pscore.setSolve(score.getSolved());
					else
						pscore.setSolve(0);
					if (score.getSubmit() != null)
						pscore.setSubmit(score.getSubmit());
					else
						pscore.setSubmit(0);
					userId = score.getUserId();
					Users user = (Users) userMap.get(userId);
					if (user != null) {
						pscore.setUserId(user.getId());
						pscore.setStudentNo(user.getStudentNo());
						pscore.setChineseName(user.getChineseName());
						if (classstudentMap.get(userId) != null) {
							int classId = (Integer) classstudentMap.get(userId);
							if (classtMap.get(classId) != null)
								pscore.setBanji((String) classtMap.get(classId));
						}

					}
					undo = new ArrayList<String>(); // 未做
					for (int k = 0; k < displaySequnceMap.size(); k++)
						undo.add(String.valueOf(k + 1)); // 初始化
					pscore.setUndo(sortDisplaysequence(undo));
					pscoreList.add(pscore);
				}
			}
		}
		for (int i = 0; i < pscoreList.size() - 1; i++) // 按照score从大到小排序
		{
			int max = i;
			for (int j = i + 1; j < pscoreList.size(); j++) {
				PMExamScore score1 = pscoreList.get(max);
				PMExamScore score2 = pscoreList.get(j);
				if (score1.getScore() < score2.getScore()) // 使用选择排序
				{
					max = j;
				}

			}
			if (max != i) {
				PMExamScore score1 = pscoreList.get(max);
				PMExamScore score2 = pscoreList.get(i);
				PMExamScore temp = score1;
				pscoreList.set(max, score2);
				pscoreList.set(i, temp);
			}
		}
		for (int i = 0; i < pscoreList.size() - 1; i++) // 对于相同score，按照rank从小到大排序
		{
			int min = i;
			for (int j = i + 1; j < pscoreList.size(); j++) {
				PMExamScore score1 = pscoreList.get(min);
				PMExamScore score2 = pscoreList.get(j);
				if (score1.getScore() == score2.getScore()) // 使用选择排序
				{
					if (score1.getRank() > score2.getRank())
						min = j;
				} else
					break;

			}
			if (min != i) {
				PMExamScore score1 = pscoreList.get(min);
				PMExamScore score2 = pscoreList.get(i);
				PMExamScore temp = score1;
				pscoreList.set(min, score2);
				pscoreList.set(i, temp);
			}
		}
		cacheManage.putObject(classId + "_" + examId, pscoreList); // 将对象存入cache
		return pscoreList;
	}

	public List<PMExamScore2> getClassExamScoreExe2() // 使用acm的格式输出考试成绩
	{
		ExamScoreCacheManager cacheManage = ExamScoreCacheManager.getInstance(); // 获取缓存
		List<PMExamScore2> pscoreList = (List<PMExamScore2>) cacheManage.getObject("2_" + classId + "_" + examId);
		if (pscoreList != null) // 获取缓存得到了对象
		{
			return pscoreList;
		}
		StudentexaminfoCacheManager infoManager = StudentexaminfoCacheManager.getInstance();
		List<Studentexaminfo> scoreList = (List<Studentexaminfo>) infoManager.getObject("2_" + classId + "_" + examId);
		if (scoreList == null) {
			scoreList = studentexaminfoDao.getClassExamScore(examId, classId); // 所有info
			infoManager.putObject("2_" + classId + "_" + examId, scoreList); // 存入cache
		}
		StudentexamdetailCacheManager detailManager = StudentexamdetailCacheManager.getInstance();
		List<Studentexamdetail> detailList = (List<Studentexamdetail>) detailManager
				.getObject("2_" + classId + "_" + examId);
		if (detailList == null) {
			detailList = studentexamdetailDao.getClassStudentexamdetail(examId, classId); // 所有detail信息
			detailManager.putObject("2_" + classId + "_" + examId, detailList);
		}
		ExamproblemsCacheManager examproblemsManager = ExamproblemsCacheManager.getInstance();
		List<Examproblems> displaySequnceList = (List<Examproblems>) examproblemsManager.getObject("2_" + examId + "");
		if (displaySequnceList == null) {
			displaySequnceList = examproblemDao.getDisplaySequenceByExamId(examId); // 所有dispalysequence
			examproblemsManager.putObject("2_" + examId + "", displaySequnceList);
		}
		HashMap displaySequnceMap = new HashMap();
		int[] problemMinElapseTime = new int[displaySequnceList.size()]; // 建立数组记录每道题的最小elapsetime
		int[] firstSolve = new int[displaySequnceList.size()]; // 记录是哪个用户最先做出了那一道题
		for (int i = 0; i < displaySequnceList.size(); i++) {
			int problemId = displaySequnceList.get(i).getProblemId();// 获取problemId
			int displaySequnce = displaySequnceList.get(i).getDisplaySequence(); // 获取displaySequnce
			displaySequnceMap.put(problemId, displaySequnce);
			problemMinElapseTime[i] = -1;
			firstSolve[i] = -1; // 初始化两个数组
		}
		UsersCacheManager userManager = UsersCacheManager.getInstance();
		List<Users> userList = (List<Users>) userManager.getObject("2_" + "exam_" + examId);
		if (userList == null) {
			userList = userDao.getUsersByExamId(examId);
			userManager.putObject("2_" + "exam_" + examId, userList);
		}
		HashMap userMap = new HashMap();
		for (int i = 0; i < userList.size(); i++) {
			int userId = userList.get(i).getId();
			userMap.put(userId, userList.get(i));
		}
		ClassstudentsCacheManager studentsManager = ClassstudentsCacheManager.getInstance();
		List<Classstudents> classstudentList = (List<Classstudents>) studentsManager.getObject("2_" + "exam_" + examId);
		if (classstudentList == null) {
			classstudentList = classstudentsDao.getClassStudentsByExamId(examId);
			studentsManager.putObject("2_" + "exam_" + examId, classstudentList);
		}
		HashMap classstudentMap = new HashMap();
		for (int m = 0; m < classstudentList.size(); m++) {
			Classstudents student = classstudentList.get(m);
			classstudentMap.put(student.getUserId(), student.getClassId()); // 建立学生与班级的hash表
		}
		ClassesCacheManager classesManager = ClassesCacheManager.getInstance();
		List<PMClasses> classes = (List<PMClasses>) classesManager.getObject("2_" + "exam_" + examId);
		if (classes == null) {
			classes = classesDao.findClassInExam(examId);
			classesManager.putObject("2_" + "exam_" + examId, classes);
		}
		HashMap classtMap = new HashMap(); // 建立班级的hash表
		for (int m = 0; m < classes.size(); m++) {
			int classId = classes.get(m).getId();
			classtMap.put(classId, classes.get(m).getName());
		}
		ExamLastSolutionStatusCacheManager lastSolutionStatusManager = ExamLastSolutionStatusCacheManager.getInstance();
		List<Object[]> solutionStatusList = (List<Object[]>) lastSolutionStatusManager
				.getObject("2_" + "exam_" + examId); // 获取每个用户题目最后提交的状态
		if (solutionStatusList == null) {
			solutionStatusList = solutionDao.getExamLastSolutionStatus(examId);
			lastSolutionStatusManager.putObject("2_" + "exam_" + examId, solutionStatusList); // 存入缓存
		}
		HashMap solutionStatusMap = new HashMap();
		for (int m = 0; m < solutionStatusList.size(); m++) {
			Object[] obj = solutionStatusList.get(m);
			if (obj[0] != null && obj[1] != null)
				solutionStatusMap.put((int) obj[0], obj[1]);
		}
		pscoreList = new ArrayList<PMExamScore2>();
		Studentexaminfo score = null;
		Studentexamdetail detail = null;
		List<String> problemSubInfo = null; // 用户问题的提交情况
		List<String> problemStatus = null; // 每道题的状态
		List<String> problemScores = null; // 每道题的得分
		int done[] = null; // 已提交的和正在做的
		int userId = 0;
		int submitNum = 0; // 提交数
		PMExamScore2 pscore = null;
		if (detailList != null) {
			int i = 0, j = 0;
			while (true) {
				if (i >= scoreList.size() && j >= detailList.size())
					break;
				if (i < scoreList.size())
					score = scoreList.get(i);
				else
					score = null;
				if (j < detailList.size())
					detail = detailList.get(j);
				if (j < detailList.size()) {
					if (score == null || score.getUserId().intValue() > detail.getUserId().intValue()) // 存在多余的detail，则创建一个PMExamScore
					{
						pscore = new PMExamScore2();
						pscore.setRank(0);
						pscore.setScore(0);
						pscore.setSolve(0);
						submitNum = 0;
						userId = detail.getUserId();
						Users user = (Users) userMap.get(userId);
						if (user != null) {
							pscore.setUserId(user.getId().intValue());
							pscore.setStudentNo(user.getStudentNo());
							pscore.setChineseName(user.getChineseName());
							if (classstudentMap.get(userId) != null) {
								int classId = (Integer) classstudentMap.get(userId);
								if (classtMap.get(classId) != null)
									pscore.setBanji((String) classtMap.get(classId));
							}

						}
						problemSubInfo = new ArrayList<String>(); // 问题提交情况
						problemStatus = new ArrayList<String>(); // 每道题的提交状态
						problemScores = new ArrayList<String>(); // 每道题得分
						for (int k = 0; k < displaySequnceMap.size(); k++)// 建立题目数那么多个题目信息字符串
						{
							String str = new String();
							problemSubInfo.add(str);
							String str2 = new String();
							problemStatus.add(str2);
							String str3 = new String();
							problemScores.add(str3);
						}
						pscore.setProblemSubInfo(problemSubInfo);
					}
					if (score != null && score.getUserId().intValue() == detail.getUserId().intValue()) {
						i++;
						pscore = new PMExamScore2();
						pscore.setRank(score.getRank());
						pscore.setScore(score.getScore());
						if (score.getSolved() != null)
							pscore.setSolve(score.getSolved());
						else
							pscore.setSolve(0);
						submitNum = 0;
						userId = score.getUserId();
						Users user = (Users) userMap.get(userId);
						if (user != null) {
							pscore.setUserId(user.getId().intValue());
							pscore.setStudentNo(user.getStudentNo());
							pscore.setChineseName(user.getChineseName());
							if (classstudentMap.get(userId) != null) {
								int classId = (Integer) classstudentMap.get(userId);
								if (classtMap.get(classId) != null)
									pscore.setBanji((String) classtMap.get(classId));
							}

						}
						problemSubInfo = new ArrayList<String>(); // 问题提交情况
						problemStatus = new ArrayList<String>(); // 每道题的提交状态
						problemScores = new ArrayList<String>(); // 每道题得分
						for (int k = 0; k < displaySequnceMap.size(); k++)// 建立题目数那么多个题目信息字符串
						{
							String str = new String();
							problemSubInfo.add(str);
							String str2 = new String();
							problemStatus.add(str2);
							String str3 = new String();
							problemScores.add(str3);
						}
						pscore.setProblemSubInfo(problemSubInfo);
					}
					if (score != null && score.getUserId().intValue() < detail.getUserId().intValue()) {
						i++;
						pscore = new PMExamScore2();
						pscore.setRank(score.getRank());
						pscore.setScore(score.getScore());
						if (score.getSolved() != null)
							pscore.setSolve(score.getSolved());
						else
							pscore.setSolve(0);
						submitNum = 0;
						userId = score.getUserId();
						Users user = (Users) userMap.get(userId);
						if (user != null) {
							pscore.setUserId(user.getId().intValue());
							pscore.setStudentNo(user.getStudentNo());
							pscore.setChineseName(user.getChineseName());
							if (classstudentMap.get(userId) != null) {
								int classId = (Integer) classstudentMap.get(userId);
								if (classtMap.get(classId) != null)
									pscore.setBanji((String) classtMap.get(classId));
							}
						}
						problemSubInfo = new ArrayList<String>(); // 问题提交情况
						problemStatus = new ArrayList<String>(); // 每道题的提交状态
						problemScores = new ArrayList<String>(); // 每道题得分
						for (int k = 0; k < displaySequnceMap.size(); k++)// 建立题目数那么多个题目信息字符串
						{
							String str = new String();
							problemSubInfo.add(str);
							String str2 = new String();
							problemStatus.add(str2);
							String str3 = new String();
							problemScores.add(str3);
						}
						pscore.setProblemSubInfo(problemSubInfo);
					}
					while (true) {
						if (j >= detailList.size())
							break;
						detail = detailList.get(j);
						if (detail == null || detail.getUserId().intValue() > userId)
							break;
						else {
							j++;
							if (detail.isFinished() == true) {
								if (displaySequnceMap.get(detail.getProblemId()) != null) {
									int displaySequnce = (Integer) displaySequnceMap.get(detail.getProblemId());
									Integer solutionId = detail.getSolutionId();
									String status = null; // 状态
									status = detail.getStatus();
									if (status != null)
										problemStatus.set(displaySequnce - 1, status);
									String str = null;
									submitNum = detail.getSubmit().intValue(); // 该题目的提交数目
									int elapsetime = detail.getElapsedTime();
									if (status != null && status.equals("AC"))
										str = String.valueOf(submitNum) + "/" + String.valueOf(elapsetime) + ":" + "1"; // 1表示该题已经解决，得到的格式为1/21003:1
									else
										str = String.valueOf(submitNum) + "/" + String.valueOf(elapsetime) + ":" + "3";
									List<String> infoList = pscore.getProblemSubInfo();
									infoList.set(displaySequnce - 1, str);
									Float ps = detail.getScore();
									if (ps != null)
										problemScores.set(displaySequnce - 1, String.valueOf(ps.floatValue())); // 将每道题得分添加到对象中
									if (problemMinElapseTime[displaySequnce - 1] > 0
											&& elapsetime < problemMinElapseTime[displaySequnce - 1]
											|| problemMinElapseTime[displaySequnce - 1] < 0 && status != null
													&& status.equals("AC")) // 用过elapsetime大于0表示已经有人做出来了
									{ // 该用户更早做出来或者
										problemMinElapseTime[displaySequnce - 1] = elapsetime;
										firstSolve[displaySequnce - 1] = detail.getUserId(); // 并记录该用户为第一个做出来的
									}

								}
							} else {
								if (displaySequnceMap.get(detail.getProblemId()) != null) {
									int displaySequnce = (Integer) displaySequnceMap.get(detail.getProblemId());
									Integer solutionId = detail.getSolutionId();
									String status = null; // 状态
									status = detail.getStatus();
									if (status != null)
										problemStatus.set(displaySequnce - 1, status);
									String str = null;
									submitNum = detail.getSubmit().intValue(); // 该题目的提交数目
									int elapsetime = detail.getElapsedTime();
									if (status != null && status.equals("AC"))
										str = String.valueOf(submitNum) + "/" + String.valueOf(elapsetime) + ":" + "1"; // 1表示该题已经解决，得到的格式为1/21003:1
									else
										str = String.valueOf(submitNum) + "/" + String.valueOf(elapsetime) + ":" + "0";
									List<String> infoList = pscore.getProblemSubInfo();
									infoList.set(displaySequnce - 1, str);
									Float ps = detail.getScore();
									if (ps != null)
										problemScores.set(displaySequnce - 1, String.valueOf(ps.floatValue())); // 将每道题得分添加到对象中
								}
							}

						}
					}
					if (score != null)
						pscore.setSubmit(score.getSubmit() + submitNum);
					else
						pscore.setSubmit(submitNum);
					pscore.setProblemStatus(problemStatus); // 将状态添加到对象中
					pscore.setProblemScores(problemScores);
					pscoreList.add(pscore);
				}
				if (i < scoreList.size() && j >= detailList.size()) {
					score = scoreList.get(i);
					i++;
					pscore = new PMExamScore2();
					pscore.setRank(score.getRank());
					pscore.setScore(score.getScore());
					if (score.getSolved() != null)
						pscore.setSolve(score.getSolved());
					else
						pscore.setSolve(0);
					if (score.getSubmit() != null)
						pscore.setSubmit(score.getSubmit());
					else
						pscore.setSubmit(0);
					userId = score.getUserId();
					Users user = (Users) userMap.get(userId);
					if (user != null) {
						pscore.setUserId(user.getId().intValue());
						pscore.setStudentNo(user.getStudentNo());
						pscore.setChineseName(user.getChineseName());
						if (classstudentMap.get(userId) != null) {
							int classId = (Integer) classstudentMap.get(userId);
							if (classtMap.get(classId) != null)
								pscore.setBanji((String) classtMap.get(classId));
						}

					}
					problemSubInfo = new ArrayList<String>(); // 问题提交情况
					problemStatus = new ArrayList<String>(); // 每道题的提交状态
					problemScores = new ArrayList<String>(); // 每道题得分
					for (int k = 0; k < displaySequnceMap.size(); k++)// 建立题目数那么多个题目信息字符串
					{
						String str = new String();
						problemSubInfo.add(str);
						String str2 = new String();
						problemStatus.add(str2);
						String str3 = new String();
						problemScores.add(str3);
					}
					pscore.setProblemSubInfo(problemSubInfo);
					pscore.setProblemStatus(problemStatus);
					pscoreList.add(pscore);
				}
			}
		}
		for (int i = 0; i < pscoreList.size(); i++) // 改写信息，为第一个把题目做出来的将题目状态改为2
		{
			PMExamScore2 score1 = pscoreList.get(i);
			userId = score1.getUserId();
			problemSubInfo = score1.getProblemSubInfo(); // 获取每个用户做的题目状况
			for (int j = 0; j < problemSubInfo.size(); j++) {
				if (firstSolve[j] == userId) // 该题目为当前用户第一个做出来
				{
					String str = problemSubInfo.get(j);
					String s[] = str.split(":");
					String str2 = s[0] + ":" + "2"; // 将状态改为2
					problemSubInfo.set(j, str2);
				}
			}
		}
		for (int i = 0; i < pscoreList.size() - 1; i++) // 按照score从大到小排序
		{
			int max = i;
			for (int j = i + 1; j < pscoreList.size(); j++) {
				PMExamScore2 score1 = pscoreList.get(max);
				PMExamScore2 score2 = pscoreList.get(j);
				if (score1.getScore() < score2.getScore()) // 使用选择排序
				{
					max = j;
				}

			}
			if (max != i) {
				PMExamScore2 score1 = pscoreList.get(max);
				PMExamScore2 score2 = pscoreList.get(i);
				PMExamScore2 temp = score1;
				pscoreList.set(max, score2);
				pscoreList.set(i, temp);
			}
		}
		for (int i = 0; i < pscoreList.size() - 1; i++) // 对于相同score，按照rank从小到大排序
		{
			int min = i;
			for (int j = i + 1; j < pscoreList.size(); j++) {
				PMExamScore2 score1 = pscoreList.get(min);
				PMExamScore2 score2 = pscoreList.get(j);
				if (score1.getScore() == score2.getScore()) // 使用选择排序
				{
					if (score1.getRank() > score2.getRank())
						min = j;
				} else
					break;

			}
			if (min != i) {
				PMExamScore2 score1 = pscoreList.get(min);
				PMExamScore2 score2 = pscoreList.get(i);
				PMExamScore2 temp = score1;
				pscoreList.set(min, score2);
				pscoreList.set(i, temp);
			}
		}
		cacheManage.putObject("2_" + classId + "_" + examId, pscoreList); // 将对象存入cache
		return pscoreList;
	}

	// 导出考试成绩到excel文件中
	public String exportExamScoresExcel() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		if (sessionInfo != null) {
			// classId = 0; // 查询全部班级时，classId为0，单独的班级查询则大于0
			List<PMExamScore> pscoreList = getClassExamScoreExe();
			HSSFWorkbook wb = new HSSFWorkbook(); // 导出到excel文件中
			HSSFSheet sheet = wb.createSheet("表一");
			HSSFRow row = sheet.createRow(0);
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			HSSFCell cell = row.createCell((short) 0);
			cell.setCellValue("序号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 1);
			cell.setCellValue("学号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 2);
			cell.setCellValue("姓名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 3);
			cell.setCellValue("班级");
			cell.setCellStyle(style);
			cell = row.createCell((short) 4);
			cell.setCellValue("分数");
			cell.setCellStyle(style);
			cell = row.createCell((short) 5);
			cell.setCellValue("解题数");
			cell.setCellStyle(style);
			cell = row.createCell((short) 6);
			cell.setCellValue("提交数");
			cell.setCellStyle(style);
			cell = row.createCell((short) 7);
			cell.setCellValue("已提交");
			cell.setCellStyle(style);
			cell = row.createCell((short) 8);
			cell.setCellValue("正在做");
			cell.setCellStyle(style);
			cell = row.createCell((short) 9);
			cell.setCellValue("未做");
			cell.setCellStyle(style);
			for (int i = 0; i < pscoreList.size(); i++) {
				row = sheet.createRow(i + 1);
				row.createCell((short) 0).setCellValue((i + 1));
				PMExamScore examscore = pscoreList.get(i);
				row.createCell((short) 1).setCellValue(examscore.getStudentNo());
				row.createCell((short) 2).setCellValue(examscore.getChineseName());
				row.createCell((short) 3).setCellValue(examscore.getBanji());
				row.createCell((short) 4).setCellValue(examscore.getScore());
				row.createCell((short) 5).setCellValue(examscore.getSolve());
				row.createCell((short) 6).setCellValue(examscore.getSubmit());
				List<String> submited = examscore.getSubmited();
				String temp = "";
				if (submited.size() > 0)
					temp = submited.get(0);
				row.createCell((short) 7).setCellValue(temp);
				List<String> doing = examscore.getDoing();
				temp = "";
				if (doing.size() > 0)
					temp = doing.get(0);
				row.createCell((short) 8).setCellValue(temp);
				List<String> undo = examscore.getUndo();
				temp = "";
				if (undo.size() > 0)
					temp = undo.get(0);
				row.createCell((short) 9).setCellValue(temp);
			}
			try {
				String userId2 = String.valueOf(sessionInfo.getTeacherId());
				File dir = new File("C:\\OJtemp\\" + userId2 + "\\");
				if (!dir.exists() && !dir.isDirectory()) {
					dir.mkdirs(); // 创建用户目录
				}
				fileName = "exportExamScores.xls";
				FileOutputStream fout = new FileOutputStream("C:\\OJtemp\\" + userId2 + "\\" + fileName);
				wb.write(fout);
				fout.close();
				File file = new File("C:\\OJtemp\\" + userId2 + "\\" + fileName);
				fileInput = new FileInputStream(file);
			} catch (Exception e) {
				logService.WriteLog("系统异常", "导出考试成绩到excel文件出错\n参数:\nexamId:[" + examId + "]", e.toString()); // 写入日志
			}
			return "exportExamScores";
		} else {
			json.setSuccess(false);
			json.setMsg("请先登录!");
			super.writeJson(json);
			try {
				fileInput.close();
			} catch (Exception e) {
				logService.WriteLog("系统异常", "导出考试成绩到excel文件出错\n参数:\nexamId:[" + examId + "]", e.toString()); // 写入日志
			}
			return null;
		}

	}

	public String exportExamScoresExcel2() { // 将acm格式的成绩样式导出到excel
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		if (sessionInfo != null) {
			int cid = classId; // 查询全部班级时，classId为0，单独的班级查询则大于0
			List<PMExamScore2> pscoreList = getClassExamScoreExe2();
			int problemNum = examproblemDao.getExamproblemNum(examId); // 题目数量
			Exam exam = examService.getExamById(examId);
			HSSFWorkbook wb = new HSSFWorkbook(); // 导出到excel文件中
			HSSFSheet sheet = wb.createSheet("表一");
			HSSFRow row = sheet.createRow(0);
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			HSSFCellStyle firstsolveStyle = null; // 第一个解决样式
			firstsolveStyle = wb.createCellStyle(); // 题目做错是的style
			firstsolveStyle.setAlignment(CellStyle.ALIGN_CENTER);
			firstsolveStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			firstsolveStyle.setWrapText(true);
			firstsolveStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
			HSSFCellStyle solveStyle = null; // 已解决的样式
			solveStyle = wb.createCellStyle(); // 题目做错是的style
			solveStyle.setAlignment(CellStyle.ALIGN_CENTER);
			solveStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			solveStyle.setWrapText(true);
			solveStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
			HSSFCellStyle undoneStyle = null; // 未解决的样式
			undoneStyle = wb.createCellStyle(); // 题目做错是的style
			undoneStyle.setAlignment(CellStyle.ALIGN_CENTER);
			undoneStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			undoneStyle.setWrapText(true);
			undoneStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
			HSSFCellStyle submitUnsolvedStyle = null;
			submitUnsolvedStyle = wb.createCellStyle(); // 题目做错是的style
			submitUnsolvedStyle.setAlignment(CellStyle.ALIGN_CENTER);
			submitUnsolvedStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			submitUnsolvedStyle.setWrapText(true);
			submitUnsolvedStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			HSSFCell cell = row.createCell((short) 0);
			cell.setCellValue("排名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 1);
			cell.setCellValue("学号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 2);
			cell.setCellValue("姓名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 3);
			cell.setCellValue("班级");
			cell.setCellStyle(style);
			cell = row.createCell((short) 4);
			cell.setCellValue("得分");
			cell.setCellStyle(style);
			cell = row.createCell((short) 5);
			cell.setCellValue("解题数");
			cell.setCellStyle(style);
			int i;
			for (i = 0; i < problemNum; i++) {
				cell = row.createCell((short) (6 + i));
				cell.setCellValue(String.valueOf(i + 1));
				cell.setCellStyle(style);
			}
			cell = row.createCell((short) (6 + i));
			cell.setCellValue("提交数/解题数");
			cell.setCellStyle(style);
			int[] totalSubmit = new int[problemNum]; // 记录每道题的提交数
			int[] totalSolved = new int[problemNum]; // 记录每道题的解题数
			for (i = 0; i < problemNum; i++) {
				totalSubmit[i] = 0;
				totalSolved[i] = 0; // 初始化
			}
			for (i = 0; i < pscoreList.size(); i++) {
				row = sheet.createRow(i + 1);
				cell = row.createCell((short) 0);
				cell.setCellValue((i + 1));
				cell.setCellStyle(style);
				PMExamScore2 examscore = pscoreList.get(i);
				cell = row.createCell((short) 1);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getStudentNo());
				cell.setCellStyle(style);
				cell = row.createCell((short) 2);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getChineseName());
				cell = row.createCell((short) 3);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getBanji());
				cell = row.createCell((short) 4);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getScore());
				cell = row.createCell((short) 5);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getSolve());
				int j = 0;
				int submit = 0;
				List<String> problemScores = examscore.getProblemScores();
				List<String> problemStatus = examscore.getProblemStatus();
				for (j = 0; j < problemNum; j++) {

					String str = examscore.getProblemSubInfo().get(j);// 对提交情况进行解析
					if (str.equals("") == false) {
						String[] s = str.split(":");
						if (Integer.parseInt(s[1]) == 1 || Integer.parseInt(s[1]) == 2) // 总的解题数加1
						{
							totalSolved[j]++;
						}
						String[] temp = s[0].split("/");
						String prefix = problemStatus.get(j) + "/" + problemScores.get(j) + "/" + temp[0];
						submit = submit + Integer.parseInt(temp[0]); // 将提交数加起来
						totalSubmit[j] += Integer.parseInt(temp[0]); // 提交数加上
						cell = row.createCell((short) (6 + j));
						Date stime = exam.getStarttime();
						Date etime = exam.getEndtime();
						if (stime.getDate() == etime.getDate() && stime.getMonth() == etime.getMonth()) // 如果考试开始时间和结束时间在同一天，则显示时和分
						{
							int time = Integer.parseInt(temp[1]);
							int hour = time / (60 * 60);
							time = time - hour * 60 * 60;
							int minute = time / 60;
							temp[1] = hour + ":" + minute;
						} else {
							int time = Integer.parseInt(temp[1]);
							int day = time / (24 * 60 * 60);
							time = time - day * 24 * 60 * 60;
							int hour = time / (60 * 60);
							time = time - hour * 60 * 60;
							int minute = time / 60;
							temp[1] = day + "天" + hour + ":" + minute;
						}
						cell.setCellValue(prefix + "\r\n" + temp[1]);
						if (Integer.parseInt(s[1]) == 0) {
							cell.setCellStyle(undoneStyle);
						}
						if (Integer.parseInt(s[1]) == 1) {
							cell.setCellStyle(solveStyle);
						}
						if (Integer.parseInt(s[1]) == 2) {
							cell.setCellStyle(firstsolveStyle);
						}
						if (Integer.parseInt(s[1]) == 3) {
							cell.setCellStyle(submitUnsolvedStyle);
						}
					} else // 为空表示用户未做过该题
					{
						cell = row.createCell((short) (6 + j));
						style.setWrapText(true);
						cell.setCellValue("--/--\r\n--/--");
						cell.setCellStyle(style);
					}
				}
				cell = row.createCell((short) (6 + j));
				cell.setCellValue(submit + "/" + examscore.getSolve());
				cell.setCellStyle(style);
			}
			row = sheet.createRow(i + 1);
			cell = row.createCell((short) (0));
			cell.setCellValue("总计    提交数/解题数");
			cell.setCellStyle(style);
			int tsubmit = 0;
			int tsolved = 0; // 所有题目的提交数和解题数
			int j;
			for (j = 0; j < problemNum; j++) {
				tsubmit += totalSubmit[j];
				tsolved += totalSolved[j];
				cell = row.createCell((short) (j + 6));
				cell.setCellValue(totalSubmit[j] + "/" + totalSolved[j]);
				cell.setCellStyle(style);
			} // 生成最后一行
			sheet.addMergedRegion(new Region(pscoreList.size() + 1, (short) 0, pscoreList.size() + 1, (short) 4)); // 合并单元格
			cell = row.createCell((short) (j + 6));
			cell.setCellValue(tsubmit + "/" + tsolved);
			cell.setCellStyle(style);
			row = sheet.createRow(i + 2);
			cell = row.createCell((short) (0));
			cell.setCellValue("第一个解决");
			cell.setCellStyle(firstsolveStyle);
			cell = row.createCell((short) (1));
			cell.setCellValue("已解决");
			cell.setCellStyle(solveStyle);
			cell = row.createCell((short) (2));
			cell.setCellValue("提交但未解决");
			cell.setCellStyle(submitUnsolvedStyle);
			cell = row.createCell((short) (3));
			cell.setCellValue("正在做");
			cell.setCellStyle(undoneStyle);
			try {
				String userId2 = String.valueOf(sessionInfo.getTeacherId());
				File dir = new File("C:\\OJtemp\\" + userId2 + "\\");
				if (!dir.exists() && !dir.isDirectory()) {
					dir.mkdirs(); // 创建用户目录
				}
				fileName = "exportExamScores2.xls";
				FileOutputStream fout = new FileOutputStream("C:\\OJtemp\\" + userId2 + "\\" + fileName);
				wb.write(fout);
				fout.close();
				File file = new File("C:\\OJtemp\\" + userId2 + "\\" + fileName);
				fileInput = new FileInputStream(file);
			} catch (Exception e) {

			}
			return "exportExamScores2";
		} else {
			json.setSuccess(false);
			json.setMsg("请先登录!");
			super.writeJson(json);
			try {
				fileInput.close();
			} catch (Exception e) {
				logService.WriteLog("系统异常", "导出考试成绩到excel文件出错\n参数:\nexamId:[" + examId + "]", e.toString()); // 写入日志
			}
			return null;
		}

	}

	public String exportClassExamScoresExcel() // 导出本班成绩到excel
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		if (sessionInfo != null) {
			List<PMExamScore> pscoreList = getClassExamScoreExe();
			HSSFWorkbook wb = new HSSFWorkbook(); // 导出到excel文件中
			HSSFSheet sheet = wb.createSheet("表一");
			HSSFRow row = sheet.createRow(0);
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			HSSFCell cell = row.createCell((short) 0);
			cell.setCellValue("序号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 1);
			cell.setCellValue("学号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 2);
			cell.setCellValue("姓名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 3);
			cell.setCellValue("班级");
			cell.setCellStyle(style);
			cell = row.createCell((short) 4);
			cell.setCellValue("分数");
			cell.setCellStyle(style);
			cell = row.createCell((short) 5);
			cell.setCellValue("解题数");
			cell.setCellStyle(style);
			cell = row.createCell((short) 6);
			cell.setCellValue("提交数");
			cell.setCellStyle(style);
			cell = row.createCell((short) 7);
			cell.setCellValue("已提交");
			cell.setCellStyle(style);
			cell = row.createCell((short) 8);
			cell.setCellValue("正在做");
			cell.setCellStyle(style);
			cell = row.createCell((short) 9);
			cell.setCellValue("未做");
			cell.setCellStyle(style);
			for (int i = 0; i < pscoreList.size(); i++) {
				row = sheet.createRow(i + 1);
				row.createCell((short) 0).setCellValue((i + 1));
				PMExamScore examscore = pscoreList.get(i);
				row.createCell((short) 1).setCellValue(examscore.getStudentNo());
				row.createCell((short) 2).setCellValue(examscore.getChineseName());
				row.createCell((short) 3).setCellValue(examscore.getBanji());
				row.createCell((short) 4).setCellValue(examscore.getScore());
				row.createCell((short) 5).setCellValue(examscore.getSolve());
				row.createCell((short) 6).setCellValue(examscore.getSubmit());
				List<String> submited = examscore.getSubmited();
				String temp = "";
				if (submited.size() > 0)
					temp = submited.get(0);
				row.createCell((short) 7).setCellValue(temp);
				List<String> doing = examscore.getDoing();
				temp = "";
				if (doing.size() > 0)
					temp = doing.get(0);
				row.createCell((short) 8).setCellValue(temp);
				List<String> undo = examscore.getUndo();
				temp = "";
				if (undo.size() > 0)
					temp = undo.get(0);
				row.createCell((short) 9).setCellValue(temp);
			}
			try {
				String userId2 = String.valueOf(sessionInfo.getTeacherId());
				File dir = new File("C:\\OJtemp\\" + userId2 + "\\");
				if (!dir.exists() && !dir.isDirectory()) {
					dir.mkdirs(); // 创建用户目录
				}
				fileName = "exportClassExamScoresExcel.xls";
				FileOutputStream fout = new FileOutputStream("C:\\OJtemp\\" + userId2 + "\\" + fileName);
				wb.write(fout);
				fout.close();
				File file = new File("C:\\OJtemp\\" + userId2 + "\\" + fileName);
				fileInput = new FileInputStream(file);
			} catch (Exception e) {

				logService.WriteLog("系统异常",
						"导出班级考试考试成绩到excel文件出错\n参数:\nexamId:[" + examId + "]\nclassId:[" + classId + "]", e.toString()); // 写入日志
			}
			return "exportClassExamScoresExcel";
		} else {
			json.setSuccess(false);
			json.setMsg("请先登录!");
			super.writeJson(json);
			try {
				fileInput.close();
			} catch (Exception e) {
				logService.WriteLog("系统异常",
						"导出班级考试考试成绩到excel文件出错\n参数:\nexamId:[" + examId + "]\nclassId:[" + classId + "]", e.toString()); // 写入日志
			}
			return null;
		}

	}

	public String exportClassExamScoresExcel2() // 使用acm的格式导出本班成绩到excel
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		if (sessionInfo != null) {
			List<PMExamScore2> pscoreList = getClassExamScoreExe2();
			int problemNum = examproblemDao.getExamproblemNum(examId); // 题目数量
			Exam exam = examService.getExamById(examId);
			HSSFWorkbook wb = new HSSFWorkbook(); // 导出到excel文件中
			HSSFSheet sheet = wb.createSheet("表一");
			HSSFRow row = sheet.createRow(0);
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			HSSFCellStyle firstsolveStyle = null; // 第一个解决样式
			firstsolveStyle = wb.createCellStyle(); // 题目做错是的style
			firstsolveStyle.setAlignment(CellStyle.ALIGN_CENTER);
			firstsolveStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			firstsolveStyle.setWrapText(true);
			firstsolveStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
			HSSFCellStyle solveStyle = null; // 已解决的样式
			solveStyle = wb.createCellStyle(); // 题目做错是的style
			solveStyle.setAlignment(CellStyle.ALIGN_CENTER);
			solveStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			solveStyle.setWrapText(true);
			solveStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
			HSSFCellStyle undoneStyle = null; // 未解决的样式
			undoneStyle = wb.createCellStyle(); // 题目做错是的style
			undoneStyle.setAlignment(CellStyle.ALIGN_CENTER);
			undoneStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			undoneStyle.setWrapText(true);
			undoneStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
			HSSFCellStyle submitUnsolvedStyle = null;
			submitUnsolvedStyle = wb.createCellStyle(); // 题目做错是的style
			submitUnsolvedStyle.setAlignment(CellStyle.ALIGN_CENTER);
			submitUnsolvedStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			submitUnsolvedStyle.setWrapText(true);
			submitUnsolvedStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			HSSFCell cell = row.createCell((short) 0);
			cell.setCellValue("排名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 1);
			cell.setCellValue("学号");
			cell.setCellStyle(style);
			cell = row.createCell((short) 2);
			cell.setCellValue("姓名");
			cell.setCellStyle(style);
			cell = row.createCell((short) 3);
			cell.setCellValue("班级");
			cell.setCellStyle(style);
			cell = row.createCell((short) 4);
			cell.setCellValue("分数");
			cell.setCellStyle(style);
			cell = row.createCell((short) 5);
			cell.setCellValue("解题数");
			cell.setCellStyle(style);
			int i;
			for (i = 0; i < problemNum; i++) {
				cell = row.createCell((short) (6 + i));
				cell.setCellValue(String.valueOf(i + 1));
				cell.setCellStyle(style);
			}
			cell = row.createCell((short) (6 + i));
			cell.setCellValue("提交数/解题数");
			cell.setCellStyle(style);
			int[] totalSubmit = new int[problemNum]; // 记录每道题的提交数
			int[] totalSolved = new int[problemNum]; // 记录每道题的解题数
			for (i = 0; i < problemNum; i++) {
				totalSubmit[i] = 0;
				totalSolved[i] = 0; // 初始化
			}
			for (i = 0; i < pscoreList.size(); i++) {
				row = sheet.createRow(i + 1);
				cell = row.createCell((short) 0);
				cell.setCellValue((i + 1));
				cell.setCellStyle(style);
				PMExamScore2 examscore = pscoreList.get(i);
				cell = row.createCell((short) 1);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getStudentNo());
				cell.setCellStyle(style);
				cell = row.createCell((short) 2);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getChineseName());
				cell = row.createCell((short) 3);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getBanji());
				cell = row.createCell((short) 4);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getScore());
				cell = row.createCell((short) 5);
				cell.setCellStyle(style);
				cell.setCellValue(examscore.getSolve());
				int j = 0;
				int submit = 0;
				List<String> problemScores = examscore.getProblemScores();
				List<String> problemStatus = examscore.getProblemStatus();
				for (j = 0; j < problemNum; j++) {
					String str = examscore.getProblemSubInfo().get(j);// 对提交情况进行解析
					if (str.equals("") == false) {
						String[] s = str.split(":");
						if (Integer.parseInt(s[1]) == 1 || Integer.parseInt(s[1]) == 2) // 总的解题数加1
						{
							totalSolved[j]++;
						}
						String[] temp = s[0].split("/");
						String prefix = problemStatus.get(j) + "/" + problemScores.get(j) + "/" + temp[0];
						submit = submit + Integer.parseInt(temp[0]); // 将提交数加起来
						totalSubmit[j] += Integer.parseInt(temp[0]); // 提交数加上
						cell = row.createCell((short) (6 + j));
						Date stime = exam.getStarttime();
						Date etime = exam.getEndtime();
						if (stime.getDate() == etime.getDate() && stime.getMonth() == etime.getMonth()) // 如果考试开始时间和结束时间在同一天，则显示时和分
						{
							int time = Integer.parseInt(temp[1]);
							int hour = time / (60 * 60);
							time = time - hour * 60 * 60;
							int minute = time / 60;
							temp[1] = hour + ":" + minute;
						} else {
							int time = Integer.parseInt(temp[1]);
							int day = time / (24 * 60 * 60);
							time = time - day * 24 * 60 * 60;
							int hour = time / (60 * 60);
							time = time - hour * 60 * 60;
							int minute = time / 60;
							temp[1] = day + "天" + hour + ":" + minute;
						}
						cell.setCellValue(prefix + "\r\n" + temp[1]);
						if (Integer.parseInt(s[1]) == 0) {
							cell.setCellStyle(undoneStyle);
						}
						if (Integer.parseInt(s[1]) == 1) {
							cell.setCellStyle(solveStyle);
						}
						if (Integer.parseInt(s[1]) == 2) {
							cell.setCellStyle(firstsolveStyle);
						}
						if (Integer.parseInt(s[1]) == 3) {
							cell.setCellStyle(submitUnsolvedStyle);
						}
					} else // 为空表示用户未做过该题
					{
						cell = row.createCell((short) (6 + j));
						cell.setCellValue("--/--\r\n--/--");
						style.setWrapText(true);
						cell.setCellStyle(style);
					}
				}
				cell = row.createCell((short) (6 + j));
				cell.setCellValue(submit + "/" + examscore.getSolve());
				cell.setCellStyle(style);
			}
			row = sheet.createRow(i + 1);
			cell = row.createCell((short) (0));
			cell.setCellValue("总计    提交数/解题数");
			cell.setCellStyle(style);
			int tsubmit = 0;
			int tsolved = 0; // 所有题目的提交数和解题数
			int j;
			for (j = 0; j < problemNum; j++) {
				tsubmit += totalSubmit[j];
				tsolved += totalSolved[j];
				cell = row.createCell((short) (j + 6));
				cell.setCellValue(totalSubmit[j] + "/" + totalSolved[j]);
				cell.setCellStyle(style);
			} // 生成最后一行
			sheet.addMergedRegion(new Region(pscoreList.size() + 1, (short) 0, pscoreList.size() + 1, (short) 4)); // 合并单元格
			cell = row.createCell((short) (j + 6));
			cell.setCellValue(tsubmit + "/" + tsolved);
			cell.setCellStyle(style);
			row = sheet.createRow(i + 2);
			cell = row.createCell((short) (0));
			cell.setCellValue("第一个解决");
			cell.setCellStyle(firstsolveStyle);
			cell = row.createCell((short) (1));
			cell.setCellValue("已解决");
			cell.setCellStyle(solveStyle);
			cell = row.createCell((short) (2));
			cell.setCellValue("提交但未解决");
			cell.setCellStyle(submitUnsolvedStyle);
			cell = row.createCell((short) (3));
			cell.setCellValue("正在做");
			cell.setCellStyle(undoneStyle);
			try {
				String userId2 = String.valueOf(sessionInfo.getTeacherId());
				File dir = new File("C:\\OJtemp\\" + userId2 + "\\");
				if (!dir.exists() && !dir.isDirectory()) {
					dir.mkdirs(); // 创建用户目录
				}
				fileName = "exportClassExamScoresExcel2.xls";
				FileOutputStream fout = new FileOutputStream("C:\\OJtemp\\" + userId2 + "\\" + fileName);
				wb.write(fout);
				fout.close();
				File file = new File("C:\\OJtemp\\" + userId2 + "\\" + fileName);
				fileInput = new FileInputStream(file);
			} catch (Exception e) {

			}
			return "exportClassExamScoresExcel2";
		} else {
			json.setSuccess(false);
			json.setMsg("请先登录!");
			super.writeJson(json);
			try {
				fileInput.close();
			} catch (Exception e) {
				logService.WriteLog("系统异常",
						"导出班级考试考试成绩到excel文件出错\n参数:\nexamId:[" + examId + "]\nclassId:[" + classId + "]", e.toString()); // 写入日志
			}
			return null;
		}

	}

	public void getAllExamsOrderByEndtime() // 获取所有的考试信息
	{
		int teacherId = pMExam.getTeacherId();
		Integer schoolId = pMExam.getSchoolId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");

		Json j = new Json();
		if (sessionInfo != null) {
			List<PMExam> examList = new ArrayList<PMExam>();
			String roleName = sessionInfo.getRoleNames();
			if (schoolId == 0 && teacherId != 0) {
				examList = examService.getAllExamsOrderByEndTime(teacherId, roleName);
			} else if (schoolId == 0 && teacherId == 0) {
				examList = examService.getAllExamsOrderByEndTime(teacherId, roleName);
			} else {
				examList = examService.getAllExamsOrderByEndTime(pMExam, roleName);
			}
			if (examList != null) {
				j.setSuccess(true);
				j.setMsg("获取所有考试成功");
				j.setObj(examList);
				logger.info("获取所有考试成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("获取所有考试失败");
				logger.info("获取所有考试失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getTeachersExam() // 获取教师的考试
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");

		Json j = new Json();
		if (sessionInfo != null) {
			String roleName = sessionInfo.getRoleNames();
			List<PMExam> examList = examService.getAllExamsOrderByEndTime(0, roleName);
			List<PMExam> list = new ArrayList<PMExam>();
			String teacherIds = pMExam.getTeacherIds();
			String[] teacherId = teacherIds.split(";");
			for (int i = 0; i < examList.size(); i++) {
				PMExam exam = examList.get(i);
				Boolean same = false;
				for (int m = 0; m < teacherId.length; m++) {
					if (!teacherId[m].equals("") && Integer.parseInt(teacherId[m]) == exam.getTeacherId().intValue()) {
						same = true;
					}
				}
				if (same)
					list.add(exam);
				;
			}
			if (list != null) {
				j.setSuccess(true);
				j.setMsg("获取排除教师考试后的考试成功");
				j.setObj(list);
				logger.info("获取排除教师考试后的考试成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("获取排除教师考试后的考试失败");
				logger.info("获取排除教师考试后的考试失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public List<String> sortDisplaysequence(List<String> list) {
		if (list == null || list.size() <= 0) {
			return list;
		}
		for (int i = 0; i < list.size(); i++) {
			int min = i;
			for (int j = i + 1; j < list.size(); j++) {
				if (Integer.parseInt(list.get(min)) > Integer.parseInt(list.get(j))) {
					min = j;
				}
			}
			if (i != min) {
				String tmp = list.get(min);
				list.set(min, list.get(i));
				list.set(i, tmp);
			}
		}
		return sort(list);
	}

	public List<String> sort(List<String> list) {
		String str = "";
		String pre = "0";
		String start = "0";
		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				str = list.get(i);
				pre = list.get(i);
				start = list.get(i);
			} else {
				if (Integer.parseInt(list.get(i)) > (Integer.parseInt(pre) + 1)) {
					if (i != 1 && Integer.parseInt(start) != Integer.parseInt(pre))
						str = str + "-" + pre;
					str = str + "、";
					str = str + list.get(i);
					start = list.get(i);
					pre = list.get(i);
				} else {
					pre = list.get(i);
					if (i == list.size() - 1)
						str = str + "-" + list.get(i);

				}
			}
		}
		List<String> temp = new ArrayList<String>();
		temp.add(str);
		return temp;
	}

	public void examAdd() // 添加考试
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				Date starttime = sdf.parse(timeFrom);
				pMExam.setStarttime(starttime);
				Date endtime = sdf.parse(timeTo);
				pMExam.setEndtime(endtime);
				Date updateTime = new Date();
				pMExam.setUpdateTime(updateTime);
				examService.examAdd(pMExam);
				j.setSuccess(true);
				j.setMsg("添加考试成功");
				logger.info("添加考试成功");
				super.writeJson(j);
			} catch (Exception e) {
				j.setSuccess(false);
				j.setMsg("添加考试失败");
				logger.info("添加考试失败");
				super.writeJson(j);
				logService.WriteLog("系统异常", "添加考试出错", e.toString()); // 写入日志
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}

	}

	public void updateExam() // 更新考试
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date starttime;
			try {
				starttime = sdf.parse(timeFrom);
				pMExam.setStarttime(starttime);
				Date endtime = sdf.parse(timeTo);
				pMExam.setEndtime(endtime);
				Date updateTime = new Date();
				pMExam.setUpdateTime(updateTime);
				int num = examService.updateExam(pMExam);
				if (num == 1) {
					ExamCacheManager examCacheManager = ExamCacheManager.getInstance();
					examCacheManager.removeAllObject();
					WSExamCacheManager wsexamCacheManager = WSExamCacheManager.getInstance();
					wsexamCacheManager.removeAllObject();
					j.setSuccess(true);
					j.setMsg("修改考试成功");
					logger.info("修改考试成功");
					super.writeJson(j);
				} else {
					j.setSuccess(false);
					j.setMsg("修改考试失败");
					logger.info("修改考试失败");
					super.writeJson(j);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				j.setSuccess(false);
				j.setMsg("修改考试失败");
				logger.info("修改考试失败");
				super.writeJson(j);
				logService.WriteLog("系统异常", "修改考试出错", e.toString()); // 写入日志
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void updateAllowCSbyexamId() // 更新考试
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {

			Date updateTime = new Date();
			pMExam.setUpdateTime(updateTime);
			int num = examService.updateAllowCSbyexamId(pMExam);
			if (num == 1) {

				j.setSuccess(true);
				j.setMsg("允许换座成功");
				logger.info("允许换座成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("允许换座失败");
				logger.info("允许换座失败");
				super.writeJson(j);
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamsNotInClass() // 获取班级没有参加的考试
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMExam> examList = examService.getExamsNotInClass(classId);
			if (examList != null) {
				j.setSuccess(true);
				j.setMsg("获取班级未参加的考试成功");
				j.setObj(examList);
				logger.info("获取班级未参加的考试成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("获取班级未参加的考试失败");
				logger.info("获取班级未参加的考试失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void addExamToClass() // 添加考试到班级
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			examclassesDao.addExamToClass(examId, classId);
			j.setSuccess(true);
			j.setMsg("添加考试成功");
			logger.info("添加考试成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void deleteClassExam() // 删除考试的班级或班级的考试
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			examclassesDao.deleteClassExam(examId, classId);
			j.setSuccess(true);
			j.setMsg("删除成功");
			logger.info("删除成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void getExamsByTeacherId() // 通过教师的id获取该教师参加的考试
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMExam> plist = examService.getExamsByTeacherId(adminId);
			j.setSuccess(true);
			j.setObj(plist);
			j.setMsg("获取教师参与的考试成功");
			logger.info("获取教师参与的考试成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	private static Lock lockAddExamInfo = new ReentrantLock();

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void addExamInfo() // 添加信息到examinfo
	{

		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo == null) {
			j.setSuccess(false);
			j.setMsg("必须先登录才能参加考试");
			super.writeJson(j);
			return;
		}
		lockAddExamInfo.lock();
		String userName = sessionInfo.getLoginName();
		String key = "addExamInfo_" + userName;
		// redis分布式锁
		int expire = 30; // 超时时间
		boolean locked = false;
//		try {
//			// 获取锁失败就结束
//			if (!redisService.lock(key, expire)) {
//				j.setSuccess(false);
//				j.setMsg("点击按钮不能过快");
//				super.writeJson(j);
//				lockAddExamInfo.unlock();
//				return;
//			}
//		} catch (Exception e) {
//			j.setSuccess(false);
//			j.setMsg("服务器错误,请重试");
//			super.writeJson(j);
//			lockAddExamInfo.unlock();
//			return;
//		}
		try {
			HttpServletRequest req = ServletActionContext.getRequest();
			HttpServletResponse response = ServletActionContext.getResponse();
			Studentexaminfo examinfo = new Studentexaminfo();
			int userId = sessionInfo.getUserId();
			String cookieName = "exam" + userId + pMExam.getId();
			Exam exam = new Exam();
			exam = examService.getExamById(pMExam.getId());
			examinfo = studentexaminfoDao.getStudentexaminfoByUserIdAnExamId(userId, pMExam.getId());
			if (examinfo == null) {
				session.put("firstLogin", true); // 学生第一次进入考试
				UUID uuid = UUID.randomUUID();
				String loginuuid = uuid.toString().replaceAll("\\-", "");
				Studentexaminfo studentexaminfo = new Studentexaminfo();

				studentexaminfo.setExamId(pMExam.getId());
				studentexaminfo.setUserId(userId);
				studentexaminfo.setScore(0);
				studentexaminfo.setSolved(0);
				studentexaminfo.setSubmit(0);
				studentexaminfo.setRank(0);
				studentexaminfo.setFirstloginTime(new Date());
				if (!exam.getAllowChangeSeat()) {
					studentexaminfo.setLoginUUID(loginuuid);
				}

				if (req.getRemoteAddr() != null) {
					studentexaminfo.setLoginIp(req.getRemoteAddr());
					// 将uuid存入cookie
					CookieInfo cookieInfo = new CookieInfo();

					try {
						if (!exam.getAllowChangeSeat()) {
							boolean result = cookieInfo.addCookie(req, response, cookieName, loginuuid);
							if (result) {
								studentexaminfoDao.save(studentexaminfo);
								j.setSuccess(true);
							} else {
								j.setSuccess(false);
								j.setMsg("cookie信息保存失败");
							}
						} else {
							studentexaminfoDao.save(studentexaminfo);
							j.setSuccess(true);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						j.setSuccess(false);
						j.setMsg("cookie信息保存失败");
					}
				} else {
					j.setSuccess(false);
					j.setMsg("获取不到Ip地址");
				}

			} else {
				session.put("firstLogin", false);// 学生不是第一次进入考试
				if (!exam.getAllowChangeSeat()) {
					String loginuuid = examinfo.getLoginUUID();
					// 检查cookie
					CookieInfo cookieInfo = new CookieInfo();
					try {
						boolean result = cookieInfo.getCookie(req, response, cookieName, loginuuid);
						if (!result) {
							j.setSuccess(false);
							j.setMsg("本场考试不允许更换电脑，需要教师在本场考试的“学生管理”里点“允许换座”，才可更换电脑。如果是同一台电脑第二次进入考试，也请教师同样操作");
						} else {
							j.setSuccess(true);
						}
					} catch (Exception e) {
						j.setSuccess(false);
						j.setMsg("本场考试不允许更换电脑，需要教师在本场考试的“学生管理”里点“允许换座”，才可更换电脑。如果是同一台电脑第二次进入考试，也请教师同样操作");
					}
				} else {
					j.setSuccess(true);
				}
			}
			super.writeJson(j);
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg("本场考试不允许更换电脑，需要教师在本场考试的“学生管理”里点“允许换座”，才可更换电脑。如果是同一台电脑第二次进入考试，也请教师同样操作");
			super.writeJson(j);
		} finally {
			lockAddExamInfo.unlock();
//			redisService.unLock(key);
		}

	}
}
