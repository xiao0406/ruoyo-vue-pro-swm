package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.jeesite.modules.fms.entity.FmsProdLine;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import com.jeesite.modules.swm.service.FmsProdLineService;

/**
 * 生产线档案Controller
 * @author wxy
 * @version 2026-03-02
 */
@Controller
@RequestMapping(value = "${adminPath}/fmsProdLine")
@Api(value = "生产线档案接口", tags = "生产线档案")
public class FmsProdLineController extends BaseController {

	@Resource
	private FmsProdLineService fmsProdLineService;



	/**
	 * 根据传入的中文转code
	 */
	@GetMapping(value = "getCode")
	@ResponseBody
	@ApiOperation(value = "根据传入的中文转code", notes = "根据传入的中文转code")
	public String getCode(String name) throws PinyinException {
		String code = PinyinHelper.getShortPinyin(name);
		return code;
	}
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public FmsProdLine get(String id, boolean isNewRecord) {
		return fmsProdLineService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(FmsProdLine fmsProdLine, Model model) {
		model.addAttribute("fmsProdLine", fmsProdLine);
		return "modules/swm/fmsProdLineList";
	}
	
	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "pageList")
	@ResponseBody
	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
	public Page<FmsProdLine> pageList(FmsProdLine fmsProdLine, HttpServletRequest request, HttpServletResponse response) {
		fmsProdLine.setPage(new Page<>(request, response));
		Page<FmsProdLine> page = fmsProdLineService.findPage(fmsProdLine);
		return page;
	}

	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public List<FmsProdLine> listData(FmsProdLine fmsProdLine, HttpServletRequest request, HttpServletResponse response) {
		List<FmsProdLine> list = fmsProdLineService.findList(fmsProdLine);
		return list;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(FmsProdLine fmsProdLine, Model model) {
		model.addAttribute("fmsProdLine", fmsProdLine);
		return "modules/swm/fmsProdLineForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(@Validated FmsProdLine fmsProdLine) {
		fmsProdLineService.save(fmsProdLine);
		return renderResult(Global.TRUE, text("保存生产线档案成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(FmsProdLine fmsProdLine) {
		fmsProdLineService.delete(fmsProdLine);
		return renderResult(Global.TRUE, text("删除生产线档案成功！"));
	}
	
}