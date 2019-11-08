package edu.dhu.action;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;

import com.alibaba.fastjson.JSONWriter;
import com.alibaba.fastjson.JSON;
import com.opensymphony.xwork2.ActionSupport;

@ParentPackage("basePackage")
@Namespace("/")
// 用于写回前台的数据，转换成json对象
public class BaseAction extends ActionSupport {

	private static final long serialVersionUID = 3265819587502402507L;

	public void writeJson(Object object) {
		try {
			String json = JSON.toJSONStringWithDateFormat(object,
					"yyyy-MM-dd HH:mm:ss");
			ServletActionContext.getResponse().setContentType(
					"text/html;charset=utf-8");
			ServletActionContext.getResponse().getWriter().write(json);
			ServletActionContext.getResponse().getWriter().flush();
			ServletActionContext.getResponse().getWriter().close();

		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
