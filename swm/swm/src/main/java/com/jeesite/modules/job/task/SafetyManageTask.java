package com.jeesite.modules.job.task;

import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.swm.dao.SwmSafetyPersonTrainingDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.entity.SwmSafetyFileManage;
import com.jeesite.modules.entity.SwmSafetyPersonTraining;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.SwmSafetyFileManageService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.utils.BatchOperationsUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 安全教育相关定时任务
 */
@Component
@Slf4j
public class SafetyManageTask {

    @Resource
    private SwmSafetyFileManageService swmSafetyFileManageService;
    @Resource
    private SwmSafetyPersonTrainingDao swmSafetyPersonTrainingDao;
    @Resource
    private SwmPersonService swmPersonService;
    @Resource
    private UserService userService;


    /**
     * 定时推送安全教育视频管理，每天0点01开始查询当天需要推送的安全教育视频，生成数据
     */
    @XxlJob("safetyPushTask")
    @Transactional(rollbackFor = Exception.class)
    public void safetyPushTask() {

        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }
        Date now = new Date();
//        //将日期的时间改为0点0分0秒
        Date startTime = DateUtils.getOfDayFirst(now);
        //将日期的时间改为23点59分59秒
        Date endTime = DateUtils.getOfDayLast(now);


        for (User user : corpList) {
            try {
                String corpCode = user.getCorpCode();
                String corpName = user.getCorpName();
                // 设置当前线程租户
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                TenantContext.set(corpCode);
                XxlJobHelper.log("租户 {}，{} 推送安全教育视频开始=============", corpCode, corpName);

                //1.先查询安全管理表，查出今天需要推送的视频，然后根据工种发送给不同的人员
                SwmSafetyFileManage swmSafetyFileManage = new SwmSafetyFileManage();
                swmSafetyFileManage.setRandom(new Random().nextInt(1_000_000));  // 防止一级缓存
                swmSafetyFileManage.getSqlMap().getWhere().and("push_date",  QueryType.GTE, startTime)
                        .and("push_date",  QueryType.LTE, endTime);
                List<SwmSafetyFileManage> list = swmSafetyFileManageService.findList(swmSafetyFileManage);
                if (CollectionUtils.isEmpty(list)){
                    XxlJobHelper.log("租户 {} 没有需要推送的安全教育视频", corpCode);
                    continue;
                }


                //视频培训记录（安全管理）信息
                List<SwmSafetyPersonTraining> insertTrainingList = new ArrayList<>();
                for (SwmSafetyFileManage fileManage : list) {
                    String jobType = fileManage.getJobType();
                    //切割工种：”，“
                    String[] jobTypes = jobType.split(",");

                    // 2.根据工种去人员表查出对应的人员
                    List<String> jobtypeList = new ArrayList<>(Arrays.asList(jobTypes));
                    List<SwmPerson> jobPersons =  swmPersonService.findListByJobTypeList(jobtypeList);

                    SwmSafetyPersonTraining safetyPersonTraining = new SwmSafetyPersonTraining();
                    safetyPersonTraining.setSafetyManageId(fileManage.getId());
                    List<SwmSafetyPersonTraining> trainings = swmSafetyPersonTrainingDao.findList(safetyPersonTraining);
                    Map<String, List<SwmSafetyPersonTraining>> traningMap = trainings.stream().collect(Collectors.groupingBy(SwmSafetyPersonTraining::getPhoneNumber));



                    //3.生成视频培训记录（人员）信息
                    for (SwmPerson person : jobPersons) {
                        // 3.1生成之前需要先查一下这一条视频是否生成过，生成过就不能生成
                        List<SwmSafetyPersonTraining> swmSafetyPersonTrainings = traningMap.get(person.getPhoneNumber());
                        if(CollectionUtils.isNotEmpty(swmSafetyPersonTrainings)){
                            continue;
                        }
                        //3.2 表里没生成过则开始生成数据
                        SwmSafetyPersonTraining training = new SwmSafetyPersonTraining();
                        training.setSafetyManageId(fileManage.getId());
                        training.setIdentityCard(person.getIdentityCard());
                        training.setPhoneNumber(person.getPhoneNumber());
                        training.setCompleteStatus("0");
                        training.setProgress("0");
                        insertTrainingList.add(training);
                    }
                    //4.修改安全管理表状态，未推送改为已推送
                    fileManage.setPushStatus("1");
                    swmSafetyFileManageService.update(fileManage);
                }
                List<List<SwmSafetyPersonTraining>> lists = BatchOperationsUtil.batchCutting(insertTrainingList, 100);
                for (List<SwmSafetyPersonTraining> list1 : lists) {
                    swmSafetyPersonTrainingDao.insertBatch(list1);
                }

            }catch (Exception e){
                XxlJobHelper.log("租户 {} 推送安全教育视频失败：{}", user.getCorpCode(), e);
            }finally {
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }

    }
}
