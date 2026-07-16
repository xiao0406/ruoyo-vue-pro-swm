package cn.iocoder.yudao.module.iot.tcp.service;

import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
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
 * TCP淇℃爣浣嶇疆鏈嶅姟
 * 鎻愪緵浠庤摑鐗欎俊鏍囦腑鑾峰彇浣嶇疆鍜屽尯鍩熶俊鎭殑閫氱敤鏈嶅姟
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
    private DeviceTenantMappingCache deviceTenantMappingCache;

    /**
     * 浠庤摑鐗欎俊鏍囦腑鑾峰彇浣嶇疆淇℃伅
     * 鎸変俊鍙峰己搴︿粠寮哄埌寮变緷娆℃煡璇紝鐩村埌鎵惧埌鍦╯wm_beacon_station琛ㄤ腑瀛樺湪鐨勪俊鏍?
     *
     * @param messageData TCP娑堟伅鏁版嵁
     * @return 浣嶇疆淇℃伅锛屽鏋滄湭鎵惧埌杩斿洖绌哄瓧绗︿覆
     */
    public String getLocationFromBeacons(TcpMessageData messageData) {
        if (messageData == null) {
            return "";
        }
        String deviceId = messageData.getDeviceId();
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);

        List<Map<String, Object>> bluetoothBeacons = messageData.getBluetoothBeacons();
        if (bluetoothBeacons == null || bluetoothBeacons.isEmpty()) {
            return "";
        }

        // 灏嗕俊鏍囨寜RSSI鍊兼帓搴忥紙浠庡己鍒板急锛?
        List<Map<String, Object>> sortedBeacons = new ArrayList<>(bluetoothBeacons);
        sortedBeacons.sort((b1, b2) -> {
            try {
                String rssi1Str = (String) b1.get("RSSI");
                String rssi2Str = (String) b2.get("RSSI");
                int rssi1 = Integer.parseInt(rssi1Str);
                int rssi2 = Integer.parseInt(rssi2Str);
                return Integer.compare(rssi2, rssi1); // 闄嶅簭鎺掑垪
            } catch (Exception e) {
                return 0;
            }
        });

        // 鎸変俊鍙峰己搴﹂『搴忔煡璇紝鐩村埌鎵惧埌瀛樺湪鐨勪俊鏍?
