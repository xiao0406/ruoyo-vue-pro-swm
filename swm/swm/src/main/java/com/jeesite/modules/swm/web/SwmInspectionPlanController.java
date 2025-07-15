package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmInspectionPlan;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.SwmInspectionPlanService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.job.task.InspectionPlanTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 巡检计划Controller
 *
 * @author Shawn
 * @version 2025-05-21
 */
@Controller
@RequestMapping(value = "${adminPath}/swmInspectionPlan")
public class SwmInspectionPlanController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SwmInspectionPlanController.class);

    @Autowired
    private SwmInspectionPlanService swmInspectionPlanService;
    @Autowired
    private SwmPersonService swmPersonService;
    @Autowired
    private InspectionPlanTask inspectionPlanTask;

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
     * 查询列表数据
     */
    @RequestMapping(value = "listTest")
    @ResponseBody
    public List<SwmInspectionPlan> listTest() {
        return swmInspectionPlanService.findList(new SwmInspectionPlan());
    }

    /**
     * 查询巡检计划下拉选择数据
     */
    @RequestMapping(value = "selectData")
    @ResponseBody
    public List<Map<String, Object>> selectData(String keyword) {
        SwmInspectionPlan query = new SwmInspectionPlan();
        // 如果有关键字，按计划名称进行模糊查询
        if (keyword != null && !keyword.isEmpty()) {
            query.setPlanName(keyword);
        }

        List<SwmInspectionPlan> planList = swmInspectionPlanService.findList(query);
        List<Map<String, Object>> result = new ArrayList<>();

        // 转换为前端需要的格式
        for (SwmInspectionPlan plan : planList) {
            Map<String, Object> item = new HashMap<>();
            item.put("value", plan.getId()); // 值为ID
            item.put("label", plan.getPlanName()); // 显示为计划名称

            // 可以添加更多需要的信息
            item.put("inspectionType", plan.getInspectionType());
            item.put("responsiblePerson", plan.getResponsiblePerson());

            result.add(item);
        }

        return result;
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
        SwmPerson swmPerson = swmPersonService.get(swmInspectionPlan.getResponsiblePersonId());
        if (swmPerson == null) {
            return renderResult(Global.FALSE, text("巡检负责人不存在！"));
        } else {
            swmInspectionPlan.setResponsiblePerson(swmPerson.getName());
        }
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
    
    /**
     * 开启巡检计划
     */
    @RequestMapping(value = "openPlan")
    @ResponseBody
    public String openPlan(SwmInspectionPlan swmInspectionPlan) {
        logger.info("开启巡检计划，ID: {}", swmInspectionPlan.getId());
        SwmInspectionPlan entity = swmInspectionPlanService.get(swmInspectionPlan.getId());
        if (entity == null) {
            return renderResult(Global.FALSE, text("巡检计划不存在！"));
        }
        entity.setPlanStatus(SwmInspectionPlan.PlanStatusEnum.OPEN);
        swmInspectionPlanService.save(entity);
        return renderResult(Global.TRUE, text("巡检计划已开启！"));
    }

    /**
     * 暂停巡检计划
     */
    @RequestMapping(value = "pausePlan")
    @ResponseBody
    public String pausePlan(SwmInspectionPlan swmInspectionPlan) {
        logger.info("暂停巡检计划，ID: {}", swmInspectionPlan.getId());
        SwmInspectionPlan entity = swmInspectionPlanService.get(swmInspectionPlan.getId());
        if (entity == null) {
            return renderResult(Global.FALSE, text("巡检计划不存在！"));
        }
        entity.setPlanStatus(SwmInspectionPlan.PlanStatusEnum.PAUSE);
        swmInspectionPlanService.save(entity);
        return renderResult(Global.TRUE, text("巡检计划已暂停！"));
    }

    /**
     * 测试接口：手动触发巡检任务生成
     * 
     * @author Shawn
     * @date 2025/01/22
     */
    @RequestMapping(value = "testCreateTask")
    @ResponseBody
    public String testCreateTask() {
        try {
            inspectionPlanTask.createInspectTask();
            return renderResult(Global.TRUE, text("巡检任务生成成功"));
        } catch (Exception e) {
            logger.error("手动触发巡检任务生成失败", e);
            return renderResult(Global.FALSE, "巡检任务生成失败：" + e.getMessage());
        }
    }

}
