package edu.dhu.action;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.opensymphony.xwork2.ActionContext;

import edu.dhu.cache.ClassesCacheManager;
import edu.dhu.cache.ClassstudentsCacheManager;
import edu.dhu.cache.ExamCacheManager;
import edu.dhu.cache.ExamLastSolutionStatusCacheManager;
import edu.dhu.cache.ExamScoreCacheManager;
import edu.dhu.cache.ExamclassesCacheManager;
import edu.dhu.cache.ExamproblemsCacheManager;
import edu.dhu.cache.ProblemsCachManager;
import edu.dhu.cache.StudentexamdetailCacheManager;
import edu.dhu.cache.StudentexaminfoCacheManager;
import edu.dhu.cache.UsersCacheManager;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.SessionInfo;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "cacheAction", results = { @Result(name = "view", location = "/admin/index.jsp") })
public class CacheAction extends BaseAction {

	private static final long serialVersionUID = -1941191540952925905L;

	private static final Logger logger = Logger.getLogger(CacheAction.class);

	public void removeAllCache() // 清除所有缓存
	{
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			ClassesCacheManager classCache = ClassesCacheManager.getInstance();
			classCache.removeAllObject();
			ClassstudentsCacheManager classstudentsCache = ClassstudentsCacheManager
					.getInstance();
			classstudentsCache.removeAllObject();
			ExamclassesCacheManager examclassesCache = ExamclassesCacheManager
					.getInstance();
			examclassesCache.removeAllObject();
			ExamproblemsCacheManager examproblemsCache = ExamproblemsCacheManager
					.getInstance();
			examproblemsCache.removeAllObject();
			ExamScoreCacheManager examScoreCache = ExamScoreCacheManager
					.getInstance();
			examScoreCache.removeAllObject();
			StudentexamdetailCacheManager studentexamdetailCache = StudentexamdetailCacheManager
					.getInstance();
			studentexamdetailCache.removeAllObject();
			StudentexaminfoCacheManager studentexaminfoCache = StudentexaminfoCacheManager
					.getInstance();
			studentexaminfoCache.removeAllObject();
			UsersCacheManager usersCache = UsersCacheManager.getInstance();
			usersCache.removeAllObject();
			edu.dhu.cache.ProblemsCachManager problemsCachManager = ProblemsCachManager
					.getInstance();
			problemsCachManager.removeAllObject();
			ExamCacheManager examCacheManager = ExamCacheManager.getInstance();
			examCacheManager.removeAllObject();
			ExamLastSolutionStatusCacheManager examLastSolutionStatusManager = ExamLastSolutionStatusCacheManager
					.getInstance();
			examLastSolutionStatusManager.removeAllObject();
			logger.info("清除系统缓存成功");
			j.setSuccess(true);
			j.setMsg("清除系统缓存成功");
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}

	}

}
