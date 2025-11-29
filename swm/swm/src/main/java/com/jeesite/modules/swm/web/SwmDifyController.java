package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
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

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmDify;
import com.jeesite.modules.swm.service.SwmDifyService;

/**
 * 安全帽：ai日报表Controller
 * @author wxy
 * @version 2025-11-27
 */
@Controller
@RequestMapping(value = "${adminPath}/swmDify")
@Api(value = "安全帽：ai日报表接口", tags = "安全帽：ai日报表")
public class SwmDifyController extends BaseController {

	@Resource
	private SwmDifyService swmDifyService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmDify get(String id, boolean isNewRecord) {
		return swmDifyService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(SwmDify swmDify, Model model) {
		model.addAttribute("swmDify", swmDify);
		return "modules/swm/swmDifyList";
	}
	
	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "pageList")
	@ResponseBody
	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
	public Page<SwmDify> pageList(SwmDify swmDify, HttpServletRequest request, HttpServletResponse response) {
		swmDify.setPage(new Page<>(request, response));
		Page<SwmDify> page = swmDifyService.findPage(swmDify);
		return page;
	}

	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public List<SwmDify> listData(SwmDify swmDify, HttpServletRequest request, HttpServletResponse response) {
		List<SwmDify> list = swmDifyService.findList(swmDify);
		return list;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(SwmDify swmDify, Model model) {
		model.addAttribute("swmDify", swmDify);
		return "modules/swm/swmDifyForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(@Validated SwmDify swmDify) {
		swmDifyService.save(swmDify);
		return renderResult(Global.TRUE, text("保存安全帽：ai日报表成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(SwmDify swmDify) {
		swmDifyService.delete(swmDify);
		return renderResult(Global.TRUE, text("删除安全帽：ai日报表成功！"));
	}
	
}