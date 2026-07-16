package cn.iocoder.yudao.module.iot.tcp.parser;

import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * TCP消息解析器
 * 解析TCP协议格式的消息数据
 *
 * 消息格式示例：
 * $13B,S,4015830001,N,0000.0000,E,00000.0000,00000,00000,VER2,00,99.0,060611-000000,19,0,42,023,001,090,
 * 80ECCCD0A8E4,-71,011,80ECCCD0A8EB,-64,011,80ECCCD241A2,-66,011,80ECCCD2419B,-65,011,80ECCCD2419C,-68,011,
 * 80ECCCD09D72,-72,011,80ECCCD0A004,-61,011,80ECCCD241E2,-72,011,80ECCCD241C6,-61,011,80ECCCD241A3,-69,011,
 * 413,92,#
 *
 * @author Shawn
 * @date 2026-03-25
 */
@Component
public class TcpMessageParser {

    private static final String GPS_UTC_PATTERN = "ddMMyy-HHmmss";
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;

    private static final Logger logger = LoggerFactory.getLogger(TcpMessageParser.class);

    /**
     * 解析TCP消息
     *
     * @param rawMessage 原始TCP消息
     * @return 解析后的数据对象，解析失败返回null
     */
    public TcpMessageData parseMessage(String rawMessage) {
        if (StringUtils.isBlank(rawMessage)) {
            logger.warn("TCP消息为空，无法解析");
            return null;
        }

        try {
            // 检查消息格式
            if (!rawMessage.startsWith("$") || !rawMessage.endsWith("#")) {
                logger.warn("TCP消息格式不正确，必须以$开头#结尾: {}", rawMessage);
                return null;
            }

            // 去掉开头的$和结尾的#，然后按逗号分割
            String content = rawMessage.substring(1, rawMessage.length() - 1);
            String[] parts = content.split(",", -1); // -1保留空字符串

            if (parts.length < 20) {
                logger.warn("TCP消息字段数量不足，期望至少20个字段，实际: {}", parts.length);
                return null;
            }

            TcpMessageData messageData = new TcpMessageData(rawMessage);

            // 解析基础字段
            parseBasicFields(messageData, parts);

            // 解析蓝牙信标数据
            parseBluetoothBeacons(messageData, parts);

            // 解析电池信息
            parseBatteryInfo(messageData, parts);

            // 计算扫描时间戳
            calculateScanTimestamp(messageData);

            //绑定设备的租户信息
            bindDeviceDbInfo(messageData);

            logger.debug("TCP消息解析成功: {}", messageData);
            return messageData;

        } catch (Exception e) {
            logger.error("解析TCP消息时发生异常: {}", rawMessage, e);
            return null;
        }
    }

    /**
     * 解析基础字段
     */
    private void parseBasicFields(TcpMessageData messageData, String[] parts) {
        // 位置0: 数据长度
        messageData.setDataLength(parts[0]);

        // 位置1: 命令类型
        messageData.setCommandType(parts[1]);

        // 位置2: 设备编号
        messageData.setDeviceId(parts[2]);

        // 位置3-6: 协议原始经纬度信息（方向位 + ddmm.mmmm / dddmm.mmmm）
        parseGpsRawFields(messageData, parts);

        // 位置12: GPS/北斗 UTC时间，专门给经纬度像素链使用。
        parseGpsUtcField(messageData, parts);

        // 位置13: GSM信号强度
        if (parts.length > 13 && StringUtils.isNotBlank(parts[13])) {
            try {
                messageData.setGsmSignalStrength(Integer.parseInt(parts[13]));
            } catch (NumberFormatException e) {
                logger.warn("GSM信号强度解析失败: {}", parts[13]);
            }
        }

        // 位置14: 报警值
        if (parts.length > 14 && StringUtils.isNotBlank(parts[14])) {
            try {
                String alarmStr = parts[14].trim();
                // 判断是否为16进制格式
                if (alarmStr.matches("[0-9A-Fa-f]+") && alarmStr.length() <= 4) {
                    // 16进制格式（如 A0）
                    messageData.setAlarmValue(Integer.parseInt(alarmStr, 16));
                    logger.info("报警值解析（16进制）: {} -> {}", alarmStr, Integer.parseInt(alarmStr, 16));
                } else {
                    // 10进制格式
                    messageData.setAlarmValue(Integer.parseInt(alarmStr));
                    logger.info("报警值解析（10进制）: {}", alarmStr);
                }
            } catch (NumberFormatException e) {
                logger.warn("报警值解析失败: {}", parts[14]);
            }
        }

        // 位置15: 状态值
        if (parts.length > 15 && StringUtils.isNotBlank(parts[15])) {
            try {
                messageData.setStatusValue(Integer.parseInt(parts[15]));
            } catch (NumberFormatException e) {
                logger.warn("状态值解析失败: {}", parts[15]);
            }
        }

        // 位置16-18: 三轴加速度
        parseAcceleration(messageData, parts);
    }

