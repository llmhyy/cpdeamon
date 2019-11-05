package edu.dhu.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;

import edu.dhu.dao.AdminusersDaoI;
import edu.dhu.dao.LogDaoI;
import edu.dhu.dao.UserDaoI;
import edu.dhu.model.Log;
import edu.dhu.model.Users;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMAdminusers;
import edu.dhu.pageModel.PMLog;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.LogServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "logAction", results = { @Result(name = "view", location = "/admin/index.jsp") })
public class LogAction extends BaseAction {

	private static final Logger logger = Logger.getLogger(CacheAction.class);
	private String type;
	private String timeFrom, timeTo;
	private String logIds;
	private String id;
	private LogServiceI logService;
	private UserDaoI userDao;
	private AdminusersDaoI adminusersDao;
	private LogDaoI logDao;

	@Autowired
	public void setLogService(LogServiceI logService) {
		this.logService = logService;
	}

	public LogServiceI getLogService() {
		return logService;
	}

	@Autowired
	public void setUserDao(UserDaoI userDao) {
		this.userDao = userDao;
	}

	public UserDaoI getUserDao() {
		return userDao;
	}

	@Autowired
	public void setAdminusersDaoI(AdminusersDaoI adminusersDao) {
		this.adminusersDao = adminusersDao;
	}

