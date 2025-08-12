package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmAlarmConfigDetail;
import com.jeesite.modules.swm.service.SwmAlarmConfigDetailService;

/**
 * swm_alarm_config_detailController
 * @author gjl
 * @version 2025-08-11
 */
@Controller
@RequestMapping(value = "${adminPath}/swmAlarmConfigDetail")
@Api(value = "swm_alarm_config_detail接口", tags = "swm_alarm_config_detail")
public class SwmAlarmConfigDetailController extends BaseController {

	@Resource
	private SwmAlarmConfigDetailService swmAlarmConfigDetailService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmAlarmConfigDetail get(String id, boolean isNewRecord) {
		return swmAlarmConfigDetailService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(SwmAlarmConfigDetail swmAlarmConfigDetail, Model model) {
		model.addAttribute("swmAlarmConfigDetail", swmAlarmConfigDetail);
		return "modules/swm/swmAlarmConfigDetailList";
	}
	
	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "pageList")
	@ResponseBody
	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
	public Page<SwmAlarmConfigDetail> pageList(SwmAlarmConfigDetail swmAlarmConfigDetail, HttpServletRequest request, HttpServletResponse response) {
		swmAlarmConfigDetail.setPage(new Page<>(request, response));
		Page<SwmAlarmConfigDetail> page = swmAlarmConfigDetailService.findPage(swmAlarmConfigDetail);
		return page;
	}

	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public List<SwmAlarmConfigDetail> listData(SwmAlarmConfigDetail swmAlarmConfigDetail, HttpServletRequest request, HttpServletResponse response) {
		List<SwmAlarmConfigDetail> list = swmAlarmConfigDetailService.findList(swmAlarmConfigDetail);
		return list;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(SwmAlarmConfigDetail swmAlarmConfigDetail, Model model) {
		model.addAttribute("swmAlarmConfigDetail", swmAlarmConfigDetail);
		return "modules/swm/swmAlarmConfigDetailForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(@Validated SwmAlarmConfigDetail swmAlarmConfigDetail) {
		swmAlarmConfigDetailService.save(swmAlarmConfigDetail);
		return renderResult(Global.TRUE, text("保存成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		swmAlarmConfigDetailService.delete(swmAlarmConfigDetail);
		return renderResult(Global.TRUE, text("删除swm_alarm_config_detail成功！"));
	}
	@GetMapping("getByMainId")
	@ResponseBody
	public SwmAlarmConfigDetail getByMainId(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		return swmAlarmConfigDetailService.findByMainId(swmAlarmConfigDetail);
	}
	
}