    /**
     * 解析协议中的原始经纬度字段
     * 字段定义：
     * 3=north_south, 4=latitude, 5=east_west, 6=longitude
     */
    private void parseGpsRawFields(TcpMessageData messageData, String[] parts) {
        if (parts.length <= 6) {
            logger.warn("TCP消息经纬度字段不足，设备ID: {}, 字段数: {}", messageData.getDeviceId(), parts.length);
            return;
        }

        String northSouth = safeTrim(parts[3]);
        String latitude = safeTrim(parts[4]);
        String eastWest = safeTrim(parts[5]);
        String longitude = safeTrim(parts[6]);

        messageData.setNorthSouth(northSouth);
        messageData.setLatitude(latitude);
        messageData.setEastWest(eastWest);
        messageData.setLongitude(longitude);

        logger.info("解析协议经纬度完成, 设备ID: {}, north_south: {}, latitude: {}, east_west: {}, longitude: {}",
                messageData.getDeviceId(), northSouth, latitude, eastWest, longitude);
    }

    /**
     * 解析协议中的GPS/北斗 UTC时间。
     * 协议格式固定为ddMMyy-HHmmss，这里按UTC时区解析成绝对时间戳。
     */
    private void parseGpsUtcField(TcpMessageData messageData, String[] parts) {
        if (parts.length <= 12) {
            return;
        }

        String gpsUtcRaw = safeTrim(parts[12]);
        messageData.setGpsUtcRaw(gpsUtcRaw);
        if (StringUtils.isBlank(gpsUtcRaw)) {
            logger.info("设备[{}]协议GPS UTC为空，保留空值等待像素链路兜底", messageData.getDeviceId());
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(GPS_UTC_PATTERN);
            sdf.setLenient(false);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date gpsUtcDate = sdf.parse(gpsUtcRaw);
            messageData.setGpsUtcTimestamp(gpsUtcDate.getTime());
            logger.info("解析协议GPS UTC成功, deviceId={}, gpsUtcRaw={}, gpsUtcTimestamp={}",
                    messageData.getDeviceId(), gpsUtcRaw, gpsUtcDate.getTime());
        } catch (ParseException ex) {
            logger.warn("解析协议GPS UTC失败, deviceId={}, gpsUtcRaw={}", messageData.getDeviceId(), gpsUtcRaw, ex);
        }
    }

    /**
     * 安全去除字符串前后空白，避免空指针
     */
    private String safeTrim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    /**
     * 解析三轴加速度
     */
    private void parseAcceleration(TcpMessageData messageData, String[] parts) {
        try {
            if (parts.length > 18) {
                int[] acceleration = new int[3];
                for (int i = 0; i < 3; i++) {
                    if (StringUtils.isNotBlank(parts[16 + i])) {
                        acceleration[i] = Integer.parseInt(parts[16 + i]);
                    }
                }
                messageData.setAcceleration(acceleration);
            }
        } catch (NumberFormatException e) {
            logger.warn("三轴加速度解析失败: {}, {}, {}",
                parts.length > 16 ? parts[16] : "",
                parts.length > 17 ? parts[17] : "",
                parts.length > 18 ? parts[18] : "");
        }
    }

