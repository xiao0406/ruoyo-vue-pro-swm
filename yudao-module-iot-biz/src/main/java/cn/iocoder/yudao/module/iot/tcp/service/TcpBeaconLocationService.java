package cn.iocoder.yudao.module.iot.tcp.service;

import cn.iocoder.yudao.module.swm.service.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.swm.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAreaDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TCP信标位置服务
 * 提供从蓝牙信标中获取位置和区域信息的通用服务
 *
 * @author Shawn
 * @date 2025-01-27
 */
@Service
public class TcpBeaconLocationService {

    private static final Logger logger = LoggerFactory.getLogger(TcpBeaconLocationService.class);

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private RedisService redisService;
    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;

    /**
     * 从蓝牙信标中获取位置信息
     * 按信号强度从强到弱依次查询，直到找到在swm_beacon_station表中存在的信标
     *
     * @param messageData TCP消息数据
     * @return 位置信息，如果未找到返回空字符串
     */
    public String getLocationFromBeacons(TcpMessageData messageData) {
        if (messageData == null) {
            return "";
        }
        String deviceId = messageData.getDeviceId();
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);

        List<Map<String, Object>> bluetoothBeacons = messageData.getBluetoothBeacons();
        if (bluetoothBeacons == null || bluetoothBeacons.isEmpty()) {
            return "";
        }

        // 将信标按RSSI值排序（从强到弱）
        List<Map<String, Object>> sortedBeacons = new ArrayList<>(bluetoothBeacons);
        sortedBeacons.sort((b1, b2) -> {
            try {
                String rssi1Str = (String) b1.get("RSSI");
                String rssi2Str = (String) b2.get("RSSI");
                int rssi1 = Integer.parseInt(rssi1Str);
                int rssi2 = Integer.parseInt(rssi2Str);
                return Integer.compare(rssi2, rssi1); // 降序排列
            } catch (Exception e) {
                return 0;
            }
        });

        // 按信号强度顺序查询，直到找到存在的信标
//        String sql = "SELECT location FROM swm_beacon_station WHERE beacon_id = ? AND status = '0'";

        for (Map<String, Object> beacon : sortedBeacons) {
            String mac = (String) beacon.get("MAC");
            String rssiStr = (String) beacon.get("RSSI");

            if (StringUtils.isNotBlank(mac)) {
                try {
//                    List<String> results = jdbcTemplate.queryForList(sql, String.class, mac.toUpperCase());
                    //查询位置名称
                    String location = this.getLocationByMac(mac, corpCode);
                    if (!location.isEmpty()) {
                        logger.info("找到信标位置: MAC={}, RSSI={}, location={}",
                            mac, rssiStr, location);
                        return location;
                    } else {
                        logger.debug("信标未在数据库中找到: MAC={}, RSSI={}", mac, rssiStr);
                    }
                } catch (Exception e) {
                    logger.error("查询location失败, MAC: {}", mac, e);
                }
            }
        }

        logger.warn("所有蓝牙信标都未在swm_beacon_station表中找到，deviceId: {}", messageData.getDeviceId());
        return "";
    }

    /**
     * 从蓝牙信标中获取区域名称
     * 按信号强度从强到弱依次查询，直到找到在swm_beacon_station表中存在的信标
     *
     * @param messageData TCP消息数据
     * @return 区域名称，如果未找到返回空字符串
     */
    public String getAreaNameFromBeacons(TcpMessageData messageData) {
        if (messageData == null) {
            return "";
        }

        String deviceId = messageData.getDeviceId();
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);

        List<Map<String, Object>> bluetoothBeacons = messageData.getBluetoothBeacons();
        if (bluetoothBeacons == null || bluetoothBeacons.isEmpty()) {
            return "";
        }

        // 将信标按RSSI值排序（从强到弱）
        List<Map<String, Object>> sortedBeacons = new ArrayList<>(bluetoothBeacons);
        sortedBeacons.sort((b1, b2) -> {
            try {
                String rssi1Str = (String) b1.get("RSSI");
                String rssi2Str = (String) b2.get("RSSI");
                int rssi1 = Integer.parseInt(rssi1Str);
                int rssi2 = Integer.parseInt(rssi2Str);
                return Integer.compare(rssi2, rssi1); // 降序排列
            } catch (Exception e) {
                return 0;
            }
        });

        // 按信号强度顺序查询，直到找到存在的信标
