package com.jeesite.modules.job.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.jeesite.modules.enums.CorpDbEnum;
import com.jeesite.modules.swm.service.TDengineService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
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
}
