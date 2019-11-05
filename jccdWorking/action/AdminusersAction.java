package edu.dhu.action;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import edu.dhu.email.SendMail;
import edu.dhu.model.Adminusers;
import edu.dhu.model.AssistantClass;
import edu.dhu.model.School;
import edu.dhu.model.Users;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMAdminusers;
import edu.dhu.pageModel.PMChapters;
import edu.dhu.pageModel.PMSchool;
import edu.dhu.pageModel.PMUser;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.AdminusersServiceI;
import edu.dhu.service.AssistantClassServiceI;
import edu.dhu.service.RedisServiceI;
import edu.dhu.service.SchoolServiceI;
import edu.dhu.service.impl.RedisService;
import edu.dhu.util.IpUtil;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "adminusersAction", results = { @Result(name = "view", location = "/admin/index.jsp") })
public class AdminusersAction extends BaseAction implements
		ModelDriven<PMAdminusers> {

	AdminusersServiceI adminusersServiceI;
	AssistantClassServiceI assistantClassService;
	PMAdminusers pmadminusers = new PMAdminusers();
	int classId;
	private static final Logger logger = Logger.getLogger(ChaptersAction.class);

	private SchoolServiceI schoolServiceI;
	
	private RedisServiceI redisService;

	public RedisServiceI getRedisService() {
		return redisService;
	}

	@Autowired
	public void setRedisService(RedisServiceI redisService) {
		this.redisService = redisService;
	}

	@Autowired
	public void setAdminusersServiceI(AdminusersServiceI adminusersServiceI) {
		this.adminusersServiceI = adminusersServiceI;
	}

	public AdminusersServiceI getAdminusersServiceI() {
		return adminusersServiceI;
	}

	public SchoolServiceI getSchoolServiceI() {
		return schoolServiceI;
	}

	@Autowired
	public void setSchoolServiceI(SchoolServiceI schoolServiceI) {
		this.schoolServiceI = schoolServiceI;
	}

	@Autowired
	public void setAssistantClassService(
			AssistantClassServiceI assistantClassService) {
		this.assistantClassService = assistantClassService;
	}

	public AssistantClassServiceI getAssistantClassService() {
		return assistantClassService;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getClassId() {
		return classId;
	}

	public void findAdminuserByAccount() {
		Json j = new Json();
		PMAdminusers adminuser = adminusersServiceI
				.getAdminuserByAccount(pmadminusers.getAccount());
		if (adminuser != null) {
			j.setSuccess(true);
			j.setMsg("查询教师信息成功");
			j.setObj(adminuser);
			logger.info("查询教师信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("查询教师信息失败");
			logger.info("查询教师信息失败");
			super.writeJson(j);
		}
	}

	public void findAllAdminusers() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMAdminusers> chapters = adminusersServiceI
					.findAllAdminusers();
			logger.info("查询所有用户成功");
			j.setSuccess(true);
			j.setMsg("查询所有用户成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findAllAdminusersInProblems() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMAdminusers> chapters = adminusersServiceI
					.findAllAdminusersInProblems();

			logger.info("查询所有problems表中教师成功");
			j.setSuccess(true);
			j.setMsg("查询所有problems表中教师成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getAdminuserByAccount() {
		// 返回前台的json数据
		Json j = new Json();
		PMAdminusers adminuser = adminusersServiceI
				.getAdminuserByAccount(pmadminusers.getAccount());
		if (adminuser != null) {
			School school = schoolServiceI.getSchoolById(adminuser
					.getSchoolId());
			adminuser.setSchoolname(school.getName());
			logger.info("查询用户成功");
			j.setSuccess(true);
			j.setMsg("查询用户成功");
			j.setObj(adminuser);
		} else {
			logger.info("查询用户失败");
			j.setSuccess(false);
			j.setMsg("查询用户失败");
			j.setObj(adminuser);
		}
		super.writeJson(j);
	}

	public void findAllAdminusersBySchoolId() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<Adminusers> adminuser = adminusersServiceI
					.getAdminusersBySchoolId(pmadminusers.getSchoolId());
			if (adminuser.size() != 0) {
				logger.info("查询学校教师用户成功");
				j.setSuccess(true);
				j.setMsg("查询学校教师用户成功");
				j.setObj(adminuser);
			} else {
				logger.info("查询学校教师用户失败");
				j.setSuccess(false);
				j.setMsg("查询学校教师用户失败");
				j.setObj(adminuser);
			}
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	@Override
	public PMAdminusers getModel() {
		// TODO Auto-generated method stub
		return pmadminusers;
	}

	public void login() {
		Json j = new Json();
		PMAdminusers a = adminusersServiceI.login(pmadminusers);
		if (a != null) {
			if (a.getActive() == 0) {
				j.setSuccess(false);
				j.setMsg("请先到邮件激活才可以登陆成功");
			} else {
				SessionInfo sessionInfo = new SessionInfo();
				sessionInfo.setTeacherId(a.getId());
				sessionInfo.setTeacherName(a.getName());
				sessionInfo.setLoginName(a.getAccount());
				sessionInfo.setLoginPassword(a.getPassword());
				sessionInfo.setTeacherEmail(a.getEmail());
				sessionInfo.setIp(IpUtil.getIpAddr(ServletActionContext
						.getRequest()));
				sessionInfo.setRoleNames(a.getRole());
				sessionInfo.setSchoolId(a.getSchoolId());
				// 将登录信息放到session中
				Map<String, Object> session = ActionContext.getContext()
						.getSession();
				session.put("sessionInfo", sessionInfo);
				
//				String ping = redisService.ping();
//				if(ping == null){
//					j.setObj(null);
//					j.setSuccess(false);
//					j.setMsg("不能ping通redis服务器");
//					logger.info("ping: "+ping);
//					super.writeJson(j);
//					return;
//				}
				
				j.setObj(sessionInfo);
				j.setSuccess(true);
				j.setMsg("登录成功");
				
//				String json = JSON.toJSONStringWithDateFormat(sessionInfo,
//						"yyyy-MM-dd HH:mm:ss");
//				redisService.set("session_admin_"+sessionInfo.getTeacherId(), json, 1800);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("用户名和密码错误，登录失败！");
		}
		super.writeJson(j);
	}

	public void logout() {
		// 删除session中用户登录信息
		Map<String, Object> session = ActionContext.getContext().getSession();
		session.remove("sessionInfo");
//		redisService.del("session_admin_"+pmadminusers.getId());
		Json j = new Json();
		j.setSuccess(true);
		j.setMsg("退出登录成功");
		super.writeJson(j);
	}

	public void getAdminuserById() // 获取单个用户的信息
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			PMAdminusers adminuser = adminusersServiceI
					.getAdminuserById(pmadminusers.getId());
			logger.info("查询用户成功");
			j.setSuccess(true);
			j.setMsg("查询用户成功");
			j.setObj(adminuser);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void editAdminuser() // 编辑教师信息,能够改变的是name，role，email
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = adminusersServiceI.editAdminuser(pmadminusers);
			if (result == true) {
				logger.info("修改信息成功");
				j.setSuccess(true);
				j.setMsg("修改信息成功");
				super.writeJson(j);
			} else {
				logger.info("修改信息失败");
				j.setSuccess(false);
				j.setMsg("修改信息失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void updateTeacher() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = adminusersServiceI
					.updateTeacherByaccount(pmadminusers);
			if (result == true) {
				logger.info("修改信息成功");
				j.setSuccess(true);
				j.setMsg("修改信息成功");
				super.writeJson(j);
			} else {
				logger.info("修改信息失败");
				j.setSuccess(false);
				j.setMsg("修改信息失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void updateTeacherNoPassword() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = adminusersServiceI
					.updateTeacherNoPasswordByaccount(pmadminusers);
			if (result == true) {
				logger.info("修改信息成功");
				j.setSuccess(true);
				j.setMsg("修改信息成功");
				super.writeJson(j);
			} else {
				logger.info("修改信息失败");
				j.setSuccess(false);
				j.setMsg("修改信息失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void updateActiveByUuid() {
		// 返回前台的json数据
		Json j = new Json();
		boolean result = adminusersServiceI.updateActiveByUuid(pmadminusers
				.getUuid());
		if (result == true) {
			logger.info("激活成功");
			j.setSuccess(true);
			j.setMsg("激活成功");
			super.writeJson(j);
		} else {
			logger.info("激活失败");
			j.setSuccess(false);
			j.setMsg("激活失败");
			super.writeJson(j);
		}
	}

	public void updatePasswordByUuid() {
		// 返回前台的json数据
		Json j = new Json();
		boolean result = adminusersServiceI.updatePasswordByUuid(pmadminusers);
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

	public void updatePasswordByAccount() {
		// 返回前台的json数据
		Json j = new Json();
		boolean result = adminusersServiceI
				.updatePasswordByAccount(pmadminusers);
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

	public void resetTPW() {// 重置教师密码
		// 返回前台的json数据
		Json j = new Json();
		UUID uuid = UUID.randomUUID();
		pmadminusers.setUuid(uuid.toString().replaceAll("\\-", ""));
		boolean result = adminusersServiceI.resetTPW(pmadminusers);
		if (result == true) {
			try {

				SendMail mail = new SendMail();
				// 收信人
				String[] list = { pmadminusers.getEmail() };
				// mail.setMailTo(list, "cc");
				mail.setMailTo(list, "to");
				// 发信人
				mail.setMailFrom("dhuoj_noreply@163.com");
				// mail.setMailFrom("duzhen");
				// 邮件主题
				mail.setSubject("OJ系统重置密码（激活）邮件");
				// 邮件发送时间
				mail.setSendDate(new Date());
				// html格式邮件
				// 邮件内容
				String context = "<html>"
						+ "<body>"
						+ "您将重置在线作业提交OJ系统的登录密码，如果这是您在进行的操作，请点击链接(如果点击无效，请复制以下链接并在浏览器中打开):<br>"
						+ "<a href='http://218.193.156.209:8080/oj/admin/resetPassword.jsp?account="
						+ pmadminusers.getAccount()
						+ "&uuid="
						+ pmadminusers.getUuid()
						+ "'>http://218.193.156.209:8080/oj/admin/resetPassword.jsp?account="
						+ pmadminusers.getAccount() + "&uuid="
						+ pmadminusers.getUuid() + "</a>" + "</body>"
						+ "</html>";
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

	public void addAdminuser() // 添加教师
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = adminusersServiceI.addAdminuser(pmadminusers);
			if (result == true) {
				logger.info("添加教师成功");
				j.setSuccess(true);
				j.setMsg("添加教师成功");
				super.writeJson(j);
			} else {
				logger.info("添加教师失败");
				j.setSuccess(false);
				j.setMsg("添加教师失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void signTeacher() // 注册教师
	{
		// 返回前台的json数据
		Json j = new Json();
		/*
		 * UUID uuid = UUID.randomUUID();
		 * pmadminusers.setUuid(uuid.toString().replaceAll("\\-", ""));
		 */
		PMAdminusers pma = adminusersServiceI
				.getAdminuserByAccount(pmadminusers.getAccount());
		if (pma != null) {
			if (pma.getQuestion().equals("") || pma.getQuestion().equals(null)) {
				boolean result = adminusersServiceI
						.updateQueAndAnsByaccount(pma);
				if (result == true) {
					logger.info("注册教师成功");
					j.setSuccess(true);
					j.setMsg("申请注册教师成功,请注意查收激活邮件！");
					super.writeJson(j);
				} else {
					logger.info("注册教师失败");
					j.setSuccess(false);
					j.setMsg("注册教师失败");
					super.writeJson(j);
				}
			} else {
				logger.info("用户名重复，请修改用户名");
				j.setSuccess(false);
				j.setMsg("用户名重复，请修改用户名");
				super.writeJson(j);
			}

		} else {
			boolean result = adminusersServiceI.addTeacher(pmadminusers);
			if (result == true) {
				logger.info("注册教师成功");
				j.setSuccess(true);
				j.setMsg("注册教师成功");
				super.writeJson(j);
			} else {
				logger.info("注册教师失败");
				j.setSuccess(false);
				j.setMsg("注册教师失败");
				super.writeJson(j);
			}
			/*
			 * try {
			 * 
			 * SendMail mail = new SendMail(); // 收信人 String[] list =
			 * {pmadminusers.getEmail()}; //mail.setMailTo(list, "cc");
			 * mail.setMailTo(list, "to"); // 发信人
			 * mail.setMailFrom("dhuoj_noreply@163.com"); //
			 * mail.setMailFrom("duzhen"); // 邮件主题
			 * mail.setSubject("OJ系统教师注册激活邮件"); // 邮件发送时间 mail.setSendDate(new
			 * Date()); // html格式邮件 // 邮件内容 String context = "<html>" + "<body>"
			 * +
			 * "您的OJ系统账号已创建，如果这是您在进行的操作，请点击链接（如果点击无效，请复制以下链接并在浏览器中打开）激活账号:<br>"
			 * +
			 * "<a href='http://218.193.156.209:8080/oj/admin/activeTeacher.jsp?account="
			 * +pmadminusers.getAccount()+"&uuid="+pmadminusers.getUuid()+
			 * "'>http://218.193.156.209:8080/oj/admin/activeTeacher.jsp?account="
			 * +pmadminusers.getAccount()+"&uuid="+pmadminusers.getUuid()+"</a>"
			 * + "</body>" + "</html>"; mail.addHtmlContext(context); // txt格式邮件
			 * // mail.addTextContext(""); mail.send();
			 * System.out.println("send success"); boolean
			 * result=adminusersServiceI.addTeacher(pmadminusers);
			 * if(result==true){ logger.info("注册教师成功"); j.setSuccess(true);
			 * j.setMsg("申请注册教师成功,请注意查收激活邮件！"); super.writeJson(j); }else{
			 * logger.info("注册教师失败"); j.setSuccess(false); j.setMsg("注册教师失败");
			 * super.writeJson(j); }
			 * 
			 * } catch (Exception e) { e.printStackTrace();
			 * logger.info("注册教师邮件发送失败,请联系管理员11249242@qq.com");
			 * j.setSuccess(false);
			 * j.setMsg("注册教师邮件发送失败,请联系管理员11249242@qq.com"); super.writeJson(j);
			 * }
			 */
		}

	}

	public void deleteAdminuser() // 删除教师
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = adminusersServiceI.deleteAminuser(pmadminusers);
			if (result == true) {
				logger.info("删除用户成功");
				j.setSuccess(true);
				j.setMsg("删除用户成功");
				super.writeJson(j);
			} else {
				logger.info("删除用户失败");
				j.setSuccess(false);
				j.setMsg("删除用户失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getPassword() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			Adminusers user = adminusersServiceI.getPassword(pmadminusers);
			if (user != null) {
				logger.info("获取教师密码成功");
				j.setSuccess(true);
				j.setObj(user);
				j.setMsg("获取教师密码成功");
				super.writeJson(j);
			} else {
				logger.info("获取教师密码失败");
				j.setSuccess(false);
				j.setMsg("获取教师密码失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void editPassword() // 修改教师密码
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = adminusersServiceI.editPassword(pmadminusers);
			if (result == true) {
				logger.info("修改密码成功");
				j.setSuccess(true);
				j.setMsg("修改密码成功");
				super.writeJson(j);
			} else {
				logger.info("修改密码失败");
				j.setSuccess(false);
				j.setMsg("修改教师密码失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void addAssistant() // 添加助管
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = adminusersServiceI.addAssistant(pmadminusers);
			if (result) {
				PMAdminusers user = adminusersServiceI
						.getAdminuserByAccount(pmadminusers.getAccount());
				logger.info("添加助教成功");
				j.setObj(user);
				j.setSuccess(true);
				j.setMsg("添加助教成功");
				super.writeJson(j);
			} else {
				logger.info("添加助教失败");
				j.setSuccess(false);
				j.setMsg("添加助教失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findAllAssistant() // 查看所有助教信息
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMAdminusers> list = adminusersServiceI
					.findAllAssistant(pmadminusers);
			logger.info("查看助教信息成功");
			j.setObj(list);
			j.setSuccess(true);
			j.setMsg("查看助教信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getAssistantById() // 查找助教的信息
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			PMAdminusers assistant = adminusersServiceI
					.getAdminuserById(pmadminusers.getId());
			List<AssistantClass> list = assistantClassService
					.getAssistantClassByAssistantId(pmadminusers.getId());
			String classIds = "";
			for (int i = 0; i < list.size(); i++) {
				classIds += list.get(i).getClassId().intValue() + ",";
			}
			assistant.setClassIds(classIds);
			logger.info("查询用户成功");
			j.setSuccess(true);
			j.setMsg("查询用户成功");
			j.setObj(assistant);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void editAssistant() // 自该助教信息
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = adminusersServiceI.editAssistant(pmadminusers);
			assistantClassService.editAssistantClass(
					pmadminusers.getTeacherId(), pmadminusers.getId(),
					pmadminusers.getClassIds());
			if (result == true) {
				logger.info("修改信息成功");
				j.setSuccess(true);
				j.setMsg("修改信息成功");
				super.writeJson(j);
			} else {
				logger.info("修改信息失败");
				j.setSuccess(false);
				j.setMsg("修改信息失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getAssistantByClassId() // 获取班级所属的助教
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMAdminusers> list = adminusersServiceI
					.getAssistantByClassId(classId);
			logger.info("查看助教信息成功");
			j.setObj(list);
			j.setSuccess(true);
			j.setMsg("查看助教信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void getAssistantNotBelongClass() // 查看不属于班级的助教
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMAdminusers> list = adminusersServiceI
					.getAssistantNotBelongClass(classId,
							pmadminusers.getAccount(), pmadminusers.getName());
			logger.info("查看不属于班级的助教信息成功");
			j.setObj(list);
			j.setSuccess(true);
			j.setMsg("查看不属于班级的助教信息成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}
}
