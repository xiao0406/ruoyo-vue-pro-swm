package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeesite.modules.fms.entity.FmsWorkGroup;
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
import com.jeesite.modules.swm.service.FmsWorkGroupService;

/**
 * 班组档案Controller
 * @author wxy
 * @version 2026-03-02
 */
@Controller
@RequestMapping(value = "${adminPath}/fmsWorkGroup")
@Api(value = "班组档案接口", tags = "班组档案")
public class FmsWorkGroupController extends BaseController {

	@Resource
	private FmsWorkGroupService fmsWorkGroupService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public FmsWorkGroup get(String id, boolean isNewRecord) {
		return fmsWorkGroupService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(FmsWorkGroup fmsWorkGroup, Model model) {
		model.addAttribute("fmsWorkGroup", fmsWorkGroup);
		return "modules/swm/fmsWorkGroupList";
	}
	
	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "pageList")
	@ResponseBody
	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
	public Page<FmsWorkGroup> pageList(FmsWorkGroup fmsWorkGroup, HttpServletRequest request, HttpServletResponse response) {
		fmsWorkGroup.setPage(new Page<>(request, response));
		Page<FmsWorkGroup> page = fmsWorkGroupService.findPage(fmsWorkGroup);
		return page;
	}

	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public List<FmsWorkGroup> listData(FmsWorkGroup fmsWorkGroup, HttpServletRequest request, HttpServletResponse response) {
		List<FmsWorkGroup> list = fmsWorkGroupService.findList(fmsWorkGroup);
		return list;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(FmsWorkGroup fmsWorkGroup, Model model) {
		model.addAttribute("fmsWorkGroup", fmsWorkGroup);
		return "modules/swm/fmsWorkGroupForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(@Validated FmsWorkGroup fmsWorkGroup) {
		fmsWorkGroupService.save(fmsWorkGroup);
		return renderResult(Global.TRUE, text("保存班组档案成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(FmsWorkGroup fmsWorkGroup) {
		fmsWorkGroupService.delete(fmsWorkGroup);
		return renderResult(Global.TRUE, text("删除班组档案成功！"));
	}
	
}