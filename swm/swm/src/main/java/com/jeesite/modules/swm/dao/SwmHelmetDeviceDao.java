/**
 * @author Shawn
 * @date 2023-05-30
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 头盔设备管理DAO接口
 * 
 * @author Shawn
 */
@MyBatisDao
public interface SwmHelmetDeviceDao extends CrudDao<SwmHelmetDevice> {

    /**
     * 查询可用的安全帽列表（未绑定人员的）
     */
    List<SwmHelmetDevice> findAvailableHelmets(@Param("keyword") String keyword);
}