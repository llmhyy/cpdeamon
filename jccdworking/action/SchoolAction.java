package edu.dhu.action;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.email.NewSendEmail;
import edu.dhu.email.SendMail;
import edu.dhu.model.Adminusers;
import edu.dhu.model.School;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMSchool;
import edu.dhu.service.AdminusersServiceI;
import edu.dhu.service.SchoolServiceI;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "schoolAction", results = { @Result(name = "view", location = "/admin/index.jsp") })
public class SchoolAction extends BaseAction implements ModelDriven<PMSchool> {

	private static final Logger logger = Logger.getLogger(ChaptersAction.class);
	private SchoolServiceI schoolServiceI;
	PMSchool pmschool = new PMSchool();
	private AdminusersServiceI adminusersServiceI;

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

	@Override
	public PMSchool getModel() {
		// TODO Auto-generated method stub
		return pmschool;
	}

	public void getShoolById() {
		// 返回前台的json数据
		Json j = new Json();
		School school = schoolServiceI.getSchoolById(pmschool.getId());
		logger.info("查询学校成功");
		j.setSuccess(true);
		j.setObj(school);
		super.writeJson(j);
	}

	public void findAllShools() {
		// 返回前台的json数据
		Json j = new Json();
		List<PMSchool> schools = schoolServiceI.findAllShools();
		logger.info("查询所有学校成功");
		j.setSuccess(true);
		j.setObj(schools);
		super.writeJson(j);
	}

	public void findAllShoolsInAdminusers() {
		// 返回前台的json数据
		Json j = new Json();
		List<PMSchool> schools = schoolServiceI.findAllShools();
		List<PMSchool> pmschools = new ArrayList<PMSchool>();
		for (int i = 0; i < schools.size(); i++) {
			List<Adminusers> adminList = adminusersServiceI
					.getAdminusersBySchoolId(schools.get(i).getId());
			if (adminList.size() != 0) {
				PMSchool pmschool = new PMSchool();
				pmschool.setId(schools.get(i).getId());
				pmschool.setName(schools.get(i).getName());
				pmschools.add(pmschool);
			}
		}
		logger.info("查询所有Adminusers表中学校成功");
		j.setSuccess(true);
		j.setObj(pmschools);
		super.writeJson(j);
	}

	public void getSchoolByName() {
		// 返回前台的json数据
		Json j = new Json();
		PMSchool school = schoolServiceI.getSchoolByName(pmschool.getName());
		if (school != null) {
			logger.info("查询学校成功");
			j.setSuccess(true);
			j.setObj(school);
		} else {
			logger.info("学校不存在");
			j.setSuccess(false);
			j.setObj(school);
		}
		super.writeJson(j);
	}

	public void updateSchool() {
		// 返回前台的json数据
		Json j = new Json();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String time = format.format(date);
		boolean result = schoolServiceI.editSchool(pmschool);
		if (result == true) {
			try {
				SendMail mail = new SendMail();
				// 收信人
				String[] list = { pmschool.getEmail() };
				// mail.setMailTo(list, "cc");
				mail.setMailTo(list, "to");
				// 发信人
				mail.setMailFrom("dhuoj_noreply@163.com");
				// mail.setMailFrom("duzhen");
				// 邮件主题
				mail.setSubject("OJ系统邮件提醒:" + pmschool.getName() + "学校名称修改成功");
				// 邮件发送时间
				mail.setSendDate(new Date());
				// html格式邮件
				// 邮件内容
				String context = "<html>"
						+ "<body>"
						+ ""
						+ pmschool.getTeacherName()
						+ "老师您好:<br>"
						+ ""
						+ pmschool.getName()
						+ "学校已修改成功，您可立即<a href='http://218.193.156.209:8080/oj/admin/index.jsp'>登录</a>账号.<br>"
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;OJ系统管理员<br>"
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;日期:"
						+ time + "<br>" + "</body>" + "</html>";
				mail.addHtmlContext(context);
				// txt格式邮件
				// mail.addTextContext("");
				mail.send();
				logger.info("邮件发送成功");
				j.setSuccess(true);
				j.setMsg("邮件发送成功");
				super.writeJson(j);
			} catch (Exception e) {
				logger.info("邮件发送失败");
				j.setSuccess(false);
				j.setMsg("邮件发送失败");
				e.printStackTrace();
				super.writeJson(j);
			}
			logger.info("修改学校名称成功");
			j.setSuccess(true);
			j.setMsg("修改学校名称成功");
			super.writeJson(j);
		} else {
			logger.info("修改学校名称失败");
			j.setSuccess(true);
			j.setMsg("修改学校名称失败");
			super.writeJson(j);
		}
	}

