package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.HelmetDevice;
import org.apache.ibatis.annotations.Param;

/**
 * 安全帽设备信息查询Dao
 * 
 * @author Shawn
 * @date 2025-01-31
 */
@MyBatisDao
public interface HelmetDeviceDao extends CrudDao<HelmetDevice> {

    /**
     * 根据设备ID查询绑定人员身份证ID
     * 
     * @param deviceId 设备ID
     * @return 身份证ID
     * @author Shawn
     * @date 2025-01-31
     */
    String getAssignedPersonByDeviceId(@Param("deviceId") String deviceId);
}