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

import java.util.List;

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
}