	public void editOrAddSchool() {
		// 返回前台的json数据
		Json j = new Json();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String time = format.format(date);
		if (pmschool.getOperate().equals("editSchool")) {
			School school = schoolServiceI.getSchoolById(pmschool.getId());
			try {

				// 1. 创建参数配置, 用于连接邮件服务器的参数配置
				Properties props = new Properties(); // 参数配置
				props.setProperty("mail.transport.protocol", "smtp"); // 使用的协议（JavaMail规范要求）
				props.setProperty("mail.smtp.host", "smtp.163.com"); // 发件人的邮箱的
																		// SMTP
																		// 服务器地址
				props.setProperty("mail.smtp.auth", "true"); // 需要请求认证

				// PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接,
				// 也可以自己开启),
				// 如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
				// 打开下面 /* ... */ 之间的注释代码, 开启 SSL 安全连接。
				/*
				 * // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接, //
				 * 需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助, //
				 * QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看) final String smtpPort =
				 * "465"; props.setProperty("mail.smtp.port", smtpPort);
				 * props.setProperty("mail.smtp.socketFactory.class",
				 * "javax.net.ssl.SSLSocketFactory");
				 * props.setProperty("mail.smtp.socketFactory.fallback",
				 * "false"); props.setProperty("mail.smtp.socketFactory.port",
				 * smtpPort);
				 */
				String subject = "OJ系统请求修改学校名称（本邮件由系统激活发送）";
				// String context="测试系统";
				// 邮件内容
				String context = "<html>" + "<body>" + ""
						+ pmschool.getTeacherName()
						+ "教师申请修改:<br>"
						+ "旧学校名称:"
						+ school.getName()
						+ "<br>"
						+ "学校名称:"
						+ pmschool.getName()
						+ "<br>"
						+ "联系邮箱:"
						+ pmschool.getEmail()
						+ "<br>"
						+ "时间:"
						+ time
						+ "<br>"
						+ "操作请点击以下链接:<br>"
						+ "<a href='http://218.193.156.209:8080/oj/admin/updateSchool.jsp?oldSchoolId="
						+ pmschool.getId()
						+ "&schoolname="
						+ URLEncoder.encode(pmschool.getName(), "UTF-8")
						+ "&teacherName="
						+ URLEncoder.encode(pmschool.getTeacherName(), "UTF-8")
						+ "&email="
						+ pmschool.getEmail()
						+ "'>http://218.193.156.209:8080/oj/admin/updateSchool.jsp?oldSchoolId="
						+ pmschool.getId() + "&schoolname="
						+ pmschool.getName() + "&teacherName="
						+ pmschool.getTeacherName() + "&email="
						+ pmschool.getEmail() + "</a>" + "</html>";
				// 2. 根据配置创建会话对象, 用于和邮件服务器交互
				Session session = Session.getDefaultInstance(props);
				session.setDebug(true); // 设置为debug模式, 可以查看详细的发送 log
				String myEmailAccount = "dhuoj_noreply@163.com";
				String receiveMailAccount = "11249242@qq.com";
				String myEmailPassword = "dhuoj124";
				// 3. 创建一封邮件
				MimeMessage message = NewSendEmail.createMimeMessage(session,
						subject, context, myEmailAccount, receiveMailAccount);

				// 4. 根据 Session 获取邮件传输对象
				Transport transport = session.getTransport();

				// 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
				//
				// PS_01: 成败的判断关键在此一句, 如果连接服务器失败, 都会在控制台输出相应失败原因的 log,
				// 仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接, 根据给出的错误
				// 类型到对应邮件服务器的帮助网站上查看具体失败原因。
				//
				// PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
				// (1) 邮箱没有开启 SMTP 服务;
				// (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
				// (3) 邮箱服务器要求必须要使用 SSL 安全连接;
				// (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
				// (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
				//
				// PS_03: 仔细看log, 认真看log, 看懂log, 错误原因都在log已说明。
				transport.connect(myEmailAccount, myEmailPassword);

				// 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients()
				// 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
				transport.sendMessage(message, message.getAllRecipients());

				// 7. 关闭连接
				transport.close();

				/*
				 * SendMail mail = new SendMail(); // 收信人 String[] list =
				 * {"931006018@qq.com"}; //mail.setMailTo(list, "cc");
				 * mail.setMailTo(list, "to"); // 发信人
				 * mail.setMailFrom("dhuoj_noreply@163.com"); //
				 * mail.setMailFrom("duzhen"); // 邮件主题
				 * mail.setSubject("OJ系统邮件"+school.getName()+"修改学校名称"); //
				 * 邮件发送时间 mail.setSendDate(new Date()); // html格式邮件 // 邮件内容
				 * String context = "<html>" + "<body>" +
				 * ""+pmschool.getTeacherName()+"教师申请修改:<br>" +
				 * "旧学校名称:"+school.getName()+"<br>" +
				 * "学校名称:"+pmschool.getName()+"<br>" +
				 * "联系邮箱:"+pmschool.getEmail()+"<br>" + //"时间:"+time+"<br>" +
				 * "操作请点击以下链接:<br>" +
				 * "<a href='http://218.193.156.209:8080/oj/admin/updateSchool.jsp?oldSchoolId="
				 * +pmschool.getId()+"&schoolname="+URLEncoder.encode(pmschool.
				 * getName
				 * (),"UTF-8")+"&teacherName="+URLEncoder.encode(pmschool.
				 * getTeacherName(),"UTF-8")+"&email="+pmschool.getEmail()+
				 * "'>218.193.156.209:8080/oj/admin/updateSchool.jsp?oldSchoolId="
				 * +pmschool.getId()+"&schoolname="+pmschool.getName()+
				 * "&teacherName="
				 * +pmschool.getTeacherName()+"&email="+pmschool.getEmail
				 * ()+"</a>" + "</body>" + "</html>";
				 * mail.addHtmlContext(context); // txt格式邮件 //
				 * mail.addTextContext(""); mail.send();
				 */
				logger.info("邮件发送成功");
				j.setSuccess(true);
				j.setMsg("邮件发送成功");
				super.writeJson(j);
			} catch (Exception e) {
				logger.info("邮件发送失败");
				j.setSuccess(false);
				j.setMsg("邮件发送失败");
				e.printStackTrace();
				super.writeJson(j);
			}
		} else if (pmschool.getOperate().equals("addSchool")) {
			try {

				// 1. 创建参数配置, 用于连接邮件服务器的参数配置
				Properties props = new Properties(); // 参数配置
				props.setProperty("mail.transport.protocol", "smtp"); // 使用的协议（JavaMail规范要求）
				props.setProperty("mail.smtp.host", "smtp.163.com"); // 发件人的邮箱的
																		// SMTP
																		// 服务器地址
				props.setProperty("mail.smtp.auth", "true"); // 需要请求认证

				// PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接,
				// 也可以自己开启),
				// 如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
				// 打开下面 /* ... */ 之间的注释代码, 开启 SSL 安全连接。
				/*
				 * // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接, //
				 * 需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助, //
				 * QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看) final String smtpPort =
				 * "465"; props.setProperty("mail.smtp.port", smtpPort);
				 * props.setProperty("mail.smtp.socketFactory.class",
				 * "javax.net.ssl.SSLSocketFactory");
				 * props.setProperty("mail.smtp.socketFactory.fallback",
				 * "false"); props.setProperty("mail.smtp.socketFactory.port",
				 * smtpPort);
				 */
				String subject = "OJ系统请求添加学校邮件（本邮件由系统激活发送）";
				// 邮件内容
				String context = "<html>" + "<body>" + ""
						+ pmschool.getTeacherName()
						+ "教师请求添加:<br>"
						+ "学校名称:"
						+ pmschool.getName()
						+ "<br>"
						+ "联系邮箱:"
						+ pmschool.getEmail()
						+ "<br>"
						+ "时间:"
						+ time
						+ "<br>"
						+ "操作请点击以下链接:<br>"
						+ "<a href='http://218.193.156.209:8080/oj/admin/addSchool.jsp?schoolname="
						+ URLEncoder.encode(pmschool.getName(), "UTF-8")
						+ "&teacherName="
						+ URLEncoder.encode(pmschool.getTeacherName(), "UTF-8")
						+ "&email="
						+ pmschool.getEmail()
						+ "'>http://218.193.156.209:8080/oj/admin/addSchool.jsp?schoolname="
						+ URLEncoder.encode(pmschool.getName(), "UTF-8")
						+ "&teacherName="
						+ URLEncoder.encode(pmschool.getTeacherName(), "UTF-8")
						+ "&email=" + pmschool.getEmail() + "</a>" + "</body>"
						+ "</html>";
				// 2. 根据配置创建会话对象, 用于和邮件服务器交互
				Session session = Session.getDefaultInstance(props);
				session.setDebug(true); // 设置为debug模式, 可以查看详细的发送 log
				String myEmailAccount = "dhuoj_noreply@163.com";
				String receiveMailAccount = "11249242@qq.com";// 11249242@qq.com
				String myEmailPassword = "dhuoj124";
				// 3. 创建一封邮件
				MimeMessage message = NewSendEmail.createMimeMessage(session,
						subject, context, myEmailAccount, receiveMailAccount);

				// 4. 根据 Session 获取邮件传输对象
				Transport transport = session.getTransport();

				// 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
				//
				// PS_01: 成败的判断关键在此一句, 如果连接服务器失败, 都会在控制台输出相应失败原因的 log,
				// 仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接, 根据给出的错误
				// 类型到对应邮件服务器的帮助网站上查看具体失败原因。
				//
				// PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
				// (1) 邮箱没有开启 SMTP 服务;
				// (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
				// (3) 邮箱服务器要求必须要使用 SSL 安全连接;
				// (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
				// (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
				//
				// PS_03: 仔细看log, 认真看log, 看懂log, 错误原因都在log已说明。
				transport.connect(myEmailAccount, myEmailPassword);

				// 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients()
				// 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
				transport.sendMessage(message, message.getAllRecipients());

				// 7. 关闭连接
				transport.close();
				/*
				 * SendMail mail = new SendMail(); // 收信人 String[] list =
				 * {"931006018@qq.com"}; //mail.setMailTo(list, "cc");
				 * mail.setMailTo(list, "to"); // 发信人
				 * mail.setMailFrom("dhuoj_noreply@163.com"); //
				 * mail.setMailFrom("duzhen"); // 邮件主题
				 * mail.setSubject("OJ系统请求添加学校邮件"); // 邮件发送时间
				 * mail.setSendDate(new Date()); // html格式邮件 // 邮件内容 String
				 * context = "<html>" + "<body>" +
				 * ""+pmschool.getTeacherName()+"教师请求添加:<br>" +
				 * "学校名称:"+pmschool.getName()+"<br>" +
				 * "联系邮箱:"+pmschool.getEmail()+"<br>" + "时间:"+time+"<br>" +
				 * "操作请点击以下链接:<br>" +
				 * "<a href='http://218.193.156.209:8080/oj/admin/addSchool.jsp?schoolname="
				 * +
				 * URLEncoder.encode(pmschool.getName(),"UTF-8")+"&teacherName="
				 * +
				 * URLEncoder.encode(pmschool.getTeacherName(),"UTF-8")+"&email="
				 * +pmschool.getEmail()+
				 * "'>http://218.193.156.209:8080/oj/admin/addSchool.jsp?schoolname="
				 * +
				 * pmschool.getName()+"&teacherName="+pmschool.getTeacherName()+
				 * "&email="+pmschool.getEmail()+"</a>" + "</body>" + "</html>";
				 * mail.addHtmlContext(context); // txt格式邮件 //
				 * mail.addTextContext(""); mail.send();
				 */
				logger.info("邮件发送成功");
				j.setSuccess(true);
				j.setMsg("邮件发送成功");
				super.writeJson(j);
			} catch (Exception e) {
				logger.info("邮件发送失败");
				j.setSuccess(false);
				j.setMsg("邮件发送失败");
				e.printStackTrace();
				super.writeJson(j);
			}

		} else {
			logger.info("发送邮件失败");
			j.setSuccess(false);
			j.setMsg("发送邮件失败");
			super.writeJson(j);
		}
	}

