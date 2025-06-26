/**
 * @author Shawn
 * @date 2025/06/26
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmJobLog;

/**
 * 定时任务调度日志表DAO接口
 * 
 * @author Shawn
 * @version 2025-06-26
 */
@MyBatisDao
public interface SwmJobLogDao extends CrudDao<SwmJobLog> {

}