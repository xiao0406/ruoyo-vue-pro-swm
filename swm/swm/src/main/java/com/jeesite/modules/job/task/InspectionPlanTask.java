package com.jeesite.modules.job.task;

import cn.hutool.core.date.DateUtil;
import com.jeesite.modules.swm.entity.SwmInspectionList;
import com.jeesite.modules.swm.entity.SwmInspectionPlan;
import com.jeesite.modules.swm.service.SwmInspectionListService;
import com.jeesite.modules.swm.service.SwmInspectionPlanService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 巡检计划定时生成巡检任务
 * @author: cjie
 * @date: 2025/6/12
 */
@Component
@Slf4j
public class InspectionPlanTask {
    @Autowired
    private SwmInspectionPlanService swmInspectionPlanService;
    @Autowired
    private SwmInspectionListService swmInspectionListService;

    /**
     * 创建巡检任务(每日凌晨创建一个未来任务)
     */
    @XxlJob("createInspectTask")
    @Transactional(rollbackFor = Exception.class)
    public void createInspectTask() {
        XxlJobHelper.log("开始创建巡检任务,时间:" + DateUtil.now());

        // 1. 查询所有有效的巡检计划
        List<SwmInspectionPlan> inspectionPlanList = swmInspectionPlanService.findList(new SwmInspectionPlan());

        // 2. 遍历每个巡检计划
        for (SwmInspectionPlan plan : inspectionPlanList) {
            try {
                // 3. 获取计划的基本信息
                String planId = plan.getId();
                String planName = plan.getPlanName();
                String inspectionType = plan.getInspectionType();
                String responsiblePersonId = plan.getResponsiblePersonId();
                Date firstInspectionTime = plan.getFirstInspectionTime();
                Integer frequencyDays = plan.getFrequencyDays();

                // 4. 检查必要字段是否为空
                if (firstInspectionTime == null || frequencyDays == null) {
                    XxlJobHelper.log("跳过计划[" + planName + "]，因为首次巡检时间或频次为空");
                    continue;
                }

                // 5. 计算需要生成的任务时间范围
                Date now = new Date();
                Date lastTaskDate = getLastTaskDate(planId); // 获取该计划最后一次任务的开始时间

                // 如果是第一次生成任务，使用首次巡检时间作为基准
                Date baseDate = (lastTaskDate != null) ? lastTaskDate : firstInspectionTime;

                // 6. 计算需要生成的任务（包括补全和未来任务）
                List<Date> taskDates = calculateTaskDates(baseDate, frequencyDays, now);

                // 7. 为每个计算出的日期创建巡检任务（跳过已存在的）
                for (Date taskDate : taskDates) {
                    SwmInspectionList task = new SwmInspectionList();
                    task.setPlanId(planId);
                    task.setPlanName(planName);
                    task.setInspectionType(inspectionType);
                    task.setInspectorId(responsiblePersonId);
                    task.setStartTime(taskDate);
                    task.setInspectionListStatus(SwmInspectionList.InspectionListStatusEnum.WAIT);

                    // 保存巡检任务
                    swmInspectionListService.save(task);
                    XxlJobHelper.log("成功创建巡检任务:计划[" + planName + "], 时间[" + DateUtil.formatDateTime(taskDate) + "]");
                }

            } catch (Exception e) {
                XxlJobHelper.log("处理计划[" + plan.getPlanName() + "]时出错:" + e.getMessage());
            }
        }

        XxlJobHelper.log("巡检任务生成完成");
    }

    /**
     * 获取指定计划的最后一次任务的开始时间
     */
    private Date getLastTaskDate(String planId) {
        SwmInspectionList lastTask = swmInspectionListService.getLastTaskByPlanId(planId);
        return lastTask != null ? lastTask.getStartTime() : null;
    }

    /**
     * 计算需要生成的巡检任务日期
     * 保证：
     * 1. 补全所有过去应生成但未生成的任务
     * 2. 确保至少有一个未来的任务
     */
    private List<Date> calculateTaskDates(Date baseDate, int frequencyDays, Date endDate) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);

        // 先补全所有过去应生成的任务
        while (calendar.getTime().before(endDate)) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, frequencyDays);
        }

        // 确保至少有一个未来的任务
        if (dates.isEmpty() || !dates.get(dates.size()-1).after(endDate)) {
            dates.add(calendar.getTime());
        }

        return dates;
    }

}
