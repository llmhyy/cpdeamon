package edu.dhu.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.dao.AdminusersDaoI;
import edu.dhu.dao.ClassesDaoI;
import edu.dhu.dao.ClassstudentsDaoI;
import edu.dhu.email.SendMail;
import edu.dhu.model.Adminusers;
import edu.dhu.model.Classes;
import edu.dhu.model.Users;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.pageModel.PMUser;
import edu.dhu.service.RedisServiceI;
import edu.dhu.service.UserServiceI;
import edu.dhu.util.IpUtil;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "userAction", results = { @Result(name = "user", location = "/front/user/index.jsp") })
public class UserAction extends BaseAction implements ModelDriven<PMUser> {

	private static final long serialVersionUID = 4426765055782995839L;

	// 记录日志
	private static final Logger logger = Logger.getLogger(UserAction.class);

	// 因为我们已经注解了service，这里我们使用面向接口编程，方便。注重功能，不注重实现形式。
	private UserServiceI userService;
	private ClassstudentsDaoI classstudentsDao;
	private ClassesDaoI classesDao;
	private AdminusersDaoI adminusersDao;
	PMUser user = new PMUser();
	private int examId;

	private RedisServiceI redisService;

	public RedisServiceI getRedisService() {
		return redisService;
	}

	@Autowired
	public void setRedisService(RedisServiceI redisService) {
		this.redisService = redisService;
	}

	@Autowired
	public void setAdminusersDaoI(AdminusersDaoI adminusersDao) {
		this.adminusersDao = adminusersDao;
	}

