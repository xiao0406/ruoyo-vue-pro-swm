package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.utils.DictUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.entity.SwmSafetyFileManage;
import com.jeesite.modules.swm.service.SwmSafetyFileManageService;

/**
 * 安全教育视频管理Controller
 * @author wxy
 * @version 2026-01-19
 */
@Controller
@RequestMapping(value = "${adminPath}/swmSafetyFileManage")
@Api(value = "安全教育视频管理接口", tags = "安全教育视频管理")
public class SwmSafetyFileManageController extends BaseController {

	@Resource
	private SwmSafetyFileManageService swmSafetyFileManageService;


	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "jobTypeList")
	@ResponseBody
	@ApiOperation(value = "工种列表", notes = "工种列表")
	public List<String> jobTypeList(String jobType) {
		List<DictData> swmJobType0 = DictUtils.getDictList("swm_job_type_0");
		List<DictData> swmJobType1 = DictUtils.getDictList("swm_job_type_1");
		List<DictData> swmJobType2 = DictUtils.getDictList("swm_job_type_2");
		List<DictData> swmJobType3 = DictUtils.getDictList("swm_job_type_3");
		List<String> job0 = swmJobType0.stream().map(DictData::getDictLabel).collect(Collectors.toList());
		List<String> job1 = swmJobType1.stream().map(DictData::getDictLabel).collect(Collectors.toList());
		List<String> job2 = swmJobType2.stream().map(DictData::getDictLabel).collect(Collectors.toList());
		List<String> job3 = swmJobType3.stream().map(DictData::getDictLabel).collect(Collectors.toList());
		job0.addAll(job1);
		job0.addAll(job2);
		job0.addAll(job3);
		return job0;
	}
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmSafetyFileManage get(String id, boolean isNewRecord) {
		return swmSafetyFileManageService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(SwmSafetyFileManage swmSafetyFileManage, Model model) {
		model.addAttribute("swmSafetyFileManage", swmSafetyFileManage);
		return "modules/swm/swmSafetyFileManageList";
	}
	
	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "pageList")
	@ResponseBody
	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
	public Page<SwmSafetyFileManage> pageList(SwmSafetyFileManage swmSafetyFileManage, HttpServletRequest request, HttpServletResponse response) {
		swmSafetyFileManage.setPage(new Page<>(request, response));
		Page<SwmSafetyFileManage> page = swmSafetyFileManageService.findPage(swmSafetyFileManage);
		return page;
	}

	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public List<SwmSafetyFileManage> listData(SwmSafetyFileManage swmSafetyFileManage, HttpServletRequest request, HttpServletResponse response) {
		List<SwmSafetyFileManage> list = swmSafetyFileManageService.findList(swmSafetyFileManage);
		return list;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(SwmSafetyFileManage swmSafetyFileManage, Model model) {
		model.addAttribute("swmSafetyFileManage", swmSafetyFileManage);
		return "modules/swm/swmSafetyFileManageForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(@Validated SwmSafetyFileManage swmSafetyFileManage) {
		swmSafetyFileManageService.save(swmSafetyFileManage);
		return renderResult(Global.TRUE, text("保存安全教育视频管理成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(SwmSafetyFileManage swmSafetyFileManage) {
		swmSafetyFileManageService.delete(swmSafetyFileManage);
		return renderResult(Global.TRUE, text("删除安全教育视频管理成功！"));
	}
	
}