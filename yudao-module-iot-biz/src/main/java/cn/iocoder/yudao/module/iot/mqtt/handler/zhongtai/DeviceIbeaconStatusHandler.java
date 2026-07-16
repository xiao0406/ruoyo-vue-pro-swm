package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
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
 * 璁惧钃濈墮淇℃伅澶勭悊鍣?
 * 澶勭悊璁惧钃濈墮淇℃伅MQTT娑堟伅
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
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Resource
    private AlarmZeroTcpProcessor alarmZeroTcpProcessor;
    @Resource
    private HazardSourceTcpProcessor hazardSourceTcpProcessor;

    private static final Logger logger = LoggerFactory.getLogger(DeviceIbeaconStatusHandler.class);
    private static final String LOG_PREFIX = "銆愪腑娉?MQTT娑堟伅-钃濈墮淇℃伅銆? ";

    // 缂撳瓨锛歬ey = sipAccount锛寁alue = 涓婁竴娆″彂閫佹椂闂存埑
    private static final ConcurrentHashMap<String, Long> ibeaconStatusMap = new ConcurrentHashMap<>();

    /**
     * 鍗遍櫓婧愭姤璀︿笓鐢?
     */
    // 缂撳瓨姣忎釜璁惧鏈€鍚庝竴娆″鐞嗙殑鏃堕棿锛屼繚璇佺嚎绋嬪畨鍏?
    private static final Map<String, Long> LAST_PROCESS_TIME_CACHE = new ConcurrentHashMap<>();
    // 10绉掗棿闅旓紙鍗曚綅锛氭绉掞級
    private static final long PROCESS_INTERVAL = 10 * 1000L;

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        // 妫€鏌ヤ富棰樻槸鍚﹀寘鍚?/sensor/ibeacon
        if (!topic.contains(MqttConstants.SENSOR +"/"+ MqttConstants.IBEACON)) {
            return false;
        }
        return true;
    }

    @Override
    public void handle(String topic, MqttMessage message) {
        // 瑙ｆ瀽娑堟伅鍐呭
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.info(LOG_PREFIX+"澶勭悊璁惧钃濈墮淇℃伅娑堟伅: topic={}, payload={}", topic, payload);

        //瀹氫綅鏁版嵁瀹炰綋绫?
        TcpMessageData messageData = new TcpMessageData();
        //钃濈墮瑙ｆ瀽鏁版嵁瀹炰綋绫?
        IBeaconMessage iBeaconMessage = new IBeaconMessage();

        //钃濈墮淇℃爣鏁版嵁鍒楄〃姣忎釜Map鍖呭惈锛歁AC, RSSI, TIME
        List<Map<String, Object>> beacons = new ArrayList<>();

        try {

            // 1.鍏堟牎楠岃澶囨槸鍚﹀叆搴?
            if (!MqttConstants.verifyDeviceExists(topic, messageData, redisService, deviceTenantMappingCache)){
                return;
            }

            // 2. 瑙ｆ瀽娑堟伅鍐呭
            // 2.1. 瑙ｆ瀽 MQTT 娑堟伅浣?
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // 2.2. 杞崲涓哄憡璀?DTO
            iBeaconMessage = JSONUtil.toBean(payload, IBeaconMessage.class);
            if (iBeaconMessage == null){
                return;
            }
            String deviceId = messageData.getDeviceId();
            // ==============妫€楠屾暟鎹槸鍚﹂噸澶嶅彂閫?
            Long now = iBeaconMessage.getTime();
            Long lastTime = ibeaconStatusMap.get(deviceId);
            if (lastTime != null && lastTime.equals(now)){
                logger.info(LOG_PREFIX+"{}鏁版嵁閲嶅鍙戦€侊紝蹇界暐", deviceId);
                return ;
            }else{
                // 娣诲姞鍒扮紦瀛?
                ibeaconStatusMap.put(deviceId, now);
            }


            // 2.3 鎷垮埌 major鍜宮inor 锛岀劧鍚庡幓鏌ヤ俊鏍囪〃锛屽緱鍒癿ac鍦板潃
            Long time = iBeaconMessage.getTime();
            for (IBeaconMessage.BeaconData beaconStation : iBeaconMessage.getData()) {
                String redisKey = beaconStation.getMajor() + "_" + beaconStation.getMinor();
                String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
                Object mac = redisService.hget(tenantKey + SwmRedisKeyConstants.SwmKey.MAJOR_MINOR_TO_MAC, redisKey);
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

            //鎷垮埌鐢甸噺
            Integer batteryLevel = MqttConstants.getBatteryLevel(deviceId, redisService, deviceTenantMappingCache);
            messageData.setBatteryLevel(batteryLevel);

            // 3. 璋冪敤瀹氫綅绠楁硶锛屽鐞嗚摑鐗欐暟鎹?
//            if (beacons.size() > 3){
            if (beacons.size() > 1){
                alarmZeroTcpProcessor.process(messageData);
                this.putIBeaconDataToRedis(messageData.getDeviceId(), beacons);
                //鍒ゆ柇鍗遍櫓婧?
                this.hazardSourceProcessor(messageData.getDeviceId(), messageData);
            }
        }catch (Exception e){
            logger.error(LOG_PREFIX+"澶勭悊璁惧钃濈墮淇℃伅娑堟伅寮傚父: topic={}, payload={}", topic, payload, e);
        }


    }

    /**
     * 灏嗚摑鐗欐暟鎹斁鍏edis锛屾湁鏁堟湡60s
     */
    public void putIBeaconDataToRedis(String deviceId, List<Map<String, Object>> beacons){
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
        redisService.hset(tenantKey +SwmRedisKeyConstants.SwmKey.ZT_DEVICE_BLE_DATA, deviceId, beacons, 60);
    }

    /**
     * 鍗遍櫓婧愭姤璀︼紝澧炲姞涓€昏緫锛屾瘡10s鎶ヤ竴娆★紝闃叉闀挎椂闂村仠鐣欏湪鍗遍櫓婧愪竴鐩存姤璀?
     */
    private void hazardSourceProcessor(String deviceId, TcpMessageData tcpData) {
        boolean alarm = MqttConstants.isAlarm(AlarmConfigEnum.WX.getCode(), deviceId, redisService, deviceTenantMappingCache);
        if (alarm) {
            tcpData.setZTDevice(true);
            Long now = tcpData.getScanTimestamp();
            // 鑾峰彇璇ヨ澶囨渶鍚庝竴娆″鐞嗘椂闂?
            long lastProcessTime = LAST_PROCESS_TIME_CACHE.getOrDefault(deviceId, 0L);
            // 鍒ゆ柇锛氳窛绂讳笂娆″鐞嗘槸鍚︽弧10绉?
            if (now - lastProcessTime >= PROCESS_INTERVAL) {
                // 鏇存柊鏈€鍚庡鐞嗘椂闂?
                LAST_PROCESS_TIME_CACHE.put(deviceId, now);

                // 鏍稿績澶勭悊閫昏緫,鍗遍櫓婧愭姤璀?
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
