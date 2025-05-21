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
import com.jeesite.modules.swm.entity.SwmInspectionPlan;
import com.jeesite.modules.swm.service.SwmInspectionPlanService;

/**
 * 巡检计划Controller
 * 
 * @author Shawn
 * @version 2025-05-21
 */
@Controller
@RequestMapping(value = "${adminPath}/swmInspectionPlan")
public class SwmInspectionPlanController extends BaseController {

    @Autowired
    private SwmInspectionPlanService swmInspectionPlanService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmInspectionPlan get(String id, boolean isNewRecord) {
        return swmInspectionPlanService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmInspectionPlan swmInspectionPlan, Model model) {
        model.addAttribute("swmInspectionPlan", swmInspectionPlan);
        return "modules/swm/swmInspectionPlanList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmInspectionPlan> listData(SwmInspectionPlan swmInspectionPlan, HttpServletRequest request,
            HttpServletResponse response) {
        swmInspectionPlan.setPage(new Page<>(request, response));
        Page<SwmInspectionPlan> page = swmInspectionPlanService.findPage(swmInspectionPlan);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmInspectionPlan swmInspectionPlan, Model model) {
        model.addAttribute("swmInspectionPlan", swmInspectionPlan);
        return "modules/swm/swmInspectionPlanForm";
    }

    /**
     * 保存巡检计划
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmInspectionPlan swmInspectionPlan) {
        swmInspectionPlanService.save(swmInspectionPlan);
        return renderResult(Global.TRUE, text("保存巡检计划成功！"));
    }

    /**
     * 删除巡检计划
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmInspectionPlan swmInspectionPlan) {
        swmInspectionPlanService.delete(swmInspectionPlan);
        return renderResult(Global.TRUE, text("删除巡检计划成功！"));
    }

}