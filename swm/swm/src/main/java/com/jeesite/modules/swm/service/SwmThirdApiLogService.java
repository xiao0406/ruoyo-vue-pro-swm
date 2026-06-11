package com.jeesite.modules.swm.service;

import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmThirdApiLogDao;
import com.jeesite.modules.swm.entity.SwmThirdApiLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 第三方接口调用日志Service。
 */
@Service
@Transactional(readOnly = true)
public class SwmThirdApiLogService extends CrudService<SwmThirdApiLogDao, SwmThirdApiLog> {

    @Transactional(readOnly = false)
    public void saveLog(SwmThirdApiLog log) {
        super.save(log);
    }
}
