package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHelmetDeviceConfig;

/**
 * 头盔设备配置DAO
 * @author Shawn
 * @date 2025-01-08
 */
@MyBatisDao
public interface SwmHelmetDeviceConfigDao extends CrudDao<SwmHelmetDeviceConfig> {
    
    /**
     * 保存或更新配置
     * @param config 配置对象
     */
    void saveOrUpdateConfig(SwmHelmetDeviceConfig config);
}