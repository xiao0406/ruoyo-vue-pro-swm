package com.jeesite.modules.job.task;


import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.service.SwmPersonScheduleService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.util.BatchOperationsUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 人员排班定时任务
 */
@Component
@Slf4j
public class PersonScheduleTask {

    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;
    @Autowired
    private SwmPersonService swmPersonService;

    /**
     * 定时生成人员的排班计划（默认白班）
     */
    @XxlJob("createDayShift")
    @Transactional(rollbackFor = Exception.class)
    public void createDayShift() {
        XxlJobHelper.log("定时生成人员的排班计划===================================");

        List<SwmPersonSchedule> insertList = new java.util.ArrayList<>();

        //1.先查询本月排班人员信息
        Date date = new Date();
//        String month = DateUtil.format(date, "yyyy-MM");
        SwmPersonSchedule swmPersonSchedule = new SwmPersonSchedule();
//        swmPersonSchedule.setMonth(month);
        List<SwmPersonSchedule> scheduleList = swmPersonScheduleService.findList(swmPersonSchedule);
        Map<String, List<SwmPersonSchedule>> scheduleListMap = scheduleList.stream().collect(Collectors.groupingBy(SwmPersonSchedule::getIdCard));
        XxlJobHelper.log("本月排班人数：{}", scheduleList.size());


        //2.查询人员信息
        SwmPerson swmPerson = new SwmPerson();
        swmPerson.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
        swmPerson.setStatus(SwmPerson.STATUS_NORMAL);
        List<SwmPerson> personList = swmPersonService.findList(swmPerson);
        XxlJobHelper.log("人员总数：{}", personList.size());


        //3.本月未排班则自动生成一个排班
        for (SwmPerson person : personList) {
            List<SwmPersonSchedule> list = scheduleListMap.get(person.getIdentityCard());
            if (list != null && !list.isEmpty()) {
                continue;
            }
            SwmPersonSchedule personSchedule = new SwmPersonSchedule();
            personSchedule.setPersonName(person.getName());
//            personSchedule.setMonth(month);
            personSchedule.setClasses("1");
            personSchedule.setIdCard(person.getIdentityCard());
            personSchedule.setEmployeeId(person.getId());
            personSchedule.setStatus(SwmPersonSchedule.STATUS_NORMAL);
            insertList.add(personSchedule);
        }
        //批量更新
        List<List<SwmPersonSchedule>> lists = BatchOperationsUtil.batchCutting(insertList, 50);
        for (List<SwmPersonSchedule> list : lists) {
            swmPersonScheduleService.insertBatch(list);
        }
        XxlJobHelper.log("生成排班计划成功：{} 共条", insertList.size());
    }
}
