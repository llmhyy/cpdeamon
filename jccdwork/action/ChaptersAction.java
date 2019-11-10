package edu.dhu.action;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ModelDriven;

import edu.dhu.model.Chapters;
import edu.dhu.pageModel.Json;
import edu.dhu.pageModel.PMChapters;
import edu.dhu.pageModel.SessionInfo;
import edu.dhu.service.ChapterServiceI;

@ParentPackage("basePackage")
@Namespace("/")
@Action(value = "chaptersAction", results = { @Result(name = "view", location = "/admin/index.jsp") })
public class ChaptersAction extends BaseAction implements
		ModelDriven<PMChapters> {

	private ChapterServiceI chapterServiceI;
	private PMChapters pmchapters = new PMChapters();
	private static final Logger logger = Logger.getLogger(ChaptersAction.class);

	public ChapterServiceI getChapterServiceI() {
		return chapterServiceI;
	}

	@Autowired
	public void setChapterServiceI(ChapterServiceI chapterServiceI) {
		this.chapterServiceI = chapterServiceI;
	}

	public void findAllChapters() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMChapters> chapters = chapterServiceI.findAllChapter();
			logger.info("查询所有章节成功");
			j.setSuccess(true);
			j.setMsg("查询所有章节成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findAllCourses() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMChapters> chapters = chapterServiceI.findAllCourses();
			logger.info("查询所有课程成功");
			j.setSuccess(true);
			j.setMsg("查询所有课程成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findChaptersByCode() {
		String code = pmchapters.getCode();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			PMChapters chapters = chapterServiceI.findChaptersByCode(code);
			logger.info("查询章节成功");
			j.setSuccess(true);
			j.setMsg("查询章节成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findCourseBycourseName() {
		String name = pmchapters.getName();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMChapters> chapters = chapterServiceI
					.findChaptersByName(name);
			logger.info("查询课程成功");
			j.setSuccess(true);
			j.setMsg("查询课程成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findChaptersByCondition() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMChapters> chapters = chapterServiceI.findAllChapter();
			logger.info("查询章节成功");
			j.setSuccess(true);
			j.setMsg("查询章节成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findChaptersBycourseCode() {
		String code = pmchapters.getCode();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMChapters> chapters = chapterServiceI
					.findChaptersBycourseCode(code);
			logger.info("查询章节成功");
			j.setSuccess(true);
			j.setMsg("查询章节成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void findChapterById() {
		int id = pmchapters.getId();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			Chapters chapters = chapterServiceI.findChapterById(id);
			j.setSuccess(true);
			j.setMsg("查询章节成功");
			j.setObj(chapters);
			super.writeJson(j);
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void deleteChapter() {
		int id = pmchapters.getId(); // id
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json json = new Json();
		if (sessionInfo != null) {
			Chapters chapters = chapterServiceI.findChapterById(id);
			int level = chapters.getLevel();
			if (level == 1) {
				String code = chapters.getCode();
				List<PMChapters> pmchaptersList = chapterServiceI
						.findChaptersBycourseCode(code);
				if (pmchaptersList.size() == 0) {
					chapterServiceI.deleteChapter(id); // 删除
					logger.info("删除成功");
					json.setSuccess(true);
					json.setMsg("删除成功");
					json.setObj(id);
					super.writeJson(json);
				} else {
					logger.info("该课程不允许被删除，只有先删除该课程包括的章节才能删除课程！");
					json.setSuccess(false);
					json.setMsg("该课程不允许被删除，只有先删除该课程包括的章节才能删除课程！");
					json.setObj(id);
					super.writeJson(json);
				}

			} else {
				chapterServiceI.deleteChapter(id); // 删除
				logger.info("删除成功");
				json.setSuccess(true);
				json.setMsg("删除成功");
				json.setObj(id);
				super.writeJson(json);
			}

		} else {
			json.setSuccess(false);
			json.setMsg("请先登录。");
			super.writeJson(json);
		}
	}

	public void editChapter() {
		int id = pmchapters.getId();
		String name = pmchapters.getName();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			boolean result = chapterServiceI.editChapter(id, name);
			if (result == true) {
				logger.info("修改章节名称成功");
				j.setSuccess(true);
				j.setMsg("修改章节名称成功");
				super.writeJson(j);
			} else {
				logger.info("修改章节名称失败");
				j.setSuccess(false);
				j.setMsg("修改章节名称失败");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void addChapter() {
		String courseCode = pmchapters.getCode();
		String name = pmchapters.getName();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMChapters> pmchaptersList = chapterServiceI
					.findChaptersBycourseCode(courseCode);
			String chapterCode = new String();
			String strCode = new String();
			if (pmchaptersList.size() < 99) {
				if (pmchaptersList.size() == 0) {
					chapterCode = courseCode + "01";
				} else {
					for (int i = 1; i <= pmchaptersList.size(); i++) {
						strCode = pmchaptersList.get(i - 1).getCode();
						int numCode = Integer.valueOf(strCode.substring(
								strCode.length() - 2, strCode.length()));
						if (i != numCode) {
							if (i < 10) {
								chapterCode = "0" + String.valueOf(i);
								break;
							} else {
								chapterCode = String.valueOf(i);
								break;
							}
						} else {
							if (i == pmchaptersList.size()) {
								int flag = i + 1;
								if (flag < 10)
									chapterCode = "0" + String.valueOf(flag);
								else
									chapterCode = String.valueOf(flag);
							}
						}
					}
					chapterCode = strCode.substring(0, 3) + chapterCode;
				}
				pmchapters.setCode(chapterCode);
				pmchapters.setName(name);
				chapterServiceI.addChapter(pmchapters);
				logger.info("添加章节成功");
				j.setSuccess(true);
				j.setMsg("添加章节成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("该课程章节已经满了不能再添加章节了！");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	public void addCourse() {
		String name = pmchapters.getName();
		Map<String, Object> session = ActionContext.getContext().getSession();
		SessionInfo sessionInfo = (SessionInfo) session.get("sessionInfo");
		// 返回前台的json数据
		Json j = new Json();
		if (sessionInfo != null) {
			List<PMChapters> pmcoursesList = chapterServiceI.findAllCourses();
			String courseCode = new String();
			String strCode = new String();
			if (pmcoursesList.size() == 0) {
				courseCode = "001";
			}
			if (pmcoursesList.size() < 999) {

				for (int i = 1; i <= pmcoursesList.size(); i++) {
					strCode = pmcoursesList.get(i - 1).getCode();
					int numCode = Integer.valueOf(strCode);
					if (i != numCode) {
						if (i < 10) {
							courseCode = "00" + String.valueOf(i);
							break;
						} else if (i >= 10 && i < 100) {
							courseCode = "0" + String.valueOf(i);
							break;
						}
					} else {
						if (i == pmcoursesList.size()) {
							int flag = i + 1;
							if (flag < 10)
								courseCode = "00" + String.valueOf(flag);
							else if (i >= 10 && i < 100)
								courseCode = "0" + String.valueOf(i);
						}
					}
				}
				pmchapters.setCode(courseCode);
				pmchapters.setName(name);
				chapterServiceI.addCourse(pmchapters);
				logger.info("添加课程成功");
				j.setSuccess(true);
				j.setMsg("添加课程成功");
				super.writeJson(j);
			} else {
				j.setSuccess(false);
				j.setMsg("课程已经满了不能再添加课程了！");
				super.writeJson(j);
			}
		} else {
			j.setSuccess(false);
			j.setMsg("请先登录。");
			super.writeJson(j);
		}
	}

	@Override
	public PMChapters getModel() {
		// TODO Auto-generated method stub
		return pmchapters;
	}

}