	public void addSchool() {
		// 返回前台的json数据
		Json j = new Json();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String time = format.format(date);

		try {
			SendMail mail = new SendMail();
			// 收信人
			String[] list = { pmschool.getEmail() };
			// mail.setMailTo(list, "cc");
			mail.setMailTo(list, "to");
			// 发信人
			mail.setMailFrom("dhuoj_noreply@163.com");
			// mail.setMailFrom("duzhen");
			// 邮件主题
			mail.setSubject("OJ系统邮件提醒:" + pmschool.getName() + "学校成功");
			// 邮件发送时间
			mail.setSendDate(new Date());
			// html格式邮件
			// 邮件内容
			String context = "<html>"
					+ "<body>"
					+ ""
					+ pmschool.getTeacherName()
					+ "老师您好:<br>"
					+ ""
					+ pmschool.getName()
					+ "学校已添加成功，您可立即<a href='http://218.193.156.209:8080/oj/admin/signTeacher.jsp'>注册</a>账号.<br>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;OJ系统管理员<br>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;日期:"
					+ time + "<br>" + "</body>" + "<ml>";
			mail.addHtmlContext(context);
			// txt格式邮件
			// mail.addTextContext("");
			mail.send();
			logger.info("邮件发送成功");
			boolean result = schoolServiceI.addSchool(pmschool);
			if (result == true) {
				logger.info("添加学校成功");
				j.setSuccess(true);
				j.setMsg("添加学校成功");
				super.writeJson(j);

			} else {
				logger.info("添加学校失败");
				j.setSuccess(true);
				j.setMsg("添加学校失败");
				super.writeJson(j);
			}
		} catch (Exception e) {
			logger.info("邮件发送失败");
			j.setSuccess(false);
			j.setMsg("邮件发送失败");
			e.printStackTrace();
			super.writeJson(j);
		}
	}

