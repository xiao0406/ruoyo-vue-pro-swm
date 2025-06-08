package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmArea;
import com.jeesite.modules.swm.service.SwmAreaService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 区域管理Controller
 * 
 * @author Shawn
 * @version 2025-06-22
 */
@Controller
@RequestMapping(value = "${adminPath}/swmArea")
@Api(value = "区域管理接口", tags = "区域管理接口")
public class SwmAreaController extends BaseController {

    @Autowired
    private SwmAreaService swmAreaService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmArea get(String id, boolean isNewRecord) {
        return swmAreaService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmArea swmArea, Model model) {
        model.addAttribute("swmArea", swmArea);
        return "modules/swm/swmAreaList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmArea> listData(SwmArea swmArea, HttpServletRequest request, HttpServletResponse response) {
        swmArea.setPage(new Page<>(request, response));
        Page<SwmArea> page = swmAreaService.findPage(swmArea);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ApiOperation("表单页面")
    public String form(SwmArea swmArea, Model model) {
        model.addAttribute("swmArea", swmArea);
        return "modules/swm/swmAreaForm";
    }

    /**
     * 保存区域
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存区域")
    public String save(@Validated SwmArea swmArea) {
        swmAreaService.save(swmArea);
        return renderResult(Global.TRUE, text("保存区域成功！"));
    }

    /**
     * 删除区域
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除区域")
    public String delete(SwmArea swmArea) {
        swmAreaService.delete(swmArea);
        return renderResult(Global.TRUE, text("删除区域成功！"));
    }
}