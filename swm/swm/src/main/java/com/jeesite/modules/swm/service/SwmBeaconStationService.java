/**
 * @author Shawn
 * @date 2026-04-08
 */
package com.jeesite.modules.swm.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.common.utils.excel.ExcelImport;
import com.jeesite.modules.swm.dao.SwmBeaconStationDao;
import com.jeesite.modules.entity.SwmBeaconStation;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.entity.SwmSiteMapManagement;
import com.jeesite.modules.swm.util.MqSendUtil;
import com.jeesite.modules.utils.BatchOperationsUtil;
import com.jeesite.modules.entity.SwmBeaconStationExport;
import com.jeesite.modules.sys.utils.CorpUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;

/**
 * 信标基站管理Service
 * 
 * @author Shawn
 */
@Service
@Transactional(readOnly = true)
public class SwmBeaconStationService extends CrudService<SwmBeaconStationDao, SwmBeaconStation> {

    @Autowired
    @Lazy
    private SwmAreaService swmAreaService;

    @Autowired
    @Lazy
    private SwmSiteMapManagementService swmSiteMapManagementService;

    @Autowired
    private MqSendUtil mqSendUtil;

    /**
     * 获取单条数据
     * 
     * @param swmBeaconStation
     * @return
     */
    @Override
    public SwmBeaconStation get(SwmBeaconStation swmBeaconStation) {
        fillCurrentCorpCode(swmBeaconStation);
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
        fillCurrentCorpCode(swmBeaconStation);
        return super.findPage(swmBeaconStation);
    }

    /**
     * 查询列表数据
     *
     * @param swmBeaconStation 查询条件
     * @return 列表数据
     */
    @Override
    public List<SwmBeaconStation> findList(SwmBeaconStation swmBeaconStation) {
        fillCurrentCorpCode(swmBeaconStation);
        return super.findList(swmBeaconStation);
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
//        SyncDataOperateTypeEnum operateType = (swmBeaconStation.getId() == null || swmBeaconStation.getId().isEmpty())
//                ? SyncDataOperateTypeEnum.BEACON_ADD
//                : SyncDataOperateTypeEnum.BEACON_EDIT;
        // 如果是电子围栏且MAC地址为空，自动生成
        generateBeaconIdForElectronicFence(swmBeaconStation);

        // 校验MAC地址重复性
        validateBeaconIdDuplicate(swmBeaconStation);
        super.save(swmBeaconStation);
//        SwmBeaconStation beaconStation = super.get(swmBeaconStation.getId());
//        mqSendUtil.sendBeaconSingleChangeMsg(operateType.getCode(), beaconStation);
    }

    /**
     * 为电子围栏生成唯一的MAC地址
     * 
     * @author Shawn
     * @date 2025/01/14
     * @param swmBeaconStation 信标基站对象
     */
    private void generateBeaconIdForElectronicFence(SwmBeaconStation swmBeaconStation) {
        // 检查是否为电子围栏且MAC地址为空
        if (swmBeaconStation != null && "2".equals(swmBeaconStation.getBeaconType())
                && (swmBeaconStation.getBeaconId() == null || swmBeaconStation.getBeaconId().trim().isEmpty())) {

            String generatedId;
            int attempts = 0;
            int maxAttempts = 10; // 最大尝试次数，防止无限循环

            do {
                generatedId = generateUniqueBeaconId();
                attempts++;

                if (attempts >= maxAttempts) {
                    throw new RuntimeException("生成唯一MAC地址失败，请重试！");
                }
            } while (isBeaconIdExists(generatedId));

            swmBeaconStation.setBeaconId(generatedId);
        }
    }

    /**
     * 生成MAC地址
     * 格式：mac + 时间戳后6位 + 随机字符串
     * 
     * @author Shawn
     * @date 2025/01/14
     * @return 生成的MAC地址
     */
    private String generateUniqueBeaconId() {
        // 获取当前时间戳的后6位
        long timestamp = System.currentTimeMillis();
        String timestampSuffix = String.valueOf(timestamp).substring(7);

        // 生成4位随机字符串（数字和字母）
        String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder randomStr = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 4; i++) {
            randomStr.append(chars.charAt(random.nextInt(chars.length())));
        }

