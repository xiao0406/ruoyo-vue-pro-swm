package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface SwmBeaconStationMapper extends BaseMapperX<SwmBeaconStationDO> {

    SwmBeaconStationDO get(@Param("id") String id, @Param("tenantId") Long tenantId);

    List<SwmBeaconStationDO> findList(@Param("tenantId") Long tenantId,
                                       @Param("beaconId") String beaconId,
                                       @Param("deviceName") String deviceName,
                                       @Param("beaconType") String beaconType,
                                       @Param("location") String location,
                                       @Param("area") String area,
                                       @Param("beaconStatus") String beaconStatus,
                                       @Param("deployStatus") String deployStatus,
                                       @Param("buildingId") String buildingId,
                                       @Param("building") String building,
                                       @Param("floorId") String floorId);

    SwmBeaconStationDO getByBeaconId(@Param("beaconId") String beaconId);

    List<SwmBeaconStationDO> findByBeaconIds(@Param("beaconIds") List<String> beaconIds);

    int countByBeaconIdAndStatus(@Param("beaconId") String beaconId, @Param("id") String id);

    List<SwmBeaconStationDO> findByLocation(@Param("location") String location);

    List<SwmBeaconStationDO> findOnlineBeacons();

    List<SwmBeaconStationDO> findByGpsRange(@Param("minLongitude") Double minLongitude,
                                             @Param("maxLongitude") Double maxLongitude,
                                             @Param("minLatitude") Double minLatitude,
                                             @Param("maxLatitude") Double maxLatitude);

    List<SwmBeaconStationDO> findByPixelRange(@Param("minX") Double minX,
                                               @Param("maxX") Double maxX,
                                               @Param("minY") Double minY,
                                               @Param("maxY") Double maxY);

    List<SwmBeaconStationDO> findByArea(@Param("area") String area);

    List<String> findAllAreas();

    List<String> findAreaOptionsFromBeacon(@Param("tenantId") Long tenantId);

    List<Map<String, Object>> findAreaNameMappings(@Param("areaIds") List<String> areaIds,
                                                    @Param("tenantId") Long tenantId);

    List<String> findFloorOptionsFromBeacon(@Param("tenantId") Long tenantId);

    List<Map<String, Object>> findFloorNameMappings(@Param("floorIds") List<String> floorIds,
                                                     @Param("tenantId") Long tenantId);

    List<SwmBeaconStationDO> findByPixelCoordinates(@Param("pixelX") Double pixelX,
                                                     @Param("pixelY") Double pixelY);

    void clearAreaAndColorByArea(@Param("area") String area,
                                  @Param("updateBy") String updateBy,
                                  @Param("updateDate") Date updateDate);

    void clearAreaAndColorByIds(@Param("ids") List<String> ids,
                                 @Param("updateBy") String updateBy,
                                 @Param("updateDate") Date updateDate);

    void updateBatch(@Param("list") List<SwmBeaconStationDO> list);

    List<SwmBeaconStationDO> findBeaconsWithoutArea();

    List<SwmBeaconStationDO> findDangerousSourceBeacons();

    List<SwmBeaconStationDO> findAvailableDangerousSourceBeacons(@Param("excludeHazardSourceId") String excludeHazardSourceId);

    List<SwmBeaconStationDO> findMacList(@Param("random") Integer random);

    List<Map<String, Object>> findBeaconLocationForMap();
}

