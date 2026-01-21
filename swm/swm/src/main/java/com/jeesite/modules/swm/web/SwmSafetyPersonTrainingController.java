package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.entity.SwmSafetyPersonTraining;
import com.jeesite.modules.swm.service.SwmSafetyPersonTrainingService;

/**
 * 视频培训记录（人员）Controller
 * @author wxy
 * @version 2026-01-19
 */
@Controller
@RequestMapping(value = "${adminPath}/swmSafetyPersonTraining")
@Api(value = "视频培训记录（人员）接口", tags = "视频培训记录（人员）")
public class SwmSafetyPersonTrainingController extends BaseController {

	@Resource
	private SwmSafetyPersonTrainingService swmSafetyPersonTrainingService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmSafetyPersonTraining get(String id, boolean isNewRecord) {
		return swmSafetyPersonTrainingService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(SwmSafetyPersonTraining swmSafetyPersonTraining, Model model) {
		model.addAttribute("swmSafetyPersonTraining", swmSafetyPersonTraining);
		return "modules/swm/swmSafetyPersonTrainingList";
	}
	
	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "pageList")
	@ResponseBody
	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
	public Page<SwmSafetyPersonTraining> pageList(SwmSafetyPersonTraining swmSafetyPersonTraining, HttpServletRequest request, HttpServletResponse response) {
		swmSafetyPersonTraining.setPage(new Page<>(request, response));
		Page<SwmSafetyPersonTraining> page = swmSafetyPersonTrainingService.findPage(swmSafetyPersonTraining);
		return page;
	}



	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public List<SwmSafetyPersonTraining> listData(SwmSafetyPersonTraining swmSafetyPersonTraining, HttpServletRequest request, HttpServletResponse response) {
		List<SwmSafetyPersonTraining> list = swmSafetyPersonTrainingService.findList(swmSafetyPersonTraining);
		return list;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(SwmSafetyPersonTraining swmSafetyPersonTraining, Model model) {
		model.addAttribute("swmSafetyPersonTraining", swmSafetyPersonTraining);
		return "modules/swm/swmSafetyPersonTrainingForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(@Validated SwmSafetyPersonTraining swmSafetyPersonTraining) {
		swmSafetyPersonTrainingService.save(swmSafetyPersonTraining);
		return renderResult(Global.TRUE, text("保存视频培训记录（人员）成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		swmSafetyPersonTrainingService.delete(swmSafetyPersonTraining);
		return renderResult(Global.TRUE, text("删除视频培训记录（人员）成功！"));
	}



	@RequestMapping(value = "appPageList")
	@ResponseBody
	@ApiOperation(value = "小程序 - 查询分页列表数据", notes = "查询分页列表数据")
	public Page<SwmSafetyPersonTraining> appPageList(SwmSafetyPersonTraining swmSafetyPersonTraining,HttpServletRequest request) {
		//鉴权
		if (!authentication(request)) {
			return null;
		}
		swmSafetyPersonTraining.setPage(new Page<>(swmSafetyPersonTraining.getPageNo(), swmSafetyPersonTraining.getPageSize()));
		Page<SwmSafetyPersonTraining> page = swmSafetyPersonTrainingService.appPageList(swmSafetyPersonTraining);
		return page;
	}

	@PostMapping(value = "appUpdate")
	@ResponseBody
	@ApiOperation(value = "小程序 - 保存数据", notes = "保存数据")
	public String appUpdate(@RequestBody SwmSafetyPersonTraining swmSafetyPersonTraining,HttpServletRequest request) {

		//鉴权
		if (!authentication(request)) {
			return renderResult(Global.FALSE, "非法请求");
		}

		swmSafetyPersonTrainingService.appUpdate(swmSafetyPersonTraining);
		return renderResult(Global.TRUE, text("保存视频培训记录（人员）成功！"));
	}

	/**
	  API 服务调用接口鉴权
	 * @param request
	 * @return
	 */
	private boolean authentication(HttpServletRequest request){

		String appToken = "4308AC04C32873ABC834BD3A74F561A9";

		//1， 校验 Token
		String token = request.getHeader("X-Token");
		if (!appToken.equals(token)) {
			return false;
		}

		// 2， 校验时间戳（防误调用/简单重放）
		String timestamp = request.getHeader("X-Timestamp");
		if (StrUtil.isBlank(timestamp)) {
			return false;
		}

		long reqTime;
		try {
			reqTime = Long.parseLong(timestamp);
		} catch (Exception e) {
			return false;
		}

		// 允许 5 分钟误差
		if (Math.abs(System.currentTimeMillis() - reqTime) > 5 * 60 * 1000) {
			return false;
		}
		return true;
	}
	
}