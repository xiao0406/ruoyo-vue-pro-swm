package com.jeesite.modules.swm.dao;

import cn.hutool.core.date.DateTime;
import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmDify;

import java.util.Date;

/**
 * 安全帽：ai日报表DAO接口
 * @author wxy
 * @version 2025-11-27
 */
@MyBatisDao
public interface SwmDifyDao extends CrudDao<SwmDify> {

    SwmDify getEntity(DateTime startYesterday, DateTime endYesterDay);
}