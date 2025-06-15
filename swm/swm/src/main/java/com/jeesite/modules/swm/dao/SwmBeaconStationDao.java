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

        /**
         * 根据GPS坐标范围查询基站列表
         */
        List<SwmBeaconStation> findByGpsRange(@Param("minLongitude") Double minLongitude,
                        @Param("maxLongitude") Double maxLongitude,
                        @Param("minLatitude") Double minLatitude,
                        @Param("maxLatitude") Double maxLatitude);

        /**
         * 根据图纸像素坐标范围查询基站列表
         */
        List<SwmBeaconStation> findByPixelRange(@Param("minX") Double minX,
                        @Param("maxX") Double maxX,
                        @Param("minY") Double minY,
                        @Param("maxY") Double maxY);

        /**
         * 根据区域查询基站列表
         */
        List<SwmBeaconStation> findByArea(@Param("area") String area);

        /**
         * 获取所有区域列表
         */
        List<String> findAllAreas();

        /**
         * 根据精确的像素坐标查找信标
         * 
         * @author Shawn
         * @date 2025-01-14
         */
        List<SwmBeaconStation> findByPixelCoordinates(@Param("pixelX") Double pixelX, @Param("pixelY") Double pixelY);

        /**
         * 清空指定区域下所有信标的区域和颜色字段
         * 
         * @author Shawn
         * @date 2025-01-14
         */
        int clearAreaAndColorByArea(@Param("area") String area, @Param("updateBy") String updateBy,
                        @Param("updateDate") java.util.Date updateDate);

        /**
         * 批量清空指定信标的区域和颜色字段
         * 
         * @author Shawn
         * @date 2025-01-14
         */
        int clearAreaAndColorByIds(@Param("ids") List<String> ids, @Param("updateBy") String updateBy,
                        @Param("updateDate") java.util.Date updateDate);
}