    /**
     * 解析蓝牙信标数据
     * 从位置19开始，每3个字段为一组：MAC地址, RSSI, 扫描时间差
     */
    private void parseBluetoothBeacons(TcpMessageData messageData, String[] parts) {
        List<Map<String, Object>> beacons = new ArrayList<>();
        Integer firstTimeDiff = null;

        // 从位置19开始，每3个字段为一组，最多10组
        for (int i = 19; i < Math.min(parts.length - 2, 49) && i + 2 < parts.length; i += 3) {
            String mac = parts[i];
            String rssi = parts[i + 1];
            String timeHex = parts[i + 2];

            // 跳过空数据
            if (StringUtils.isBlank(mac) || StringUtils.isBlank(rssi) || StringUtils.isBlank(timeHex)) {
                continue;
            }

            // 跳过全是逗号的情况
            if (",".equals(mac.trim()) || ",".equals(rssi.trim()) || ",".equals(timeHex.trim())) {
                continue;
            }

            try {
                Map<String, Object> beacon = new HashMap<>();
                beacon.put("MAC", mac);
                beacon.put("RSSI", rssi);
                beacon.put("TIME", timeHex);

                // 记录第一个有效的时间差用于计算扫描时间戳
                if (firstTimeDiff == null && StringUtils.isNotBlank(timeHex)) {
                    firstTimeDiff = Integer.parseInt(timeHex, 16); // 十六进制转十进制
                }

                beacons.add(beacon);

            } catch (Exception e) {
                logger.warn("解析蓝牙信标数据失败: MAC={}, RSSI={}, TIME={}", mac, rssi, timeHex, e);
            }
        }

        messageData.setBluetoothBeacons(beacons);
        messageData.setScanTimeDiff(firstTimeDiff);

        logger.info("解析到 {} 个有效蓝牙信标，扫描时间差: {} 秒", beacons.size(), firstTimeDiff);
    }

    /**
     * 解析电池信息
     */
    private void parseBatteryInfo(TcpMessageData messageData, String[] parts) {
        // 电池电压在倒数第三个位置（因为末尾有空字符串）
        if (parts.length >= 3) {
            String voltageStr = parts[parts.length - 3];
            if (StringUtils.isNotBlank(voltageStr)) {
                try {
                    messageData.setBatteryVoltage(Integer.parseInt(voltageStr));
                } catch (NumberFormatException e) {
                    logger.warn("电池电压解析失败: {}", voltageStr);
                }
            }
        }

        // 电池电量在倒数第二个位置（因为末尾有空字符串）
        if (parts.length >= 2) {
            String levelStr = parts[parts.length - 2];
            if (StringUtils.isNotBlank(levelStr)) {
                try {
                    messageData.setBatteryLevel(Integer.parseInt(levelStr));
                } catch (NumberFormatException e) {
                    logger.warn("电池电量解析失败: {}", levelStr);
                }
            }
        }
    }

    /**
     * 计算扫描时间戳
     * 扫描时间 = 当前时间 - 扫描时间差
     */
    private void calculateScanTimestamp(TcpMessageData messageData) {
        if (messageData.getScanTimeDiff() != null) {
            long currentTime = System.currentTimeMillis();
            long scanTime = currentTime - (messageData.getScanTimeDiff() * 1000L);
            messageData.setScanTimestamp(scanTime);

            logger.info("计算扫描时间戳: 当前时间={}, 时间差={}秒, 扫描时间={}",
                currentTime, messageData.getScanTimeDiff(), scanTime);
        }
    }


    /**
     * 绑定设备库信息
     */
    private void bindDeviceDbInfo(TcpMessageData messageData) {
        String deviceId = messageData.getDeviceId();
        if (deviceId == null){
            return;
        }
        String dbName = deviceTenantMappingCache.getDbName(deviceId);
        messageData.setDbName(dbName);
        logger.debug("绑定设备库信息: 设备ID={}, 库名={}", deviceId, dbName);
    }
}