//        String sql = "SELECT a.area_name " +
//                    "FROM swm_beacon_station bs " +
//                    "LEFT JOIN swm_area a ON bs.area = a.id AND a.del_flag = '0' AND a.status = '0' " +
//                    "WHERE bs.beacon_id = ? AND bs.status = '0'";
//

        for (Map<String, Object> beacon : sortedBeacons) {
            String mac = (String) beacon.get("MAC");
            String rssiStr = (String) beacon.get("RSSI");

            if (StringUtils.isNotBlank(mac)) {
                try {
//                    List<String> results = jdbcTemplate.queryForList(sql, String.class, mac.toUpperCase());
                    //根据mac地址查询区域信息
                    Map<String, Object> result = this.getAreaNameByMac(mac, corpCode);
                    String areaName = null;
                    if (result != null){
                        areaName = (String)result.get("areaName");
                    }

                    if (!areaName.isEmpty() ) {
                        logger.info("找到信标区域: MAC={}, RSSI={}, areaName={}",
                            mac, rssiStr, areaName);
                        return areaName;
                    } else {
                        logger.debug("信标未在数据库中找到区域: MAC={}, RSSI={}", mac, rssiStr);
                    }
                } catch (Exception e) {
                    logger.error("查询areaName失败, MAC: {}", mac, e);
                }
            }
        }

        logger.warn("所有蓝牙信标都未在swm_beacon_station表中找到区域，deviceId: {}", messageData.getDeviceId());
        return "";
    }

    /**
     * 将MAC地址格式化为带冒号的格式
     *
     * @param mac 无冒号的MAC地址（如：80ECCCD0BCF6）
     * @return 带冒号的MAC地址（如：80:EC:CC:D0:BC:F6）
     */
    public String formatMacWithColon(String mac) {
        if (mac == null || mac.length() != 12) {
            return mac;
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < mac.length(); i += 2) {
            if (i > 0) {
                formatted.append(":");
            }
            formatted.append(mac.substring(i, i + 2));
        }
        return formatted.toString();
    }

    /**
     * 从蓝牙信标中获取区域名称
     * 过滤 RSSI ≤ -80 的信标，按信号强度从强到弱查询
     */
    public Map<String,Object> getAreaNameFromBeaconsFilter(TcpMessageData messageData) {
        if (messageData == null) {
            return null;
        }
        String deviceId = messageData.getDeviceId();
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);

        List<Map<String, Object>> bluetoothBeacons = messageData.getBluetoothBeacons();
        if (bluetoothBeacons == null || bluetoothBeacons.isEmpty()) {
            return null;
        }

        // 1. 过滤 RSSI > -80 且可解析的信标
        List<Map<String, Object>> validBeacons = new ArrayList<>();
        for (Map<String, Object> beacon : bluetoothBeacons) {
            Object rssiObj = beacon.get("RSSI");
            if (rssiObj == null) {
                continue;
            }

            try {
                int rssi = Integer.parseInt(rssiObj.toString());
                if (rssi > -80) {
                    beacon.put("_rssiInt", rssi); // 缓存解析结果
                    validBeacons.add(beacon);
                }
            } catch (Exception e) {
                logger.debug("非法 RSSI，忽略该信标: {}", beacon);
            }
        }

        if (validBeacons.isEmpty()) {
            logger.warn("所有蓝牙信标 RSSI ≤ -80，被过滤，deviceId: {}", messageData.getDeviceId());
            return null;
        }

        // 2. 按 RSSI 从强到弱排序
        validBeacons.sort((b1, b2) -> {
            int rssi1 = (int) b1.get("_rssiInt");
            int rssi2 = (int) b2.get("_rssiInt");
            return Integer.compare(rssi2, rssi1);
        });

        // 3. 按信号强度顺序查询区域
//        String sql =
//                "SELECT a.area_name as areaName ,a.id as id " +
//                        "FROM swm_beacon_station bs " +
//                        "INNER JOIN swm_area a ON bs.area = a.id AND a.del_flag = '0' AND a.status = '0' " +
//                        "WHERE bs.beacon_id = ? AND bs.status = '0'";

        for (Map<String, Object> beacon : validBeacons) {
            String mac = (String) beacon.get("MAC");
            int rssi = (int) beacon.get("_rssiInt");

            if (StringUtils.isBlank(mac)) {
                continue;
            }

            try {
//                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, mac.toUpperCase());
//                if (!results.isEmpty()){
//                    return results.get(0);
//                }
                //根据mac地址查询区域信息
                Map<String, Object> result = this.getAreaNameByMac(mac, corpCode);
                return result;


            } catch (Exception e) {
                logger.error("查询 areaName 失败, MAC: {}", mac, e);
            }
        }

        logger.warn("有效蓝牙信标均未在 swm_beacon_station 表中找到区域，deviceId: {}",
                messageData.getDeviceId());
        return null;
    }

    /**
     * 根据mac地址查出信标所属位置
     */
    public String getLocationByMac(String mac, String corpCode) {
        if (StringUtils.isBlank(mac)) {
            return null;
        }
        String beaconCacheKey = corpCode+ SwmRedisKeyConstants.SwmKey.BEACON_MAC_CACHE_KEY;
        SwmBeaconStation beaconStation = (SwmBeaconStation) redisService.hget(beaconCacheKey,mac.toUpperCase() );
        if (ObjectUtils.isEmpty(beaconStation)){
            return null;
        }
        String location = beaconStation.getLocation();
        return location;
    }

    /**
     * 根据mac地址查出信标所属区域
     */
    public Map<String, Object> getAreaNameByMac(String mac, String corpCode) {
        if (StringUtils.isBlank(mac)) {
            return null;
        }

        // 1.先查出信标所属区域
        String beaconCacheKey = corpCode+ SwmRedisKeyConstants.SwmKey.BEACON_MAC_CACHE_KEY;
        SwmBeaconStation beaconStation = (SwmBeaconStation) redisService.hget(beaconCacheKey,mac.toUpperCase() );
        if (ObjectUtils.isEmpty(beaconStation)){
            return null;
        }
        String areaId = beaconStation.getArea();
        if (StringUtils.isBlank(areaId)){
            return null;
        }
        //2.根据区域id查出区域名称
        String areaIdCacheKey = corpCode+ SwmRedisKeyConstants.SwmKey.AREA_ID_CACHE_KEY;
        SwmArea swmArea = (SwmArea) redisService.hget(areaIdCacheKey, areaId);
        if (ObjectUtils.isEmpty(swmArea)){
            return null;
        }
        String areaName = swmArea.getAreaName();
        String id = swmArea.getId();
        Map<String, Object> result = new HashMap<>();
        result.put("areaName", areaName);
        result.put("id", id);

        return result;
    }

}
