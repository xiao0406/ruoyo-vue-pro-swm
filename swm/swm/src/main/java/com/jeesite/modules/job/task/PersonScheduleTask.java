package com.jeesite.modules.job.task;


import cn.hutool.core.date.DateUtil;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.service.SwmPersonScheduleService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.UserUtils;
import com.jeesite.modules.util.BatchOperationsUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    @Autowired
    private UserService userService;


    /**
     * 定时生成人员的排班计划（默认白班）
     */
    @XxlJob("createDayShift")
    @Transactional(rollbackFor = Exception.class)
    public void createDayShift() {
        XxlJobHelper.log("定时生成人员的排班计划===================================");


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
            XxlJobHelper.log("开始处理租户：{} ========================", corpCode);


            try {
                List<SwmPersonSchedule> insertList = new java.util.ArrayList<>();
                //1.先查询本月排班人员信息
                Date date = new Date();
                String month = DateUtil.format(date, "yyyy-MM");
                SwmPersonSchedule swmPersonSchedule = new SwmPersonSchedule();
                swmPersonSchedule.setMonth(month);

                // 传随机值，使 SQL 每次不同，目的是取消一级缓存
                swmPersonSchedule.setRandom(new Random().nextInt(1_000_000));
                List<SwmPersonSchedule> scheduleList = swmPersonScheduleService.scheduleList(swmPersonSchedule);
                Map<String, List<SwmPersonSchedule>> scheduleListMap = scheduleList.stream().collect(Collectors.groupingBy(SwmPersonSchedule::getIdCard));
                XxlJobHelper.log("本月排班人数：{}", scheduleList.size());


                //2.查询人员信息
                SwmPerson swmPerson = new SwmPerson();
                swmPerson.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
                swmPerson.setStatus(SwmPerson.STATUS_NORMAL);
                swmPerson.setRandom(new Random().nextInt(1_000_000));
                List<SwmPerson> personList = swmPersonService.peronsList(swmPerson);
                XxlJobHelper.log("人员总数：{}", personList.size());


                //3.本月未排班则自动生成一个排班
                for (SwmPerson person : personList) {
                    List<SwmPersonSchedule> list = scheduleListMap.get(person.getIdentityCard());
                    if (list != null && !list.isEmpty()) {
                        continue;
                    }
                    XxlJobHelper.log("开始处理：租户：，人员：{}",corpCode, person.getName());
                    SwmPersonSchedule personSchedule = new SwmPersonSchedule();
                    personSchedule.setPersonName(person.getName());
                    personSchedule.setMonth(month);
                    personSchedule.setClasses("1");
                    personSchedule.setIdCard(person.getIdentityCard());
                    personSchedule.setEmployeeId(person.getId());
                    personSchedule.setStatus(SwmPersonSchedule.STATUS_NORMAL);
                    personSchedule.setCorpCode(person.getCorpCode());
                    personSchedule.setCorpName(person.getCorpName());
                    insertList.add(personSchedule);
                }
                //批量更新
                List<List<SwmPersonSchedule>> lists = BatchOperationsUtil.batchCutting(insertList, 50);
                for (List<SwmPersonSchedule> list : lists) {
                    swmPersonScheduleService.insertBatch(list);
                }
                CorpUtils.removeCurrentCorpCode(null);
                XxlJobHelper.log("生成排班计划成功：{} 共条 =============================", insertList.size());
            }catch (Exception e){
                XxlJobHelper.log("生成排班计划失败：{}", e.getMessage());
            }finally {
                CorpUtils.removeCurrentCorpCode(null);
            }
        }
    }
}
