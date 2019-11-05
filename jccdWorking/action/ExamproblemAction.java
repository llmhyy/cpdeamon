package edu.dhu.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import edu.dhu.cache.ProblemsCachManager;
import edu.dhu.cache.WSProblemsCachManager;
import edu.dhu.dao.ExamDaoI;
import edu.dhu.model.Examproblems;
import edu.dhu.pageModel.DataGrid;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMExamproblem;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.ExamproblemServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "examproblemAction", results = { @Result(name = "examproblemList", location = "/front/user/examproblemList.jsp") })
public class ExamproblemAction extends BaseAction implements
		ModelDriven<PMExamproblem> {

	private static final long serialVersionUID = -4300471520281614603L;

	private static final Logger logger = Logger
			.getLogger(ExamproblemAction.class);

	private ExamproblemServiceI examproblemService;

	private ExamDaoI examDao;

	PMExamproblem pExamproblem = new PMExamproblem();

	@Override
	public PMExamproblem getModel() {
		return pExamproblem;
	}

	public ExamproblemServiceI getExamproblemService() {
		return examproblemService;
	}

	@Autowired
	public void setExamproblemService(ExamproblemServiceI examproblemService) {
		this.examproblemService = examproblemService;
	}

	@Autowired
	public void setExamDao(ExamDaoI examDao) {
		this.examDao = examDao;
	}

	public ExamDaoI getExamDao() {
		return examDao;
	}

	// http://localhost/oj/examproblemAction!getExamproblemList.action?examId=1
	public void getExamproblemList() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			DataGrid dataGrid = examproblemService.dataGrid(this.pExamproblem);
			if (dataGrid != null) {
				j.setSuccess(true);
				j.setMsg("获取考试题目列表成功");
				j.setObj(dataGrid);
				logger.info("获取考试题目列表成功");
			} else {
				j.setSuccess(false);
				j.setMsg("获取考试题目列表失败");
				logger.info("获取考试题目列表失败");
			}
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}

	}

	// http://localhost/oj/examproblemAction!deleteExamproblem.action?id=2
	public void deleteExamproblem() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			String msg = examproblemService.remove(this.pExamproblem);
			if (msg == "success") {

				int examId = pExamproblem.getExamId();
				int num = examproblemService.getExamproblemNum(examId); // 更新题目数量
				examDao.updateExamproblemNum(examId, num);
				
				j.setSuccess(true);
				j.setMsg("删除考试题目成功");
				logger.info("删除考试题目成功");
			} else {
				j.setSuccess(false);
				j.setMsg("删除考试题目失败");
				logger.info("删除考试题目失败");
			}
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("您不是管理员，没有权限访问此页面");
			super.writeJson(j);
		}
	}

	// http://localhost/oj/examproblemAction!insertExamproblem.action?examId=2&problemId=4&score=50&displaySequence=3
	public void insertExamproblem() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			String msg = examproblemService.add(pExamproblem);
			if (msg == "success") {
				int examId = pExamproblem.getExamId();
				int num = examproblemService.getExamproblemNum(examId); // 更新题目数量
				examDao.updateExamproblemNum(examId, num);
				j.setSuccess(true);
				j.setMsg("添加考试题目成功");
				logger.info("添加考试题目成功");
			} else {
				j.setSuccess(false);
				j.setMsg("添加考试题目失败");
				logger.info("添加考试题目失败");
			}
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void cloneExam() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<Examproblems> newexproList = examproblemService
					.getProblemByExamId(pExamproblem.getNewExamId());
			List<Examproblems> exproList = examproblemService
					.getProblemByExamId(pExamproblem.getExamId());
			int count = 0;
			int pid = 0;
			if (newexproList.size() == 0) {
				for (int i = 0; i < exproList.size(); i++) {
					pExamproblem.setProblemId(exproList.get(i).getProblemId());
					pExamproblem.setExamId(pExamproblem.getNewExamId());
					pExamproblem.setScore(exproList.get(i).getScore());
					pExamproblem.setDisplaySequence(exproList.get(i)
							.getDisplaySequence());
					String msg = examproblemService.add(pExamproblem);
					if (msg == "success") {
						int examId = pExamproblem.getExamId();
						int num = examproblemService.getExamproblemNum(examId); // 更新题目数量
						examDao.updateExamproblemNum(examId, num);
						count++;
					} else {
						pid = exproList.get(i).getProblemId();
						return;
					}
				}
				if (count == exproList.size()) {
					j.setSuccess(true);
					j.setMsg("克隆考试题目成功");
					logger.info("克隆考试题目成功");
				} else {
					j.setSuccess(false);
					j.setMsg("克隆考试题目Id:" + pid + "失败");
					logger.info("克隆考试部分题目Id:" + pid + "失败");
				}
			} else {
				j.setSuccess(false);
				j.setMsg("只有新的试卷才能克隆，如果想要克隆请先删除本场考试中的所有题目。");
				logger.info("克隆考试部分题目Id:" + pid + "失败");
			}
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	// http://localhost/oj/examproblemAction!alterDisplaySequence.action?id=2&displaySequence=4
	public void alterDisplaySequence() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			String msg = examproblemService
					.alterDisplaySequence(this.pExamproblem);
			if (msg == "success") {
				ProblemsCachManager problemsCachManager = ProblemsCachManager
						.getInstance();
				problemsCachManager.removeAllObject();
				WSProblemsCachManager wsproblemsCachManager = WSProblemsCachManager
						.getInstance();
				wsproblemsCachManager.removeAllObject();
				j.setSuccess(true);
				j.setMsg("更改考试题目顺序成功");
				logger.info("更改考试题目顺序成功");
			} else {
				j.setSuccess(false);
				j.setMsg("更改考试题目顺序失败");
				logger.info("更改考试题目顺序失败");
			}
			super.writeJson(j);

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void alterScore() { // 更改题目的分数
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = examproblemService.alterScore(
					pExamproblem.getId(), pExamproblem.getScore(),
					pExamproblem.getExamId());
			if (result == true) {
				ProblemsCachManager problemsCachManager = ProblemsCachManager
						.getInstance();
				problemsCachManager.removeAllObject();
				WSProblemsCachManager wsproblemsCachManager = WSProblemsCachManager
						.getInstance();
				wsproblemsCachManager.removeAllObject();

				j.setSuccess(true);
				j.setMsg("更改考试题目分数成功");
				logger.info("更改考试题目分数成功");
			} else {
				j.setSuccess(false);
				j.setMsg("更改考试题目分数失败");
				logger.info("更改考试题目分数失败");
			}
			super.writeJson(j);

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void alterDeadline() // 更新题目截止时间
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				Date deadline = sdf.parse(pExamproblem.getStrDeadline());
				Date updateTime = new Date();
				boolean result = examproblemService.alterDeadline(
						pExamproblem.getId(), deadline, updateTime,
						pExamproblem.getExamId());
				if (result == true) {
					ProblemsCachManager problemsCachManager = ProblemsCachManager
							.getInstance();
					problemsCachManager.removeAllObject();
					WSProblemsCachManager wsproblemsCachManager = WSProblemsCachManager
							.getInstance();
					wsproblemsCachManager.removeAllObject();
					j.setSuccess(true);
					j.setMsg("更改考试题目截止时间成功");
					logger.info("更改考试题目截止时间成功");
				} else {
					j.setSuccess(false);
					j.setMsg("更改考试题目截止时间失败");
					logger.info("更改考试题目截止时间失败");
				}
				super.writeJson(j);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				j.setSuccess(false);
				j.setMsg("更改考试题目截止时间失败");
				logger.info("更改考试题目截止时间失败");
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void alterbestBeforeAndscoreCoefByIds() // 更新题目截止时间
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {

			String ids = pExamproblem.getIds();
			String id[] = ids.split(",");
			int num = 0;
			List done = new ArrayList();
			for (int i = 0; i < id.length; i++) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm");
					Date bestBefore = sdf
							.parse(pExamproblem.getStrBestBefore());
					Date updateTime = new Date();
					float scoreCoef = pExamproblem.getScoreCoef();
					boolean result = examproblemService
							.alterbestBeforeAndscoreCoef(
									Integer.parseInt(id[i]), bestBefore,
									updateTime, scoreCoef,
									pExamproblem.getExamId());
					if (result == true) {
						num++;
						done.add(id[i]);
					}
				} catch (Exception e) {

				}
			}
			if (num > 0) {
				ProblemsCachManager problemsCachManager = ProblemsCachManager
						.getInstance();
				problemsCachManager.removeAllObject();
				WSProblemsCachManager wsproblemsCachManager = WSProblemsCachManager
						.getInstance();
				wsproblemsCachManager.removeAllObject();

				j.setSuccess(true);
				j.setObj(done);
				j.setMsg("更改考试题目最佳提交时间和成绩打折系数成功,更新" + num + "条记录");
				logger.info("更改考试题目最佳提交时间和成绩打折系数成功,更新" + num + "条记录");
			} else {
				j.setSuccess(false);
				j.setMsg("更改考试题目最佳提交时间和成绩打折系数失败");
				logger.info("更改考试题目最佳提交时间和成绩打折系数失败");
			}
			super.writeJson(j);

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	// http://localhost/oj/examproblemAction!getExamproblemCount.action?examId=1
	public void getExamproblemCount() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			int i = -1;
			try {
				i = examproblemService.getMaxDisplaySequence(this.pExamproblem
						.getExamId());
			} catch (Exception e) {
				j.setSuccess(false);
				j.setMsg("获取考试题目数失败");
				j.setObj(null);
				logger.info("获取考试题目数失败");
				super.writeJson(j);
			}

			j.setSuccess(true);
			j.setMsg("获取考试题目数成功");
			j.setObj(i);
			logger.info("获取考试题目数成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	// http://localhost/oj/examproblemAction!getProblemNotInExam.action?examId=1
	public void getProblemNotInExam() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			DataGrid dataGrid = examproblemService
					.getDataGridNotInExam(this.pExamproblem);
			if (dataGrid != null) {
				j.setSuccess(true);
				j.setMsg("获取题目列表成功");
				j.setObj(dataGrid);
				logger.info("获取题目列表成功");
			} else {
				j.setSuccess(false);
				j.setMsg("获取题目列表失败");
				logger.info("获取题目列表失败");
			}
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	// 通过examProblem的Id获取题目信息
	public void getProblemByExamProblemId() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			PMExamproblem p = examproblemService
					.getProblemByExamProblemId(pExamproblem.getId());
			if (p != null) {
				j.setSuccess(true);
				j.setMsg("获取题目信息成功");
				j.setObj(p);
				logger.info("获取题目信息成功");
			} else {
				j.setSuccess(false);
				j.setMsg("获取题目信息失败");
				logger.info("获取题目信息失败");
			}
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void alterScoreByIds() // 一次更新多道考题的score
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			String ids = pExamproblem.getIds();
			String id[] = ids.split(",");
			int num = 0;
			List done = new ArrayList();
			for (int i = 0; i < ids.length() - 1; i++) {
				try {
					boolean result = examproblemService.alterScore(
							Integer.parseInt(id[i]), pExamproblem.getScore(),
							pExamproblem.getExamId());
					if (result == true) {
						num++;
						done.add(id[i]);
					}
				} catch (Exception e) {

				}
			}
			if (num > 0) {
				ProblemsCachManager problemsCachManager = ProblemsCachManager
						.getInstance();
				problemsCachManager.removeAllObject();
				WSProblemsCachManager wsproblemsCachManager = WSProblemsCachManager
						.getInstance();
				wsproblemsCachManager.removeAllObject();

				j.setSuccess(true);
				j.setObj(done);
				j.setMsg("更改考试题目分数成功,更新" + num + "条记录");
				logger.info("更改考试题目分数成功,更新" + num + "条记录");
			} else {
				j.setSuccess(false);
				j.setMsg("更改考试题目分数失败");
				logger.info("更改考试题目分数失败");
			}
			super.writeJson(j);

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}

	public void alterDeadlineByIds() // 更新多组题目截止时间
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			String ids = pExamproblem.getIds();
			String id[] = ids.split(",");
			int num = 0;
			List done = new ArrayList();
			for (int i = 0; i < id.length; i++) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm");
					Date deadline = sdf.parse(pExamproblem.getStrDeadline());
					Date updateTime = new Date();
					boolean result = examproblemService.alterDeadline(
							Integer.parseInt(id[i]), deadline, updateTime,
							pExamproblem.getExamId());
					if (result == true) {
						num++;
						done.add(id[i]);
					}
				} catch (Exception e) {

				}
			}
			if (num > 0) {
				ProblemsCachManager problemsCachManager = ProblemsCachManager
						.getInstance();
				problemsCachManager.removeAllObject();
				WSProblemsCachManager wsproblemsCachManager = WSProblemsCachManager
						.getInstance();
				wsproblemsCachManager.removeAllObject();

				j.setSuccess(true);
				j.setObj(done);
				j.setMsg("更改考试题目截止时间成功,更新" + num + "条记录");
				logger.info("更改考试题目截止时间成功,更新" + num + "条记录");
			} else {
				j.setSuccess(false);
				j.setMsg("更改考试题目截止时间失败");
				logger.info("更改考试题目截止时间失败");
			}
			super.writeJson(j);

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录!");
			super.writeJson(j);
		}
	}
}
