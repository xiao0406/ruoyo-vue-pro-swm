package com.jeesite.modules.swm.job;

import com.jeesite.common.lang.DateUtils;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.service.support.adapt.GlobalCalculateAdapter;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.service.SwmPersonScheduleService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.xxl.job.core.handler.IJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 类说明
 * 月度计划顺延定时任务
 *
 * @author 李鹏
 * @date 2022/6/5
 */
@Component
@Slf4j
@Transactional(readOnly = true)
public class FmsMonthPlanProlongTask extends IJobHandler {

    @Autowired
    private GlobalCalculateAdapter calculateAdapter;
    @Autowired
    private UserService userService;
    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;
    @Autowired
    private SwmPersonService swmPersonService;

    /**
     * 任务调度测试：testDataService.executeTestTask(userService, 1, 2L, 3F, 4D, 'abc')
     * busiDate 格式 yyyy-MM-dd
     */
    @Override
    @Transactional(readOnly = false)
    public void execute() {
        log.info("######月度计划顺延定时任务【开始】######");

        // 根据当前日期判断是生成当月还是下月排班
        Calendar today = Calendar.getInstance();
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

        String sourceMonth;
        String targetMonth;

        if (dayOfMonth > 20) {
            // 20号之后，生成下个月的排班，复制当前月
            sourceMonth = DateUtils.formatDate(today.getTime(), "yyyy-MM");

            Calendar nextMonthCal = (Calendar) today.clone();
            nextMonthCal.add(Calendar.MONTH, 1);
            targetMonth = DateUtils.formatDate(nextMonthCal.getTime(), "yyyy-MM");

            log.info("当前日期为 {} 号，执行【下月】排班生成：从 {} 复制到 {}", dayOfMonth, sourceMonth, targetMonth);
        } else {
            // 20号及之前，生成当月的排班，复制上个月
            targetMonth = DateUtils.formatDate(today.getTime(), "yyyy-MM");

            Calendar prevMonthCal = (Calendar) today.clone();
            prevMonthCal.add(Calendar.MONTH, -1);
            sourceMonth = DateUtils.formatDate(prevMonthCal.getTime(), "yyyy-MM");

            log.info("当前日期为 {} 号，执行【当月】排班生成：从 {} 复制到 {}", dayOfMonth, sourceMonth, targetMonth);
        }

        User params = new User();
        params.setLoginCode("virtual_gd_job_admin");
        User currentUser = userService.getByLoginCode(params);

        // 复制排班数据
        copyScheduleData(sourceMonth, targetMonth, currentUser.getUserCode());

        log.info("######月度计划顺延定时任务【结束】######");
    }

    /**
     * 复制排班数据
     *
     * @param sourceMonth 源月份，格式：yyyy-MM
     * @param targetMonth 目标月份，格式：yyyy-MM
     * @param userCode    操作用户代码
     */
    private void copyScheduleData(String sourceMonth, String targetMonth, String userCode) {
        log.info("开始复制排班数据，从源月份: {} 到目标月份: {}", sourceMonth, targetMonth);

        try {
            // 1. 查询源月份的所有排班数据
            SwmPersonSchedule query = new SwmPersonSchedule();
            query.setMonth(sourceMonth);
            List<SwmPersonSchedule> sourceMonthSchedules = swmPersonScheduleService.findList(query);

            if (sourceMonthSchedules == null || sourceMonthSchedules.isEmpty()) {
                log.info("源月份 {} 没有排班数据，无需复制", sourceMonth);
                return;
            }

            log.info("源月份 {} 共有 {} 条排班数据", sourceMonth, sourceMonthSchedules.size());

            // 存储需要保存到目标月份的排班数据
            List<SwmPersonSchedule> targetMonthSchedulesToSave = new ArrayList<>();

            // 2. 遍历源月份的排班数据
            for (SwmPersonSchedule sourceSchedule : sourceMonthSchedules) {
                String idCard = sourceSchedule.getIdCard();

                // 增加人员状态判断
                SwmPerson person = swmPersonService.getByIdentityCard(idCard);
                if (person == null || !"0".equals(person.getStatus()) || !"1".equals(person.getPersonnelStatus())) {
                    log.info("人员 {} (身份证: {}) 状态无效或已离职，跳过排班复制",
                            sourceSchedule.getPersonName(), idCard);
                    continue;
                }

                // 3. 检查该人员在目标月份是否已有排班数据
                List<SwmPersonSchedule> existingTargetMonthSchedules = swmPersonScheduleService
                        .findByIdCardAndMonth(idCard, targetMonth);

                // 如果目标月份已有该人员的排班数据，则跳过
                if (existingTargetMonthSchedules != null && !existingTargetMonthSchedules.isEmpty()) {
                    log.info("人员 {} (身份证: {}) 在目标月份 {} 已有排班数据，跳过复制",
                            sourceSchedule.getPersonName(), idCard, targetMonth);
                    continue;
                }

                // 4. 创建新的排班数据对象
                SwmPersonSchedule newSchedule = new SwmPersonSchedule();
                newSchedule.setPersonName(sourceSchedule.getPersonName());
                newSchedule.setIdCard(idCard);
                newSchedule.setMonth(targetMonth);
                newSchedule.setClasses(sourceSchedule.getClasses());
                newSchedule.setRemarks("系统自动从" + sourceMonth + "复制");
                newSchedule.setCreateBy(userCode);
                newSchedule.setUpdateBy(userCode);

                targetMonthSchedulesToSave.add(newSchedule);
            }

            // 5. 批量保存到目标月份
            if (!targetMonthSchedulesToSave.isEmpty()) {
                log.info("需要复制到目标月份 {} 的排班数据共 {} 条", targetMonth, targetMonthSchedulesToSave.size());
                swmPersonScheduleService.batchSave(targetMonthSchedulesToSave);
                log.info("成功复制 {} 条排班数据到目标月份 {}", targetMonthSchedulesToSave.size(), targetMonth);
            } else {
                log.info("没有需要复制到目标月份的排班数据");
            }

        } catch (Exception e) {
            log.error("复制排班数据到目标月份时发生错误", e);
            throw e;
        }
    }
}
