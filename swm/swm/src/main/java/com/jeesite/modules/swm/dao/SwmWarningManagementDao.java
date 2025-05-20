package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmWarningManagement;

import java.util.List;

/**
 * 预警管理DAO接口
 * 
 * @author zwf
 * @version 2025-05-16
 */
@MyBatisDao
public interface SwmWarningManagementDao extends CrudDao<SwmWarningManagement> {
    
    /**
     * 查询所有记录，不带默认的状态过滤
     */
    List<SwmWarningManagement> findAllWithoutStatusFilter();
    
} 