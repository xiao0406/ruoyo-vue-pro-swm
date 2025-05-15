/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 信标基站管理DAO接口
 * 
 * @author Shawn
 */
@MyBatisDao
public interface SwmBeaconStationDao extends CrudDao<SwmBeaconStation> {

    /**
     * 根据信标编号查询基站
     */
    SwmBeaconStation getByBeaconId(@Param("beaconId") String beaconId);

    /**
     * 查询指定位置的基站列表
     */
    List<SwmBeaconStation> findByLocation(@Param("location") String location);

    /**
     * 查询在线状态的基站列表
     */
    List<SwmBeaconStation> findOnlineBeacons();
}