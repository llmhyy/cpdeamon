package edu.dhu.action;

import java.io.InputStream;

import java.text.SimpleDateFormat;
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

import edu.dhu.dao.ClassesDaoI;
import edu.dhu.dao.ClassstudentsDaoI;
import edu.dhu.dao.ExamClassesDaoI;
import edu.dhu.model.Classes;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMAdminusers;
import edu.dhu.pageModel.PMClasses;
import edu.dhu.pageModel.PMUser;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.ClassesServiceI;
import edu.dhu.service.LogServiceI;
import java.util.Date;
@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "classesAction", results = { @Result(name = "nologin", location = "/admin/index.jsp") })
public class ClassesAction extends BaseAction implements ModelDriven<PMClasses> {

	private static final Logger logger = Logger.getLogger(ClassesAction.class);
	private PMClasses pmclasses = new PMClasses();
	private ClassesServiceI classesServiceI;
	private int classId, userId, examId;
	private ClassstudentsDaoI classstudentsDao;
	private ExamClassesDaoI examclassesDao;
	private ClassesDaoI classesDao;
	private LogServiceI logService;
	private InputStream fileInput;

	PMAdminusers pmadminusers = new PMAdminusers();

	@Override
	public PMClasses getModel() {
		// TODO Auto-generated method stub
		return pmclasses;
	}

	@Autowired
	public void setClassesServiceI(ClassesServiceI classesServiceI) {
		this.classesServiceI = classesServiceI;
	}

