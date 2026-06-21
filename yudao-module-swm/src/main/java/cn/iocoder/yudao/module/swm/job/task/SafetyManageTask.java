package cn.iocoder.yudao.module.swm.job.task;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmSafetyPersonTrainingMapper;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyFileManageDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyPersonTrainingDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonService;
import cn.iocoder.yudao.module.swm.service.SwmSafetyFileManageService;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import cn.iocoder.yudao.module.swm.util.BatchOperationsUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private SwmSafetyPersonTrainingMapper swmSafetyPersonTrainingMapper;
    @Resource
    private SwmPersonService swmPersonService;
    @Resource
    private TenantService tenantService;

    /**
     * 定时推送安全教育视频管理，每天0点01开始查询当天需要推送的安全教育视频，生成数据
     */
    @XxlJob("safetyPushTask")
    @Transactional(rollbackFor = Exception.class)
    public void safetyPushTask() {
        List<TenantDO> tenantList = tenantService.getTenantListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollectionUtils.isEmpty(tenantList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        // 当天 00:00:00 ~ 23:59:59
        LocalDateTime startTime = LocalDate.now().atStartOfDay();
        LocalDateTime endTime = LocalDate.now().atTime(LocalTime.MAX);

        for (TenantDO tenant : tenantList) {
            Long tenantId = tenant.getId();
            try {
                TenantUtils.execute(tenantId, () -> {
                    XxlJobHelper.log("租户 {} 推送安全教育视频开始=============", tenantId);

                    // 1. 查询当天需要推送的安全教育视频
                    List<SwmSafetyFileManageDO> list = swmSafetyFileManageService.findList(startTime, endTime);
                    if (CollectionUtils.isEmpty(list)) {
                        XxlJobHelper.log("租户 {} 没有需要推送的安全教育视频", tenantId);
                        return;
                    }

                    List<SwmSafetyPersonTrainingDO> insertTrainingList = new ArrayList<>();
                    for (SwmSafetyFileManageDO fileManage : list) {
                        List<String> jobtypeList = null;
                        if (!"1".equals(fileManage.getSelectAll())) {
                            String[] jobTypes = fileManage.getJobType().split(",");
                            jobtypeList = new ArrayList<>(Arrays.asList(jobTypes));
                        }
                        List<SwmPersonDO> jobPersons = swmPersonService.findListByJobTypeList(jobtypeList);

                        List<SwmSafetyPersonTrainingDO> trainings = swmSafetyPersonTrainingMapper.selectList(
                                new LambdaQueryWrapper<SwmSafetyPersonTrainingDO>()
                                        .eq(SwmSafetyPersonTrainingDO::getSafetyManageId, fileManage.getId())
                        );
                        Map<String, List<SwmSafetyPersonTrainingDO>> traningMap = trainings.stream()
                                .collect(Collectors.groupingBy(SwmSafetyPersonTrainingDO::getPhoneNumber));

                        for (SwmPersonDO person : jobPersons) {
                            List<SwmSafetyPersonTrainingDO> existing = traningMap.get(person.getPhoneNumber());
                            if (CollectionUtils.isNotEmpty(existing)) {
                                continue;
                            }
                            SwmSafetyPersonTrainingDO training = new SwmSafetyPersonTrainingDO();
                            training.setSafetyManageId(fileManage.getId());
                            training.setIdentityCard(person.getIdentityCard());
                            training.setPhoneNumber(person.getPhoneNumber());
                            training.setCompleteStatus("0");
                            training.setProgress("0");
                            insertTrainingList.add(training);
                        }
                        fileManage.setPushStatus("1");
                        swmSafetyFileManageService.updateById(fileManage);
                    }
                    List<List<SwmSafetyPersonTrainingDO>> batches = BatchOperationsUtil.batchCutting(insertTrainingList, 100);
                    for (List<SwmSafetyPersonTrainingDO> batch : batches) {
                        swmSafetyPersonTrainingMapper.insertBatch(batch);
                    }
                });
            } catch (Exception e) {
                XxlJobHelper.log("租户 {} 推送安全教育视频失败：{}", tenantId, e);
            }
        }
    }
}
