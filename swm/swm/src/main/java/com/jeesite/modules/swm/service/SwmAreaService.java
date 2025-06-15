package com.jeesite.modules.swm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmArea;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import com.jeesite.modules.swm.dao.SwmAreaDao;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 区域管理Service
 * 
 * @author Shawn
 * @version 2025-06-22
 */
@Service
@Transactional(readOnly = true)
public class SwmAreaService extends CrudService<SwmAreaDao, SwmArea> {

    @Autowired
    private SwmBeaconStationService swmBeaconStationService;

    /**
     * 获取单条数据
     * 
     * @param swmArea
     * @return
     */
    @Override
    public SwmArea get(SwmArea swmArea) {
        return super.get(swmArea);
    }

    /**
     * 查询分页数据
     * 
     * @param swmArea 查询条件
     * @return
     */
    @Override
    public Page<SwmArea> findPage(SwmArea swmArea) {
        return super.findPage(swmArea);
    }

    /**
     * 查询列表数据
     * 
     * @param swmArea
     * @return
     */
    @Override
    public List<SwmArea> findList(SwmArea swmArea) {
        return super.findList(swmArea);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmArea
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmArea swmArea) {
        super.save(swmArea);
    }

    /**
     * 更新状态
     * 
     * @param swmArea
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmArea swmArea) {
        super.updateStatus(swmArea);
    }

    /**
     * 删除数据
     * 
     * @param swmArea
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmArea swmArea) {
        super.delete(swmArea);
    }

    /**
     * 根据坐标列表更新信标的区域和颜色
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param areaId         区域ID
     * @param coordinateList 坐标列表，格式如："(1860, 1622), (7132, 3146)"
     * @param beaconColor    信标颜色
     */
    @Transactional(readOnly = false)
    public void updateBeaconsByCoordinates(String areaId, String coordinateList, String beaconColor) {
        if (coordinateList == null || coordinateList.trim().isEmpty()) {
            return;
        }

        // 使用正则表达式解析坐标
        Pattern pattern = Pattern.compile("\\((\\d+(?:\\.\\d+)?),\\s*(\\d+(?:\\.\\d+)?)\\)");
        Matcher matcher = pattern.matcher(coordinateList);

        while (matcher.find()) {
            try {
                Double pixelX = Double.parseDouble(matcher.group(1));
                Double pixelY = Double.parseDouble(matcher.group(2));

                // 根据坐标查找信标
                List<SwmBeaconStation> beacons = swmBeaconStationService.findByPixelCoordinates(pixelX, pixelY);

                // 更新找到的信标
                for (SwmBeaconStation beacon : beacons) {
                    beacon.setArea(areaId);
                    if (beaconColor != null && !beaconColor.trim().isEmpty()) {
                        beacon.setBeaconColor(beaconColor);
                    }
                    swmBeaconStationService.save(beacon);
                }
            } catch (NumberFormatException e) {
                // 忽略格式错误的坐标
                continue;
            }
        }
    }
}