        return "mac" + timestampSuffix + randomStr.toString();
    }

    /**
     * 检查MAC地址是否已存在
     * 
     * @author Shawn
     * @date 2025/01/14
     * @param beaconId MAC地址
     * @return true-已存在，false-不存在
     */
    private boolean isBeaconIdExists(String beaconId) {
        if (beaconId == null || beaconId.trim().isEmpty()) {
            return false;
        }

        SwmBeaconStation existingBeacon = dao.getByBeaconId(beaconId);
        return existingBeacon != null;
    }

    /**
     * 校验MAC地址重复性
     * 
     * @author Shawn
     * @date 2025/06/22
     * @param swmBeaconStation 信标基站对象
     */
    private void validateBeaconIdDuplicate(SwmBeaconStation swmBeaconStation) {
        if (swmBeaconStation == null || swmBeaconStation.getBeaconId() == null
                || swmBeaconStation.getBeaconId().trim().isEmpty()) {
            return;
        }

        // 检查是否存在相同MAC地址且状态为正常的记录
        int count = dao.countByBeaconIdAndStatus(swmBeaconStation.getBeaconId(), swmBeaconStation.getId());
        if (count > 0) {
            throw new RuntimeException("MAC地址 [" + swmBeaconStation.getBeaconId() + "] 已存在，不能重复保存！");
        }
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
//        mqSendUtil.sendBeaconDeleteMqMessage(SyncDataOperateTypeEnum.BEACON_DELETE.getCode(), swmBeaconStation.getId());
    }

    /**
     * 根据MAC地址获取信标基站
     * 
     * @param beaconId MAC地址
     * @return 信标基站对象
     */
    public SwmBeaconStation getByBeaconId(String beaconId) {
        if (beaconId == null || beaconId.trim().isEmpty()) {
            return null;
        }
        return dao.getByBeaconId(beaconId);
    }
    
    /**
     * 根据MAC地址批量查询信标基站
     * 
     * @param beaconIds MAC地址列表
     * @return 信标基站列表
     */
    public List<SwmBeaconStation> findByBeaconIds(List<String> beaconIds) {
        if (beaconIds == null || beaconIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return dao.findByBeaconIds(beaconIds);
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
     * 查询没有区域ID的信标列表
     *
     * @author Shawn
     * @date 2025-01-14
     * @return 没有区域ID的信标列表
     */
    public List<SwmBeaconStation> findBeaconsWithoutArea() {
        return dao.findBeaconsWithoutArea();
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
                map.put("value", beacon.getBeaconId()); // 值使用MAC地址
                map.put("label", beacon.getBeaconId()); // 显示文本也使用MAC地址
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
     * 获取信标所属区域下拉选项
     *
     * 大白话说，就是先从当前信标列表里把真正用到的区域ID找出来，
     * 再去区域表翻译成人能看懂的名字，翻译不到就直接回退显示ID。
     *
     * @author Shawn
     * @date 2026-04-08
     * @return 下拉选项列表
     */
    public List<Map<String, Object>> getAreaOptionsFromBeacon() {
        try {
            logger.info("开始获取信标所属区域下拉选项");

            // 先按当前信标列表口径拿出去重后的区域ID，保持和现有列表范围一致。
            SwmBeaconStation query = new SwmBeaconStation();
            fillCurrentCorpCode(query);
            List<String> areaIds = filterValidAreaIds(dao.findAreaOptionsFromBeacon(query));
            if (CollectionUtil.isEmpty(areaIds)) {
                logger.info("当前信标列表没有可用的所属区域，下拉选项返回空列表");
                return new ArrayList<>();
            }

            // 再批量翻译区域名称，避免一个ID查一次数据库。
            Map<String, String> areaNameMap = buildAreaNameMap(areaIds, query.getCorpCode());
            List<Map<String, Object>> options = buildAreaOptions(areaIds, areaNameMap);

            logger.info("获取信标所属区域下拉选项成功，areaCount={}", options.size());
            return options;
        } catch (Exception e) {
            logger.error("获取信标所属区域下拉选项失败", e);
            throw new RuntimeException("获取信标所属区域下拉选项失败", e);
        }
    }

    /**
     * 获取信标所属楼层下拉选项
     *
     * 大白话说，就是先从当前信标列表里把真正用到的楼层ID找出来，
     * 再去底图表翻译成人能看懂的楼层名，翻译不到就直接回退显示ID。
     *
     * @author Shawn
     * @date 2026-04-08
     * @return 下拉选项列表
     */
    public List<Map<String, Object>> getFloorOptionsFromBeacon() {
        try {
            logger.info("开始获取信标所属楼层下拉选项");

            // 先按当前信标列表口径拿出去重后的楼层ID，保持和现有列表范围一致。
            SwmBeaconStation query = new SwmBeaconStation();
            fillCurrentCorpCode(query);
            List<String> floorIds = filterValidAreaIds(dao.findFloorOptionsFromBeacon(query));
            if (CollectionUtil.isEmpty(floorIds)) {
                logger.info("当前信标列表没有可用的所属楼层，下拉选项返回空列表");
                return new ArrayList<>();
            }

            // 再批量翻译楼层名称，避免一个ID查一次数据库。
            Map<String, String> floorNameMap = buildFloorNameMap(floorIds, query.getCorpCode());
            List<Map<String, Object>> options = buildFloorOptions(floorIds, floorNameMap);

            logger.info("获取信标所属楼层下拉选项成功，floorCount={}", options.size());
            return options;
        } catch (Exception e) {
            logger.error("获取信标所属楼层下拉选项失败", e);
            throw new RuntimeException("获取信标所属楼层下拉选项失败", e);
        }
    }

    /**
     * 过滤空值、空串和只有空格的区域ID
     *
     * @author Shawn
     * @date 2026-04-08
     * @param areaIds 原始区域ID列表
     * @return 清洗后的区域ID列表
     */
    private List<String> filterValidAreaIds(List<String> areaIds) {
        if (CollectionUtil.isEmpty(areaIds)) {
            return new ArrayList<>();
        }

        List<String> validAreaIds = new ArrayList<>();

        // 这里统一做一次trim，避免数据库里有前后空格导致翻译失败。
        for (String areaId : areaIds) {
            if (StringUtils.isBlank(areaId)) {
                continue;
            }

            validAreaIds.add(areaId.trim());
        }
        return validAreaIds;
    }

    /**
     * 批量构建区域ID到区域名称的映射
     *
     * @author Shawn
     * @date 2026-04-08
     * @param areaIds 区域ID列表
     * @return 区域名称映射
     */
    private Map<String, String> buildAreaNameMap(List<String> areaIds, String corpCode) {
        Map<String, String> areaNameMap = new HashMap<>();
        if (CollectionUtil.isEmpty(areaIds)) {
            return areaNameMap;
        }

        // 这里不额外限制区域状态，尽量把历史区域名称也翻译出来。
        List<Map<String, Object>> areaMappings = dao.findAreaNameMappings(areaIds, corpCode);
        if (CollectionUtil.isEmpty(areaMappings)) {
            return areaNameMap;
        }

        for (Map<String, Object> areaMapping : areaMappings) {
            if (areaMapping == null || areaMapping.isEmpty()) {
                continue;
            }

            String areaId = toTrimmedString(areaMapping.get("value"));
            String areaName = toTrimmedString(areaMapping.get("label"));
            if (StringUtils.isAnyBlank(areaId, areaName)) {
                continue;
            }

            areaNameMap.put(areaId, areaName);
        }
        return areaNameMap;
    }

    /**
     * 批量构建楼层ID到楼层名称的映射
     *
     * @author Shawn
     * @date 2026-04-08
     * @param floorIds 楼层ID列表
     * @return 楼层名称映射
     */
    private Map<String, String> buildFloorNameMap(List<String> floorIds, String corpCode) {
        Map<String, String> floorNameMap = new HashMap<>();
        if (CollectionUtil.isEmpty(floorIds)) {
            return floorNameMap;
        }

        // 楼层名统一从底图表翻译，只认 floor 节点，避免把建筑节点名称带进来。
        List<Map<String, Object>> floorMappings = dao.findFloorNameMappings(floorIds, corpCode);
        if (CollectionUtil.isEmpty(floorMappings)) {
            return floorNameMap;
        }

        for (Map<String, Object> floorMapping : floorMappings) {
            if (floorMapping == null || floorMapping.isEmpty()) {
                continue;
            }

            String floorId = toTrimmedString(floorMapping.get("value"));
            String floorName = toTrimmedString(floorMapping.get("label"));
            if (StringUtils.isAnyBlank(floorId, floorName)) {
                continue;
            }

            floorNameMap.put(floorId, floorName);
        }
        return floorNameMap;
    }

    /**
     * 把区域ID列表组装成前端可直接使用的下拉结构
     *
     * @author Shawn
     * @date 2026-04-08
     * @param areaIds 区域ID列表
     * @param areaNameMap 区域名称映射
     * @return 下拉选项列表
     */
    private List<Map<String, Object>> buildAreaOptions(List<String> areaIds, Map<String, String> areaNameMap) {
        List<Map<String, Object>> options = new ArrayList<>();
        if (CollectionUtil.isEmpty(areaIds)) {
            return options;
        }

        for (String areaId : areaIds) {
            String displayName = areaNameMap.get(areaId);
            if (StringUtils.isBlank(displayName)) {
                displayName = areaId;
            }

            Map<String, Object> option = new HashMap<>();
            option.put("value", areaId);
            option.put("label", displayName);
            option.put("areaName", displayName);
            options.add(option);
        }
        return options;
    }

    /**
     * 把楼层ID列表组装成前端可直接使用的下拉结构
     *
     * @author Shawn
     * @date 2026-04-08
     * @param floorIds 楼层ID列表
     * @param floorNameMap 楼层名称映射
     * @return 下拉选项列表
     */
    private List<Map<String, Object>> buildFloorOptions(List<String> floorIds, Map<String, String> floorNameMap) {
        List<Map<String, Object>> options = new ArrayList<>();
        if (CollectionUtil.isEmpty(floorIds)) {
            return options;
        }

        for (String floorId : floorIds) {
            String displayName = floorNameMap.get(floorId);
            if (StringUtils.isBlank(displayName)) {
                displayName = floorId;
            }

            Map<String, Object> option = new HashMap<>();
            option.put("value", floorId);
            option.put("label", displayName);
            option.put("floorName", displayName);
            options.add(option);
        }
        return options;
    }

    /**
     * 把对象安全转成去空格后的字符串
     *
     * @author Shawn
     * @date 2026-04-08
     * @param value 原始值
     * @return 去空格后的字符串
     */
    private String toTrimmedString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    /**
     * 给查询对象补上当前租户，避免手写SQL漏掉多租户条件。
     *
     * @param swmBeaconStation 信标查询对象
     */
    private void fillCurrentCorpCode(SwmBeaconStation swmBeaconStation) {
        if (swmBeaconStation == null) {
            return;
        }
        if (StringUtils.isNotBlank(swmBeaconStation.getCorpCode())) {
            return;
        }
        swmBeaconStation.setCorpCode(CorpUtils.getCurrentCorpCode());
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

    /**
     * 清空指定区域下所有信标的区域和颜色字段
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param area       区域ID
     * @param updateBy   更新人
     * @param updateDate 更新时间
     * @return 更新的记录数
     */
    @Transactional(readOnly = false)
    public int clearAreaAndColorByArea(String area, String updateBy, java.util.Date updateDate) {
        return dao.clearAreaAndColorByArea(area, updateBy, updateDate);
    }

    /**
     * 批量清空指定信标的区域和颜色字段
     * 
     * @author Shawn
     * @date 2025-01-14
     * @param ids        信标ID列表
     * @param updateBy   更新人
     * @param updateDate 更新时间
     * @return 更新的记录数
     */
    @Transactional(readOnly = false)
    public int clearAreaAndColorByIds(List<String> ids, String updateBy, java.util.Date updateDate) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return dao.clearAreaAndColorByIds(ids, updateBy, updateDate);
    }

    /**
     * 根据信标ID获取信标名称
     * 
     * @param beaconId 信标ID
     * @return 包含信标信息的Map，包含deviceName字段
     */
    public Map<String, Object> getBeaconNameById(String beaconId) {
        if (beaconId == null || beaconId.trim().isEmpty()) {
            return null;
        }
        
        SwmBeaconStation beacon = dao.getByBeaconId(beaconId);
        if (beacon == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", beacon.getId());
        result.put("beaconId", beacon.getBeaconId());
        // 如果设备名称为空，则使用MAC地址作为设备名称
        String deviceName = beacon.getDeviceName();
        if (deviceName == null || deviceName.trim().isEmpty() || "null".equals(deviceName)) {
            deviceName = beacon.getBeaconId();
        }
        result.put("deviceName", deviceName);
        result.put("location", beacon.getLocation());
        
        return result;
    }

    /**
     * 获取危险源类型的信标列表用于下拉框选择
     * 
     * @author Shawn
     * @date 2025-07-27
     * @return 危险源信标下拉框数据列表
     */
    public List<Map<String, Object>> getDangerousSourceBeaconSelectList() {
        // 查询危险源类型的信标
        List<SwmBeaconStation> beaconList = dao.findDangerousSourceBeacons();
        
        // 转换为下拉框所需的格式，但label使用device_name
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (beaconList != null && !beaconList.isEmpty()) {
            for (SwmBeaconStation beacon : beaconList) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", beacon.getBeaconId()); // 值仍然使用MAC地址
                
                // 显示文本使用设备名称，如果设备名称为空则使用MAC地址
                String deviceName = beacon.getDeviceName();
                if (deviceName == null || deviceName.trim().isEmpty() || "null".equals(deviceName)) {
                    deviceName = beacon.getBeaconId();
                }
                map.put("label", deviceName); // 显示设备名称
                map.put("location", beacon.getLocation()); // 位置信息
                resultList.add(map);
            }
        }
        return resultList;
    }

    /**
     * 获取可用的危险源类型信标列表（排除已使用的）
     * 
     * @author Shawn
     * @date 2025-07-27
     * @param excludeHazardSourceId 要排除的危险源ID（编辑时传入当前记录ID）
     * @return 可用的危险源信标下拉框数据列表
     */
    public List<Map<String, Object>> getAvailableDangerousSourceBeaconSelectList(String excludeHazardSourceId) {
        // 查询可用的危险源类型信标
        List<SwmBeaconStation> beaconList = dao.findAvailableDangerousSourceBeacons(excludeHazardSourceId);
        
        // 转换为下拉框所需的格式，但label使用device_name
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (beaconList != null && !beaconList.isEmpty()) {
            for (SwmBeaconStation beacon : beaconList) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", beacon.getBeaconId()); // 值仍然使用MAC地址
                
                // 显示文本使用设备名称，如果设备名称为空则使用MAC地址
                String deviceName = beacon.getDeviceName();
                if (deviceName == null || deviceName.trim().isEmpty() || "null".equals(deviceName)) {
                    deviceName = beacon.getBeaconId();
                }
                map.put("label", deviceName); // 显示设备名称
                map.put("location", beacon.getLocation()); // 位置信息
                resultList.add(map);
            }
        }
        return resultList;
    }

    /**
     * 导出算法格式数据
     *
     * 大白话：按楼层ID分组返回信标数据，MAC地址格式转换为小写+冒号分隔，
     * 只查常规信标（beaconType=1），按租户过滤。
     * 无 floorId 的信标归入当前租户下启用的顶级地图记录 id 作为默认楼层分组。
     *
     * @author Shawn
     * @date 2026-04-09
     * @return 按楼层分组的信标算法数据
     */
    public Map<String, Map<String, Object>> exportAlgorithmFormat() {
        logger.info("开始导出信标算法格式数据");

        // 1. 获取默认楼层ID：当前租户下 status='0' 的第一条顶级地图记录 id
        String defaultFloorId = null;
        try {
            SwmSiteMapManagement activeMap = swmSiteMapManagementService.findActiveMap();
            if (activeMap != null && StringUtils.isNotBlank(activeMap.getId())) {
                defaultFloorId = activeMap.getId();
                logger.info("获取到默认楼层ID: {}", defaultFloorId);
            }
        } catch (Exception e) {
            logger.warn("查询默认楼层失败，继续导出有 floorId 的信标", e);
        }

        // 2. 创建查询条件，只查常规信标
        SwmBeaconStation query = new SwmBeaconStation();
        fillCurrentCorpCode(query);
        query.setBeaconType(SwmBeaconStation.BeaconTypeEnum.CONVENTION);

        // 3. 查询所有常规信标（不分页）
        List<SwmBeaconStation> beaconList = findList(query);
        if (CollectionUtil.isEmpty(beaconList)) {
            logger.info("当前无可导出的常规信标数据");
            return new HashMap<>();
        }

        // 4. 按楼层ID分组构建返回结构
        Map<String, Map<String, Object>> result = new HashMap<>();
        int totalBeaconCount = 0;
        int noFloorIdCount = 0;
        for (SwmBeaconStation beacon : beaconList) {
            // 跳过无MAC地址的信标
            String beaconId = beacon.getBeaconId();
            if (StringUtils.isBlank(beaconId)) {
                continue;
            }

            // 确定分组楼层ID：无 floorId 时使用 defaultFloorId，若 defaultFloorId 也不存在则跳过
            String floorId = beacon.getFloorId();
            if (StringUtils.isBlank(floorId)) {
                if (StringUtils.isBlank(defaultFloorId)) {
                    noFloorIdCount++;
                    continue;
                }
                floorId = defaultFloorId;
            }

            // MAC地址格式转换：80ECCCD23F2F -> 80:ec:cc:d2:3f:2f
            String formattedMac = formatMacAddress(beaconId);

            // 按楼层ID分组
            Map<String, Object> floorData = result.computeIfAbsent(floorId, k -> new HashMap<>());

            // 构建内层数据结构
            Map<String, Object> beaconData = new HashMap<>();
            // 无 floorId 的信标 location 设为"全景地图信标"
            if (StringUtils.isBlank(beacon.getFloorId())) {
                beaconData.put("location", "全景地图信标");
            } else {
                beaconData.put("location", beacon.getLocation() != null ? beacon.getLocation() : "");
            }
            beaconData.put("x", beacon.getPixelX() != null ? beacon.getPixelX() : 0);
            beaconData.put("y", beacon.getPixelY() != null ? beacon.getPixelY() : 0);
            beaconData.put("floorId", floorId);

            floorData.put(formattedMac, beaconData);
            totalBeaconCount++;
        }

        logger.info("导出信标算法格式数据成功，共 {} 个楼层，{} 条信标，无 floorId 且无默认分组跳过 {} 条",
                result.size(), totalBeaconCount, noFloorIdCount);
        return result;
    }

    /**
     * MAC地址格式转换
     *
     * 数据库存储格式：80ECCCD23F2F（12位大写无冒号）
     * 算法要求格式：80:ec:cc:d2:3f:2f（小写带冒号）
     *
     * @author Shawn
     * @date 2026-04-09
     * @param mac 原始MAC地址
     * @return 格式化后的MAC地址
     */
    private String formatMacAddress(String mac) {
        if (StringUtils.isBlank(mac)) {
            return "";
        }

        // 去除可能的冒号和空格，统一处理
        String cleanMac = mac.replace(":", "").replace("-", "").trim().toLowerCase();

        // 长度校验，标准MAC地址为12位
        if (cleanMac.length() != 12) {
            logger.warn("MAC地址格式异常，长度不是12位: {}", mac);
            return mac.toLowerCase();
        }

        // 每2位插入冒号
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < cleanMac.length(); i += 2) {
            if (i > 0) {
                formatted.append(":");
            }
            formatted.append(cleanMac.substring(i, i + 2));
        }

        return formatted.toString();
    }

    @Transactional(readOnly = false)
    public Integer importData(MultipartFile file) {
        ExcelImport excelImport = null;
        List<SwmBeaconStation> swmBeaconStationList = new ArrayList<>();
        Integer count = 0;
        try {
            excelImport = new ExcelImport(file, 2, 0);
            List<SwmBeaconStationExport> list = excelImport.getDataList(SwmBeaconStationExport.class);

            SwmBeaconStation swmBeaconStation1 = new SwmBeaconStation();
            fillCurrentCorpCode(swmBeaconStation1);
            //查询所有的信标信息
            List<SwmBeaconStation> stationList = this.dao.findList(swmBeaconStation1);
            Map<String, String> stationMap = stationList.stream().collect(Collectors.toMap(SwmBeaconStation::getBeaconId, SwmBeaconStation::getId));

            if (CollectionUtil.isNotEmpty(list)){
                for (SwmBeaconStationExport production : list) {
                    //存在相同信标则跳过
                    if (StringUtil.isNotEmpty(stationMap.get(production.getBeaconId()))){
                        SwmBeaconStation swmBeaconStation = new SwmBeaconStation();
                        swmBeaconStation.setBeaconId(production.getBeaconId());
                        swmBeaconStation.setMajor(production.getMajor());
                        swmBeaconStation.setMinor(production.getMinor());
                        swmBeaconStation.setId(stationMap.get(production.getBeaconId()));
                        swmBeaconStationList.add(swmBeaconStation);
                    }
                }
                List<List<SwmBeaconStation>> lists = BatchOperationsUtil.batchCutting(swmBeaconStationList, 100);
                for (List<SwmBeaconStation> list1 : lists) {
                    this.dao.updateBatch(list1);
                }
                count = list.size();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    public List<SwmBeaconStation> findMacList(SwmBeaconStation station) {
        return this.dao.findMacList(station);
    }
}
