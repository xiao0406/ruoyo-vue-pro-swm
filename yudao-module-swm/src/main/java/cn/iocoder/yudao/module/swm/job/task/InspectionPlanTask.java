package cn.iocoder.yudao.module.swm.job.task;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionListDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionPlanDO;
import cn.iocoder.yudao.module.swm.enums.SwmEnums;
import cn.iocoder.yudao.module.swm.service.SwmInspectionListService;
import cn.iocoder.yudao.module.swm.service.SwmInspectionPlanService;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 巡检计划定时生成巡检任务
 * @author: cjie
 * @date: 2025/6/12
 */
@Component
@Slf4j
public class InspectionPlanTask {
    @Resource
    private SwmInspectionPlanService swmInspectionPlanService;
    @Resource
    private SwmInspectionListService swmInspectionListService;
    @Resource
    private TenantService tenantService;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 创建巡检任务(每日凌晨创建一个未来任务)
     */
    @XxlJob("createInspectTask")
    @Transactional(rollbackFor = Exception.class)
    public void createInspectTask() {

        //获取系统所有租户信息
        List<TenantDO> tenantList = tenantService.getTenantListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollectionUtils.isEmpty(tenantList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }
        //为每个租户都生成排班计划
        for (TenantDO tenant : tenantList) {
            Long tenantId = tenant.getId();
            try {
                TenantUtils.execute(tenantId, () -> {
                    XxlJobHelper.log("开始处理租户：{} ({}) ========================", tenant.getName(), tenantId);

                    LocalDateTime startTime = LocalDateTime.now();
                XxlJobHelper.log("========== 开始执行巡检任务生成定时任务 ==========");
                XxlJobHelper.log("任务开始时间: " + startTime.format(DATE_TIME_FORMAT));
                log.info("========== 开始执行巡检任务生成定时任务 ==========");
                log.info("任务开始时间: {}", startTime.format(DATE_TIME_FORMAT));

                // 1. 查询所有有效的巡检计划
                SwmInspectionPlanDO queryPlan = new SwmInspectionPlanDO();
                queryPlan.setPlanStatus(SwmEnums.PlanStatusEnum.OPEN.getValue());  // 只查询开启状态的计划
                List<SwmInspectionPlanDO> inspectionPlanList = swmInspectionPlanService.findList(queryPlan);
                XxlJobHelper.log("查询到 " + inspectionPlanList.size() + " 个开启状态的巡检计划");
                log.info("查询到 {} 个开启状态的巡检计划", inspectionPlanList.size());

                int successCount = 0;
                int skipCount = 0;
                int errorCount = 0;

                // 2. 遍历每个巡检计划
                for (SwmInspectionPlanDO plan : inspectionPlanList) {
                    try {
                        // 3. 获取计划的基本信息
                        String planId = plan.getId();
                        String planName = plan.getPlanName();
                        String inspectionType = plan.getInspectionType();
                        String responsiblePersonId = plan.getResponsiblePersonId();
                        LocalDateTime firstInspectionTime = plan.getFirstInspectionTime();
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
                        SwmInspectionListDO lastTask = getLastTaskByPlanId(planId);
                        LocalDateTime now = LocalDateTime.now();

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

                                LocalDateTime taskDate;
                                if (firstInspectionTime.isAfter(now)) {
                                    taskDate = firstInspectionTime;
                                } else {
                                    taskDate = combineDateAndTime(now, firstInspectionTime);
                                }

                                // 防重复检查：检查该日期是否已有任务
                                String targetDateStr = taskDate.format(DATE_FORMAT);
                                boolean taskExistsForDate = swmInspectionListService.existsByPlanIdAndDate(planId, targetDateStr);

                                if (!taskExistsForDate) {
                                    SwmInspectionListDO task = new SwmInspectionListDO();
                                    task.setPlanId(planId);
                                    task.setPlanName(planName);
                                    task.setInspectionType(inspectionType);
                                    task.setInspectorId(responsiblePersonId);
                                    task.setStartTime(taskDate);
                                    task.setInspectionListStatus(SwmEnums.InspectionListStatusEnum.WAIT.getValue());

                                    // 保存巡检任务
                                    swmInspectionListService.createSwmInspectionList(task);
                                    String successMsg = "成功创建一次性巡检任务: 计划[" + planName + "], 时间[" + taskDate.format(DATE_TIME_FORMAT) + "], 新任务ID: [" + task.getId() + "]";
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
                        LocalDateTime newTaskDate = null;

                        if (lastTask == null) {
                            // 如果没有上一次任务，则使用首次巡检时间作为基准
                            XxlJobHelper.log("计划[" + planName + "]无历史任务，使用首次巡检时间作为基准");
                            log.info("计划[{}]无历史任务，使用首次巡检时间作为基准", planName);
                            needCreateTask = true;

                            // 如果首次巡检时间未到，使用首次巡检时间
                            // 如果首次巡检时间已过，则使用当前日期+首次巡检时间的时分秒
                            if (firstInspectionTime.isAfter(now)) {
                                newTaskDate = firstInspectionTime;
                                XxlJobHelper.log("首次巡检时间未到，使用原定时间: [" + newTaskDate.format(DATE_TIME_FORMAT) + "]");
                                log.info("首次巡检时间未到，使用原定时间: [{}]", newTaskDate.format(DATE_TIME_FORMAT));
                            } else {
                                newTaskDate = combineDateAndTime(now, firstInspectionTime);
                                XxlJobHelper.log("首次巡检时间已过，更新为当前日期+原时间: [" + newTaskDate.format(DATE_TIME_FORMAT) + "]");
                                log.info("首次巡检时间已过，更新为当前日期+原时间: [{}]", newTaskDate.format(DATE_TIME_FORMAT));
                            }
                        } else {
                            // 有上一次任务，检查是否需要创建新任务
                            LocalDateTime lastTaskDate = lastTask.getStartTime();
                            XxlJobHelper.log("计划[" + planName + "]最后任务时间: [" + lastTaskDate.format(DATE_TIME_FORMAT) + "]");
                            log.info("计划[{}]最后任务时间: [{}]", planName, lastTaskDate.format(DATE_TIME_FORMAT));

                            // 计算下一次任务日期
                            LocalDateTime expectedNextDate = lastTaskDate.plusDays(frequencyDays);

                            XxlJobHelper.log("计划[" + planName + "]下一次预期执行时间: [" + expectedNextDate.format(DATE_TIME_FORMAT) + "]");
                            log.info("计划[{}]下一次预期执行时间: [{}]", planName, expectedNextDate.format(DATE_TIME_FORMAT));

                            // 使用日期级比较判断是否需要创建新任务（修复1天频次隔天生成的问题）
                            // 只比较年月日，忽略时分秒
                            java.time.LocalDate expectedDate = expectedNextDate.toLocalDate();
                            java.time.LocalDate nowDate = now.toLocalDate();

                            boolean shouldCreateTaskByDate = !expectedDate.isAfter(nowDate);

                            if (shouldCreateTaskByDate) {
                                needCreateTask = true;
                                // 使用当前日期+上一次任务的时分秒
                                newTaskDate = combineDateAndTime(now, lastTaskDate);
                                XxlJobHelper.log("已到达或超过预期执行日期，需要创建新任务，时间为: [" + newTaskDate.format(DATE_TIME_FORMAT) + "]");
                                log.info("已到达或超过预期执行日期，需要创建新任务，时间为: [{}]", newTaskDate.format(DATE_TIME_FORMAT));
                            } else {
                                XxlJobHelper.log("未到达预期执行日期，不需要创建新任务");
                                log.info("未到达预期执行日期，不需要创建新任务");
                            }
                        }

                        // 7. 创建新任务（增加防重复检查）
                        if (needCreateTask && newTaskDate != null) {
                            // 防重复检查：检查该日期是否已有任务
                            String targetDateStr = newTaskDate.format(DATE_FORMAT);
                            boolean taskExistsForDate = swmInspectionListService.existsByPlanIdAndDate(planId, targetDateStr);

                            if (!taskExistsForDate) {
                                SwmInspectionListDO task = new SwmInspectionListDO();
                                task.setPlanId(planId);
                                task.setPlanName(planName);
                                task.setInspectionType(inspectionType);
                                task.setInspectorId(responsiblePersonId);
                                task.setStartTime(newTaskDate);
                                task.setInspectionListStatus(SwmEnums.InspectionListStatusEnum.WAIT.getValue());

                                // 保存巡检任务
                                swmInspectionListService.createSwmInspectionList(task);
                                String successMsg = "成功创建巡检任务: 计划[" + planName + "], 时间[" + newTaskDate.format(DATE_TIME_FORMAT) + "], 新任务ID: [" + task.getId() + "]";
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

                LocalDateTime endTime = LocalDateTime.now();
                long executionTime = ChronoUnit.MILLIS.between(startTime, endTime);

                XxlJobHelper.log("========== 巡检任务生成定时任务执行完成 ==========");
                XxlJobHelper.log("任务结束时间: " + endTime.format(DATE_TIME_FORMAT));
                XxlJobHelper.log("任务执行时间: " + executionTime + "毫秒");
                XxlJobHelper.log("处理结果: 共处理" + inspectionPlanList.size() + "个计划，成功创建" + successCount + "个任务，跳过" + skipCount + "个计划，失败" + errorCount + "个计划");

                log.info("========== 巡检任务生成定时任务执行完成 ==========");
                log.info("任务结束时间: {}", endTime.format(DATE_TIME_FORMAT));
                log.info("任务执行时间: {}毫秒", executionTime);
                log.info("处理结果: 共处理{}个计划，成功创建{}个任务，跳过{}个计划，失败{}个计划",
                        inspectionPlanList.size(), successCount, skipCount, errorCount);

                return null; // TenantUtils.execute 需要返回值
            });
            } catch (Exception e) {
                XxlJobHelper.log(e);
            }
        }
    }

    /**
     * 获取指定计划的最后一次任务
     */
    private SwmInspectionListDO getLastTaskByPlanId(String planId) {
        SwmInspectionListDO lastTask = swmInspectionListService.getLastTaskByPlanId(planId);
        if (lastTask != null) {
            log.debug("获取到计划[{}]最后一次任务, ID: [{}], 开始时间: [{}]",
                    planId, lastTask.getId(), lastTask.getStartTime() != null ? lastTask.getStartTime().format(DATE_TIME_FORMAT) : "未设置");
        } else {
            log.debug("计划[{}]没有历史任务记录", planId);
        }
        return lastTask;
    }

    /**
     * 合并日期和时间
     * 使用date1的年月日和date2的时分秒
     */
    private LocalDateTime combineDateAndTime(LocalDateTime date1, LocalDateTime date2) {
        LocalDateTime result = LocalDateTime.of(
                date1.toLocalDate(),
                date2.toLocalTime()
        );
        log.debug("合并日期和时间: 日期源[{}], 时间源[{}], 结果[{}]",
                date1.format(DATE_TIME_FORMAT), date2.format(DATE_TIME_FORMAT), result.format(DATE_TIME_FORMAT));
        return result;
    }
}
