package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmThirdApiLog;

/**
 * 第三方接口调用日志DAO。
 */
@MyBatisDao
public interface SwmThirdApiLogDao extends CrudDao<SwmThirdApiLog> {
}
