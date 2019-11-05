package edu.dhu.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.cache.ExamCacheManager;
import edu.dhu.cache.ProblemsCachManager;
import edu.dhu.cache.WSProblemsCachManager;
import edu.dhu.dao.AdminusersDaoI;
import edu.dhu.dao.ProblemtestcasesDaoI;
import edu.dhu.model.Adminusers;
import edu.dhu.model.Exam;
import edu.dhu.model.Problems;
import edu.dhu.model.Problemtestcases;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMExam;
import edu.dhu.pageModel.PMExamProblemInfo;
import edu.dhu.pageModel.PMProblemInfo;
import edu.dhu.pageModel.PMProblems;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.ExamServiceI;
import edu.dhu.service.ProblemsServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "problemsAction", results = { @Result(name = "takeAnExam", location = "/user/takeAnExam.jsp") })
public class ProblemsAction extends BaseAction implements
		ModelDriven<PMProblems> {

	private static final long serialVersionUID = -2988368111795658835L;
	// 记录日志
	private static final Logger logger = Logger.getLogger(ProblemsAction.class);

	PMProblems pMProblems = new PMProblems();

	private ProblemsServiceI problemsServiceI;
	private ExamServiceI examService;
	private ProblemtestcasesDaoI problemtestcasesDao;
	private AdminusersDaoI adminuserDao;

	@Override
	public PMProblems getModel() {
		return pMProblems;
	}

	public ProblemsServiceI getProblemsServiceI() {
		return problemsServiceI;
	}

	@Autowired
	public void setProblemsServiceI(ProblemsServiceI problemsServiceI) {
		this.problemsServiceI = problemsServiceI;
	}

	public ProblemtestcasesDaoI getProblemtestcasesDao() {
		return problemtestcasesDao;
	}

	@Autowired
	public void setProblemtestcasesDao(ProblemtestcasesDaoI problemtestcasesDao) {
		this.problemtestcasesDao = problemtestcasesDao;
	}

	public ExamServiceI getExamService() {
		return examService;
	}

	@Autowired
	public void setExamService(ExamServiceI examService) {
		this.examService = examService;
	}

	@Autowired
	public void setAdminuserDao(AdminusersDaoI adminuserDao) {
		this.adminuserDao = adminuserDao;
	}

	public AdminusersDaoI getAdminuserDao() {
		return adminuserDao;
	}

	// 获取题目列表
	public void getProblemsList() {

		// 从session中获取登录的用户id
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		// 用户已经登录
		if (sessionInfo != null) {
			int userId;
			// 如果session里的用户是student，则忽略传入的参数userid，使用session里的userid
			if (sessionInfo.getRoleNames().equals("student")) {
				userId = sessionInfo.getUserId();
			}
			// 如果session里的用户是teacher或者admin，则根据传入的参数userid获取该学生的考试题目列表
			else {
				session.put("firstLogin", false);
				userId = pMProblems.getUserId();
			}
			// 根据userid和examid判断该学生是否能参加这场考试，如果不能，则返回error
			if (!examService.checkExamByUserIdAndExamId(pMProblems.getExamId(),
					userId)) {
				logger.info("userId为:" + userId + "的学生没有examID为:"
						+ pMProblems.getExamId() + "的考试。");
				j.setSuccess(false);
				j.setMsg("您无法参加该场考试");
				super.writeJson(j);
				return;
			}
			// 根据exam ID和user ID获取题目列表信息
			List<PMProblems> problemsList = problemsServiceI
					.findAllProblemsByExamId(pMProblems.getExamId(), userId,(boolean)session.get("firstLogin"));
			session.put("firstLogin", false);
			// 根据examID查询该场考试的信息,先从缓冲中获取该场考试的信息
			ExamCacheManager examCacheManager = ExamCacheManager.getInstance();
			Exam exam = (Exam) examCacheManager.getObject("theExamById"
					+ pMProblems.getExamId());
			System.out.println(exam);
			if (exam == null) {
				exam = examService.getExamById(pMProblems.getExamId());
				examCacheManager.putObject(
						"theExamById" + pMProblems.getExamId(), exam);
				System.out.println(exam);
			}

			// 返回前段页面的对象
			PMExamProblemInfo pMExamProblemInfo = new PMExamProblemInfo();
			pMExamProblemInfo.setProblemsList(problemsList);
			pMExamProblemInfo.setExam(exam);
			logger.info("查询所有考试题目成功");
			j.setSuccess(true);
			j.setMsg("查询所有考试题目成功");
			j.setObj(pMExamProblemInfo);
			super.writeJson(j);
			/*
			 * // 首先需要验证现在是否是考试时间范围内 Date now = new Date(); // 根据examId获取exam
			 * Exam exam = examService.getExamById(pMProblems.getExamId()); Date
			 * startTime = exam.getStarttime(); Date endTime =
			 * exam.getEndtime(); if(now.getTime() >= startTime.getTime() &&
			 * now.getTime() <= endTime.getTime()) { int userId =
			 * sessionInfo.getUserId(); // 根据exam ID和user ID获取题目列表信息
			 * List<PMProblems> problems =
			 * problemsServiceI.findAllProblemsByExamId(pMProblems.getExamId(),
			 * userId); logger.info("查询所有考试题目成功"); j.setSuccess(true);
			 * j.setMsg("查询所有考试题目成功"); j.setObj(problems); super.writeJson(j); }
			 * else { logger.info("考试时间没到，无法查看考试题目。"); j.setSuccess(false);
			 * j.setMsg("考试时间没到，无法查看考试题目。"); super.writeJson(j); }
			 */
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	// 根据题目ID获取题目信息
	public void getProblemById() {
		// 返回前台的json数据
		Json j = new Json();
		ProblemsCachManager problemsCachManager = ProblemsCachManager
				.getInstance();
		PMProblemInfo problem = (PMProblemInfo) problemsCachManager
				.getObject("problemId" + pMProblems.getId());
		System.out.println(problem);
		if (problem == null) {
			problem = problemsServiceI.findProblemInfoById(pMProblems.getId());
			problemsCachManager.putObject("problemId" + pMProblems.getId(),
					problem);
			System.out.println(problem);
		}

		if (problem != null) {
			logger.info("查询题目ID:" + pMProblems.getId() + "的题目成功");
			j.setSuccess(true);
			j.setMsg("查询题目ID:" + pMProblems.getId() + "的题目成功");
			j.setObj(problem);
			super.writeJson(j);
		} else {
			logger.info("查询题目ID:" + pMProblems.getId() + "的题目失败");
			j.setSuccess(false);
			j.setMsg("查询题目ID:" + pMProblems.getId() + "的题目失败");
			super.writeJson(j);
		}
	}

	// 根据题目ID获取题目信息
	public void getProblemByIdAndExamId() {
		// 返回前台的json数据
		Json j = new Json();
		ProblemsCachManager problemsCachManager = ProblemsCachManager
				.getInstance();
		PMProblemInfo problem = (PMProblemInfo) problemsCachManager
				.getObject("problemId" + pMProblems.getId());
		System.out.println(problem);
		if (problem == null) {
			problem = problemsServiceI.findProblemInfoByIdAndExamId(
					pMProblems.getId(), pMProblems.getExamId());
			problemsCachManager.putObject("problemId" + pMProblems.getId(),
					problem);
			System.out.println(problem);
		}

		if (problem != null) {
			logger.info("查询题目ID:" + pMProblems.getId() + "的题目成功");
			j.setSuccess(true);
			j.setMsg("查询题目ID:" + pMProblems.getId() + "的题目成功");
			j.setObj(problem);
			super.writeJson(j);
		} else {
			logger.info("查询题目ID:" + pMProblems.getId() + "的题目失败");
			j.setSuccess(false);
			j.setMsg("查询题目ID:" + pMProblems.getId() + "的题目失败");
			super.writeJson(j);
		}
	}

	// 查找所有问题
	public void findProblemsByCondition() {
		// 查询时使用的查询条件
		String keywords = pMProblems.getKeywords();
		String courseCode = pMProblems.getCourseCode();
		String chapterCode = pMProblems.getChapterCode();
		String source = pMProblems.getSource();
		String difficulty = pMProblems.getDifficulty();
		int teacherId = pMProblems.getTeacherId();
		String sortContent = pMProblems.getSortContent(); // 排序内容，排序方式
		String sortType = pMProblems.getSortType();
		//
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMProblems> problems = problemsServiceI
					.findProblemsByCondition(keywords, courseCode, chapterCode,
							source, difficulty, teacherId, sortContent,
							sortType);
			logger.info("查询所有考试题目成功");
			j.setSuccess(true);
			j.setMsg("查询所有考试题目成功");
			j.setObj(problems);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void viewProblemDetailInformationById() // 查看问题的详细信息
	{
		int id = pMProblems.getId(); // 试题的id
		int examId = pMProblems.getExamId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			PMProblems problem = problemsServiceI
					.viewProblemDetailInformationById(id, examId);
			logger.info("查询题目信息成功");
			j.setSuccess(true);
			j.setMsg("查询题目信息成功");
			j.setObj(problem);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void editProblem() // 编辑问题
	{
		List<String> inputList = pMProblems.getInput();
		List<String> outputList = pMProblems.getOutput();
		List<String> idList = pMProblems.getTestcaseId();
		String str1[] = inputList.get(0).split("\n<分隔符>\n");
		String str2[] = outputList.get(0).split("\n<分隔符>\n");
		String str3[] = idList.get(0).split("\n<分隔符>\n");
		List<String> temp1 = new ArrayList<String>();
		List<String> temp2 = new ArrayList<String>();
		List<String> temp3 = new ArrayList<String>();
		for (int i = 0; i < str1.length; i++) {
			temp1.add(str1[i]);
			temp2.add(str2[i]);
			temp3.add(str3[i]);
		}
		pMProblems.setInput(temp1);
		pMProblems.setOutput(temp2);
		pMProblems.setTestcaseId(temp3);
		Date updateTime = new Date();
		pMProblems.setUpdateTime(updateTime);
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json json = new Json();
		if (sessionInfo != null) {
			int id = pMProblems.getId();
			problemsServiceI.editProblem(pMProblems); // 将问题信息添加到problems表中
			List<String> inputs = pMProblems.getInput();
			List<String> outputs = pMProblems.getOutput(); // 从页面得到的测试用例与数据库中的进行比对
			List<String> ids = pMProblems.getTestcaseId();
			for (int i = 0; i < inputs.size(); i++) {
				if (Integer.parseInt(ids.get(i)) > 0) // 如果id大于0，则表示当前的测试用例是原有的测试用例
				{
					Problemtestcases c = new Problemtestcases();
					c.setId(Integer.parseInt(ids.get(i)));
					c.setInput(inputs.get(i));
					c.setOutput(outputs.get(i));
					c.setProblemId(id);
					problemtestcasesDao.updateTestcase(c); // 更新测试用例
				}
			}
			List<Problemtestcases> testcases = problemtestcasesDao
					.getProblemtestcasesByProblemId(id); // 从数据库中读取到的测试用例
			int dexist[] = new int[testcases.size()]; // 从页面上传的测试用例是否存在当前用例，1表示存在，0表示不存在
			int nexist[] = new int[inputs.size()]; // 记录页面测试用例
			for (int i = 0; i < testcases.size(); i++)
				dexist[i] = 0; // 表示不存在
			for (int i = 0; i < inputs.size(); i++)
				nexist[i] = 0;
			for (int i = 0; i < inputs.size(); i++) {
				String input = inputs.get(i);
				String output = outputs.get(i);
				for (int j = 0; j < testcases.size(); j++) {
					if (testcases.get(j).getInput().equals(input)
							&& testcases.get(j).getOutput().equals(output)
							&& dexist[j] == 0) // 用例与数据库中一个用例相同
					{
						nexist[i] = 1;
						dexist[j] = 1;
						break;
					}
				}
			}
			// dexist[j]=0表示该测试用例已经没用，应该将其删除
			for (int i = 0; i < testcases.size(); i++) {
				if (dexist[i] == 0) {
					int caseId = testcases.get(i).getId();
					problemtestcasesDao.deleteTestCase(caseId); // 删除该测试用例
					// System.out.println("testcases:"+i);
				}
			}
			for (int i = 0; i < inputs.size(); i++) {
				if (nexist[i] == 0) // 表明该测试用例是新添加的，要将该测试用例添加到数据库中
				{
					Problemtestcases testcase = new Problemtestcases();
					testcase.setInput(inputs.get(i));
					testcase.setOutput(outputs.get(i));
					testcase.setProblemId(id);
					problemtestcasesDao.save(testcase);
				}
			}

			// 清空缓存
			ProblemsCachManager problemsCachManager = ProblemsCachManager
					.getInstance();
			problemsCachManager.removeAllObject();
			WSProblemsCachManager wsproblemsCachManager = WSProblemsCachManager
					.getInstance();
			wsproblemsCachManager.removeAllObject();

			logger.info("修改题目信息成功");
			json.setSuccess(true);
			json.setMsg("修改题目信息成功");
			//json.setObj(pMProblems);
			super.writeJson(json);
		} else {
			json.setSuccess(false);
			json.setMsg("请先登录。");
			super.writeJson(json);
		}

	}

	public void deleteProblem() {
		int id = pMProblems.getId(); // 试题的id
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json json = new Json();
		if (sessionInfo != null) {

			List<Exam> examList = examService.getExamByProblemId(id);
			if (examList.size() > 0) {
				String msg = "该题已被以下考试收录，请将其从考试中删除后再删除题目:<br>";
				for (int i = 0; i < examList.size(); i++) {
					Exam exam = examList.get(i);
					msg = msg + exam.getName() + "<br>";
				}
				logger.info("删除题目失败");
				json.setSuccess(false);
				json.setMsg(msg);
				super.writeJson(json);
			} else {
				problemtestcasesDao.deleteProblemTestcaseByProblemId(id); // 删除测试用例
				Problems problem = problemsServiceI.findProblemById(id);
				String description = problem.getDescription();
				String inputRuqirement = problem.getInputRequirement();
				String outputRuqirement = problem.getOutputRequirement();
				problemsServiceI.deleteProblem(id); // 删除题目
				deleteImageAndFile(description); // 删除描述输入输出说明中的图片和文件
				deleteImageAndFile(inputRuqirement);
				deleteImageAndFile(outputRuqirement);
				logger.info("删除题目成功");
				json.setSuccess(true);
				json.setMsg("删除题目成功");
				json.setObj(id);
				super.writeJson(json);
			}
		} else {
			json.setSuccess(false);
			json.setMsg("请先登录。");
			super.writeJson(json);
		}
	}

	public void getProblemBelowExam() {
		int id = pMProblems.getId(); // 试题的id
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json json = new Json();
		if (sessionInfo != null) {

			List<Exam> examList = examService.getExamByProblemId(id);
			List<PMExam> list = new ArrayList<PMExam>();
			for (int i = 0; i < examList.size(); i++) {
				Exam exam = examList.get(i);
				PMExam e = new PMExam();
				String name = exam.getName();
				e.setId(exam.getId());
				e.setName(name);
				Integer teacherId = exam.getTeacherId();
				e.setTeacherId(teacherId);
				if (teacherId != null) {
					Adminusers adminuser = adminuserDao.get(Adminusers.class,
							teacherId.intValue());
					String teacherName = adminuser.getName();
					e.setTeacherName(teacherName);
				}
				list.add(e);
			}
			logger.info("查询题目所属的考试成功");
			json.setSuccess(true);
			json.setObj(list);
			json.setMsg("查询题目所属的考试成功");
			super.writeJson(json);
		} else {
			json.setSuccess(false);
			json.setMsg("请先登录。");
			super.writeJson(json);
		}
	}

	public void deleteImageAndFile(String source) // 删除题目后删除图片和文件
	{
		String str = source;
		while (str.indexOf("<img") >= 0) // 删除图片
		{
			int x = str.indexOf("<img");
			int y;
			for (y = x; y < str.length(); y++) {
				if (str.toCharArray()[y] == '>') // 找到第一个">"退出循环
				{
					break;
				}
			}
			String img = str.substring(x, y);
			int m = img.indexOf("src=\"");
			img = img.substring(m + 5, img.length());
			int n = img.indexOf("\"");
			img = img.substring(0, n); // 获得img的src
			ServletContext servletContext = ServletActionContext
					.getServletContext();
			File dir = new File(servletContext.getRealPath("/"));
			File file = new File(dir.getParent() + img);
			if (file.exists() == true) {
				file.delete();
			}
			String str1 = str.substring(0, x);
			String str2 = str.substring(y + 1, str.length());
			str = str1 + str2;
		}
		str = source;
		while (str.indexOf("<a") >= 0) // 删除图片
		{
			int x = str.indexOf("<a");
			int y;
			for (y = x; y < str.length(); y++) {
				if (str.toCharArray()[y] == '>') // 找到第一个">"退出循环
				{
					break;
				}
			}
			String temp = str.substring(x, y);
			int m = temp.indexOf("href=\"");
			temp = temp.substring(m + 6, temp.length());
			int n = temp.indexOf("\"");
			temp = temp.substring(0, n); // 获得img的src
			ServletContext servletContext = ServletActionContext
					.getServletContext();
			File dir = new File(servletContext.getRealPath("/"));
			File file = new File(dir.getParent() + temp);
			if (file.exists() == true) {
				file.delete();
			}
			String str1 = str.substring(0, x);
			String str2 = str.substring(y + 1, str.length());
			str = str1 + str2;
		}
	}

}
