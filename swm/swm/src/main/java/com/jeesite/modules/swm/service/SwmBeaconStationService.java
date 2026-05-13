/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.entity.Page;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.service.CrudService;
import com.jeesite.common.utils.excel.ExcelImport;
import com.jeesite.modules.config.RabbitMqConfig;
import com.jeesite.modules.enums.SyncDataOperateTypeEnum;
import com.jeesite.modules.swm.dao.SwmBeaconStationDao;
import com.jeesite.modules.swm.entity.SwmArea;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.util.MqSendUtil;
import com.jeesite.modules.utils.BatchOperationsUtil;
import com.jeesite.modules.entity.SwmBeaconStationExport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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
    private MqSendUtil mqSendUtil;

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

    @Transactional(readOnly = false)
    public Integer importData(MultipartFile file) {
        ExcelImport excelImport = null;
        List<SwmBeaconStation> swmBeaconStationList = new ArrayList<>();
        Integer count = 0;
        try {
            excelImport = new ExcelImport(file, 2, 0);
            List<SwmBeaconStationExport> list = excelImport.getDataList(SwmBeaconStationExport.class);

            SwmBeaconStation swmBeaconStation1 = new SwmBeaconStation();
            //查询所有的信标信息
            List<SwmBeaconStation> stationList = this.dao.findList(swmBeaconStation1);
            Map<String, String> stationMap = stationList.stream().collect(Collectors.toMap(SwmBeaconStation::getBeaconId, SwmBeaconStation::getId));


            if (CollectionUtil.isNotEmpty(list)){
                //获取所有区域
                List<String> areaNameList = list.stream().map(SwmBeaconStationExport::getArea).collect(Collectors.toList());
                SwmArea area = new SwmArea();
                area.getSqlMap().getWhere().and("area_name", QueryType.IN, areaNameList);
                area.setStatus(SwmArea.STATUS_NORMAL);
                List<SwmArea> swmAreaList = swmAreaService.findList(area);
                Map<String, String> areaMap = swmAreaList.stream().collect(Collectors.toMap(SwmArea::getAreaName, SwmArea::getId));

                for (SwmBeaconStationExport production : list) {
                    //存在相同信标则跳过
                    if (StringUtil.isNotEmpty(stationMap.get(production.getBeaconId()))){
                        continue;
                    }
                    if (StringUtil.isBlank(production.getBeaconId())){
                        throw new RuntimeException("信标：" + production.getBeaconId() + "的信标不能为空" );
                    }
//                    if (StringUtil.isBlank(production.getArea())){
//                        throw new RuntimeException("信标：" + production.getBeaconId() + "的区域内容为空");
//                    }

                    SwmBeaconStation swmBeaconStation = new SwmBeaconStation();

                    if (StringUtil.isNotEmpty(areaMap.get(production.getArea()))){
                        swmBeaconStation.setArea(areaMap.get(production.getArea()));
                    }
                    swmBeaconStation.setBeaconId(production.getBeaconId());
                    swmBeaconStation.setDeviceName(production.getDeviceName());
                    swmBeaconStation.setPixelX(production.getPixelX());
                    swmBeaconStation.setPixelY(production.getPixelY());
                    swmBeaconStation.setLocation(production.getLocation());
                    swmBeaconStation.setBeaconType(production.getBeaconType());
                    swmBeaconStation.setBeaconStatus(production.getBeaconStatus());
                    swmBeaconStationList.add(swmBeaconStation);
                }
                List<List<SwmBeaconStation>> lists = BatchOperationsUtil.batchCutting(swmBeaconStationList, 100);
                for (List<SwmBeaconStation> list1 : lists) {
                    this.dao.insertBatch(list1);
                }
                count = list.size();
//
//                // ========== 构建MQ消息数据 ==========
//                List<SwmBeaconStation> fullBeacons = swmBeaconStationList.stream()
//                        .map(SwmBeaconStation::getBeaconId)
//                        .filter(StringUtils::isNotBlank)
//                        .collect(Collectors.collectingAndThen(
//                                Collectors.toList(), // 去重：避免重复ID查库
//                                deviceIds -> deviceIds.isEmpty()
//                                        ? new ArrayList<>() // 无有效ID时返回空列表
//                                        : this.dao.findByBeaconIds(deviceIds) // 有ID则批量查完整记录
//                        ));
//
//
//                // ========== 发送批量导入MQ消息 ==========
//                if (!fullBeacons.isEmpty()) {
//                    mqSendUtil.sendBeaconBatchChangeMsg(SyncDataOperateTypeEnum.BEACON_IMPORT.getCode(), fullBeacons);
//                } else {
//                    logger.warn("============批量导入Excel无成功数据，不发送MQ============");
//                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    /**
     * 查询信标位置信息（用于前端地图显示）
     *
     * @return 信标位置信息Map，key为beacon_id，value包含location、x、y
     */
    public Map<String, Map<String, Object>> getBeaconLocationForMap() {
        List<Map<String, Object>> beaconList = dao.findBeaconLocationForMap();
        
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (beaconList != null && !beaconList.isEmpty()) {
            for (Map<String, Object> beacon : beaconList) {
                String beaconId = (String) beacon.get("beacon_id");
                if (beaconId != null && !beaconId.trim().isEmpty()) {
                    // 将MAC地址格式化为 xx:xx:xx:xx:xx:xx 格式
                    String formattedBeaconId = formatMacAddress(beaconId);
                    Map<String, Object> locationInfo = new HashMap<>();
                    locationInfo.put("location", beacon.get("location"));
                    locationInfo.put("x", beacon.get("pixel_x"));
                    locationInfo.put("y", beacon.get("pixel_y"));
                    
                    result.put(formattedBeaconId, locationInfo);
                }
            }
        }
        return result;
    }

    /**
     * 格式化MAC地址为标准格式
     * 
     * @param macAddress MAC地址字符串
     * @return 格式化后的MAC地址（如：80ECCCD20B57 -> 80:ec:cc:d2:0b:57）
     */
    private String formatMacAddress(String macAddress) {
        if (macAddress == null || macAddress.length() != 12) {
            return macAddress;
        }
        
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < 12; i += 2) {
            if (i > 0) {
                formatted.append(":");
            }
            formatted.append(macAddress.substring(i, i + 2).toLowerCase());
        }
        return formatted.toString();
    }
}