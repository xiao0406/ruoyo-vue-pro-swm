package com.jeesite.modules.swm.service;

import com.jeesite.modules.swm.entity.SwmPersonScheduleLog;

import java.util.List;

public interface SwmPersonScheduleLogService  {
    /**
     * 保存班次修改日志
     */
    void saveScheduleLog(SwmPersonScheduleLog log);

    List<SwmPersonScheduleLog> getClassesRecord(String id);
}