	public ClassesServiceI getClassesServiceI() {
		return classesServiceI;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getClassId() {
		return classId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

	public void setExamId(int examId) {
		this.examId = examId;
	}

	public int getExamId() {
		return examId;
	}

	@Autowired
	public void setClassstudentsDao(ClassstudentsDaoI classstudentsDao) {
		this.classstudentsDao = classstudentsDao;
	}

	public ClassstudentsDaoI getClassstudentsDaoI() {
		return classstudentsDao;
	}

	@Autowired
	public void setClassesDao(ClassesDaoI classesDao) {
		this.classesDao = classesDao;
	}

	public ClassesDaoI getClassesDao() {
		return classesDao;
	}

	@Autowired
	private void setExamClassesDaoI(ExamClassesDaoI examclassesDao) {
		this.examclassesDao = examclassesDao;
	}

	public ExamClassesDaoI getExamClassesDaoI() {
		return examclassesDao;
	}
	
	public InputStream getFileInput() {
		return fileInput;
	}

	public void setFileInput(InputStream fileInput) {
		this.fileInput = fileInput;
	}

	public void findAllClass() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<Classes> classes = classesServiceI.findAllClass();
			logger.info("查询所有班级信息成功");
			j.setSuccess(true);
			j.setMsg("查询所有班级信息成功");
			j.setObj(classes);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findClassesByCondition() {
		int schoolId = pmclasses.getId();// 用id代表schoolId获取本校老师 0代表根据教师Id查询
		int teacherId = pmclasses.getTeacherId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			if (schoolId == 0 && teacherId != 0) {
				List<PMClasses> classes = classesServiceI.findClassesByCondition(teacherId);
				j.setObj(classes);
			} else {
				List<PMClasses> classes = classesServiceI.findClassesByCondition(pmclasses);
				j.setObj(classes);
			}
			logger.info("查询所有班级信息成功");
			j.setSuccess(true);
			j.setMsg("查询所有班级信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findAllClasses() {
		int schoolId = pmclasses.getId();// 用id代表schoolId获取本校老师 0代表根据教师Id查询
		int teacherId = pmclasses.getTeacherId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			if (schoolId != 0 && teacherId == 0) {
				List<PMClasses> classes = classesServiceI.findClassesByCondition(pmclasses);
				j.setObj(classes);
			} else {
				List<PMClasses> classes = classesServiceI.findClassesByCondition(teacherId);
				j.setObj(classes);
			}
			logger.info("查询所有班级信息成功");
			j.setSuccess(true);
			j.setMsg("查询所有班级信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void editClassMessage() // int id, String className, int teacherId
	{ // 修改班级信息
		int id = pmclasses.getId();
		String className = pmclasses.getName();
		int teacherId = pmclasses.getTeacherId();
		int advance = pmclasses.getAdvance();
		int late = pmclasses.getLate();
		int reject = pmclasses.getReject();
		String weektime = pmclasses.getWeektime();
		Date first_week_monday = pmclasses.getFirst_week_monday();
		
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = classesServiceI.editClassMessage(id, className,teacherId,advance,late,reject,weektime,first_week_monday);
			if (result == true) {
				logger.info("修改班级信息成功");
				j.setSuccess(true);
				j.setMsg("修改班级信息成功");
				j.setObj(result);
				super.writeJson(j);
			} else {
				logger.info("修改班级信息失败");
				j.setSuccess(false);
				j.setMsg("修改班级信息失败");
				j.setObj(result);
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void deleteClass() {
		int id = pmclasses.getId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = classesServiceI.deleteClass(id);
			if (result == true) {
				logger.info("删除班级成功");
				j.setSuccess(true);
				j.setMsg("删除班级成功");
				j.setObj(result);
				super.writeJson(j);
			} else {
				logger.info("删除班级失败");
				j.setSuccess(false);
				j.setMsg("删除班级失败");
				j.setObj(result);
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findClassStudentsById() {
		int classId = pmclasses.getId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMUser> users = classesServiceI.findClassStudentsById(classId);
			logger.info("查询班级所有学生信息成功");
			j.setSuccess(true);
			j.setMsg("查询班级所有学生信息成功");
			j.setObj(users);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void deleteClassStudentsById() // 通过编号删除班级学生
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = classesServiceI.deleteClassStudentsByUserId(classId, userId);
			if (result == true) {
				int studentsNum = classstudentsDao.getClassStudentsNum(classId);
				classesDao.updateClassStudentsNum(classId, studentsNum);
				logger.info("删除班级学生成功");
				j.setSuccess(true);
				j.setMsg("删除班级学生成功");
				super.writeJson(j);
			} else {
				logger.info("删除班级学生失败");
				j.setSuccess(false);
				j.setMsg("删除班级学生失败");
				super.writeJson(j);
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}

	}

	public void addClass() // 添加班级
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			classesServiceI.addClass(pmclasses);
			logger.info("添加班级成功");
			j.setSuccess(true);
			j.setMsg("添加班级成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findClassInExam() // 查看参与考试的班级
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMClasses> classList = classesServiceI.findClassInExam(examId);
			logger.info("查看参与考试的班级成功");
			;
			j.setSuccess(true);
			j.setObj(classList);
			j.setMsg("查看参与考试的班级成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findClassNotInExam() // 查看参与考试的班级
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMClasses> classList = classesServiceI.findClassNotInExam(examId);
			logger.info("查看未参与考试的班级成功");
			;
			j.setSuccess(true);
			j.setObj(classList);
			j.setMsg("查看未参与考试的班级成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void addClassToExam() // 添加班级到考试中
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			examclassesDao.addClassToExam(examId, classId);
			logger.info("添加班级成功");
			;
			j.setSuccess(true);
			j.setMsg("添加班级成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findClassById() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			Classes onclass = classesServiceI.findClassById(classId);
			logger.info("查询班级成功");
			;
			j.setSuccess(true);
			j.setObj(onclass);
			j.setMsg("查询班级成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findNotEndClassByTeacherId() // 查询教师所属的班级
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<Classes> classes = classesServiceI.findNotEndClassByTeacherId(pmclasses.getTeacherId());
			logger.info("查询班级成功");
			;
			j.setSuccess(true);
			j.setObj(classes);
			j.setMsg("查询班级成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void takeClassByInviteCode() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			// 判断邀请码对应的班级是否存在
			int id = sessionInfo.getUserId();
			j=classesServiceI.takeOneClass(pmclasses.getInviteCode(), id);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录");
			super.writeJson(j);
		}
	}

}
