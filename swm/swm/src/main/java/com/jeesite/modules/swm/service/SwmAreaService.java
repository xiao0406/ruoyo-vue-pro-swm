package com.jeesite.modules.swm.service;

import com.jeesite.modules.entity.AiDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.sys.utils.UserUtils;
import com.jeesite.modules.entity.SwmArea;
import com.jeesite.modules.entity.SwmBeaconStation;
import com.jeesite.modules.swm.dao.SwmAreaDao;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

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
     * 清空指定区域下所有信标的区域和颜色字段
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param areaId 区域ID
     */
    @Transactional(readOnly = false)
    public void clearBeaconAreaAndColor(String areaId) {
        if (areaId == null || areaId.trim().isEmpty()) {
            logger.warn("clearBeaconAreaAndColor: areaId为空");
            return;
        }

        logger.info("开始清空区域 {} 下的所有信标关联", areaId);

        // 先查找该区域下的所有信标（用于日志记录）
        List<SwmBeaconStation> beacons = swmBeaconStationService.findByArea(areaId);
        logger.info("找到区域 {} 下的信标数量: {}", areaId, beacons != null ? beacons.size() : 0);

        if (beacons != null && !beacons.isEmpty()) {
            for (SwmBeaconStation beacon : beacons) {
                logger.info("将清空信标 {} (坐标: {}, {}) 的区域关联", beacon.getBeaconId(), beacon.getPixelX(),
                        beacon.getPixelY());
            }
        }

        // 使用直接的SQL更新来强制设置NULL值
        int updatedCount = swmBeaconStationService.clearAreaAndColorByArea(areaId, UserUtils.getUser().getLoginCode(),
                new java.util.Date());

        logger.info("成功清空区域 {} 下 {} 个信标的关联", areaId, updatedCount);
    }

    /**
     * 更新区域的信标关联（编辑模式专用）
     * 先清空该区域的所有信标关联，再根据新的坐标列表重新设置
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param areaId         区域ID
     * @param coordinateList 新的坐标列表
     * @param beaconColor    信标颜色
     */
    @Transactional(readOnly = false)
    public void updateAreaBeaconAssociation(String areaId, String coordinateList, String beaconColor) {
        // 第一步：清空该区域下所有信标的关联
        clearBeaconAreaAndColor(areaId);

        // 第二步：根据新的坐标列表重新设置信标关联
        updateBeaconsByCoordinates(areaId, coordinateList, beaconColor);
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
            logger.warn("updateBeaconsByCoordinates: coordinateList为空");
            return;
        }

        logger.info("开始根据坐标列表更新信标关联，区域ID: {}, 坐标列表: {}", areaId, coordinateList);

        // 使用正则表达式解析坐标
        Pattern pattern = Pattern.compile("\\((\\d+(?:\\.\\d+)?),\\s*(\\d+(?:\\.\\d+)?)\\)");
        Matcher matcher = pattern.matcher(coordinateList);

        int processedCount = 0;
        while (matcher.find()) {
            try {
                Double pixelX = Double.parseDouble(matcher.group(1));
                Double pixelY = Double.parseDouble(matcher.group(2));

                logger.info("处理坐标: ({}, {})", pixelX, pixelY);

                // 根据坐标查找信标
                List<SwmBeaconStation> beacons = swmBeaconStationService.findByPixelCoordinates(pixelX, pixelY);

                logger.info("坐标 ({}, {}) 找到信标数量: {}", pixelX, pixelY, beacons != null ? beacons.size() : 0);

                // 更新找到的信标
                if (beacons != null && !beacons.isEmpty()) {
                    for (SwmBeaconStation beacon : beacons) {
                        logger.info("更新信标 {} 的区域关联为: {}", beacon.getBeaconId(), areaId);
                        beacon.setArea(areaId);
                        if (beaconColor != null && !beaconColor.trim().isEmpty()) {
                            beacon.setBeaconColor(beaconColor);
                        }
                        swmBeaconStationService.save(beacon);
                        processedCount++;
                    }
                } else {
                    logger.warn("坐标 ({}, {}) 没有找到对应的信标", pixelX, pixelY);
                }
            } catch (NumberFormatException e) {
                logger.error("解析坐标失败: {}", matcher.group(), e);
                continue;
            }
        }

        logger.info("完成信标关联更新，共处理 {} 个信标", processedCount);
    }

    /**
     * 根据信标ID列表更新信标的区域和颜色
     *
     * @author Shawn
     * @date 2025-01-14
     * @param areaId      区域ID
     * @param bids        信标ID列表
     * @param beaconColor 信标颜色
     */
    @Transactional(readOnly = false)
    public void updateBeaconsByIds(String areaId, List<String> bids, String beaconColor) {
        if (bids == null || bids.isEmpty()) {
            logger.warn("updateBeaconsByIds: bids为空");
            return;
        }

        logger.info("开始根据信标ID列表更新信标关联，区域ID: {}, 信标ID列表: {}", areaId, bids);

        int processedCount = 0;
        for (String bid : bids) {
            if (bid == null || bid.trim().isEmpty()) {
                logger.warn("跳过空的信标ID");
                continue;
            }

            try {
                // 根据主键ID查找信标
                SwmBeaconStation beacon = swmBeaconStationService.get(bid.trim());

                if (beacon != null) {
                    logger.info("更新信标 {} 的区域关联为: {}", beacon.getBeaconId(), areaId);
                    beacon.setArea(areaId);
                    if (beaconColor != null && !beaconColor.trim().isEmpty()) {
                        beacon.setBeaconColor(beaconColor);
                    }
                    swmBeaconStationService.save(beacon);
                    processedCount++;
                } else {
                    logger.warn("信标ID {} 没有找到对应的信标", bid);
                }
            } catch (Exception e) {
                logger.error("处理信标ID {} 失败", bid, e);
                continue;
            }
        }

        logger.info("完成信标关联更新，共处理 {} 个信标", processedCount);
    }

    /**
     * 更新区域的信标关联（编辑模式专用，通过信标ID）
     * 先清空该区域的所有信标关联，再根据新的信标ID列表重新设置
     *
     * @author Shawn
     * @date 2025-01-14
     * @param areaId      区域ID
     * @param bids        新的信标ID列表
     * @param beaconColor 信标颜色
     */
    @Transactional(readOnly = false)
    public void updateAreaBeaconAssociationByIds(String areaId, List<String> bids, String beaconColor) {
        // 第一步：清空该区域下所有信标的关联
        clearBeaconAreaAndColor(areaId);

        // 第二步：根据新的信标ID列表重新设置信标关联
        updateBeaconsByIds(areaId, bids, beaconColor);
    }

    /**
     * 获取区域对应的信标坐标列表
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param areaId 区域ID
     * @return 坐标字符串，格式如："(1860, 1622), (7132, 3146)"
     */
    public String getAreaBeaconCoordinates(String areaId) {
        if (areaId == null || areaId.trim().isEmpty()) {
            return "";
        }

        // 查找该区域下的所有信标
        List<SwmBeaconStation> beacons = swmBeaconStationService.findByArea(areaId);

        if (beacons == null || beacons.isEmpty()) {
            return "";
        }

        // 构建坐标字符串
        StringBuilder coordinates = new StringBuilder();
        for (int i = 0; i < beacons.size(); i++) {
            SwmBeaconStation beacon = beacons.get(i);
            if (beacon.getPixelX() != null && beacon.getPixelY() != null) {
                if (i > 0) {
                    coordinates.append(", ");
                }
                coordinates.append("(")
                        .append(beacon.getPixelX().intValue())
                        .append(", ")
                        .append(beacon.getPixelY().intValue())
                        .append(")");
            }
        }

        return coordinates.toString();
    }

    /**
     * 测试坐标匹配功能
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param coordinateList 坐标列表
     */
    public void testCoordinateMatch(String coordinateList) {
        if (coordinateList == null || coordinateList.trim().isEmpty()) {
            logger.warn("testCoordinateMatch: coordinateList为空");
            return;
        }

        logger.info("开始测试坐标匹配，坐标列表: {}", coordinateList);

        // 使用正则表达式解析坐标
        Pattern pattern = Pattern.compile("\\((\\d+(?:\\.\\d+)?),\\s*(\\d+(?:\\.\\d+)?)\\)");
        Matcher matcher = pattern.matcher(coordinateList);

        int matchCount = 0;
        while (matcher.find()) {
            try {
                Double pixelX = Double.parseDouble(matcher.group(1));
                Double pixelY = Double.parseDouble(matcher.group(2));

                logger.info("解析到坐标: ({}, {})", pixelX, pixelY);

                // 根据坐标查找信标
                List<SwmBeaconStation> beacons = swmBeaconStationService.findByPixelCoordinates(pixelX, pixelY);

                if (beacons != null && !beacons.isEmpty()) {
                    for (SwmBeaconStation beacon : beacons) {
                        logger.info("找到信标: ID={}, BeaconId={}, 坐标=({}, {}), 当前区域={}",
                                beacon.getId(), beacon.getBeaconId(), beacon.getPixelX(), beacon.getPixelY(),
                                beacon.getArea());
                    }
                    matchCount += beacons.size();
                } else {
                    logger.warn("坐标 ({}, {}) 没有找到对应的信标", pixelX, pixelY);
                }
            } catch (NumberFormatException e) {
                logger.error("解析坐标失败: {}", matcher.group(), e);
            }
        }

        logger.info("坐标匹配测试完成，共找到 {} 个信标", matchCount);
    }

    /**
     * 获取指定区域下的所有信标
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param areaId 区域ID
     * @return 信标列表
     */
    public List<SwmBeaconStation> getBeaconsByArea(String areaId) {
        return swmBeaconStationService.findByArea(areaId);
    }

    /**
     * 删除区域并清空相关信标
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param areaId 区域ID
     */
    @Transactional(readOnly = false)
    public void deleteAreaWithBeacons(String areaId) {
        if (areaId == null || areaId.trim().isEmpty()) {
            logger.warn("deleteAreaWithBeacons: areaId为空");
            return;
        }

        logger.info("开始删除区域 {} 并清空相关信标", areaId);

        // 先清空该区域下所有信标的关联
        clearBeaconAreaAndColor(areaId);

        // 再删除区域记录
        SwmArea area = new SwmArea();
        area.setId(areaId);
        delete(area);

        logger.info("成功删除区域 {} 并清空相关信标", areaId);
    }

    /**
     * 检查信标坐标是否已被其他区域使用
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param coordinateList 坐标列表，格式如："(1860, 1622), (7132, 3146)"
     * @param currentAreaId  当前区域ID，编辑时传入（可为空）
     * @return 冲突信息列表
     */
    public List<Map<String, Object>> checkBeaconConflicts(String coordinateList, String currentAreaId) {
        List<Map<String, Object>> conflicts = new ArrayList<>();

        if (coordinateList == null || coordinateList.trim().isEmpty()) {
            return conflicts;
        }

        logger.info("开始检查信标坐标冲突，坐标列表: {}, 当前区域ID: {}", coordinateList, currentAreaId);

        // 使用正则表达式解析坐标
        Pattern pattern = Pattern.compile("\\((\\d+(?:\\.\\d+)?),\\s*(\\d+(?:\\.\\d+)?)\\)");
        Matcher matcher = pattern.matcher(coordinateList);

        while (matcher.find()) {
            try {
                Double pixelX = Double.parseDouble(matcher.group(1));
                Double pixelY = Double.parseDouble(matcher.group(2));

                logger.info("检查坐标: ({}, {})", pixelX, pixelY);

                // 根据坐标查找信标
                List<SwmBeaconStation> beacons = swmBeaconStationService.findByPixelCoordinates(pixelX, pixelY);

                if (beacons != null && !beacons.isEmpty()) {
                    for (SwmBeaconStation beacon : beacons) {
                        String beaconArea = beacon.getArea();

                        // 如果信标已被其他区域使用（不是当前区域）
                        if (beaconArea != null && !beaconArea.trim().isEmpty() &&
                                !beaconArea.equals(currentAreaId)) {

                            // 获取区域名称
                            String areaName = "";
                            try {
                                SwmArea area = get(beaconArea);
                                if (area != null) {
                                    areaName = area.getAreaName();
                                }
                            } catch (Exception e) {
                                logger.warn("获取区域名称失败: {}", beaconArea, e);
                                areaName = "未知区域";
                            }

                            Map<String, Object> conflict = new HashMap<>();
                            conflict.put("coordinate", "(" + pixelX.intValue() + ", " + pixelY.intValue() + ")");
                            conflict.put("beaconId", beacon.getBeaconId());
                            conflict.put("conflictAreaId", beaconArea);
                            conflict.put("conflictAreaName", areaName);

                            conflicts.add(conflict);

                            logger.warn("发现信标冲突: 坐标({}, {}), 信标ID: {}, 已被区域 {} ({}) 使用",
                                    pixelX, pixelY, beacon.getBeaconId(), areaName, beaconArea);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                logger.error("解析坐标失败: {}", matcher.group(), e);
                continue;
            }
        }

        logger.info("信标冲突检查完成，共发现 {} 个冲突", conflicts.size());
        return conflicts;
    }

    /**
     * 检查信标ID是否已被其他区域使用
     *
     * @author Shawn
     * @date 2025-01-14
     * @param bids          信标ID列表
     * @param currentAreaId 当前区域ID，编辑时传入（可为空）
     * @return 冲突信息列表
     */
    public List<Map<String, Object>> checkBeaconConflictsByIds(List<String> bids, String currentAreaId) {
        List<Map<String, Object>> conflicts = new ArrayList<>();

        if (bids == null || bids.isEmpty()) {
            return conflicts;
        }

        logger.info("开始检查信标ID冲突，信标ID列表: {}, 当前区域ID: {}", bids, currentAreaId);

        for (String bid : bids) {
            if (bid == null || bid.trim().isEmpty()) {
                continue;
            }

            try {
                // 根据主键ID查找信标
                SwmBeaconStation beacon = swmBeaconStationService.get(bid.trim());

                if (beacon != null) {
                    String beaconArea = beacon.getArea();

                    // 如果信标已被其他区域使用（不是当前区域）
                    if (beaconArea != null && !beaconArea.trim().isEmpty() &&
                            !beaconArea.equals(currentAreaId)) {

                        // 获取区域名称
                        String areaName = "";
                        try {
                            SwmArea area = get(beaconArea);
                            if (area != null) {
                                areaName = area.getAreaName();
                            }
                        } catch (Exception e) {
                            logger.warn("获取区域名称失败: {}", beaconArea, e);
                            areaName = "未知区域";
                        }

                        Map<String, Object> conflict = new HashMap<>();
                        conflict.put("beaconId", beacon.getBeaconId());
                        conflict.put("conflictAreaId", beaconArea);
                        conflict.put("conflictAreaName", areaName);
                        conflict.put("coordinate", "(" +
                                (beacon.getPixelX() != null ? beacon.getPixelX().intValue() : "未知") + ", " +
                                (beacon.getPixelY() != null ? beacon.getPixelY().intValue() : "未知") + ")");

                        conflicts.add(conflict);

                        logger.warn("发现信标冲突: 信标ID: {}, 已被区域 {} ({}) 使用",
                                beacon.getBeaconId(), areaName, beaconArea);
                    }
                } else {
                    logger.warn("信标ID {} 没有找到对应的信标", bid);
                }
            } catch (Exception e) {
                logger.error("检查信标ID {} 冲突失败", bid, e);
                continue;
            }
        }

        logger.info("信标ID冲突检查完成，共发现 {} 个冲突", conflicts.size());
        return conflicts;
    }

    public List<AiDto.Trajectory> findAddressList() {
        return this.dao.findAddressList();
    }
}