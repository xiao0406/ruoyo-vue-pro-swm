/**
 * @author Shawn
 * @date 2026-04-08
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.entity.SwmBeaconStation;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 信标基站管理DAO接口
 * 
 * @author Shawn
 */
@MyBatisDao
public interface SwmBeaconStationDao extends CrudDao<SwmBeaconStation> {

        /**
         * 根据MAC地址查询基站
         */
        SwmBeaconStation getByBeaconId(@Param("beaconId") String beaconId);
        
        /**
         * 根据MAC地址列表批量查询基站
         */
        List<SwmBeaconStation> findByBeaconIds(@Param("beaconIds") List<String> beaconIds);

        /**
         * 检查是否存在相同MAC地址且状态为正常的记录（用于重复性校验）
         * 
         * @author Shawn
         * @date 2025/06/22
         */
        int countByBeaconIdAndStatus(@Param("beaconId") String beaconId, @Param("id") String excludeId);

        /**
         * 查询指定位置的基站列表
         */
        List<SwmBeaconStation> findByLocation(@Param("location") String location);

        /**
         * 查询在线状态的基站列表
         */
        List<SwmBeaconStation> findOnlineBeacons();

        /**
         * 查询没有区域ID的信标列表
         *
         * @author Shawn
         * @date 2025-01-14
         */
        List<SwmBeaconStation> findBeaconsWithoutArea();

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
         * 获取当前信标列表实际使用到的区域ID列表
         *
         * @author Shawn
         * @date 2026-04-08
         * @param swmBeaconStation 信标查询对象
         * @return 去重后的区域ID列表
         */
        List<String> findAreaOptionsFromBeacon(SwmBeaconStation swmBeaconStation);

        /**
         * 根据区域ID列表查询区域名称映射
         *
         * @author Shawn
         * @date 2026-04-08
         * @param areaIds 区域ID列表
         * @return 区域ID和区域名称的映射列表
         */
        List<Map<String, Object>> findAreaNameMappings(@Param("areaIds") List<String> areaIds,
                        @Param("corpCode") String corpCode);

        /**
         * 获取当前信标列表实际使用到的楼层ID列表
         *
         * @author Shawn
         * @date 2026-04-08
         * @param swmBeaconStation 信标查询对象
         * @return 去重后的楼层ID列表
         */
        List<String> findFloorOptionsFromBeacon(SwmBeaconStation swmBeaconStation);

        /**
         * 根据楼层ID列表查询楼层名称映射
         *
         * @author Shawn
         * @date 2026-04-08
         * @param floorIds 楼层ID列表
         * @return 楼层ID和楼层名称的映射列表
         */
        List<Map<String, Object>> findFloorNameMappings(@Param("floorIds") List<String> floorIds,
                        @Param("corpCode") String corpCode);

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

        /**
         * 查询危险源类型的信标列表
         * 
         * @author Shawn
         * @date 2025-07-27
         * @return 危险源信标列表
         */
        List<SwmBeaconStation> findDangerousSourceBeacons();

        /**
         * 查询危险源类型的信标列表（排除已使用的）
         * 
         * @author Shawn
         * @date 2025-07-27
         * @param excludeHazardSourceId 要排除的危险源ID（编辑时传入当前记录ID）
         * @return 可用的危险源信标列表
         */
        List<SwmBeaconStation> findAvailableDangerousSourceBeacons(@Param("excludeHazardSourceId") String excludeHazardSourceId);

        void updateBatch(List<SwmBeaconStation> list);

        List<SwmBeaconStation> findMacList(SwmBeaconStation station);
}
