package edu.dhu.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;
import com.sun.org.apache.bcel.internal.classfile.Field;

import edu.dhu.dao.ExamClassesDaoI;
import edu.dhu.model.Classes;
import edu.dhu.model.SoftwareVersion;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.SoftwareVersionMServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "SoftwareVersionMAction", results = { @Result(name = "nologin", location = "/admin/index.jsp") })
public class SoftwareVersionMAction extends BaseAction{

	//对应普通数据库操作的字段
	private static final Logger logger = Logger.getLogger(SoftwareVersionMAction.class);
	private static final String root_UPV = ServletActionContext.getServletContext().getRealPath("/UpDateVersion")+"\\UPV";
	private static final String root_MDF = ServletActionContext.getServletContext().getRealPath("/UpDateVersion")+"\\MD5s";
	private static final String root_ZIP = ServletActionContext.getServletContext().getRealPath("/UpDateVersion")+"\\Zips";
	private HttpSession sessiont = ServletActionContext.getRequest().getSession();
	private SoftwareVersionMServiceI softwareVersionMServiceI;
	private int versionId;
	private String versionName;
	private String versionDescription;	
	//对应文件上传下载和进度匹配的移植字段
	private File TheFile;// 对应文件域的file，封装文件内容
    private String FileName;// 封装文件名
    private String DFileName;//封装请求下载的文件名
    private String CFileName;//封装请求查询处理进度的文件名
    private String hiddenFlag;//封装请求的上传还是更新类型
    private int downloadName;//封装请求下载的文件名对应id
    private String CHECK_NAME="CHECK...UPLOAD";//上传总进度存储名称
    
    private int progress_num=0;
    private final double total_progress_upload = 50.0;
    private final double total_progress_unzip = 29.0;
    private final double total_progress_initialMD5 = 10.0;
    private final double total_progress_zip = 10.0;
    //key唯一标识位+_文件名 value=进度条数
    private static Map<String,Integer> map_user_progress=new HashMap<String,Integer>();
    

	public int getDownloadName() {
		return downloadName;
	}

	public void setDownloadName(int downloadName) {
		this.downloadName = downloadName;
	}

	private Map<String, Object> session = ActionContext.getContext().getSession();
	
	public int getVersionId() {
		return versionId;
	}

	public String getHiddenFlag() {
		return hiddenFlag;
	}

	public void setHiddenFlag(String hiddenFlag) {
		this.hiddenFlag = hiddenFlag;
	}

