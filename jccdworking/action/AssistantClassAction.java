package edu.dhu.action;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.dao.AssistantClassDaoI;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMAssistantClass;
import edu.dhu.pageModel.SessionInfo;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "assistantClassAction", results = { @Result(name = "view", location = "/admin/index.jsp") })
public class AssistantClassAction extends BaseAction implements
		ModelDriven<PMAssistantClass> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger
			.getLogger(PMAssistantClass.class);
	AssistantClassDaoI assistantClassDao;
	int assistantId, classId;

	@Autowired
	public void setAssistantClassDao(AssistantClassDaoI assistantClassDao) {
		this.assistantClassDao = assistantClassDao;
	}

	public AssistantClassDaoI getAssistantClassDao() {
		return assistantClassDao;
	}

	public void setAssistantId(int assistantId) {
		this.assistantId = assistantId;
	}

	public int getAssistantId() {
		return assistantId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getClassId() {
		return classId;
	}

	@Override
	public PMAssistantClass getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteClassAssistant() // 删除班级的助教
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = assistantClassDao.delAssistantClass(assistantId,
					classId);
			if (result) {
				logger.info("删除班级助教成功");
				j.setSuccess(true);
				j.setMsg("删除班级助教成功");
				super.writeJson(j);
			} else {
				logger.info("删除班级助教失败");
				j.setSuccess(false);
				j.setMsg("删除班级助教失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void addClassAssistant() // 为班级添加助教
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = assistantClassDao.addAssistantClass(assistantId,
					classId);
			if (result) {
				logger.info("添加班级助教成功");
				j.setSuccess(true);
				j.setMsg("添加班级助教成功");
				super.writeJson(j);
			} else {
				logger.info("添加班级助教失败");
				j.setSuccess(false);
				j.setMsg("添加班级助教失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}
}
