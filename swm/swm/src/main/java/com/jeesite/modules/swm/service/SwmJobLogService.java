/**
 * @author Shawn
 * @date 2025/06/26
 */
package com.jeesite.modules.swm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmJobLog;
import com.jeesite.modules.swm.dao.SwmJobLogDao;

/**
 * 定时任务调度日志表Service
 * 
 * @author Shawn
 * @version 2025-06-26
 */
@Service
@Transactional(readOnly = true)
public class SwmJobLogService extends CrudService<SwmJobLogDao, SwmJobLog> {

}