	public void sendUpdateReason() {
		// 返回前台的json数据
		Json j = new Json();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String time = format.format(date);
		try {
			SendMail mail = new SendMail();
			// 收信人
			String[] list = { pmschool.getEmail() };
			// mail.setMailTo(list, "cc");
			mail.setMailTo(list, "to");
			// 发信人
			mail.setMailFrom("dhuoj_noreply@163.com");
			// mail.setMailFrom("duzhen");
			// 邮件主题
			mail.setSubject("OJ系统邮件提醒:" + pmschool.getName() + "学校名称修改不成功");
			// 邮件发送时间
			mail.setSendDate(new Date());
			// html格式邮件
			// 邮件内容
			String context = "<html>"
					+ "<body>"
					+ ""
					+ pmschool.getTeacherName()
					+ "老师您好:<br>"
					+ "&nbsp;&nbsp;&nbsp;"
					+ pmschool.getName()
					+ "无效，无法修改<br>"
					+ "&nbsp;&nbsp;&nbsp;具体信息:<br>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ pmschool.getReason()
					+ "<br>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;OJ系统管理员<br>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;日期:"
					+ time + "<br>" + "</body>" + "</html>";
			mail.addHtmlContext(context);
			// txt格式邮件
			// mail.addTextContext("");
			mail.send();
			logger.info("邮件发送成功");
			j.setSuccess(true);
			j.setMsg("邮件发送成功");
			super.writeJson(j);
		} catch (Exception e) {
			logger.info("邮件发送失败");
			j.setSuccess(false);
			j.setMsg("邮件发送失败");
			e.printStackTrace();
			super.writeJson(j);
		}
	}

