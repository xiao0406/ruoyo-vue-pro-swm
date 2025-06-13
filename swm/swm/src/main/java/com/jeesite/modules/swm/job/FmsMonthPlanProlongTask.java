package com.jeesite.modules.swm.job;


import com.jeesite.common.lang.DateUtils;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.service.support.adapt.GlobalCalculateAdapter;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.service.SwmPersonScheduleService;
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


    /**
     * 任务调度测试：testDataService.executeTestTask(userService, 1, 2L, 3F, 4D, 'abc')
     * busiDate 格式 yyyy-MM-dd
     */
    @Override
    @Transactional(readOnly = false)
    public void execute() {
        log.info("######月度计划顺延定时任务【开始】######");

        User params = new User();
        params.setLoginCode("virtual_gd_job_admin");
        User currentUser = userService.getByLoginCode(params);

        GlobalCalculateAdapter.OfFirst_Last ofMonthFirst_last = calculateAdapter.getOfMonthFirst_Last();
        Date busiDate = DateUtils.parseDate(ofMonthFirst_last.getBusiDate());

        // 获取当前月份和下个月份
        String currMonth = DateUtils.formatDate(busiDate, "yyyy-MM");
        
        // 计算下个月的日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(busiDate);
        calendar.add(Calendar.MONTH, 1);
        String nextMonth = DateUtils.formatDate(calendar.getTime(), "yyyy-MM");
        
        log.info("当前月份: {}, 下个月份: {}", currMonth, nextMonth);
        
        // 复制当前月排班数据到下个月
        copyScheduleDataToNextMonth(currMonth, nextMonth, currentUser.getUserCode());

        log.info("######月度计划顺延定时任务【结束】######");
    }
    
    /**
     * 复制当前月排班数据到下个月
     * 
     * @param currentMonth 当前月份，格式：yyyy-MM
     * @param nextMonth 下个月份，格式：yyyy-MM
     * @param userCode 操作用户代码
     */
    private void copyScheduleDataToNextMonth(String currentMonth, String nextMonth, String userCode) {
        log.info("开始复制排班数据，当前月: {}, 下个月: {}", currentMonth, nextMonth);
        
        try {
            // 1. 查询当前月份的所有排班数据
            SwmPersonSchedule query = new SwmPersonSchedule();
            query.setMonth(currentMonth);
            List<SwmPersonSchedule> currentMonthSchedules = swmPersonScheduleService.findList(query);
            
            if (currentMonthSchedules == null || currentMonthSchedules.isEmpty()) {
                log.info("当前月 {} 没有排班数据，无需复制", currentMonth);
                return;
            }
            
            log.info("当前月 {} 共有 {} 条排班数据", currentMonth, currentMonthSchedules.size());
            
            // 存储需要保存到下个月的排班数据
            List<SwmPersonSchedule> nextMonthSchedulesToSave = new ArrayList<>();
            
            // 2. 遍历当前月份的排班数据
            for (SwmPersonSchedule currentSchedule : currentMonthSchedules) {
                String idCard = currentSchedule.getIdCard();
                
                // 3. 检查该人员在下个月是否已有排班数据
                List<SwmPersonSchedule> existingNextMonthSchedules = 
                        swmPersonScheduleService.findByIdCardAndMonth(idCard, nextMonth);
                
                // 如果下个月已有该人员的排班数据，则跳过
                if (existingNextMonthSchedules != null && !existingNextMonthSchedules.isEmpty()) {
                    log.info("人员 {} (身份证: {}) 在下个月 {} 已有排班数据，跳过复制", 
                            currentSchedule.getPersonName(), idCard, nextMonth);
                    continue;
                }
                
                // 4. 创建新的排班数据对象
                SwmPersonSchedule newSchedule = new SwmPersonSchedule();
                newSchedule.setPersonName(currentSchedule.getPersonName());
                newSchedule.setIdCard(idCard);
                newSchedule.setMonth(nextMonth);
                newSchedule.setClasses(currentSchedule.getClasses());
                newSchedule.setRemarks("系统自动从" + currentMonth + "复制");
                newSchedule.setCreateBy(userCode);
                newSchedule.setUpdateBy(userCode);
                
                nextMonthSchedulesToSave.add(newSchedule);
            }
            
            // 5. 批量保存到下个月
            if (!nextMonthSchedulesToSave.isEmpty()) {
                log.info("需要复制到下个月 {} 的排班数据共 {} 条", nextMonth, nextMonthSchedulesToSave.size());
                swmPersonScheduleService.batchSave(nextMonthSchedulesToSave);
                log.info("成功复制 {} 条排班数据到下个月 {}", nextMonthSchedulesToSave.size(), nextMonth);
            } else {
                log.info("没有需要复制到下个月的排班数据");
            }
            
        } catch (Exception e) {
            log.error("复制排班数据到下个月时发生错误", e);
            throw e;
        }
    }
}

