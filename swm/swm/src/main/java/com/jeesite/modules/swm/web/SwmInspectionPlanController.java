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
import com.jeesite.modules.entity.SwmHazardSource;
import com.jeesite.modules.swm.service.SwmHazardSourceService;
import org.apache.commons.lang3.StringUtils;

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
    @Autowired
    private SwmHazardSourceService swmHazardSourceService;

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
        // 验证巡检负责人是否存在
        SwmPerson swmPerson = swmPersonService.get(swmInspectionPlan.getResponsiblePersonId());
        if (swmPerson == null) {
            return renderResult(Global.FALSE, text("巡检负责人不存在！"));
        } else {
            swmInspectionPlan.setResponsiblePerson(swmPerson.getName());
        }

        // 处理危险源巡检类型的特殊情况（多选危险源）
        if ("3".equals(swmInspectionPlan.getInspectionType()) && swmInspectionPlan.getHazardSourceIds() != null
                && swmInspectionPlan.getHazardSourceIds().length > 0) {
            // 获取选中的危险源ID数组
            String[] hazardSourceIds = swmInspectionPlan.getHazardSourceIds();

            // 创建一个包含所有危险源名称的列表，用于生成计划名称
            List<String> hazardSourceNames = new ArrayList<>();

            // 将所有选中的危险源ID拼接为逗号分隔的字符串
            String combinedHazardSourceIds = String.join(",", hazardSourceIds);
            swmInspectionPlan.setHazardSourceId(combinedHazardSourceIds);

            // 收集所有危险源名称
            for (String hazardSourceId : hazardSourceIds) {
                SwmHazardSource hazardSource = swmHazardSourceService.get(hazardSourceId);
                if (hazardSource != null) {
                    hazardSourceNames.add(hazardSource.getHazardName());
                    // 更新危险源的巡检状态
                    updateHazardSourcePatrolStatus(hazardSourceId, "1", swmInspectionPlan);
                }
            }

            // 将所有危险源名称以逗号分隔存储
            if (!hazardSourceNames.isEmpty()) {
                String combinedHazardSourceNames = String.join(",", hazardSourceNames);
                swmInspectionPlan.setHazardSourceName(combinedHazardSourceNames);

                // 如果用户没有指定计划名称，使用多个危险源的组合名称或数量描述
                if (StringUtils.isBlank(swmInspectionPlan.getPlanName())) {
                    if (hazardSourceNames.size() <= 2) {
                        // 如果只有1-2个危险源，直接使用它们的名称作为计划名称
                        swmInspectionPlan.setPlanName(combinedHazardSourceNames + "巡检计划");
                    } else {
                        // 如果有3个或更多危险源，使用数量描述
                        swmInspectionPlan.setPlanName(hazardSourceNames.size() + "个危险源巡检计划");
                    }
                }
            }

            // 保存巡检计划
            swmInspectionPlanService.save(swmInspectionPlan);

            return renderResult(Global.TRUE, text("创建巡检计划成功！"));
        } else {
            // 常规单个巡检计划保存
            swmInspectionPlanService.save(swmInspectionPlan);

            // 如果是危险源巡检且有关联的危险源，更新危险源的巡检状态
            if ("3".equals(swmInspectionPlan.getInspectionType())
                    && StringUtils.isNotBlank(swmInspectionPlan.getHazardSourceId())) {
                updateHazardSourcePatrolStatus(swmInspectionPlan.getHazardSourceId(), "1", swmInspectionPlan);
            }

            return renderResult(Global.TRUE, text("保存巡检计划成功！"));
        }
    }

    /**
     * 更新危险源的巡检状态
     * 
     * @param hazardSourceId   危险源ID
     * @param isPatrolIncluded 是否加入巡检（1-是，0-否）
     * @param plan             巡检计划信息
     */
    private void updateHazardSourcePatrolStatus(String hazardSourceId, String isPatrolIncluded,
            SwmInspectionPlan plan) {
        SwmHazardSource hazardSource = swmHazardSourceService.get(hazardSourceId);
        if (hazardSource != null) {
            hazardSource.setIsPatrolIncluded(isPatrolIncluded);

            // 如果加入巡检，同步巡检相关信息
            if ("1".equals(isPatrolIncluded)) {
                hazardSource.setFrequencyDays(plan.getFrequencyDays());
                hazardSource.setResponsiblePersonId(plan.getResponsiblePersonId());
                hazardSource.setResponsiblePerson(plan.getResponsiblePerson());
                hazardSource.setFirstInspectionTime(plan.getFirstInspectionTime());
            } else {
                // 如果取消巡检，清空巡检相关信息
                hazardSource.setFrequencyDays(null);
                hazardSource.setResponsiblePersonId(null);
                hazardSource.setResponsiblePerson(null);
                hazardSource.setFirstInspectionTime(null);
            }

            // 保存更新后的危险源信息
            swmHazardSourceService.save(hazardSource);
            logger.info("已更新危险源ID: {} 的巡检状态为: {}", hazardSourceId, isPatrolIncluded);
        }
    }

    /**
     * 删除巡检计划
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmInspectionPlan swmInspectionPlan) {
        // 获取完整的巡检计划信息
        SwmInspectionPlan plan = swmInspectionPlanService.get(swmInspectionPlan.getId());

        // 检查是否有关联的危险源
        if (plan != null && StringUtils.isNotBlank(plan.getHazardSourceId())) {
            // 检查是否包含多个危险源ID（逗号分隔）
            String hazardSourceId = plan.getHazardSourceId();
            String[] hazardSourceIds = hazardSourceId.split(",");

            // 遍历所有关联的危险源ID
            for (String id : hazardSourceIds) {
                if (StringUtils.isNotBlank(id)) {
                    // 获取关联的危险源
                    SwmHazardSource hazardSource = swmHazardSourceService.get(id);
                    if (hazardSource != null) {
                        // 将危险源的"是否加入巡检"设置为否
                        hazardSource.setIsPatrolIncluded("0");
                        // 清除巡检相关字段
                        hazardSource.setFrequencyDays(null);
                        hazardSource.setResponsiblePersonId(null);
                        hazardSource.setFirstInspectionTime(null);
                        // 保存更新后的危险源信息
                        swmHazardSourceService.save(hazardSource);
                        logger.info("已将危险源ID: {} 的巡检状态设置为否", id);
                    }
                }
            }
        }

        // 删除巡检计划
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

    /**
     * 获取巡检计划详情
     */
    @RequestMapping(value = "get")
    @ResponseBody
    public SwmInspectionPlan getInspectionPlan(SwmInspectionPlan swmInspectionPlan) {
        SwmInspectionPlan entity = swmInspectionPlanService.get(swmInspectionPlan);

        // 如果是危险源巡检类型且有关联的危险源ID
        if (entity != null && "3".equals(entity.getInspectionType()) &&
                StringUtils.isNotBlank(entity.getHazardSourceId())) {

            // 将hazardSourceId转换为hazardSourceIds数组（支持单个或多个危险源）
            String[] hazardSourceIds = entity.getHazardSourceId().split(",");
            entity.setHazardSourceIds(hazardSourceIds);

            // 处理危险源名称
            if (StringUtils.isNotBlank(entity.getHazardSourceName())) {
                // 如果已经有危险源名称，直接使用
                entity.setHazardSourceNames(entity.getHazardSourceName().split(","));
            } else {
                // 如果没有名称，查询每个危险源的名称
                String[] hazardSourceNames = new String[hazardSourceIds.length];
                for (int i = 0; i < hazardSourceIds.length; i++) {
                    SwmHazardSource hazardSource = swmHazardSourceService.get(hazardSourceIds[i]);
                    hazardSourceNames[i] = hazardSource != null ? hazardSource.getHazardName() : hazardSourceIds[i];
                }
                entity.setHazardSourceNames(hazardSourceNames);
            }
        }

        return entity;
    }

}
