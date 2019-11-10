package edu.dhu.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

import edu.dhu.dao.AdminusersDaoI;
import edu.dhu.dao.ClassesDaoI;
import edu.dhu.dao.ClassstudentsDaoI;
import edu.dhu.model.Adminusers;
import edu.dhu.model.Classes;
import edu.dhu.model.Users;
import edu.dhu.pageModel.PMUser;
import edu.dhu.service.UserServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "importClassStudentsFileAction", results = {
		@Result(name = "SUCCESS", location = "/admin/uploadFileSuccess.jsp"),
		@Result(name = "FAIL", location = "/admin/uploadFileFail.jsp") })
public class importClassStudentsFileAction extends ActionSupport {
	private File upload;
	private String uploadContentType;
	private String uploadFileName;
	private String savePath;
	int classId;

	private UserServiceI userService;
	private ClassstudentsDaoI classstudentsDao;
	private ClassesDaoI classesDao;
	private AdminusersDaoI adminusersDao;

	@Autowired
	public void setAdminusersDaoI(AdminusersDaoI adminusersDao) {
		this.adminusersDao = adminusersDao;
	}

	public AdminusersDaoI getAdminusersDaoI() {
		return adminusersDao;
	}

	@Autowired
	public void setUserServiceI(UserServiceI userService) {
		this.userService = userService;
	}

	public UserServiceI getUserServiceI() {
		return userService;
	}

	@Autowired
	public void setClassstudentsDao(ClassstudentsDaoI classstudentsDao) {
		this.classstudentsDao = classstudentsDao;
	}

	public ClassstudentsDaoI getClassstudentsDaoI() {
		return classstudentsDao;
	}

	public void setSavePath(String value) {
		this.savePath = value;
	}

	private String getSavePath() throws Exception {
		return ServletActionContext.getServletContext().getRealPath("/file/");
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}

	public File getUpload() {
		return this.upload;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}

	public String getUploadContentType() {
		return this.uploadContentType;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public String getUploadFileName() {
		return this.uploadFileName;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getClassId() {
		return classId;
	}

	@Autowired
	public void setClassesDao(ClassesDaoI classesDao) {
		this.classesDao = classesDao;
	}

	public ClassesDaoI getClassesDao() {
		return classesDao;
	}

	@Override
	public String execute() {
		Calendar c = Calendar.getInstance();// 可以对每个时间域单独修改
		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH));
		if (month.length() == 1)
			month = "0" + month;
		String date = String.valueOf(c.get(Calendar.DATE));
		if (date.length() == 1)
			date = "0" + date;
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		if (hour.length() == 1)
			hour = "0" + hour;
		String minute = String.valueOf(c.get(Calendar.MINUTE));
		if (minute.length() == 1)
			minute = "0" + minute;
		String second = String.valueOf(c.get(Calendar.SECOND));
		if (second.length() == 1)
			second = "0" + second;
		String[] temp = uploadFileName.split("\\.");
		String ext = "." + temp[1]; // 得到文件后缀
		if (ext.equals(".csv") == false)
			return "FAIL";
		uploadFileName = year + "" + month + "" + date + "" + hour + ""
				+ minute + "" + second + ext; // 以时间作为文件名
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(getSavePath() + "\\"
					+ getUploadFileName());
			FileInputStream fis = new FileInputStream(getUpload());
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			fis.close();
			boolean result = decodeFile(getSavePath() + "\\"
					+ getUploadFileName()); // 解析文件
			if (result == false) {
				// System.out.println(getSavePath()+"\\"+getUploadFileName()+"文件格式不正确!");
				File file = new File(getSavePath() + "\\" + getUploadFileName());
				file.delete();
				return "FAIL";
			}
			// System.out.println(getSavePath()+"\\"+getUploadFileName()+"文件解析成功!");
			File file = new File(getSavePath() + "\\" + getUploadFileName()); // 删除文件
			file.delete();
			return "SUCCESS";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// System.out.println(getUploadFileName()+"上传失败!");
			return "FAIL";
		}

	}

	public boolean decodeFile(String filePath) // 解析csv文件
	{
		File file = new File(filePath);
		List<PMUser> userList = new ArrayList<PMUser>();
		try {
			FileReader in1 = new FileReader(file);
			BufferedReader in2 = new BufferedReader(in1);
			String s = "";
			String row[];
			s = in2.readLine();
			row = s.split(",");
			if (row[0].equals("学号") && row[1].equals("姓名")
					&& row[2].equals("自然班级")) {
				while ((s = in2.readLine()) != null) {
					row = s.split(",");
					if (row.length < 2)
						return false;
					String studentNo = row[0];
					String chineseName = row[1];
					String banji = row[2];
					PMUser user = new PMUser(); // 将信息存储
					user.setStudentNo(studentNo);
					user.setChineseName(chineseName);
					user.setBanji(banji);
					userList.add(user);
				}
				Classes onclass = classesDao.get(Classes.class, classId);
				if (onclass != null) {
					int teacherId = onclass.getTeacherId();
					Adminusers adminuser = adminusersDao.get(Adminusers.class,
							teacherId);
					if (adminuser != null) {
						int schoolId = adminuser.getSchoolId();
						// 开始导入信息
						for (int i = 0; i < userList.size(); i++) {
							PMUser p = userList.get(i);
							p.setSchoolId(schoolId);
							String studentNo = p.getStudentNo();

							Users user = userService
									.findUserByStudentNoSchoolId(studentNo,
											schoolId); // 用户信息
							if (user != null) // users表中存在用户则只在classStudents表中插入数据并且更新学生班级
							{
								user.setBanji(p.getBanji());
								boolean updateresult = userService.updateStudentBanji(user);
								if(updateresult){
									int id = user.getId();
									boolean result = classstudentsDao
											.findClassStudentByUserId(id, classId); // 如果为true则表明该学生已在表中
									if (result == false){
										result = classstudentsDao
												.insertClassStudent(id, classId); // 将用户插入classstudents表
									}
								}
							} else {
								p = userList.get(i);
								boolean result = userService.insertUser(p);
								if (result == true) {
									studentNo = p.getStudentNo();
									user = userService
											.findUserByStudentNoSchoolId(
													studentNo, schoolId);
									int id = user.getId();
									result = classstudentsDao
											.insertClassStudent(id, classId); // 将用户插入classstudents表
								}

							}
						}
						// 更新学生人数
						int studentsNum = classstudentsDao
								.getClassStudentsNum(classId);
						boolean result = classesDao.updateClassStudentsNum(
								classId, studentsNum);

					}
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