	public void setVersionId(int versionId) {
		this.versionId = versionId;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getVersionDescription() {
		return versionDescription;
	}

	public void setVersionDescription(String versionDescription) {
		this.versionDescription = versionDescription;
	}

	public SoftwareVersionMServiceI getSoftwareVersionMServiceI() {
		return softwareVersionMServiceI;
	}
	
	@Autowired
	public void setSoftwareVersionMServiceI(
			SoftwareVersionMServiceI softwareVersionMServiceI) {
		this.softwareVersionMServiceI = softwareVersionMServiceI;
	}

	public File getTheFile() {
		return TheFile;
	}

	public void setTheFile(File theFile) {
		TheFile = theFile;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public String getDFileName() {
		return DFileName;
	}

	public void setDFileName(String dFileName) {
		DFileName = dFileName;
	}

	public String getCFileName() {
		return CFileName;
	}

	public void setCFileName(String cFileName) {
		CFileName = cFileName;
	}
	
	private String getCHECK_Name(){
		return "CHECK_"+getFileName();
	}

	//获取所有版本信息
	public void getAllVersionMessage(){
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			List<SoftwareVersion> softwareVersion = softwareVersionMServiceI.getAllSoftwareVersion();
			logger.info("查询所有客户端软件版本成功....getAllVersionMessage");
			j.setSuccess(true);
			j.setMsg("查询所有客户端软件版本成功");
			j.setObj(softwareVersion);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}
	
	//修改版本信息
	public void UpdateVersionMessage(){
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			if(softwareVersionMServiceI.editSoftwareVersion(versionId, versionName, versionDescription)){
				logger.info("更改客户端软件版本"+versionId+"成功....UpdateVersionMessage");
				j.setSuccess(true);
				j.setMsg("更改客户端软件版本"+versionId+"成功");
				super.writeJson(j);
			}else{
				logger.info("更改客户端软件版本"+versionId+"失败....UpdateVersionMessage");
				j.setSuccess(false);
				j.setMsg("更改客户端软件版本"+versionId+"失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}
	
	//删除对应id的版本信息，并删除对应的软件包和目录以及相关md5文件
	public void DeleteVersion(){
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		Json j = new Json();
		if (sessionInfo != null) {
			SoftwareVersion sw = softwareVersionMServiceI.getSoftwareByID(versionId);
			if(sw.getId() == null){
				//数据库不存在该条例
				logger.info("数据库不存在"+versionId+"的SoftwareVersion....DeleteVersion");
				j.setSuccess(false);
				j.setMsg("删除客户端软件版本"+versionId+"失败");
				super.writeJson(j);
				return;
			}
			if(softwareVersionMServiceI.deleteSoftwareVersion(versionId)){
				logger.info("删除客户端软件版本"+versionId+"成功....DeleteVersion");
				j.setSuccess(true);
				j.setMsg("删除客户端软件版本"+versionId+"成功");
				super.writeJson(j);
				DeleteALL(root_MDF+"\\"+sw.getLocation()+".nax");
				DeleteALL(root_UPV+"\\"+sw.getLocation());
				DeleteALL(root_ZIP+"\\"+sw.getLocation()+".zip");
			}else{
				logger.info("删除客户端软件版本"+versionId+"失败....DeleteVersion");
				j.setSuccess(false);
				j.setMsg("删除客户端软件版本"+versionId+"失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}
	
	//文件下载请求动作
	public void DownLoad() throws Exception{
		String filename = DFileName.replaceAll(" ", "+");
		filename = new String(org.apache.commons.codec.binary.Base64.decodeBase64(filename.getBytes()),"utf-8");
//		System.out.println("Receive DownLoadRequest-->"+filename+"-->"+DFileName);
		try{
			HttpServletResponse response=ServletActionContext.getResponse();
			OutputStream os = response.getOutputStream();
//			System.out.println(ServletActionContext.getServletContext().getRealPath("/UpDateVersion"));
//			System.out.println(filename.contains(ServletActionContext.getServletContext().getRealPath("/UpDateVersion")));
			File file = new File(ServletActionContext.getServletContext().getRealPath("/UpDateVersion") + File.separator + filename);
			if(filename.contains(ServletActionContext.getServletContext().getRealPath("/UpDateVersion"))){
				file = new File(filename);
			}else
				file = new File(ServletActionContext.getServletContext().getRealPath("/UpDateVersion") + File.separator + filename);
			if(file.exists()){
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				byte[] buff = new byte[1024];
				int size;
				while((size = bis.read(buff))!=-1){
					os.write(buff,0,size);
				}
				bis.close();
				fis.close();
			}else{
				os.write(new String("No Such File!").getBytes());
			}
			os.close();
			logger.info("下载文件"+filename+"成功....DownLoad");
		}
		catch(Exception e){
			e.printStackTrace();
			logger.info("下载文件"+filename+"失败....DownLoad\n"+e.getMessage());
		}
	}
		
	//文件上传请求动作
	public void UpLoad() throws IOException {
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		//未登录状态无法上传文件
		if(sessionInfo == null){
			setCheckProgress(getCHECK_Name(), -1, "未登录！");
			return;
		}
		String resultSucc = "{\"result\":\"success\"}";
		String resultFail = "{\"result\":\"failed\"}";
		int succ = 0;
		try{
			//Delete All First；有待商榷
			//目录名称更改为versionName字符串的MD5串
			String directionName = getMD5FromString(versionName);
			List<SoftwareVersion> tmp = softwareVersionMServiceI.getAllSoftwareVersion();
			//首先检查是否含有相同versionName的情况，存在则
			//1.CheckProgress设置为-1
			//2.停止操作，返回错误信息
			boolean could_go_on = true;
			for(SoftwareVersion sw : tmp){
				System.out.println(sw.getVersionName()+"::"+sw.getLocation());
				if(versionName.equals(sw.getVersionName()) || directionName.equals(sw.getLocation())){
					could_go_on = false;
					break;
				}
			}
			//若存在同名versionName或者同目录名的情况，返回报错
			if(!could_go_on){
				setCheckProgress(getCHECK_Name(), -1, "存在同名的版本");
				return;
			}
			
			//准备接受上传文件
			//zip压缩包接受
			//存储在UpDateVersion\Zips里et           
			InputStream is = new FileInputStream(getTheFile());
	
			//将文件大小转化为K
			int fileSizeByte =is.available();
			double filesize =fileSizeByte/1024.0;						
			
//			System.out.println("Testing Upload....." + filesize+"::"+ServletActionContext.getRequest().getContentType()
//					+"::"+ServletActionContext.getRequest().getContentLength());
			
	        final String checkName = getCHECK_Name();
	        //装填文件进度
	        String progressKeyString=sessiont.getId()+"_"+checkName;
	        map_user_progress.put(progressKeyString,progress_num);
	        
	        String zipFilePathName = root_ZIP+"\\"+directionName+".zip";//~UpDateVersion\Zips\???.zip
	        File outfile = new File(zipFilePathName);
	        outfile.deleteOnExit();
	        if(!outfile.exists())
	        {
	        	File dic = new File(root_ZIP);
	        	if(!dic.exists())
	        		dic.mkdirs();
	        	dic = new File(root_MDF);
	        	if(!dic.exists())
	        		dic.mkdirs();
	        	outfile.createNewFile();
	        }
	        OutputStream os = new FileOutputStream(outfile);
	        //将文件大小分上传文件所占进度条比例数作为闸
			double progress_step=filesize/total_progress_upload;
			
	        //DecimalFormat df =new DecimalFormat("0.0");	       
	        double i=0.0;
	        byte[] buffer = new byte[1024];int count = 0;
	        while((count = is.read(buffer)) != -1){
	        	os.write(buffer, 0, count);
	        	++i;
	        	//没每到达闸门进度进一
	        	while (i>=progress_step) {
					++progress_num;
					map_user_progress.put(progressKeyString,progress_num);
					i=0.0;
				}	        	        	
	        }
	        //再次确认上传结束
	        progress_num=0+(int)total_progress_upload;
	        map_user_progress.put(progressKeyString,progress_num);
	        os.flush();
	        os.close();
	        is.close();
	        
	        //解压，并返回文件数量，位置为~UpDateVersion\UPV\???\
	        //解压存储目录；压缩文件位置；进度存储名；该过程进度总量
	        succ = UnZip(root_UPV+"\\"+directionName, zipFilePathName, checkName, total_progress_unzip);
	        
	        //在对应目录下生成一份约定俗成的定位文件，命名为location.nax，内容为directionName
	        //客户端也根据这份location.nax进行文件的定位更新
	        //解压存储目录，需定位到多文件名目录；定位文件名，直接生成在根目录下；记录的定位信息
	        InitLocationFile(root_UPV+"\\"+directionName, "location.nax", directionName);
	        //在config目录下寻找Config.properties文件，不存在则创建，写入URLip和URLport
	        WriteIPnPortLocation(root_UPV+"\\"+directionName);
	        
	        //生成MD5文件，存放至~UpDateVersion\MD5s\???.nax
	        //解压存储目录；MD5文件存储位置；进度存储名；该过程的进度总量；文件总量，需+1为定位文件
	        InitFileMD5List(root_UPV+"\\"+directionName,root_MDF+"\\"+directionName+".nax",progressKeyString,total_progress_initialMD5,succ+1);
	        
	        //生成包含location.nax的压缩包，代替原来的Zips下的文件
	        //解压存储目录；压缩存储目录和位置；进度存储名；该过程进度总量；文件总量
	        Zip(root_UPV+"\\"+directionName, root_ZIP+"\\"+directionName+".zip", progressKeyString, total_progress_zip, succ+1);
	        
	        //完成后存储至数据库
	        softwareVersionMServiceI.addSoftwareVersion(new SoftwareVersion(0,
	        		versionName, versionDescription, directionName));
	        logger.info("上传zip压缩包"+FileName+"-->"+directionName+"成功....UpLoad");
	        
	        //确保进度任务完成
	        progress_num=100;
	        map_user_progress.put(progressKeyString,progress_num);
	        //setCheckProgress(checkName, 1, "");
		}
		catch(Exception e){
			e.printStackTrace();
			logger.info("上传zip压缩包"+FileName+"-->"+versionName+"失败....UpLoad");
			setCheckProgress(getCHECK_Name(), -1, "");
		}
		finally{
			HttpServletResponse response=ServletActionContext.getResponse();
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out;
			out = response.getWriter();
			if(succ > 0)
				out.println(resultSucc);
			else
				out.println(resultFail);
			out.flush(); 
			out.close();
		}
	}
	
	//写入Config.properties文件
	private void WriteIPnPortLocation(String upvPath) throws IOException {
		File upv_dist = new File(upvPath);
		while(upv_dist.list().length == 1){
			upv_dist = new File(upv_dist.getAbsolutePath(),upv_dist.list()[0]);
		}
		upv_dist = new File(upv_dist.getAbsolutePath(),"config");
		if(!upv_dist.exists())
			upv_dist.mkdir();
		File configFile = new File(upv_dist.getAbsolutePath(),"Config.properties");
		if(!configFile.exists())
			configFile.createNewFile();
		Properties property = new Properties();
		FileInputStream fis = new FileInputStream(configFile);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		property.load(br);
		br.close();
		isr.close();
		fis.close();
		HttpServletRequest request = ServletActionContext.getRequest();
		String ip = request.getLocalAddr();
		int port = request.getLocalPort();
		logger.info("Writting IPnPort..."+ip+":"+port+"...WriteIPnPortLocation");
		property.setProperty("URLip", ip);
		property.setProperty("URLport", ""+port);
		FileOutputStream fos = new FileOutputStream(configFile);
		property.store(fos, "update Config.properties");
		fos.close();
	}

	public void UpDate() throws IOException{
		int id = Integer.parseInt(getHiddenFlag().split("::")[1]);
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		//未登录状态无法上传文件
		if(sessionInfo == null){
			setCheckProgress(getCHECK_Name(), -1, "未登录！");
			return;
		}
		String resultSucc = "{\"result\":\"success\"}";
		String resultFail = "{\"result\":\"failed\"}";
		int succ = 0;
		try{
			SoftwareVersion sw = softwareVersionMServiceI.getSoftwareByID(id);
			//目录名称更改为versionName字符串的MD5串
			String directionName = sw.getLocation();
			//操作前先删除原文件
			DeleteALL(root_MDF+"\\"+directionName+".nax");
			DeleteALL(root_UPV+"\\"+directionName);
			DeleteALL(root_ZIP+"\\"+directionName+".zip");
			//准备接受上传文件
			//zip压缩包接受
			//存储在UpDateVersion\Zips里
			InputStream is = new FileInputStream(getTheFile());  
		    final String checkName = getCHECK_Name();
		    String zipFilePathName = root_ZIP+"\\"+directionName+".zip";//~UpDateVersion\Zips\???.zip
		    File outfile = new File(zipFilePathName);
		    outfile.deleteOnExit();
		    if(!outfile.exists())
		    {
		       	File dic = new File(root_ZIP);
		       	if(!dic.exists())
		      		dic.mkdirs();
		       	dic = new File(root_MDF);
		       	if(!dic.exists())
		       		dic.mkdirs();
		       	outfile.createNewFile();
		    }
		    OutputStream os = new FileOutputStream(outfile);
		    IOUtils.copy(is, os);
		    os.flush();
			os.close();
			is.close();
			
			//解压，并返回文件数量，位置为~UpDateVersion\UPV\???\
			//解压存储目录；压缩文件位置；进度存储名；该过程进度总量
			succ = UnZip(root_UPV+"\\"+directionName, zipFilePathName, checkName, total_progress_unzip);
			        
			//在对应目录下生成一份约定俗成的定位文件，命名为location.nax，内容为directionName
			//客户端也根据这份location.nax进行文件的定位更新
			//解压存储目录，需定位到多文件名目录；定位文件名，直接生成在根目录下；记录的定位信息
			InitLocationFile(root_UPV+"\\"+directionName, "location.nax", directionName);
			WriteIPnPortLocation(root_UPV+"\\"+directionName);
			        
			//生成MD5文件，存放至~UpDateVersion\MD5s\???.nax
			//解压存储目录；MD5文件存储位置；进度存储名；该过程的进度总量；文件总量，需+1为定位文件
			InitFileMD5List(root_UPV+"\\"+directionName,root_MDF+"\\"+directionName+".nax",checkName,total_progress_initialMD5,succ+1);
			        
			//生成包含location.nax的压缩包，代替原来的Zips下的文件
			//解压存储目录；压缩存储目录和位置；进度存储名；该过程进度总量；文件总量
			Zip(root_UPV+"\\"+directionName, root_ZIP+"\\"+directionName+".zip", checkName, total_progress_zip, succ+1);
			        
			//完成后更新至数据库，并删除原来的direcionName下的所有文件
			softwareVersionMServiceI.editSoftwareVersion(id, versionName, versionDescription);
			//setCheckProgress(checkName, 1, "");//确保上传完成指令下达
			logger.info("上传zip压缩包"+FileName+"-->"+directionName+"成功....UpDate");
		}
		catch(Exception e){
			e.printStackTrace();
			logger.info("上传zip压缩包"+FileName+"-->"+versionName+"失败....UpDate");
			setCheckProgress(getCHECK_Name(), -1, "");
		}
		finally{
			HttpServletResponse response=ServletActionContext.getResponse();
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out;
			out = response.getWriter();
			if(succ > 0)
				out.println(resultSucc);
			else
				out.println(resultFail);
			out.flush(); 
			out.close();
		}
	}
	
	private void InitLocationFile(String upvPath, String locationFileName,
			String directionName) throws Exception {
		File upv_dist = new File(upvPath);
		while(upv_dist.list().length == 1){
			upv_dist = new File(upv_dist.getAbsolutePath(),upv_dist.list()[0]);
		}
		//文件定位到多文件或目录的位置
		File location = new File(upv_dist.getAbsolutePath()+"\\"+locationFileName);
		System.out.println(upv_dist.getAbsolutePath()+"\\"+locationFileName);
		FileOutputStream fos = new FileOutputStream(location);
		fos.write(directionName.getBytes());
		fos.close();
	}

	private String getMD5FromString(String versionName2) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("md5");
		md5.update(versionName.getBytes("utf-8"));
		byte[] encryption = md5.digest();
		StringBuffer strBuf = new StringBuffer();  
        for (int i = 0; i < encryption.length; i++) {  
            if (Integer.toHexString(0xff & encryption[i]).length() == 1) {  
                strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));  
            } else {
                strBuf.append(Integer.toHexString(0xff & encryption[i]));  
            }
        }
        return strBuf.toString();
	}

	private void setCheckProgress(String checkName, int i, String message) {
	
        HttpSession sessiont = ServletActionContext.getRequest().getSession();
        final String progressKeyString=sessiont.getId()+"_"+checkName;
        map_user_progress.put(progressKeyString,progress_num);
		if(i > 0){
			int now_progress = 0;
			Object tmp = sessiont.getAttribute(checkName);
			if(tmp!=null)
				now_progress = (int)tmp;
			now_progress += i;
			sessiont.setAttribute(checkName, now_progress);
			sessiont.setAttribute(checkName+"_msg", message);
			//System.out.println(checkName+"-->"+session.get(checkName));
			//System.out.println(checkName+"_msg"+"-->"+session.get(checkName+"_msg"));
			
		}
		else{
			sessiont.removeAttribute(checkName);
			sessiont.removeAttribute(checkName+"_msg");
		}
	}

	public void CheckProgress() throws IOException {
		
		HttpSession sessiont = ServletActionContext.getRequest().getSession();
		String checkName = "CHECK_"+getCFileName();		
		int progress_upload = -1;
		Object progress = null;
		Object tmp = sessiont.getAttribute(CHECK_NAME);
		if(tmp != null){
			progress_upload = (int)tmp;
		}
		//tmp为空则tmp无赋值，progress_upload为-1
		//progress = sessiont.getAttribute(checkName);
		String progressNameString=sessiont.getId()+"_"+checkName;
		progress=map_user_progress.get(progressNameString);
		if(progress_upload != -1){
			//正在上传，但是尚未结束
			//progress = (int)(progress_upload*0.01*total_progress_upload);
			sessiont.setAttribute(checkName, progress);
			System.out.println("uploading..."+progress_upload+"---"+progress);
			//进度满了就删除
			if(progress_upload == 100)
				map_user_progress.remove(progressNameString);
				sessiont.removeAttribute(CHECK_NAME);
		}
		if(progress == null)
			progress = 0;
		String msg = (String)session.get(checkName+"_msg");
		HttpServletResponse response=ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out;
		out = response.getWriter();
		out.println("{\"progress\":\""+progress+"\",\"msg\":\""+msg+"\"}");
		out.flush(); 
		out.close();
		if((Integer)progress >= 100 || (Integer)progress == -1)
		{
			map_user_progress.remove(progressNameString);
			System.out.print("上传成功，删除闪传文件进度缓存");
			sessiont.removeAttribute(checkName);
		}
	}

	private void InitFileMD5List(String rootpath, String md5Path, String progressKeyString, Double total_progress, int file_num) {
		double progress_step=file_num/total_progress_initialMD5;
		File file = new File(rootpath);
		HashMap<String,String> map = new HashMap<>();
		List<Integer> paras = new LinkedList<Integer>();
		paras.add(0);
		paras.add(progress_num);
		AddToMap(file, map, progressKeyString, total_progress, file_num, paras,progress_step);
//		System.out.println("mapsize->"+map.size());
		File md5 = new File(md5Path);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(md5));
			oos.writeObject(map);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        progress_num+=total_progress_initialMD5;
        map_user_progress.put(progressKeyString, progress_num);
	}
	
	private void AddToMap(File file, HashMap<String, String> map, 
			String progressKeyString, Double total_progress, int file_num, List<Integer> paras,double progress_step) {
		if(file.isDirectory()){
			String[] list = file.list();
			for (int i=0; i<list.length; i++) {
	               AddToMap(new File(file, list[i]), map, progressKeyString, total_progress, file_num, paras,progress_step);
	           }
		}else{
			try {
				MessageDigest md5 = MessageDigest.getInstance("md5");
				FileInputStream fi = new FileInputStream(file);
				DigestInputStream di = new DigestInputStream(fi, md5);
				byte[] buffer = new byte[1024];
				while(di.read(buffer) > 0);
				md5 = di.getMessageDigest();
	            BigInteger bi = new BigInteger(1, md5.digest());  
	            String value = bi.toString(16);
	            map.put(file.getAbsolutePath(), value);
	            di.close();
	            fi.close();
	            int counter = paras.get(0);
	            int has_num = paras.get(1);
	            
	            counter++;
	            //每一次到达闸后进度条+1
	            if(counter==progress_step){
	            	++has_num;
	            	map_user_progress.put(progressKeyString, has_num);
	            	counter=0;
	            }
	          
//	            int k = (int)(1.0*total_progress/file_num*counter - has_num);
//	            if(k >= 1)
//	            {
//	            	//setCheckProgress(checkName, k, "");
//	              	//has_num+=k;
//	            }
	            paras.set(0, counter);
	            paras.set(1, has_num);	            	            
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
				//setCheckProgress(checkName, -1, "md5文件分析错误");
			}
			
		}
	}

	private void DeleteALL(String root) {
		File file = new File(root);
		DeleteDir(file);
		file.deleteOnExit();
	}

	private Boolean DeleteDir(File file) {
		if(file.isDirectory()){
			String[] list = file.list();
			Boolean res = true;
			for (int i=0; i<list.length; i++) {
	               boolean success = DeleteDir(new File(file, list[i]));
//	               System.out.println("DeletingUPV..."+file.getName()+"..."+success);
	               if (!success) {
	                   res = false;
	              }
            }
			file.delete();
			return res;
		}else
			return file.delete();
	}

	private int UnZip(String rootpath,String zipFilePath, String checkName, double total_progress) throws IOException{
		String progressKeyString=sessiont.getId()+"_"+checkName;
		File file = new File(zipFilePath);
	   	ZipFile zf = new ZipFile(file,Charset.forName("GBK"));//根据情况添加相应的编码容错机制
		InputStream is = null;  
	    FileOutputStream fos = null;  
	    BufferedOutputStream bos = null;  
		Boolean hasunziped = false;
		int file_num = 0;int k = 0;
		new File(rootpath).mkdirs();
	    try {
	       	Enumeration<?> entries = zf.entries();
	       	int counter = 0;int has_num = progress_num;//用于更新进度条的两个控制计数器
	       	int file_nums = zf.size();
	       	double file_step=file_nums/total_progress_unzip;
	       	while(entries.hasMoreElements()){
	       		byte buf[] = new byte[1024];
	       		ZipEntry entry = (ZipEntry)entries.nextElement();
	       		String tmpname = entry.getName();
//	       		System.out.println(tmpname);
	       		boolean hasdir = false;//目录标识，查看该文件是否在路径上存在目录
	            if(tmpname.lastIndexOf("/") != -1){
	            	hasdir = true;  
	            }
	       		String fname = rootpath + "\\" + tmpname;
	            if(entry.isDirectory())//目录
	            {
	            	File tp = new File(fname);
	            	if(!tp.exists())
	               		tp.mkdirs();
	                }else{//文件
	                	File tfile = new File(fname);  
	                	if(!tfile.exists()){//存在目录先创建目录
	                    	if(hasdir){
	                    	   new File(fname.substring(0, fname.lastIndexOf("/"))).mkdirs();
	                    	}
	                    	tfile.createNewFile();
	                    }else{
	                    	tfile.delete();
	                    	tfile.createNewFile();
	                    }
	                    is = zf.getInputStream(entry);  
	                    fos = new FileOutputStream(tfile);  
	                    bos = new BufferedOutputStream(fos, 1024);  
	                    int count = -1;
	                    while((count = is.read(buf)) > -1)  
	                    {  
	                        bos.write(buf, 0, count);  
	                    }
	                    bos.flush();
	                    bos.close();
	                    fos.close();
	                    is.close();
	                    file_num ++;
	                }
	                ++counter;
	                //到达百分比闸门
	                if(counter>=file_step){	                	
	                	++has_num;
	                	map_user_progress.put(progressKeyString,has_num);
	                	counter=0;
	                	
	                }	                
	        	}
	        	zf.close();
	        	//结束后确保进度条到达相应位置
	        	progress_num+=total_progress_unzip;
	        	map_user_progress.put(progressKeyString,progress_num);
	        	hasunziped = true;
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	logger.error(e.getMessage());
	        setCheckProgress(checkName, -1, "解压时发生错误");
	    } finally {
	       	if(file.exists())
	      		file.delete();//解压完毕删除压缩包
	    }
		return file_num;
	}
	
	//解压存储目录；压缩存储目录和位置；进度存储名；该过程进度总量
	private void Zip(String upvPath, String zipFileName, String progressKeyString, double progress, int fileNum) throws Exception {
		File upv_dist = new File(upvPath);
		//定位至根目录，根据子文件或目录数量，大于1即为根目录
		while(upv_dist.list().length == 1){
			upv_dist = new File(upv_dist.getAbsolutePath(),upv_dist.list()[0]);
		}
		List<Integer> paras = new LinkedList<Integer>();
		paras.add(0);
		paras.add(progress_num);
        ZipOutputStream out = new ZipOutputStream( new FileOutputStream(new File(zipFileName)));
        compress(out,upv_dist,"",progressKeyString, progress, fileNum, paras);
        
        progress_num+=total_progress_zip;
        map_user_progress.put(progressKeyString, progress_num);
        out.close();
	}
	
	//同解压一样迭代压缩，使用list控制进度读写，同压缩
	private void compress(ZipOutputStream out, File upv_dist, String base, String progressKeyString, Double progress
			, int fileNum, List<Integer> paras) throws Exception {
		//如果路径为目录（文件夹）
        if(upv_dist.isDirectory())
        {
            //取出文件夹中的文件（或子文件夹）
            File[] flist = upv_dist.listFiles();
            if(flist.length==0)//如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
            {
                out.putNextEntry(new ZipEntry(base+"/"));
                out.closeEntry();
            }
            else//如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
            {
                for(int i=0;i<flist.length;i++)
                {
                    compress(out,flist[i],base+"/"+flist[i].getName(), progressKeyString, progress, fileNum, paras);
                }
            }
        }
        else//如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
        {   
            out.putNextEntry( new ZipEntry(base) );
            FileInputStream fis = new FileInputStream(upv_dist);
            BufferedInputStream bis = new BufferedInputStream(fis);
            int tag;byte[] bts = new byte[1024];
            Double  progress_step =fileNum/total_progress_zip;
            //将源文件写入到zip文件中
            while((tag=bis.read(bts,0,1024))!=-1)
            {
                out.write(bts, 0, tag);
            }
            out.closeEntry();
            bis.close();
            fis.close();
            int counter = paras.get(0);
            int has_num = paras.get(1);
            counter++; 
            //到达闸门进度进1
            if (counter == progress_step) {
				++has_num;
				map_user_progress.put(progressKeyString, has_num);
				counter=0;
			}
            
//            int k = (int)(1.0*progress/fileNum*counter - has_num);
//            if(k >= 1)
//            {
//            	setCheckProgress(checkName, k, "");
//              	has_num+=k;
//            }
            paras.set(0, counter);
            paras.set(1, has_num);
        }
	}

	public void downloadZipFile(){
		logger.info((downloadName+"...downloadZipFile"));
		HttpServletResponse response=ServletActionContext.getResponse();
		response.setContentType("application/zip");  
        SoftwareVersion sw = softwareVersionMServiceI.getSoftwareByID(downloadName);
        String fullFileName = root_ZIP+"\\"+sw.getLocation()+".zip"; 
        InputStream in = null;
        OutputStream out = null; 
		try {
			String filename = new String(sw.getVersionName().replace(" ", "_").getBytes("UTF-8"), "ISO-8859-1");
			in = new FileInputStream(fullFileName);
			response.setHeader("Content-Disposition", "attachment;filename="+filename+".zip");
			response.setContentLength(in.available());
			out = response.getOutputStream();
	        int b;  
	        while((b=in.read())!= -1)  
	        {  
	            out.write(b);  
	        }  
	          
	        in.close();  
	        out.close(); 
		} catch (Exception e) {
			e.printStackTrace();
			if(in!= null)
				try{
					in.close();
				}catch (Exception e2) {}
			if(out != null)
				try{
					out.close();
				}catch (Exception e3) {}
		}
        
	}
	
}
