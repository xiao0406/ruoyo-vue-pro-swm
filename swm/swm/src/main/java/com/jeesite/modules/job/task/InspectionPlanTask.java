package com.jeesite.modules.job.task;

import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.swm.entity.SwmInspectionList;
import com.jeesite.modules.swm.entity.SwmInspectionPlan;
import com.jeesite.modules.swm.service.SwmInspectionListService;
import com.jeesite.modules.swm.service.SwmInspectionPlanService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
    
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserService userService;

    /**
     * 创建巡检任务(每日凌晨创建一个未来任务)
     */
    @XxlJob("createInspectTask")
    @Transactional(rollbackFor = Exception.class)
    public void createInspectTask() {

        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }
        //为每个租户都生成排班计划
        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();
            //设置当前线程的租户信息
            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            TenantContext.set(corpCode);
            XxlJobHelper.log("开始处理租户：{} ========================", corpCode);

            try {

                Date startTime = new Date();
                XxlJobHelper.log("========== 开始执行巡检任务生成定时任务 ==========");
                XxlJobHelper.log("任务开始时间: " + dateTimeFormat.format(startTime));
                log.info("========== 开始执行巡检任务生成定时任务 ==========");
                log.info("任务开始时间: {}", dateTimeFormat.format(startTime));

                // 1. 查询所有有效的巡检计划
                SwmInspectionPlan queryPlan = new SwmInspectionPlan();
                queryPlan.setPlanStatus(SwmInspectionPlan.PlanStatusEnum.OPEN);  // 只查询开启状态的计划
                queryPlan.setRandom(new Random().nextInt(1_000_000));
                List<SwmInspectionPlan> inspectionPlanList = swmInspectionPlanService.findList(queryPlan);
                XxlJobHelper.log("查询到 " + inspectionPlanList.size() + " 个开启状态的巡检计划");
                log.info("查询到 {} 个开启状态的巡检计划", inspectionPlanList.size());

                int successCount = 0;
                int skipCount = 0;
                int errorCount = 0;

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
                        String hazardSourceId = plan.getHazardSourceId();
                        String hazardSourceName = plan.getHazardSourceName();

                        XxlJobHelper.log("处理计划: [" + planName + "], ID: [" + planId + "], 巡检频次: [" + frequencyDays + "天]");
                        XxlJobHelper.log("关联危险源: [" + (hazardSourceName != null ? hazardSourceName : "无") + "], 危险源ID: [" + (hazardSourceId != null ? hazardSourceId : "无") + "]");
                        log.info("处理计划: [{}], ID: [{}], 巡检频次: [{}天]", planName, planId, frequencyDays);
                        log.info("关联危险源: [{}], 危险源ID: [{}]", (hazardSourceName != null ? hazardSourceName : "无"), (hazardSourceId != null ? hazardSourceId : "无"));

                        // 4. 检查必要字段是否为空
                        if (firstInspectionTime == null || frequencyDays == null) {
                            String msg = "跳过计划[" + planName + "]，因为首次巡检时间或频次为空";
                            XxlJobHelper.log(msg);
                            log.warn(msg);
                            skipCount++;
                            continue;
                        }

                        // 5. 获取该计划最新的巡检任务
                        SwmInspectionList lastTask = getLastTaskByPlanId(planId);
                        Date now = new Date();

                        // 如果频次为0，表示只巡检一次
                        if (frequencyDays == 0) {
                            if (lastTask != null) {
                                // 已经有一次巡检任务了，跳过
                                String skipMsg = "计划[" + planName + "]的巡检频次为0，表示只巡检一次，已有巡检任务，跳过";
                                XxlJobHelper.log(skipMsg);
                                log.info(skipMsg);
                                skipCount++;
                                continue;
                            } else {
                                // 还没有巡检任务，创建一次
                                XxlJobHelper.log("计划[" + planName + "]的巡检频次为0，表示只巡检一次，创建首次巡检任务");
                                log.info("计划[{}]的巡检频次为0，表示只巡检一次，创建首次巡检任务", planName);

                                Date taskDate;
                                if (firstInspectionTime.after(now)) {
                                    taskDate = firstInspectionTime;
                                } else {
                                    taskDate = combineDateAndTime(now, firstInspectionTime);
                                }

                                // 防重复检查：检查该日期是否已有任务
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                String targetDateStr = dateFormat.format(taskDate);
                                boolean taskExistsForDate = swmInspectionListService.existsByPlanIdAndDate(planId, targetDateStr);

                                if (!taskExistsForDate) {
                                    SwmInspectionList task = new SwmInspectionList();
                                    task.setPlanId(planId);
                                    task.setPlanName(planName);
                                    task.setInspectionType(inspectionType);
                                    task.setInspectorId(responsiblePersonId);
                                    task.setStartTime(taskDate);
                                    task.setInspectionListStatus(SwmInspectionList.InspectionListStatusEnum.WAIT);

                                    // 保存巡检任务
                                    swmInspectionListService.save(task);
                                    String successMsg = "成功创建一次性巡检任务: 计划[" + planName + "], 时间[" + dateTimeFormat.format(taskDate) + "], 新任务ID: [" + task.getId() + "]";
                                    XxlJobHelper.log(successMsg);
                                    log.info(successMsg);
                                    successCount++;
                                } else {
                                    String skipMsg = "计划[" + planName + "]在日期[" + targetDateStr + "]已有任务，跳过重复创建";
                                    XxlJobHelper.log(skipMsg);
                                    log.info(skipMsg);
                                    skipCount++;
                                }
                                continue;
                            }
                        }

                        if (lastTask != null) {
                            XxlJobHelper.log("该计划已有巡检任务，最新任务ID: [" + lastTask.getId() + "]");
                            log.info("该计划已有巡检任务，最新任务ID: [{}]", lastTask.getId());
                        } else {
                            XxlJobHelper.log("该计划无历史巡检任务");
                            log.info("该计划无历史巡检任务");
                        }

                        // 6. 确定是否需要创建新任务
                        boolean needCreateTask = false;
                        Date newTaskDate = null;

                        if (lastTask == null) {
                            // 如果没有上一次任务，则使用首次巡检时间作为基准
                            XxlJobHelper.log("计划[" + planName + "]无历史任务，使用首次巡检时间作为基准");
                            log.info("计划[{}]无历史任务，使用首次巡检时间作为基准", planName);
                            needCreateTask = true;

                            // 如果首次巡检时间未到，使用首次巡检时间
                            // 如果首次巡检时间已过，则使用当前日期+首次巡检时间的时分秒
                            if (firstInspectionTime.after(now)) {
                                newTaskDate = firstInspectionTime;
                                XxlJobHelper.log("首次巡检时间未到，使用原定时间: [" + dateTimeFormat.format(newTaskDate) + "]");
                                log.info("首次巡检时间未到，使用原定时间: [{}]", dateTimeFormat.format(newTaskDate));
                            } else {
                                newTaskDate = combineDateAndTime(now, firstInspectionTime);
                                XxlJobHelper.log("首次巡检时间已过，更新为当前日期+原时间: [" + dateTimeFormat.format(newTaskDate) + "]");
                                log.info("首次巡检时间已过，更新为当前日期+原时间: [{}]", dateTimeFormat.format(newTaskDate));
                            }
                        } else {
                            // 有上一次任务，检查是否需要创建新任务
                            Date lastTaskDate = lastTask.getStartTime();
                            XxlJobHelper.log("计划[" + planName + "]最后任务时间: [" + dateTimeFormat.format(lastTaskDate) + "]");
                            log.info("计划[{}]最后任务时间: [{}]", planName, dateTimeFormat.format(lastTaskDate));

                            // 计算下一次任务日期
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(lastTaskDate);
                            calendar.add(Calendar.DAY_OF_MONTH, frequencyDays);
                            Date expectedNextDate = calendar.getTime();

                            XxlJobHelper.log("计划[" + planName + "]下一次预期执行时间: [" + dateTimeFormat.format(expectedNextDate) + "]");
                            log.info("计划[{}]下一次预期执行时间: [{}]", planName, dateTimeFormat.format(expectedNextDate));

                            // 使用日期级比较判断是否需要创建新任务（修复1天频次隔天生成的问题）
                            Calendar expectedCal = Calendar.getInstance();
                            expectedCal.setTime(expectedNextDate);
                            Calendar nowCal = Calendar.getInstance();
                            nowCal.setTime(now);

                            // 只比较年月日，忽略时分秒
                            boolean shouldCreateTaskByDate = expectedCal.get(Calendar.YEAR) < nowCal.get(Calendar.YEAR) ||
                                    (expectedCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                                            expectedCal.get(Calendar.DAY_OF_YEAR) <= nowCal.get(Calendar.DAY_OF_YEAR));

                            if (shouldCreateTaskByDate) {
                                needCreateTask = true;
                                // 使用当前日期+上一次任务的时分秒
                                newTaskDate = combineDateAndTime(now, lastTaskDate);
                                XxlJobHelper.log("已到达或超过预期执行日期，需要创建新任务，时间为: [" + dateTimeFormat.format(newTaskDate) + "]");
                                log.info("已到达或超过预期执行日期，需要创建新任务，时间为: [{}]", dateTimeFormat.format(newTaskDate));
                            } else {
                                XxlJobHelper.log("未到达预期执行日期，不需要创建新任务");
                                log.info("未到达预期执行日期，不需要创建新任务");
                            }
                        }

                        // 7. 创建新任务（增加防重复检查）
                        if (needCreateTask && newTaskDate != null) {
                            // 防重复检查：检查该日期是否已有任务
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String targetDateStr = dateFormat.format(newTaskDate);
                            boolean taskExistsForDate = swmInspectionListService.existsByPlanIdAndDate(planId, targetDateStr);

                            if (!taskExistsForDate) {
                                SwmInspectionList task = new SwmInspectionList();
                                task.setPlanId(planId);
                                task.setPlanName(planName);
                                task.setInspectionType(inspectionType);
                                task.setInspectorId(responsiblePersonId);
                                task.setStartTime(newTaskDate);
                                task.setInspectionListStatus(SwmInspectionList.InspectionListStatusEnum.WAIT);

                                // 保存巡检任务
                                swmInspectionListService.save(task);
                                String successMsg = "成功创建巡检任务: 计划[" + planName + "], 时间[" + dateTimeFormat.format(newTaskDate) + "], 新任务ID: [" + task.getId() + "]";
                                XxlJobHelper.log(successMsg);
                                log.info(successMsg);
                                successCount++;
                            } else {
                                String skipMsg = "计划[" + planName + "]在日期[" + targetDateStr + "]已有任务，跳过重复创建";
                                XxlJobHelper.log(skipMsg);
                                log.info(skipMsg);
                                skipCount++;
                            }
                        } else {
                            String skipMsg = "计划[" + planName + "]不满足创建新任务的条件，跳过";
                            XxlJobHelper.log(skipMsg);
                            log.info(skipMsg);
                            skipCount++;
                        }

                        XxlJobHelper.log("------------------------------------------------");
                        log.info("------------------------------------------------");

                    } catch (Exception e) {
                        String errorMsg = "处理计划[" + plan.getPlanName() + "]时出错: " + e.getMessage();
                        XxlJobHelper.log(errorMsg);
                        log.error(errorMsg, e);
                        errorCount++;
                    }
                }

                Date endTime = new Date();
                long executionTime = endTime.getTime() - startTime.getTime();

                XxlJobHelper.log("========== 巡检任务生成定时任务执行完成 ==========");
                XxlJobHelper.log("任务结束时间: " + dateTimeFormat.format(endTime));
                XxlJobHelper.log("任务执行时间: " + executionTime + "毫秒");
                XxlJobHelper.log("处理结果: 共处理" + inspectionPlanList.size() + "个计划，成功创建" + successCount + "个任务，跳过" + skipCount + "个计划，失败" + errorCount + "个计划");

                log.info("========== 巡检任务生成定时任务执行完成 ==========");
                log.info("任务结束时间: {}", dateTimeFormat.format(endTime));
                log.info("任务执行时间: {}毫秒", executionTime);
                log.info("处理结果: 共处理{}个计划，成功创建{}个任务，跳过{}个计划，失败{}个计划",
                        inspectionPlanList.size(), successCount, skipCount, errorCount);


            }catch (Exception e){
                XxlJobHelper.log(e);
            }finally {
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }
    }

    /**
     * 获取指定计划的最后一次任务
     */
    private SwmInspectionList getLastTaskByPlanId(String planId) {
        SwmInspectionList lastTask = swmInspectionListService.getLastTaskByPlanId(planId);
        if (lastTask != null) {
            log.debug("获取到计划[{}]最后一次任务, ID: [{}], 开始时间: [{}]", 
                    planId, lastTask.getId(), lastTask.getStartTime() != null ? dateTimeFormat.format(lastTask.getStartTime()) : "未设置");
        } else {
            log.debug("计划[{}]没有历史任务记录", planId);
        }
        return lastTask;
    }
    
    /**
     * 合并日期和时间
     * 使用date1的年月日和date2的时分秒
     */
    private Date combineDateAndTime(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        
        // 保留date1的年月日
        int year = calendar1.get(Calendar.YEAR);
        int month = calendar1.get(Calendar.MONTH);
        int day = calendar1.get(Calendar.DAY_OF_MONTH);
        
        // 保留date2的时分秒
        int hour = calendar2.get(Calendar.HOUR_OF_DAY);
        int minute = calendar2.get(Calendar.MINUTE);
        int second = calendar2.get(Calendar.SECOND);
        
        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.set(year, month, day, hour, minute, second);
        
        Date result = resultCalendar.getTime();
        log.debug("合并日期和时间: 日期源[{}], 时间源[{}], 结果[{}]", 
                dateTimeFormat.format(date1), dateTimeFormat.format(date2), dateTimeFormat.format(result));
        return result;
    }
}