	public AdminusersDaoI getAdminusersDaoI() {
		return adminusersDao;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
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

	public void setLogIds(String logIds) {
		this.logIds = logIds;
	}

	public String getLogIds() {
		return logIds;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Autowired
	private void setLogDao(LogDaoI logDao) {
		this.logDao = logDao;
	}

	public LogDaoI getLogDao() {
		return logDao;
	}

	public void getAllLog() // 获取所有的log
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			if (sessionInfo.getRoleNames().equals("admin")) {
				List<Log> logList = logService.getAllLog();
				HashMap userMap = new HashMap();
				List<Users> userList = userDao.getAllStudents();
				for (int i = 0; i < userList.size(); i++) {
					Users user = userList.get(i);
					userMap.put(user.getId(), user.getChineseName());
				}
				List<PMAdminusers> teacherList = adminusersDao
						.findAllAdminusers();
				HashMap teacherMap = new HashMap();
				for (int i = 0; i < teacherList.size(); i++) {
					PMAdminusers teacher = teacherList.get(i);
					teacherMap.put(teacher.getId(), teacher.getName());
				}
				List<PMLog> pList = new ArrayList<PMLog>();
				for (int i = 0; i < logList.size(); i++) {
					PMLog p = new PMLog();
					Log log = logList.get(i);
					p.setId(log.getId());
					p.setOptime(log.getOptime());
					p.setType(log.getType());
					p.setUserId(log.getUserId());
					if (log.getUserType() != null) {
						if (log.getUserType().equals("teacher"))
							p.setUserType("教师");
						if (log.getUserType().equals("admin"))
							p.setUserType("管理员");
						if (log.getUserType().equals("admin") == false
								&& log.getUserType().equals("teacher") == false)
							p.setUserType("学生");
					}
					p.setAbstractContent(log.getAbstractContent());
					String userName = null;
					if (log.getUserType().equals("teacher")
							|| log.getUserType().equals("admin"))
						userName = (String) teacherMap.get(log.getUserId());
					else
						userName = (String) userMap.get(log.getUserId());
					p.setUserName(userName);
					pList.add(p);
				}
				j.setSuccess(true);
				j.setMsg("查询日志成功");
				j.setObj(pList);
				super.writeJson(j);
				logger.info("查询日志成功");
			} else {
				j.setSuccess(false);
				j.setMsg("你不是管理员，没有查询权限");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getLogByCondition() // 按条件查询log
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			if (sessionInfo.getRoleNames().equals("admin")) {
				List<Log> logList = logService.getLogByCondition(type,
						timeFrom, timeTo);
				HashMap userMap = new HashMap();
				List<Users> userList = userDao.getAllStudents();
				for (int i = 0; i < userList.size(); i++) {
					Users user = userList.get(i);
					userMap.put(user.getId(), user.getChineseName());
				}
				List<PMAdminusers> teacherList = adminusersDao
						.findAllAdminusers();
				HashMap teacherMap = new HashMap();
				for (int i = 0; i < teacherList.size(); i++) {
					PMAdminusers teacher = teacherList.get(i);
					teacherMap.put(teacher.getId(), teacher.getName());
				}
				List<PMLog> pList = new ArrayList<PMLog>();
				for (int i = 0; i < logList.size(); i++) {
					PMLog p = new PMLog();
					Log log = logList.get(i);
					p.setId(log.getId());
					p.setOptime(log.getOptime());
					p.setType(log.getType());
					p.setUserId(log.getUserId());
					if (log.getUserType() != null) {
						if (log.getUserType().equals("teacher"))
							p.setUserType("教师");
						if (log.getUserType().equals("admin"))
							p.setUserType("管理员");
						if (log.getUserType().equals("admin") == false
								&& log.getUserType().equals("teacher") == false)
							p.setUserType("学生");
					}
					p.setAbstractContent(log.getAbstractContent());
					String userName = null;
					if (log.getUserType().equals("teacher")
							|| log.getUserType().equals("admin"))
						userName = (String) teacherMap.get(log.getUserId());
					else
						userName = (String) userMap.get(log.getUserId());
					p.setUserName(userName);
					pList.add(p);
				}
				j.setSuccess(true);
				j.setMsg("查询日志成功");
				j.setObj(pList);
				super.writeJson(j);
				logger.info("查询日志成功");
			} else {
				j.setSuccess(false);
				j.setMsg("你不是管理员，没有查询权限");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void deleteByIds() // 通过id组成的串删除
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			if (sessionInfo.getRoleNames().equals("admin")) {
				String ids[] = logIds.split(",");
				int num = 0;
				List done = new ArrayList();
				for (int i = 0; i < ids.length; i++) {
					try {
						boolean result = logDao.deleteById(Integer
								.parseInt(ids[i]));
						if (result == true) {
							num++;
							done.add(ids[i]);
						}
					} catch (Exception e) {

					}
				}
				j.setSuccess(true);
				j.setMsg("删除日志成功,共删除" + num + "条日志");
				j.setObj(done);
				super.writeJson(j);
				logger.info("删除日志成功,共删除" + num + "条日志");
			} else {
				j.setSuccess(false);
				j.setMsg("你不是管理员，没有查询权限");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void deleteById() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			if (sessionInfo.getRoleNames().equals("admin")) {
				boolean result = false;
				try {
					result = logDao.deleteById(Integer.parseInt(id));
				} catch (Exception e) {

				}
				if (result == true) {
					j.setSuccess(true);
					j.setMsg("删除日志成功");
					super.writeJson(j);
					logger.info("删除日志成功");
				} else {
					j.setSuccess(false);
					j.setMsg("删除日志失败");
					super.writeJson(j);
					logger.info("删除日志失败");
				}

			} else {
				j.setSuccess(false);
				j.setMsg("你不是管理员，没有查询权限");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getLogById() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			if (sessionInfo.getRoleNames().equals("admin")) {
				Log log = null;
				try {
					log = logDao.getLogById(Integer.parseInt(id));
				} catch (Exception e) {

				}
				if (log != null) {
					HashMap userMap = new HashMap();
					List<Users> userList = userDao.getAllStudents();
					for (int i = 0; i < userList.size(); i++) {
						Users user = userList.get(i);
						userMap.put(user.getId(), user.getChineseName());
					}
					List<PMAdminusers> teacherList = adminusersDao
							.findAllAdminusers();
					HashMap teacherMap = new HashMap();
					for (int i = 0; i < teacherList.size(); i++) {
						PMAdminusers teacher = teacherList.get(i);
						teacherMap.put(teacher.getId(), teacher.getName());
					}
					PMLog p = new PMLog();
					p.setId(log.getId());
					p.setOptime(log.getOptime());
					p.setType(log.getType());
					p.setUserId(log.getUserId());
					if (log.getUserType() != null) {
						if (log.getUserType().equals("teacher"))
							p.setUserType("教师");
						if (log.getUserType().equals("admin"))
							p.setUserType("管理员");
						if (log.getUserType().equals("admin") == false
								&& log.getUserType().equals("teacher") == false)
							p.setUserType("学生");
					}
					p.setAbstractContent(log.getAbstractContent());
					String userName = null;
					if (log.getUserType().equals("teacher")
							|| log.getUserType().equals("admin"))
						userName = (String) teacherMap.get(log.getUserId());
					else
						userName = (String) userMap.get(log.getUserId());
					p.setUserName(userName);
					p.setContent(log.getContent());
					j.setSuccess(true);
					j.setMsg("查询日志成功");
					j.setObj(p);
					super.writeJson(j);
					logger.info("查询日志成功");
				} else {
					j.setSuccess(false);
					j.setMsg("查询日志失败");
					super.writeJson(j);
					logger.info("查询日志失败");
				}
			} else {
				j.setSuccess(false);
				j.setMsg("你不是管理员，没有查询权限");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}
}
