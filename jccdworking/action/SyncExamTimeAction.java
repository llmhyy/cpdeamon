package edu.dhu.action;

import java.util.Date;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.cache.ExamCacheManager;
import edu.dhu.model.Exam;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMExamTime;
import edu.dhu.service.ExamServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "syncExamTime", results = { @Result(name = "nologin", location = "/admin/index.jsp") })
public class SyncExamTimeAction extends BaseAction implements
		ModelDriven<PMExamTime> {

	private static final long serialVersionUID = 3788385928663734491L;

	PMExamTime pMExamTime = new PMExamTime();

	@Override
	public PMExamTime getModel() {
		return pMExamTime;
	}

	private ExamServiceI examService;

	public ExamServiceI getExamService() {
		return examService;
	}

	@Autowired
	public void setExamService(ExamServiceI examService) {
		this.examService = examService;
	}

	public void getExamTimeById() {
		// 获取系统当前时间
		Date now = new Date();
		Json j = new Json();
		PMExamTime examTime = new PMExamTime();

		// 根据examID查询该场考试的信息,先从缓冲中获取该场考试的信息
		ExamCacheManager examCacheManager = ExamCacheManager.getInstance();
		Exam exam = (Exam) examCacheManager.getObject("theExamById"
				+ pMExamTime.getExamId());
		if (exam == null) {
			exam = examService.getExamById(pMExamTime.getExamId());
			examCacheManager.putObject("theExamById" + pMExamTime.getExamId(),
					exam);
		}

		Date endTime = exam.getEndtime();

		long day = 0;
		long hour = 0;
		long minute = 0;
		long second = 0;

		// 如果考试还没有没结束，则获取正确的day,hour,minute,second
		// 否则如果考试结束了，day,hour,minute,second都设置为0
		if (now.getTime() < endTime.getTime()) {
			// 获取day,hour,minute,second
			day = (endTime.getTime() - now.getTime()) / (1000 * 3600 * 24);
			hour = (endTime.getTime() - now.getTime() - day
					* (1000 * 3600 * 24))
					/ (1000 * 3600);
			minute = (endTime.getTime() - now.getTime() - day
					* (1000 * 3600 * 24) - hour * (1000 * 3600))
					/ (1000 * 60);
			second = (endTime.getTime() - now.getTime() - day
					* (1000 * 3600 * 24) - hour * (1000 * 3600) - minute
					* (1000 * 60)) / 1000;
		}

		examTime.setExamId(pMExamTime.getExamId());
		examTime.setDay(day);
		examTime.setHour(hour);
		examTime.setMinute(minute);
		examTime.setSecond(second);

		j.setSuccess(true);
		j.setObj(examTime);
		j.setMsg("获取考试剩余时间成功。");
		super.writeJson(j);
	}
}
