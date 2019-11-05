package edu.dhu.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;



@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "UpLoadFileAction")
public class UpLoadFileAction extends ActionSupport{
	
	private static final long serialVersionUID = 10086L;  
    private File TheFile;// 对应文件域的file，封装文件内容
    private String FileName;// 封装文件名
    private String DFileName;//封装请求下载的文件名
    private String CFileName;//封装请求查询处理进度的文件名
    private Map<String, Object> session = ActionContext.getContext().getSession();
	
	public String getDFileName() {
		return DFileName;
	}

	public void setDFileName(String dFileName) {
		DFileName = dFileName;
	}

	public String getCFileName() {
		return "CHECK_"+CFileName;
	}

	public void setCFileName(String cFileName) {
		CFileName = cFileName;
	}

	//文件下载请求动作
	public void DownLoad() throws Exception{
		String filename = DFileName.replaceAll(" ", "+");
		filename = new String(org.apache.commons.codec.binary.Base64.decodeBase64(filename.getBytes()),"utf-8");
		System.out.println("Receive DownLoadRequest-->"+filename+"-->"+DFileName);
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
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//文件上传请求动作
	public void UpLoad() throws IOException {
		String root = ServletActionContext.getServletContext().getRealPath("/UpDateVersion");
		String resultSucc = "{\"result\":\"success\"}";
		String resultFail = "{\"result\":\"failed\"}";
		int succ = 0;
		try{
			//Delete All First
			DeleteALL(root);
			InputStream is = new FileInputStream(getTheFile());  
	        String title = getFileName();
	        String checkName = "CHECK_"+title;
	        File outfile = new File(root+"\\"+title);
	        outfile.deleteOnExit();
	        if(!outfile.exists())
	        {
	        	File dic = new File(root);
	        	if(!dic.exists())
	        		dic.mkdirs();
	        	outfile.createNewFile();
	        }
	        OutputStream os = new FileOutputStream(outfile);
	        IOUtils.copy(is, os);
	        os.flush();
//	        IOUtils.closeQuietly(is);
//	        IOUtils.closeQuietly(os);
	        os.close();
	        is.close();
	        setCheckProgress(checkName,33);
	        succ = UnZip(root,title,checkName,33);
//	        System.out.println("DeletingZIP..."+outfile.delete());
	        InitFileMD5List(root,checkName,34,succ);
		}
		catch(Exception e){
			e.printStackTrace();
			setCheckProgress("CHECK_"+getCFileName(), -1);
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
	
	private void setCheckProgress(String checkName, int i) {
		int now_progress = 0;
		if(session.containsKey(checkName))
			now_progress = (int)session.get(checkName);
		now_progress += i;
		session.put(checkName, now_progress);
	}
	private void setCheckProgress(String checkName, String error) {
		if(session.containsKey(checkName))
			session.remove(checkName);
		session.put(checkName, error);
	}

	public void CheckProgress() throws IOException {
		String checkName = getCFileName();
		Object progress = session.get(checkName);
		if(progress == null)
			progress = 0;
//		System.out.println("Get CheckProcess at UpLoadFileAction..."+checkName + "---" + progress);
		HttpServletResponse response=ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out;
		out = response.getWriter();
		out.println("{\"progress\":\""+progress+"\"}");
		out.flush(); 
		out.close();
		if((int)progress >= 100 || (int)progress == -1)
			session.remove(checkName);
	}

	private void InitFileMD5List(String rootpath, String checkName, int total_progress, int file_num) {
		File file = new File(rootpath + "\\UPV");
		HashMap<String,String> map = new HashMap<>();
		List<Integer> paras = new LinkedList<Integer>();
		paras.add(0);
		paras.add(0);
		AddToMap(file, map, checkName, total_progress, file_num, paras);
		System.out.println("mapsize->"+map.size());
		File md5 = new File(rootpath + "\\md5.nax");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(md5));
			oos.writeObject(map);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void AddToMap(File file, HashMap<String, String> map, 
			String checkName, int total_progress, int file_num, List<Integer> paras) {
		if(file.isDirectory()){
			String[] list = file.list();
			for (int i=0; i<list.length; i++) {
                AddToMap(new File(file, list[i]), map, checkName, total_progress, file_num, paras);
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
	            int k = (int)(1.0*total_progress/file_num*counter - has_num);
                if(k >= 1)
                {
                	setCheckProgress(checkName, k);
                	has_num+=k;
                }
                paras.set(0, counter);
                paras.set(1, has_num);
			} catch (Exception e) {
				e.printStackTrace();
				setCheckProgress(checkName, -1);
			}
			
		}
	}

	private void DeleteALL(String root) {
		File file = new File(root + "\\UPV");
		DeleteDir(file);
	}

	private Boolean DeleteDir(File file) {
		if(file.isDirectory()){
			String[] list = file.list();
			Boolean res = true;
			for (int i=0; i<list.length; i++) {
                boolean success = DeleteDir(new File(file, list[i]));
//                System.out.println("DeletingUPV..."+file.getName()+"..."+success);
                if (!success) {
                    res = false;
                }
            }
			file.delete();
			return res;
		}else
			return file.delete();
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

	public void setFileName(String fileFileName) {
		this.FileName = fileFileName;
	}

	private int UnZip(String rootpath,String name, String checkName, int total_progress) throws IOException{
		File file = new File(rootpath + "\\" + name);
    	ZipFile zf = new ZipFile(file,Charset.forName("GBK"));//根据情况添加相应的编码容错机制
		InputStream is = null;  
	    FileOutputStream fos = null;  
	    BufferedOutputStream bos = null;  
		Boolean hasunziped = false;
		rootpath = rootpath + "\\UPV";
		int file_num = 0;int k = 0;
		new File(rootpath).mkdirs();
        try {
        	Enumeration<?> entries = zf.entries();
        	int counter = 0;int has_num = 0;//用于更新进度条的两个控制计数器
        	int file_nums = zf.size();
        	while(entries.hasMoreElements()){
        		byte buf[] = new byte[1024];
        		ZipEntry entry = (ZipEntry)entries.nextElement();
        		String tmpname = entry.getName();
//        		System.out.println(tmpname);
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
                counter++;
                k = (int)(1.0*total_progress/file_nums*counter - has_num);
                if(k >= 1)
                {
                	setCheckProgress(checkName, k);
                	has_num+=k;
                }
        	}
        	zf.close();
        	hasunziped = true;
        } catch (Exception e) {
            e.printStackTrace();
            setCheckProgress(checkName, -1);
        } finally {
//        	if(file.exists())
//        		file.delete();//解压完毕删除压缩包
        }
		return file_num;
	}
	
}
