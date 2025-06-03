package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmSiteMapManagement;

/**
 * 场地底图管理表DAO接口
 * 
 * @author zwf
 * @version 2025-05-30
 */
@MyBatisDao
public interface SwmSiteMapManagementDao extends CrudDao<SwmSiteMapManagement> {
    
    /**
     * 更新底图状态
     * @param swmSiteMapManagement 实体对象
     * @return 影响行数
     */
    long updateMapStatus(SwmSiteMapManagement swmSiteMapManagement);
    
    /**
     * 物理删除底图
     * @param swmSiteMapManagement 实体对象
     * @return 影响行数
     */
    long physicalDelete(SwmSiteMapManagement swmSiteMapManagement);
    
    /**
     * 查询启用状态的地图
     * @return 启用状态的地图实体
     */
    SwmSiteMapManagement findActiveMap();
} 