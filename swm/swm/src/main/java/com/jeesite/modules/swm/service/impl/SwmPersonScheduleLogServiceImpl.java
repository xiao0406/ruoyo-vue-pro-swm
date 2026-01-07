package com.jeesite.modules.swm.service.impl;

import com.jeesite.common.idgen.IdGen;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonScheduleLogDao;
import com.jeesite.modules.swm.entity.SwmPersonScheduleLog;
import com.jeesite.modules.swm.service.SwmPersonScheduleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SwmPersonScheduleLogServiceImpl extends CrudService<SwmPersonScheduleLogDao, SwmPersonScheduleLog> implements SwmPersonScheduleLogService {

    @Autowired
    private SwmPersonScheduleLogDao swmPersonScheduleLogDao;

    @Override
    public void saveScheduleLog(SwmPersonScheduleLog log) {
        // 若未设置主键，自动生成（Jeesite自带工具类）
        if (log.getId() == null || log.getId().trim().isEmpty()) {
            log.setId(IdGen.uuid()); // 雪花算法/UUID生成主键
        }
        swmPersonScheduleLogDao.insertLog(log);
    }

    @Override
    public List<SwmPersonScheduleLog> getClassesRecord(String id) {
        return swmPersonScheduleLogDao.getClassesRecord(id);
    }
}
