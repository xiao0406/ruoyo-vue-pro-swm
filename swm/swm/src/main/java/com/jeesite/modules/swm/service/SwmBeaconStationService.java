/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmBeaconStationDao;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信标基站管理Service
 * 
 * @author Shawn
 */
@Service
@Transactional(readOnly = true)
public class SwmBeaconStationService extends CrudService<SwmBeaconStationDao, SwmBeaconStation> {

    /**
     * 获取单条数据
     * 
     * @param swmBeaconStation
     * @return
     */
    @Override
    public SwmBeaconStation get(SwmBeaconStation swmBeaconStation) {
        return super.get(swmBeaconStation);
    }

    /**
     * 查询分页数据
     * 
     * @param swmBeaconStation 查询条件
     * @return
     */
    @Override
    public Page<SwmBeaconStation> findPage(SwmBeaconStation swmBeaconStation) {
        return super.findPage(swmBeaconStation);
    }

    /**
     * 查询分页数据（带分页参数）
     * 
     * @param page             分页参数
     * @param swmBeaconStation 查询条件
     * @return
     */
    public Page<SwmBeaconStation> findPage(Page<SwmBeaconStation> page, SwmBeaconStation swmBeaconStation) {
        swmBeaconStation.setPage(page);
        return this.findPage(swmBeaconStation);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmBeaconStation
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmBeaconStation swmBeaconStation) {
        super.save(swmBeaconStation);
    }

    /**
     * 更新状态
     * 
     * @param swmBeaconStation
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmBeaconStation swmBeaconStation) {
        super.updateStatus(swmBeaconStation);
    }

    /**
     * 删除数据
     * 
     * @param swmBeaconStation
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmBeaconStation swmBeaconStation) {
        super.delete(swmBeaconStation);
    }

    /**
     * 根据信标编号获取信标基站
     * 
     * @param beaconId 信标编号
     * @return 信标基站对象
     */
    public SwmBeaconStation getByBeaconId(String beaconId) {
        if (beaconId == null || beaconId.trim().isEmpty()) {
            return null;
        }
        return dao.getByBeaconId(beaconId);
    }

    /**
     * 查询指定位置的基站列表
     * 
     * @param location 位置关键词
     * @return 基站列表
     */
    public List<SwmBeaconStation> findByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }
        return dao.findByLocation(location);
    }

    /**
     * 查询在线状态的基站列表
     * 
     * @return 在线基站列表
     */
    public List<SwmBeaconStation> findOnlineBeacons() {
        return dao.findOnlineBeacons();
    }

    /**
     * 将信标基站列表转换为下拉框所需的格式
     * 
     * @param beaconList 信标基站列表
     * @return 下拉框数据列表
     */
    public List<Map<String, Object>> convertToSelectList(List<SwmBeaconStation> beaconList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (beaconList != null && !beaconList.isEmpty()) {
            for (SwmBeaconStation beacon : beaconList) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", beacon.getBeaconId()); // 值使用信标ID
                map.put("label", beacon.getBeaconId()); // 显示文本也使用信标ID
                // 可以添加额外信息，如位置
                map.put("location", beacon.getLocation());
                resultList.add(map);
            }
        }
        return resultList;
    }

    /**
     * 根据GPS坐标范围查询基站列表
     * 
     * @param minLongitude 最小经度
     * @param maxLongitude 最大经度
     * @param minLatitude  最小纬度
     * @param maxLatitude  最大纬度
     * @return 基站列表
     */
    public List<SwmBeaconStation> findByGpsRange(Double minLongitude, Double maxLongitude,
            Double minLatitude, Double maxLatitude) {
        return dao.findByGpsRange(minLongitude, maxLongitude, minLatitude, maxLatitude);
    }

    /**
     * 根据图纸像素坐标范围查询基站列表
     * 
     * @param minX 最小X坐标
     * @param maxX 最大X坐标
     * @param minY 最小Y坐标
     * @param maxY 最大Y坐标
     * @return 基站列表
     */
    public List<SwmBeaconStation> findByPixelRange(Double minX, Double maxX, Double minY, Double maxY) {
        return dao.findByPixelRange(minX, maxX, minY, maxY);
    }

    /**
     * 根据区域查询基站列表
     * 
     * @param area 区域名称
     * @return 基站列表
     */
    public List<SwmBeaconStation> findByArea(String area) {
        if (area == null || area.trim().isEmpty()) {
            return null;
        }
        return dao.findByArea(area);
    }

    /**
     * 获取所有区域列表
     * 
     * @return 区域列表
     */
    public List<String> findAllAreas() {
        return dao.findAllAreas();
    }

    /**
     * 根据精确的像素坐标查找信标
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param pixelX X坐标
     * @param pixelY Y坐标
     * @return 匹配的信标列表
     */
    public List<SwmBeaconStation> findByPixelCoordinates(Double pixelX, Double pixelY) {
        if (pixelX == null || pixelY == null) {
            return new ArrayList<>();
        }
        return dao.findByPixelCoordinates(pixelX, pixelY);
    }
}