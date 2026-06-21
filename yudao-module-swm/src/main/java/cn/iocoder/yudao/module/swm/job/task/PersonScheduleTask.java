package cn.iocoder.yudao.module.swm.job.task;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonScheduleService;
import cn.iocoder.yudao.module.swm.service.SwmPersonService;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import cn.iocoder.yudao.module.swm.util.BatchOperationsUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import jakarta.annotation.Resource;
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

    @Resource
    private SwmPersonScheduleService swmPersonScheduleService;
    @Resource
    private SwmPersonService swmPersonService;
    @Resource
    private TenantService tenantService;

    /**
     * 定时生成人员的排班计划（默认白班）
     */
    @XxlJob("createDayShift")
    @Transactional(rollbackFor = Exception.class)
    public void createDayShift() {
        XxlJobHelper.log("定时生成人员的排班计划===================================");

        List<TenantDO> tenantList = tenantService.getTenantListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollectionUtils.isEmpty(tenantList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        for (TenantDO tenant : tenantList) {
            Long tenantId = tenant.getId();
            try {
                TenantUtils.execute(tenantId, () -> {
                    XxlJobHelper.log("开始处理租户：{} ========================", tenantId);

                    List<SwmPersonScheduleDO> insertList = new ArrayList<>();

                    // 1. 查询在职人员
                    SwmPersonDO query = new SwmPersonDO();
                    query.setPersonnelStatus("1"); // 在职
                    query.setStatus("0");          // 正常
                    List<SwmPersonDO> personList = swmPersonService.findList(query);
                    XxlJobHelper.log("人员总数：{}", personList.size());
                    if (CollectionUtils.isEmpty(personList)) {
                        return;
                    }

                    // 2. 查询已有排班
                    List<SwmPersonScheduleDO> scheduleList = swmPersonScheduleService.findList(new SwmPersonScheduleDO());
                    Map<String, List<SwmPersonScheduleDO>> scheduleMap = scheduleList.stream()
                            .collect(Collectors.groupingBy(SwmPersonScheduleDO::getIdCard));
                    XxlJobHelper.log("本月排班人数：{}", scheduleList.size());

                    // 3. 未排班人员自动生成白班排班
                    for (SwmPersonDO person : personList) {
                        if (CollectionUtils.isNotEmpty(scheduleMap.get(person.getIdentityCard()))) {
                            continue;
                        }
                        XxlJobHelper.log("开始处理：租户：{}，人员：{}", tenantId, person.getName());
                        SwmPersonScheduleDO schedule = new SwmPersonScheduleDO();
                        schedule.setPersonName(person.getName());
                        schedule.setClasses("1"); // 白班
                        schedule.setIdCard(person.getIdentityCard());
                        schedule.setEmployeeId(person.getId());
                        schedule.setStatus("0"); // 正常
                        insertList.add(schedule);
                    }

                    List<List<SwmPersonScheduleDO>> batches = BatchOperationsUtil.batchCutting(insertList, 50);
                    for (List<SwmPersonScheduleDO> batch : batches) {
                        swmPersonScheduleService.insertBatch(batch);
                    }
                    XxlJobHelper.log("生成排班计划成功：共 {} 条 =============================", insertList.size());
                });
            } catch (Exception e) {
                XxlJobHelper.log("租户 {} 生成排班计划失败：{}", tenantId, e.getMessage());
            }
        }
    }
}