	public void sendReason() {
		// 返回前台的json数据
		Json j = new Json();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String time = format.format(date);
		try {
			SendMail mail = new SendMail();
			// 收信人
			String[] list = { pmschool.getEmail() };
			// mail.setMailTo(list, "cc");
			mail.setMailTo(list, "to");
			// 发信人
			mail.setMailFrom("dhuoj_noreply@163.com");
			// mail.setMailFrom("duzhen");
			// 邮件主题
			mail.setSubject("OJ系统邮件提醒:" + pmschool.getName() + "学校激活不成功");
			// 邮件发送时间
			mail.setSendDate(new Date());
			// html格式邮件
			// 邮件内容
			String context = "<html>"
					+ "<body>"
					+ ""
					+ pmschool.getTeacherName()
					+ "老师您好:<br>"
					+ "&nbsp;&nbsp;&nbsp;"
					+ pmschool.getName()
					+ "无效，无法添加<br>"
					+ "&nbsp;&nbsp;&nbsp;具体信息:<br>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ pmschool.getReason()
					+ "<br>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;OJ系统管理员<br>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;日期:"
					+ time + "<br>" + "</body>" + "</html>";
			mail.addHtmlContext(context);
			// txt格式邮件
			// mail.addTextContext("");
			mail.send();
			logger.info("邮件发送成功");
			j.setSuccess(true);
			j.setMsg("邮件发送成功");
			super.writeJson(j);
		} catch (Exception e) {
			logger.info("邮件发送失败");
			j.setSuccess(false);
			j.setMsg("邮件发送失败");
			e.printStackTrace();
			super.writeJson(j);
		}
	}
}
