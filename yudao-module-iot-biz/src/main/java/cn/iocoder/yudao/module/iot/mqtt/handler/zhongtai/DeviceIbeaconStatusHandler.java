package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.dal.dao.BeaconStationDao;
import cn.iocoder.yudao.module.iot.enums.AlarmConfigEnum;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.dto.zhongtai.ZhongTaiAlarmDto;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.processor.AlarmZeroTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.HazardSourceTcpProcessor;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备蓝牙信息处理器
 * 处理设备蓝牙信息MQTT消息
 *
 * @author wxy
 * @date 2026-04-03
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class DeviceIbeaconStatusHandler implements MqttBusinessHandler {

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;
    @Resource
    private AlarmZeroTcpProcessor alarmZeroTcpProcessor;
    @Resource
    private HazardSourceTcpProcessor hazardSourceTcpProcessor;

    private static final Logger logger = LoggerFactory.getLogger(DeviceIbeaconStatusHandler.class);
    private static final String LOG_PREFIX = "【中泰-MQTT消息-蓝牙信息】- ";

    // 缓存：key = sipAccount，value = 上一次发送时间戳
    private static final ConcurrentHashMap<String, Long> ibeaconStatusMap = new ConcurrentHashMap<>();

    /**
     * 危险源报警专用
     */
    // 缓存每个设备最后一次处理的时间，保证线程安全
    private static final Map<String, Long> LAST_PROCESS_TIME_CACHE = new ConcurrentHashMap<>();
    // 10秒间隔（单位：毫秒）
    private static final long PROCESS_INTERVAL = 10 * 1000L;

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        // 检查主题是否包含 /sensor/ibeacon
        if (!topic.contains(MqttConstants.SENSOR +"/"+ MqttConstants.IBEACON)) {
            return false;
        }
        return true;
    }

    @Override
    public void handle(String topic, MqttMessage message) {
        // 解析消息内容
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.info(LOG_PREFIX+"处理设备蓝牙信息消息: topic={}, payload={}", topic, payload);

        //定位数据实体类
        TcpMessageData messageData = new TcpMessageData();
        //蓝牙解析数据实体类
        IBeaconMessage iBeaconMessage = new IBeaconMessage();

        //蓝牙信标数据列表每个Map包含：MAC, RSSI, TIME
        List<Map<String, Object>> beacons = new ArrayList<>();

        try {

            // 1.先校验设备是否入库
            if (!MqttConstants.verifyDeviceExists(topic, messageData, redisService, deviceCorpMappingCache)){
                return;
            }

            // 2. 解析消息内容
            // 2.1. 解析 MQTT 消息体
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // 2.2. 转换为告警 DTO
            iBeaconMessage = JSONUtil.toBean(payload, IBeaconMessage.class);
            if (iBeaconMessage == null){
                return;
            }
            String deviceId = messageData.getDeviceId();
            // ==============检验数据是否重复发送
            Long now = iBeaconMessage.getTime();
            Long lastTime = ibeaconStatusMap.get(deviceId);
            if (lastTime != null && lastTime.equals(now)){
                logger.info(LOG_PREFIX+"{}数据重复发送，忽略", deviceId);
                return ;
            }else{
                // 添加到缓存
                ibeaconStatusMap.put(deviceId, now);
            }


            // 2.3 拿到 major和minor ，然后去查信标表，得到mac地址
            Long time = iBeaconMessage.getTime();
            for (IBeaconMessage.BeaconData beaconStation : iBeaconMessage.getData()) {
                String redisKey = beaconStation.getMajor() + "_" + beaconStation.getMinor();
                String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
                Object mac = redisService.hget(corpCode + SwmRedisKeyConstants.SwmKey.MAJOR_MINOR_TO_MAC, redisKey);
                Map<String, Object> beacon = new HashMap<>();
                if(mac != null){
                    Integer rssi = beaconStation.getRssi();
                    if (rssi <= -75){
                        continue;
                    }
                    beacon.put("MAC", mac);
                    beacon.put("RSSI", beaconStation.getRssi().toString());
                    beacon.put("TIME", time.toString());
                    beacons.add(beacon);
                }
            }
            messageData.setBluetoothBeacons(beacons);
            messageData.setScanTimestamp(time);

            //拿到电量
            Integer batteryLevel = MqttConstants.getBatteryLevel(deviceId, redisService, deviceCorpMappingCache);
            messageData.setBatteryLevel(batteryLevel);

            // 3. 调用定位算法，处理蓝牙数据
//            if (beacons.size() > 3){
            if (beacons.size() > 1){
                alarmZeroTcpProcessor.process(messageData);
                this.putIBeaconDataToRedis(messageData.getDeviceId(), beacons);
                //判断危险源
                this.hazardSourceProcessor(messageData.getDeviceId(), messageData);
            }
        }catch (Exception e){
            logger.error(LOG_PREFIX+"处理设备蓝牙信息消息异常: topic={}, payload={}", topic, payload, e);
        }


    }

    /**
     * 将蓝牙数据放入redis，有效期60s
     */
    public void putIBeaconDataToRedis(String deviceId, List<Map<String, Object>> beacons){
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
        redisService.hset(corpCode +SwmRedisKeyConstants.SwmKey.ZT_DEVICE_BLE_DATA, deviceId, beacons, 60);
    }

    /**
     * 危险源报警，增加个逻辑，每10s报一次，防止长时间停留在危险源一直报警
     */
    private void hazardSourceProcessor(String deviceId, TcpMessageData tcpData) {
        boolean alarm = MqttConstants.isAlarm(AlarmConfigEnum.WX.getCode(), deviceId, redisService, deviceCorpMappingCache);
        if (alarm) {
            tcpData.setZTDevice(true);
            Long now = tcpData.getScanTimestamp();
            // 获取该设备最后一次处理时间
            long lastProcessTime = LAST_PROCESS_TIME_CACHE.getOrDefault(deviceId, 0L);
            // 判断：距离上次处理是否满10秒
            if (now - lastProcessTime >= PROCESS_INTERVAL) {
                // 更新最后处理时间
                LAST_PROCESS_TIME_CACHE.put(deviceId, now);

                // 核心处理逻辑,危险源报警
                hazardSourceTcpProcessor.process(tcpData, null);
            }
        }
    }


    @Data
    public class IBeaconMessage {
        private String type;
        private Long user;
        private Long time;
        @JsonProperty("helmet_battery")
        private Integer helmetBattery;

        private List<BeaconData> data;

        @Data
        public  class BeaconData {
            private Integer major;
            private Integer minor;
            private Integer battery;
            private Integer rssi;
        }
    }
}