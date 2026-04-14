package com.jeesite.modules.job.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.enums.CorpDbEnum;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.utils.R;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 清理Tdengine表数据定时任务
 */

@Slf4j
@Component
public class ClearTdengineTableDataTask {

    @Autowired
    private TDengineService tDengineService;
    @Autowired
    private UserService userService;
    @Autowired
    private SwmHelmetDeviceService deviceService;

    //当天报警表
    private static final String SWM_WARNING_MANAGEMENT_SUPER_TABLE_TODAY = "swm_warning_management_today";

    /**
     * 清理swm_warning_management_today表数据
     */
    @XxlJob("clearSwmWarningManagementTodayTable")
    @Transactional(rollbackFor = Exception.class)
    public void clearSwmWarningManagementTodayTable() {

        XxlJobHelper.log("定时清理swm_warning_management_today表数据=============================");
        Date date = new Date();
        DateTime startDate = DateUtil.offsetHour(date, -24);

        CorpDbEnum[] values = CorpDbEnum.values();
        for (CorpDbEnum value : values) {
            String dbName = value.getDbName();
            String corpCode = value.getCorpCode();
            String sql = "DELETE FROM " + dbName +"."+ SWM_WARNING_MANAGEMENT_SUPER_TABLE_TODAY + " WHERE create_date < '" + startDate + "'";
            tDengineService.executeTDengineSQLByXXJOB(sql,corpCode);
            XxlJobHelper.log("清理swm_warning_management_today表数据 SQL: {}", sql);
        }
    }


    /**
     * 定时清理helmet_runde_ca_report_location表
     */
    @XxlJob("clearRundeTable")
    @Transactional(rollbackFor = Exception.class)
    public void clearRundeTable() throws InterruptedException {

        XxlJobHelper.log("TDengine定时清理开始 =============================");

        Date now = new Date();
        DateTime cutoffTime = DateUtil.offsetMonth(now, -1);
        String cutoffStr = DateUtil.formatDateTime(cutoffTime);

        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        for (User user : corpList) {
            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();
            XxlJobHelper.log("当前租户{}", corpCode);
            try {
                // 设置当前线程租户
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                TenantContext.set(corpCode);

                SwmHelmetDevice device = new SwmHelmetDevice();
                device.setRandom(new Random().nextInt(1_000_000));  // 防止一级缓存
                device.setCorpCode(corpCode);
                List<SwmHelmetDevice> deviceList = deviceService.findDeviceCorpMapping(device);
                for (SwmHelmetDevice helmetDevice : deviceList) {
                    String deviceId = helmetDevice.getDeviceId();
                    String idcard = helmetDevice.getAssignedPerson();


                    String dbName = CorpDbEnum.getDbNameByCorpCode(corpCode);
                    XxlJobHelper.log("清理helmet_runde_ca_report_location表数据 deviceId: {}, idcard: {}，租户{},库名{}", deviceId, idcard,corpCode,dbName);

                    String deleteSql = "DELETE FROM " + dbName + "." +  TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION +
                            "_" +deviceId + "_"+ idcard +
                            " WHERE time < '" + cutoffStr + "'";
                    tDengineService.executeTDengineSQLByXXJOB(deleteSql, corpCode);
                    XxlJobHelper.log("清理helmet_runde_ca_report_location表数据 SQL: {}", deleteSql);
                    //睡0.3s
                    Thread.sleep(300);
                    XxlJobHelper.log("清理helmet_runde_ca_report_location表数据 SQL: {}", deleteSql);
                }
            }catch (Exception e){
                XxlJobHelper.log("清理helmet_runde_ca_report_location表数据异常: {}", e.getMessage());
            }finally {
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }

        XxlJobHelper.log("TDengine定时清理结束 =============================");
    }

    /**
     * 定时清理external_coordinate_data表
     */
    @XxlJob("clearExtenrnalTable")
    @Transactional(rollbackFor = Exception.class)
    public void clearExtenrnalTable() {


        XxlJobHelper.log("TDengine定时清理开始 =============================");


        Date now = new Date();
        DateTime cutoffTime = DateUtil.offsetMonth(now, -3);
        String cutoffStr = DateUtil.formatDateTime(cutoffTime);



        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        for (User user : corpList) {
            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();

            // 设置当前线程租户
            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            TenantContext.set(corpCode);

            SwmHelmetDevice device = new SwmHelmetDevice();
            device.setRandom(new Random().nextInt(1_000_000));  // 防止一级缓存

            List<SwmHelmetDevice> deviceList = deviceService.findDeviceCorpMapping(device);
            for (SwmHelmetDevice helmetDevice : deviceList) {
                String deviceId = helmetDevice.getDeviceId();
                String idcard = helmetDevice.getAssignedPerson();

                String dbName = CorpDbEnum.getDbNameByCorpCode(corpCode);

                String deleteSql = "DELETE FROM " + dbName + "." +  TdengineSuperTableConstant.EXTERNAL_COORDINATE_DATA +
                        "_" +deviceId + "_"+ idcard +
                        " WHERE time < '" + cutoffStr + "'";
                tDengineService.executeTDengineSQLByXXJOB(deleteSql, corpCode);
                XxlJobHelper.log("清理external_coordinate_data表数据 SQL: {}", deleteSql);
            }

        }

        XxlJobHelper.log("TDengine定时清理结束 =============================");
    }
}