//        String sql = "SELECT location FROM swm_beacon_station WHERE beacon_id = ? AND status = '0'";

        for (Map<String, Object> beacon : sortedBeacons) {
            String mac = (String) beacon.get("MAC");
            String rssiStr = (String) beacon.get("RSSI");

            if (StringUtils.isNotBlank(mac)) {
                try {
//                    List<String> results = jdbcTemplate.queryForList(sql, String.class, mac.toUpperCase());
                    //鏌ヨ浣嶇疆鍚嶇О
                    String location = this.getLocationByMac(mac, tenantKey);
                    if (!location.isEmpty()) {
                        logger.info("鎵惧埌淇℃爣浣嶇疆: MAC={}, RSSI={}, location={}",
                            mac, rssiStr, location);
                        return location;
                    } else {
                        logger.debug("淇℃爣鏈湪鏁版嵁搴撲腑鎵惧埌: MAC={}, RSSI={}", mac, rssiStr);
                    }
                } catch (Exception e) {
                    logger.error("鏌ヨlocation澶辫触, MAC: {}", mac, e);
                }
            }
        }

        logger.warn("鎵€鏈夎摑鐗欎俊鏍囬兘鏈湪swm_beacon_station琛ㄤ腑鎵惧埌锛宒eviceId: {}", messageData.getDeviceId());
        return "";
    }

    /**
     * 浠庤摑鐗欎俊鏍囦腑鑾峰彇鍖哄煙鍚嶇О
     * 鎸変俊鍙峰己搴︿粠寮哄埌寮变緷娆℃煡璇紝鐩村埌鎵惧埌鍦╯wm_beacon_station琛ㄤ腑瀛樺湪鐨勪俊鏍?
     *
     * @param messageData TCP娑堟伅鏁版嵁
     * @return 鍖哄煙鍚嶇О锛屽鏋滄湭鎵惧埌杩斿洖绌哄瓧绗︿覆
     */
    public String getAreaNameFromBeacons(TcpMessageData messageData) {
        if (messageData == null) {
            return "";
        }

        String deviceId = messageData.getDeviceId();
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);

        List<Map<String, Object>> bluetoothBeacons = messageData.getBluetoothBeacons();
        if (bluetoothBeacons == null || bluetoothBeacons.isEmpty()) {
            return "";
        }

        // 灏嗕俊鏍囨寜RSSI鍊兼帓搴忥紙浠庡己鍒板急锛?
        List<Map<String, Object>> sortedBeacons = new ArrayList<>(bluetoothBeacons);
        sortedBeacons.sort((b1, b2) -> {
            try {
                String rssi1Str = (String) b1.get("RSSI");
                String rssi2Str = (String) b2.get("RSSI");
                int rssi1 = Integer.parseInt(rssi1Str);
                int rssi2 = Integer.parseInt(rssi2Str);
                return Integer.compare(rssi2, rssi1); // 闄嶅簭鎺掑垪
            } catch (Exception e) {
                return 0;
            }
        });

        // 鎸変俊鍙峰己搴﹂『搴忔煡璇紝鐩村埌鎵惧埌瀛樺湪鐨勪俊鏍?
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
                    //鏍规嵁mac鍦板潃鏌ヨ鍖哄煙淇℃伅
                    Map<String, Object> result = this.getAreaNameByMac(mac, tenantKey);
                    String areaName = null;
                    if (result != null){
                        areaName = (String)result.get("areaName");
                    }

                    if (!areaName.isEmpty() ) {
                        logger.info("鎵惧埌淇℃爣鍖哄煙: MAC={}, RSSI={}, areaName={}",
                            mac, rssiStr, areaName);
                        return areaName;
                    } else {
                        logger.debug("淇℃爣鏈湪鏁版嵁搴撲腑鎵惧埌鍖哄煙: MAC={}, RSSI={}", mac, rssiStr);
                    }
                } catch (Exception e) {
                    logger.error("鏌ヨareaName澶辫触, MAC: {}", mac, e);
                }
            }
        }

        logger.warn("鎵€鏈夎摑鐗欎俊鏍囬兘鏈湪swm_beacon_station琛ㄤ腑鎵惧埌鍖哄煙锛宒eviceId: {}", messageData.getDeviceId());
        return "";
    }

    /**
     * 灏哅AC鍦板潃鏍煎紡鍖栦负甯﹀啋鍙风殑鏍煎紡
     *
     * @param mac 鏃犲啋鍙风殑MAC鍦板潃锛堝锛?0ECCCD0BCF6锛?
     * @return 甯﹀啋鍙风殑MAC鍦板潃锛堝锛?0:EC:CC:D0:BC:F6锛?
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
     * 浠庤摑鐗欎俊鏍囦腑鑾峰彇鍖哄煙鍚嶇О
     * 杩囨护 RSSI 鈮?-80 鐨勪俊鏍囷紝鎸変俊鍙峰己搴︿粠寮哄埌寮辨煡璇?
     */
    public Map<String,Object> getAreaNameFromBeaconsFilter(TcpMessageData messageData) {
        if (messageData == null) {
            return null;
        }
        String deviceId = messageData.getDeviceId();
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);

        List<Map<String, Object>> bluetoothBeacons = messageData.getBluetoothBeacons();
        if (bluetoothBeacons == null || bluetoothBeacons.isEmpty()) {
            return null;
        }

        // 1. 杩囨护 RSSI > -80 涓斿彲瑙ｆ瀽鐨勪俊鏍?
        List<Map<String, Object>> validBeacons = new ArrayList<>();
        for (Map<String, Object> beacon : bluetoothBeacons) {
            Object rssiObj = beacon.get("RSSI");
            if (rssiObj == null) {
                continue;
            }

            try {
                int rssi = Integer.parseInt(rssiObj.toString());
                if (rssi > -80) {
                    beacon.put("_rssiInt", rssi); // 缂撳瓨瑙ｆ瀽缁撴灉
                    validBeacons.add(beacon);
                }
            } catch (Exception e) {
                logger.debug("闈炴硶 RSSI锛屽拷鐣ヨ淇℃爣: {}", beacon);
            }
        }

        if (validBeacons.isEmpty()) {
            logger.warn("鎵€鏈夎摑鐗欎俊鏍?RSSI 鈮?-80锛岃杩囨护锛宒eviceId: {}", messageData.getDeviceId());
            return null;
        }

        // 2. 鎸?RSSI 浠庡己鍒板急鎺掑簭
        validBeacons.sort((b1, b2) -> {
            int rssi1 = (int) b1.get("_rssiInt");
            int rssi2 = (int) b2.get("_rssiInt");
            return Integer.compare(rssi2, rssi1);
        });

        // 3. 鎸変俊鍙峰己搴﹂『搴忔煡璇㈠尯鍩?
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
                //鏍规嵁mac鍦板潃鏌ヨ鍖哄煙淇℃伅
                Map<String, Object> result = this.getAreaNameByMac(mac, tenantKey);
                return result;


            } catch (Exception e) {
                logger.error("鏌ヨ areaName 澶辫触, MAC: {}", mac, e);
            }
        }

        logger.warn("鏈夋晥钃濈墮淇℃爣鍧囨湭鍦?swm_beacon_station 琛ㄤ腑鎵惧埌鍖哄煙锛宒eviceId: {}",
                messageData.getDeviceId());
        return null;
    }

    /**
     * 鏍规嵁mac鍦板潃鏌ュ嚭淇℃爣鎵€灞炰綅缃?
     */
    public String getLocationByMac(String mac, String tenantKey) {
        if (StringUtils.isBlank(mac)) {
            return null;
        }
        String beaconCacheKey = tenantKey+ SwmRedisKeyConstants.SwmKey.BEACON_MAC_CACHE_KEY;
        SwmBeaconStation beaconStation = (SwmBeaconStation) redisService.hget(beaconCacheKey,mac.toUpperCase() );
        if (ObjectUtils.isEmpty(beaconStation)){
            return null;
        }
        String location = beaconStation.getLocation();
        return location;
    }

    /**
     * 鏍规嵁mac鍦板潃鏌ュ嚭淇℃爣鎵€灞炲尯鍩?
     */
    public Map<String, Object> getAreaNameByMac(String mac, String tenantKey) {
        if (StringUtils.isBlank(mac)) {
            return null;
        }

        // 1.鍏堟煡鍑轰俊鏍囨墍灞炲尯鍩?
        String beaconCacheKey = tenantKey+ SwmRedisKeyConstants.SwmKey.BEACON_MAC_CACHE_KEY;
        SwmBeaconStation beaconStation = (SwmBeaconStation) redisService.hget(beaconCacheKey,mac.toUpperCase() );
        if (ObjectUtils.isEmpty(beaconStation)){
            return null;
        }
        String areaId = beaconStation.getArea();
        if (StringUtils.isBlank(areaId)){
            return null;
        }
        //2.鏍规嵁鍖哄煙id鏌ュ嚭鍖哄煙鍚嶇О
        String areaIdCacheKey = tenantKey+ SwmRedisKeyConstants.SwmKey.AREA_ID_CACHE_KEY;
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

