package cn.iocoder.yudao.module.swm.job.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDevicePageReqVO;
import cn.iocoder.yudao.module.swm.enums.TdengineSuperTableConstants;
import cn.iocoder.yudao.module.swm.api.enums.TenantDbEnum;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.dal.tdengine.TdengineRestClient;
import cn.iocoder.yudao.module.swm.service.SwmHelmetDeviceService;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 清理 TDengine 表数据定时任务
 * <p>
 * 租户遍历使用 TenantUtils.execute(tenantId, ...)，
 * TDengine 库名通过 TenantDbEnum.getDbNameByTenantId 解析。
 */
@Slf4j
@Component
public class ClearTdengineTableDataTask {

    @Resource
    private TdengineRestClient tdengineRestClient;
    @Resource
    private TenantService tenantService;
    @Resource
    private SwmHelmetDeviceService deviceService;

    // 当天报警表
    private static final String SWM_WARNING_MANAGEMENT_SUPER_TABLE_TODAY = "swm_warning_management_today";

    /**
     * 清理 swm_warning_management_today 表数据
     */
    @XxlJob("clearSwmWarningManagementTodayTable")
    @Transactional(rollbackFor = Exception.class)
    public void clearSwmWarningManagementTodayTable() {
        XxlJobHelper.log("定时清理swm_warning_management_today表数据=============================");
        Date date = new Date();
        DateTime startDate = DateUtil.offsetHour(date, -24);

        for (TenantDbEnum value : TenantDbEnum.values()) {
            String dbName = value.getDbName();
            String sql = "DELETE FROM " + dbName + "." + SWM_WARNING_MANAGEMENT_SUPER_TABLE_TODAY
                    + " WHERE create_date < '" + startDate + "'";
            tdengineRestClient.executeByTenantId(sql, value.getTenantId());
            XxlJobHelper.log("清理swm_warning_management_today表数据 SQL: {}", sql);
        }
    }

    /**
     * 定时清理 helmet_runde_ca_report_location 表
     */
    @XxlJob("clearRundeTable")
    @Transactional(rollbackFor = Exception.class)
    public void clearRundeTable() throws InterruptedException {
        XxlJobHelper.log("TDengine定时清理开始 =============================");

        Date now = new Date();
        DateTime cutoffTime = DateUtil.offsetMonth(now, -1);
        String cutoffStr = DateUtil.formatDateTime(cutoffTime);

        List<TenantDO> tenantList = tenantService.getTenantListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollectionUtils.isEmpty(tenantList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        for (TenantDO tenant : tenantList) {
            Long tenantId = tenant.getId();
            XxlJobHelper.log("当前租户 {}", tenantId);
            try {
                TenantUtils.execute(tenantId, () -> {
                    try {
                        SwmHelmetDevicePageReqVO pageReqVO = new SwmHelmetDevicePageReqVO();
                        pageReqVO.setPageNo(1);
                        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
                        PageResult<SwmHelmetDeviceDO> pageResult = deviceService.getHelmetDevicePage(pageReqVO);
                        String dbName = TenantDbEnum.getDbNameByTenantId(tenantId);
                        for (SwmHelmetDeviceDO helmetDevice : pageResult.getList()) {
                            String deviceId = helmetDevice.getDeviceId();
                            String idcard = helmetDevice.getAssignedPerson();
                            String deleteSql = "DELETE FROM " + dbName + "."
                                    + TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION
                                    + "_" + deviceId + "_" + idcard
                                    + " WHERE time < '" + cutoffStr + "'";
                            tdengineRestClient.executeByTenantId(deleteSql, tenantId);
                            XxlJobHelper.log("清理helmet_runde_ca_report_location deviceId={}, idcard={}, 租户={}, 库={}", deviceId, idcard, tenantId, dbName);
                            Thread.sleep(300);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        XxlJobHelper.log("清理helmet_runde_ca_report_location异常: {}", e.getMessage());
                    }
                });
            } catch (Exception e) {
                XxlJobHelper.log("租户 {} 清理异常: {}", tenantId, e.getMessage());
            }
        }
        XxlJobHelper.log("TDengine定时清理结束 =============================");
    }

    /**
     * 定时清理 external_coordinate_data 表
     */
    @XxlJob("clearExtenrnalTable")
    @Transactional(rollbackFor = Exception.class)
    public void clearExtenrnalTable() {
        XxlJobHelper.log("TDengine定时清理开始 =============================");

        Date now = new Date();
        DateTime cutoffTime = DateUtil.offsetMonth(now, -3);
        String cutoffStr = DateUtil.formatDateTime(cutoffTime);

        List<TenantDO> tenantList = tenantService.getTenantListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollectionUtils.isEmpty(tenantList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        for (TenantDO tenant : tenantList) {
            Long tenantId = tenant.getId();
            try {
                TenantUtils.execute(tenantId, () -> {
                    try {
                        SwmHelmetDevicePageReqVO pageReqVO = new SwmHelmetDevicePageReqVO();
                        pageReqVO.setPageNo(1);
                        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
                        PageResult<SwmHelmetDeviceDO> pageResult = deviceService.getHelmetDevicePage(pageReqVO);
                        String dbName = TenantDbEnum.getDbNameByTenantId(tenantId);
                        for (SwmHelmetDeviceDO helmetDevice : pageResult.getList()) {
                            String deviceId = helmetDevice.getDeviceId();
                            String idcard = helmetDevice.getAssignedPerson();
                            String deleteSql = "DELETE FROM " + dbName + "."
                                    + TdengineSuperTableConstants.EXTERNAL_COORDINATE_DATA
                                    + "_" + deviceId + "_" + idcard
                                    + " WHERE time < '" + cutoffStr + "'";
                            tdengineRestClient.executeByTenantId(deleteSql, tenantId);
                            XxlJobHelper.log("清理external_coordinate_data SQL: {}", deleteSql);
                        }
                    } catch (Exception e) {
                        XxlJobHelper.log("清理external_coordinate_data异常: {}", e.getMessage());
                    }
                });
            } catch (Exception e) {
                XxlJobHelper.log("租户 {} 清理异常: {}", tenantId, e.getMessage());
            }
        }
        XxlJobHelper.log("TDengine定时清理结束 =============================");
    }
}
