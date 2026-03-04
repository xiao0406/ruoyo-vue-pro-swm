package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeesite.modules.fms.entity.FmsPositionArchive;
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
import com.jeesite.modules.swm.service.FmsPositionArchiveService;

/**
 * 位置档案定义Controller
 * @author wxy
 * @version 2026-03-02
 */
@Controller
@RequestMapping(value = "${adminPath}/fmsPositionArchive")
@Api(value = "位置档案定义接口", tags = "位置档案定义")
public class FmsPositionArchiveController extends BaseController {

	@Resource
	private FmsPositionArchiveService fmsPositionArchiveService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public FmsPositionArchive get(String id, boolean isNewRecord) {
		return fmsPositionArchiveService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(FmsPositionArchive fmsPositionArchive, Model model) {
		model.addAttribute("fmsPositionArchive", fmsPositionArchive);
		return "modules/swm/fmsPositionArchiveList";
	}
	
	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "pageList")
	@ResponseBody
	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
	public Page<FmsPositionArchive> pageList(FmsPositionArchive fmsPositionArchive, HttpServletRequest request, HttpServletResponse response) {
		fmsPositionArchive.setPage(new Page<>(request, response));
		Page<FmsPositionArchive> page = fmsPositionArchiveService.findPage(fmsPositionArchive);
		return page;
	}

	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public List<FmsPositionArchive> listData(FmsPositionArchive fmsPositionArchive, HttpServletRequest request, HttpServletResponse response) {
		List<FmsPositionArchive> list = fmsPositionArchiveService.findList(fmsPositionArchive);
		return list;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(FmsPositionArchive fmsPositionArchive, Model model) {
		model.addAttribute("fmsPositionArchive", fmsPositionArchive);
		return "modules/swm/fmsPositionArchiveForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(@Validated FmsPositionArchive fmsPositionArchive) {
		fmsPositionArchiveService.save(fmsPositionArchive);
		return renderResult(Global.TRUE, text("保存位置档案定义成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(FmsPositionArchive fmsPositionArchive) {
		fmsPositionArchiveService.delete(fmsPositionArchive);
		return renderResult(Global.TRUE, text("删除位置档案定义成功！"));
	}
	
}