	public AdminusersDaoI getAdminusersDaoI() {
		return adminusersDao;
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

	@Override
	public PMUser getModel() {
		return user;
	}

	public UserServiceI getUserService() {
		return userService;
	}

	@Autowired
	public void setUserService(UserServiceI userService) {
		this.userService = userService;
	}

	// 返回的页面
	public String user() {
		return "user";
	}

	public String userAdd() {
		return "userAdd";
	}

	public String userEdit() {
		return "userEdit";
	}

	public void setExamId(int examId) {
		this.examId = examId;
	}

	public int getExamId() {
		return examId;
	}

	// 前台的注册
	public void reg() {
		Json j = new Json();
		try {
			user.setFlag("1");
			userService.save(user);
			j.setSuccess(true);
			j.setMsg("注册成功");
		} catch (Exception e) {
			e.printStackTrace();
			j.setSuccess(false);
			j.setMsg(e.getMessage().toString());
		}
		super.writeJson(j);
	}

	// 前台登陆页面
	public void login() {
		Json j = userLogin(user);
		super.writeJson(j);
	}

	public Json userLogin(PMUser user) {
		Json j = new Json();
		PMUser u = userService.login(user);
		if (u != null) {
			SessionInfo sessionInfo = new SessionInfo();
			sessionInfo.setUserId(u.getId());
			sessionInfo.setStudentNo(u.getStudentNo());
			sessionInfo.setLoginName(u.getUsername());
			sessionInfo.setRoleNames("student");
			sessionInfo.setLoginPassword(u.getPassword());
			sessionInfo.setFlag(u.getFlag());
			sessionInfo.setBanji(u.getBanji());
			sessionInfo.setEmail(u.getEmail());
			sessionInfo.setChineseName(u.getChineseName());
			sessionInfo.setIp(IpUtil.getIpAddr(ServletActionContext.getRequest()));
			// 将登录信息放到session中
			Map<String, Object> session = ActionContext.getContext().getSession();
			session.put("sessionInfo", sessionInfo);

			// String ping = redisService.ping();
			// if(ping == null){
			// j.setObj(null);
			// j.setSuccess(false);
			// j.setMsg("不能ping通redis服务器");
			// logger.info("ping: "+ping);
			// return j;
			// }

			j.setObj(sessionInfo);
			j.setSuccess(true);
			j.setMsg("登录成功");

			// String json = JSON.toJSONStringWithDateFormat(sessionInfo,
			// "yyyy-MM-dd HH:mm:ss");
			// redisService.set("session_user_"+sessionInfo.getUserId(), json,
			// 1800);
		} else {
			j.setSuccess(false);
			j.setMsg("登录失败");
		}
		return j;
	}

	// 退出登录
	public void logout() {
		// 删除session中用户登录信息
		Map<String, Object> session = ActionContext.getContext().getSession();
		session.remove("sessionInfo");
		// redisService.del("session_user_"+user.getId());
		Json j = new Json();
		j.setSuccess(true);
		j.setMsg("退出登录成功");
		super.writeJson(j);
	}

	// 删除操作
	public void remove() {
		userService.remove(user.getIds());
		Json j = new Json();
		j.setSuccess(true);
		j.setMsg("删除成功");
		super.writeJson(j);
	}

	// 修改用户信息
	public void editUserInfo() {
		// 从session中获取登录的用户id
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		int id = sessionInfo.getUserId();
		this.user.setId(id);

		Json j = new Json();
		boolean b = userService.editUserInfo(user);
		if (b) {
			j.setSuccess(true);
			j.setMsg("修改用户信息成功！");
			logger.info("********修改用户信息成功！********");
		} else {
			j.setSuccess(false);
			j.setMsg("修改用户信息失败！");
		}
		super.writeJson(j);
	}

	public void editUserInfoByTeacher() // 老师修改学生的信息
	{
		int id = user.getId();
		String chineseName = user.getChineseName();
		String username = user.getUsername();
		String banji = user.getBanji();
		String email = user.getEmail();
		String studentNo = user.getStudentNo();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = userService.editUserInfoByTeacher(id, studentNo, chineseName, username, banji, email);
			if (result == true) {
				j.setSuccess(true);
				j.setMsg("修改学生信息成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("修改学生信息失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	// 修改用户密码
	public void editUserPassword() {
		// 从session中获取登录的用户id
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		int id = sessionInfo.getUserId();
		this.user.setId(id);
		boolean b = userService.editUserPassword(user);

		Json j = new Json();
		if (b) {
			j.setSuccess(true);
			j.setMsg("修改用户密码成功");
			logger.info("********修改用户密码成功********");
		} else {
			j.setSuccess(false);
			j.setMsg("修改用户密码失败");
		}
		super.writeJson(j);
	}

	public void editUserPasswordByTeacher() { // 老师修改学生密码
		// 从session中获取登录的用户id
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		boolean b = userService.editUserPassword(user);

		Json j = new Json();
		if (b) {
			j.setSuccess(true);
			j.setMsg("修改用户密码成功");
			logger.info("********修改用户密码成功********");
		} else {
			j.setSuccess(false);
			j.setMsg("修改用户密码失败");
		}
		super.writeJson(j);
	}

	public void getAllStudents() // 获取所有的学生信息
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMUser> userList = userService.getAllStudents();
			if (userList != null) {
				j.setSuccess(true);
				j.setMsg("获取学生信息成功");
				j.setObj(userList);
				logger.info("获取学生信息成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("获取学生信息失败");
				logger.info("获取学生信息失败");
				super.writeJson(j);
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getPassword() // 获取学生的密码
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			Users user = userService.getUserPssword(this.user.getId().intValue()); // 用户信息
			if (user != null) {
				j.setSuccess(true);
				j.setMsg("获取学生账号成功");
				j.setObj(user);
				logger.info("获取学生账号成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("学生账号不存在！");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getUser() // 获取学生的信息
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			Users user = userService.getUser(this.user.getId().intValue()); // 用户信息
			if (user != null) {
				PMUser p = new PMUser();
				p.setId(user.getId());
				p.setUsername(user.getUsername());
				p.setChineseName(user.getChineseName());
				p.setStudentNo(user.getStudentNo());
				p.setEmail(user.getEmail());
				p.setBanji(user.getBanji());
				j.setSuccess(true);
				j.setMsg("获取学生信息成功");
				j.setObj(p);
				logger.info("获取学生信息成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("学生账号不存在！");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getUserByStudentNo() // 根据学号获取学生的信息
	{
		String studentNo = user.getStudentNo();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			Users user = userService.getUserByStudentNo(studentNo); // 用户信息
			if (user != null) {
				PMUser p = new PMUser();
				p.setUsername(user.getUsername());
				p.setChineseName(user.getChineseName());
				p.setStudentNo(user.getStudentNo());
				p.setBanji(user.getBanji());
				p.setPassword(user.getPassword());
				j.setSuccess(true);
				j.setMsg("获取学生信息成功");
				j.setObj(p);
				logger.info("获取学生信息成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("学生学号不存在！");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findUserByStudentNoSchoolId() // 根据学号学校Id获取学生的信息
	{
		String studentNo = user.getStudentNo();
		int schoolId = user.getSchoolId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			if (sessionInfo.getRoleNames().equals("student") && !sessionInfo.getStudentNo().equals(studentNo)) {
				j.setSuccess(false);
				j.setMsg("请尝试安全操作");
				logger.info("恶意请求");
				super.writeJson(j);
				return;
			}
			Users user = userService.findUserByStudentNoSchoolId(studentNo, schoolId); // 用户信息
			if (user != null) {

				j.setSuccess(true);
				j.setMsg("获取学生信息成功");
				j.setObj(user);
				logger.info("获取学生信息成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("学生学号不存在！");
				super.writeJson(j);
			}
		}else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getUserAdd() // 添加学生的信息
	{
		String studentNo = user.getStudentNo();
		String chineseName = user.getChineseName();
		String username = "";
		String banji = user.getBanji();
		String password = "";
		int classId = user.getId();

		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		try {
			if (sessionInfo != null) {

				Classes onclass = classesDao.get(Classes.class, classId);
				if (onclass != null) {
					int teacherId = onclass.getTeacherId();
					Adminusers adminuser = adminusersDao.get(Adminusers.class, teacherId);
					if (adminuser != null) {
						int schoolId = adminuser.getSchoolId();
						Users user = userService.findUserByStudentNoSchoolId(studentNo, schoolId); // 用户信息
						if (user != null) {
							boolean result = classstudentsDao.findClassStudentByUserId(user.getId(), classId); // 如果为true则表明该学生已在表中
							if (result) {
								j.setSuccess(false);
								j.setMsg("该学生已存在，不能重复添加！");
								super.writeJson(j);
							} else {
								result = classstudentsDao.insertClassStudent(user.getId(), classId); // 强用户插入classstudents表
								// 更新学生人数
								int studentsNum = classstudentsDao.getClassStudentsNum(classId);
								boolean results = classesDao.updateClassStudentsNum(classId, studentsNum);
								if (results) {
									j.setSuccess(true);
									j.setMsg("添加学生信息成功");
									logger.info("添加学生信息成功");
									super.writeJson(j);
								} else {
									j.setSuccess(false);
									j.setMsg("添加学生信息失败");
									super.writeJson(j);
								}

							}

						} else {
							boolean flag = false;
							PMUser pMuser = new PMUser();
							// 将pMProblemTestCaseAdd 同属性的数据复制到problems
							pMuser.setBanji(banji);
							pMuser.setSchoolId(schoolId);
							pMuser.setChineseName(chineseName);
							pMuser.setStudentNo(studentNo);
							pMuser.setUsername(username);
							pMuser.setPassword(password);
							flag = userService.addSignStudent(pMuser);
							if (flag) {
								Users u = userService.findUserByStudentNoSchoolId(studentNo, schoolId);
								if (u != null) {
									boolean result = classstudentsDao.findClassStudentByUserId(u.getId(), classId); // 如果为true则表明该学生已在表中
									if (result == false)
										result = classstudentsDao.insertClassStudent(u.getId(), classId); // 强用户插入classstudents表
								}

								// 更新学生人数
								int studentsNum = classstudentsDao.getClassStudentsNum(classId);
								boolean results = classesDao.updateClassStudentsNum(classId, studentsNum);
								j.setSuccess(true);
								j.setMsg("添加学生信息成功");
								logger.info("添加学生信息成功");
								super.writeJson(j);
							} else {
								j.setSuccess(false);
								j.setMsg("添加学生信息失败");
								super.writeJson(j);
							}

						}
					}
				}
			} else {
				j.setSuccess(false);
				j.setMsg("请先登录。");
				super.writeJson(j);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	// 按条件查找学生
	public void findStudentsByCondition() {

		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMUser> userList = new ArrayList<PMUser>();
			if (user.getSchoolId() == 0) {
				userList = userService.getAllStudents();

			} else {
				userList = userService.findStudentsByCondition(user);
			}

			if (userList != null && userList.size() != 0) {
				j.setSuccess(true);
				j.setMsg("获取学生信息成功");
				j.setObj(userList);
				logger.info("获取学生信息成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("获取学生信息失败");
				logger.info("获取学生信息失败");
				super.writeJson(j);
			}

		} else {
			j.setSuccess(false);
			j.setMsg("请先登录");
			super.writeJson(j);
		}
	}

	public void signUser() {

		Json j = new Json();
		Users pmuser = userService.findStudentByusername(user.getUsername());
		if (pmuser == null) {
			Users u = userService.findUserByStudentNoSchoolId(user.getStudentNo(), user.getSchoolId()); // 用户信息
			if (u != null) {
				if (u.getUsername() == "" || u.getUsername().equals("")) {
					boolean results = userService.updateSignStudent(user);
					if (results) {
						j.setSuccess(true);
						j.setMsg("修改学生注册信息成功");
						logger.info("修改学生注册信息成功");
						super.writeJson(j);
					} else {
						j.setSuccess(false);
						j.setMsg("修改学生注册信息失败");
						logger.info("修改学生注册信息失败");
						super.writeJson(j);
					}
				} else {
					j.setSuccess(false);
					j.setObj(u);
					j.setMsg("1");
					logger.info("1");
					super.writeJson(j);
				}
			} else {
				boolean results = userService.addSignStudent(user);
				if (results) {
					j.setSuccess(true);
					j.setMsg("添加学生注册信息成功");
					logger.info("添加学生注册信息成功");
					super.writeJson(j);
				} else {
					j.setSuccess(false);
					j.setMsg("添加学生注册信息失败");
					logger.info("添加学生注册信息失败");
					super.writeJson(j);
				}
			}
		} else {
			j.setSuccess(false);
			j.setMsg("该账号已被使用，请换一个账号");
			logger.info("该账号已被使用，请换一个账号");
			super.writeJson(j);
		}

	}

	public void findStudentByusername() {
		Json j = new Json();
		Users pmuser = userService.findStudentByusername(user.getUsername());
		if (pmuser != null) {
			j.setSuccess(true);
			j.setMsg("查询学生信息成功");
			j.setObj(pmuser);
			logger.info("查询学生信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("查询学生信息失败");
			logger.info("查询学生信息失败");
			super.writeJson(j);
		}
	}

	public void getUserByStudentNoClassId() {
		Json j = new Json();
		int classId = user.getId();
		String studentNo = user.getStudentNo();

		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		if (sessionInfo != null) {
			Classes onclass = classesDao.get(Classes.class, classId);
			if (onclass != null) {
				int teacherId = onclass.getTeacherId();
				Adminusers adminuser = adminusersDao.get(Adminusers.class, teacherId);
				if (adminuser != null) {
					int schoolId = adminuser.getSchoolId();
					Users user = userService.findUserByStudentNoSchoolId(studentNo, schoolId); // 用户信息
					if (user != null) // users表中存在用户则只在classStudents表中插入数据
					{
						PMUser p = new PMUser();
						p.setUsername(user.getUsername());
						p.setChineseName(user.getChineseName());
						p.setStudentNo(user.getStudentNo());
						p.setBanji(user.getBanji());
						p.setPassword(user.getPassword());
						p.setEmail(user.getEmail());
						j.setSuccess(true);
						j.setMsg("获取学生信息成功");
						j.setObj(p);
						logger.info("获取学生信息成功");
						super.writeJson(j);
					} else {
						j.setSuccess(false);
						j.setMsg("学生不存在！");
						super.writeJson(j);
					}
				}

			} else {
				j.setSuccess(false);
				j.setMsg("请先登录");
				super.writeJson(j);
			}

		}
	}

	public void resetTPW() {// 重置学生密码
		// 返回前台的json数据
		Json j = new Json();
		UUID uuid = UUID.randomUUID();
		user.setUuid(uuid.toString().replaceAll("\\-", ""));
		boolean result = userService.resetTPW(user);
		if (result == true) {
			try {

				SendMail mail = new SendMail();
				// 收信人
				String[] list = { user.getEmail() };
				// mail.setMailTo(list, "cc");
				mail.setMailTo(list, "to");
				// 发信人
				mail.setMailFrom("dhuoj_noreply@163.com");
				// mail.setMailFrom("duzhen");
				// 邮件主题
				mail.setSubject("OJ系统重置密码激活邮件");
				// 邮件发送时间
				mail.setSendDate(new Date());
				// html格式邮件
				// 邮件内容
				String context = "<html>" + "<body>"
						+ "您将重置在线作业提交OJ系统的登录密码，如果这是您在进行的操作，请点击链接(如果点击无效，请复制以下链接并在浏览器中打开):<br>"
						+ "<a href='http://218.193.156.209:8080/oj/user/resetPassword.jsp?account=" + user.getUsername()
						+ "&uuid=" + user.getUuid() + "'>http://218.193.156.209:8080/oj/user/resetPassword.jsp?account="
						+ user.getUsername() + "&uuid=" + user.getUuid() + "</a>" + "</body>" + "</html>";
				mail.addHtmlContext(context);
				// txt格式邮件
				// mail.addTextContext("");
				mail.send();
				System.out.println("send success");
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.info("重置密码邮件发送成功");
			j.setSuccess(true);
			j.setMsg("重置密码邮件发送成功");
			super.writeJson(j);
		} else {
			logger.info("重置密码邮件发送失败");
			j.setSuccess(false);
			j.setMsg("重置密码邮件发送失败");
			super.writeJson(j);
		}
	}

	public void updatePasswordByUuid() {
		// 返回前台的json数据
		Json j = new Json();
		boolean result = userService.updatePasswordByUuid(user);
		if (result == true) {
			logger.info("重置密码成功");
			j.setSuccess(true);
			j.setMsg("重置密码成功");
			super.writeJson(j);
		} else {
			logger.info("重置密码失败");
			j.setSuccess(false);
			j.setMsg("重置密码失败");
			super.writeJson(j);
		}
	}

	public void updatePasswordByUserName() {
		// 返回前台的json数据
		Json j = new Json();
		boolean result = userService.updatePasswordByUserName(user);
		if (result == true) {
			logger.info("重置密码成功");
			j.setSuccess(true);
			j.setMsg("重置密码成功");
			super.writeJson(j);
		} else {
			logger.info("重置密码失败");
			j.setSuccess(false);
			j.setMsg("重置密码失败");
			super.writeJson(j);
		}
	}

}
