package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.iocoder.yudao.module.iot.config.SpringContextHolder;

import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.AlarmConfigEnum;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.processor.LongTimeStaticTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 璁惧鍦ㄧ嚎鐘舵€佸鐞嗗櫒
 * 澶勭悊璁惧鍦ㄧ嚎鐘舵€丮QTT娑堟伅
 *
 * @author wxy
 * @date 2026-04-03
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class DeviceRunStatusHandler implements MqttBusinessHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRunStatusHandler.class);
    private static final String LOG_PREFIX = "銆愪腑娉?MQTT娑堟伅-璁惧鐘舵€併€? ";


    // 缂撳瓨锛歬ey = sipAccount锛寁alue = 涓婁竴娆″彂閫佹椂闂存埑
    private static final ConcurrentHashMap<String, Long> runStatusMap = new ConcurrentHashMap<>();

    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Resource
    private DeviceSoSAlarmHandler deviceSoSAlarmHandler;
    @Resource
    private LongTimeStaticTcpProcessor longTimeStaticTcpProcessor;

    @Resource
    private RedisService redisService;
    private RedisUtil redisUtil;
    private RedisUtil getRedisUtil() {
        if (redisUtil == null) {
            redisUtil = SpringContextHolder.getBean(RedisUtil.class);
        }
        return redisUtil;
    }


    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        // 妫€鏌ヤ富棰樻槸鍚︿互 /status/online 缁撳熬
        if (!topic.contains(MqttConstants.STATUS +"/"+ MqttConstants.RUN_STATUS)) {
            return false;
        }
        return true;
    }

    @Override
    public void handle(String topic, MqttMessage message){
        // 瑙ｆ瀽娑堟伅鍐呭
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.info(LOG_PREFIX+"澶勭悊璁惧杩愯鐘舵€佹秷鎭? topic={}, payload={}", topic, payload);
        try {
            //瀹氫綅鏁版嵁瀹炰綋绫?
            TcpMessageData messageData = new TcpMessageData();
            DeviceStatusDto deviceStatusDto = null;

            // 1.鍏堟牎楠岃澶囨槸鍚﹀叆搴?
            if (!MqttConstants.verifyDeviceExists(topic, messageData, redisService, deviceTenantMappingCache)){
                return;
            }

            String deviceId = messageData.getDeviceId();

            // 2. 瑙ｆ瀽娑堟伅鍐呭
            // 2.1. 瑙ｆ瀽 MQTT 娑堟伅浣?
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // 2.2. 杞崲涓哄憡璀?DTO
            deviceStatusDto = JSONUtil.toBean(payload, DeviceStatusDto.class);
            if (cn.hutool.core.util.ObjectUtil.isEmpty(deviceStatusDto)){
                return;
            }

            // ==============妫€楠屾暟鎹槸鍚﹂噸澶嶅彂閫?
            Long now = deviceStatusDto.getTime();
            Long lastTime = runStatusMap.get(deviceId);
            if (lastTime != null && lastTime.equals(now)){
                logger.info(LOG_PREFIX+"澶勭悊璁惧杩愯鐘舵€佹秷鎭? 璁惧ID={}锛屾暟鎹噸澶嶅彂閫侊紝蹇界暐", deviceId);
                return ;
            }else{
                // 娣诲姞鍒扮紦瀛?
                runStatusMap.put(deviceId, now);
            }
            //3 鎷垮埌璁惧鐘舵€侊紝鐒跺悗鏀惧叆缂撳瓨
            String deviceStatus = deviceStatusDto.getValue();
            // 杩囨湡鏃堕棿,3鍒嗛挓
            long expirationTime = 60 * 3;
            //闈欓粯,24灏忔椂
            long silentTime = 60*60*24;
            if (StrUtil.isNotEmpty(deviceStatus)){
                if (DeviceStatusDto.online.equals(deviceStatus)){
                    logger.info(LOG_PREFIX+"澶勭悊璁惧涓婄嚎娑堟伅: 璁惧ID={}", deviceId);
                    //璇存槑璁惧瀛樺湪锛屽皢璁惧鍦ㄧ嚎鐘舵€佹斁鍏edis涓?
                    this.getRedisUtil().setZTDeviceOnline(deviceId,expirationTime);
                    //鍒犻櫎闈欓粯缂撳瓨
                    this.removeLongTimeStaticRedisData(deviceId);
                }else if (DeviceStatusDto.offline.equals(deviceStatus)){
                    logger.info(LOG_PREFIX+"澶勭悊璁惧绂荤嚎娑堟伅: 璁惧ID={}", deviceId);
                    //璇存槑璁惧涓嶅瓨鍦紝绉婚櫎redis
                    this.getRedisUtil().removeDeviceOnline(deviceId);
                    //鍒犻櫎闈欓粯缂撳瓨
                    this.removeLongTimeStaticRedisData(deviceId);
                }else if (DeviceStatusDto.nonSilent.equals(deviceStatus)){
                    logger.info(LOG_PREFIX+"澶勭悊璁惧闈為潤榛樻秷鎭? 璁惧ID={}", deviceId);
                    //璇存槑璁惧瀛樺湪锛屽皢璁惧鍦ㄧ嚎鐘舵€佹斁鍏edis涓?骞朵笖瑕佽Е鍙戦潤榛樻姤璀?
                    this.getRedisUtil().refreshDeviceOnlineByTime(deviceId,expirationTime);
                    //鍒犻櫎闈欓粯缂撳瓨
                    this.removeLongTimeStaticRedisData(deviceId);
                }else if (DeviceStatusDto.silent.equals(deviceStatus)){

                    logger.info(LOG_PREFIX+"澶勭悊璁惧闈欓粯娑堟伅: 璁惧ID={}", deviceId);
                    //璇存槑璁惧瀛樺湪锛屽皢璁惧鍦ㄧ嚎鐘舵€佹斁鍏edis涓?骞朵笖瑕佽Е鍙戦潤榛樻姤璀?榛樿缁?灏忔椂鍦ㄧ嚎)
                    this.getRedisUtil().refreshDeviceOnlineByTime(deviceId,silentTime);
                    //鍐欏叆闈欓粯涓撶敤reids
                    this.refreshDeviceInfo(deviceId, payload,silentTime);
                    // 瑙﹀彂闈欓粯鎶ヨ
                    this.longTimeStaticProcessor(deviceId, messageData, payload,deviceStatusDto);
                }
            }
        }catch (Exception e){
            logger.error(LOG_PREFIX+"澶勭悊璁惧杩愯鐘舵€佹秷鎭紓甯? topic={}, payload={}", topic, payload, e);
        }
    }


    /**
     * 闈欓粯鎶ヨ澶勭悊閫昏緫
     */
    private void longTimeStaticProcessor(String deviceId, TcpMessageData messageData,String payload,DeviceStatusDto alarmDTO) {
        logger.info(LOG_PREFIX+"澶勭悊璁惧闈欓粯鎶ヨ娑堟伅: 璁惧ID={}", deviceId);

        //1.浠巖edis鍙栧嚭钃濈墮鏁版嵁锛屽苟涓旀瀯寤簃essageData
        List<Map<String, Object>> beacons = deviceSoSAlarmHandler.getIBeaconDataToRedis(deviceId);
        messageData.setBluetoothBeacons(beacons);
        messageData.setRawMessage(payload);
        //getScanTimestamp
        messageData.setScanTimestamp(alarmDTO.getTime());
        //3.妫€鏌ユ槸鍚﹂渶瑕佹姤璀?
        boolean alarm = MqttConstants.isAlarm(AlarmConfigEnum.CSJ.getCode(), deviceId,redisService, deviceTenantMappingCache);
        if (alarm){
            //杩欓噷璁剧疆涓嘲璁惧鏍囪瘑
            messageData.setZTDevice(true);
            longTimeStaticTcpProcessor.process(messageData, null);
            logger.info(LOG_PREFIX+"澶勭悊璁惧闈欓粯鎶ヨ娑堟伅: 璁惧ID={}锛屽凡澶勭悊", deviceId);
        }
    }

    /**
     * 鍙敤浜庤澶囬潤榛樼姸鎬侊紝鍥犱负闈欓粯鐘舵€佽澶囦笉涓婃姤鏁版嵁锛屾墍浠ラ渶瑕佸畾鏃朵笂鎶ユ暟鎹?
     * 涓嘲甯藉瓙-璁惧璇︾粏淇℃伅,key涓鸿澶嘔D锛寁alue涓鸿澶囪缁嗕俊鎭紙TcpMessageData锛夛紝涓昏鐢ㄤ簬璁惧闈欓粯鐘舵€侊紝璁惧涓嶄笂鎶?
     * 鏁版嵁锛岄偅涔堝氨闇€瑕佺敤杩欎釜鏉ュ畾鏃朵笂鎶ユ暟鎹?
     */
    private void refreshDeviceInfo(String deviceId, String payload, long time) {
        TcpMessageData messageData = new TcpMessageData();
        //1.鍏堣幏鍙栬澶囩殑钃濈墮鏁版嵁
        List<Map<String, Object>> beacons = deviceSoSAlarmHandler.getIBeaconDataToRedis(deviceId);
        messageData.setRawMessage(payload);
        //getScanTimestamp
        messageData.setScanTimestamp(System.currentTimeMillis());
        messageData.setBluetoothBeacons(beacons);

        //2.鎷垮埌鐢甸噺
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
        Integer batteryLevel = (Integer) redisService.hget(tenantKey + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_BATTERY, deviceId);
        messageData.setBatteryLevel(batteryLevel);
        messageData.setDeviceId(deviceId);

        //3.鏀惧叆redis
        redisService.hset(tenantKey + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_MESSAGE_DATA, deviceId, messageData,time);
    }

    /**
     * 鍒犻櫎闈欓粯鐘舵€佷笅鐨剅edis鏁版嵁
     */
    public void removeLongTimeStaticRedisData(String deviceId) {
        redisService.hdel(deviceTenantMappingCache.getTenantKey(deviceId) + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_MESSAGE_DATA, deviceId);
    }


    /**
     * 璁惧杩愯鐘舵€丏TO
     */
    @Data
    public class DeviceStatusDto {
        //璁惧鏍囪瘑
        private String sip;
        //鏃堕棿鎴筹紙姣锛?
        private Long time;
        //绫诲瀷锛歳un_status-杩愯鐘舵€?
        private String type;
        //鐘舵€佸€硷細value 鍦ㄧ嚎鐘舵€佸€?  0绂荤嚎	1鍦ㄧ嚎	3闈為潤榛?4闈欓粯
        private String value;

        //鍦ㄧ嚎
        private final  static String online = "1";
        //绂荤嚎
        private final  static String offline = "0";
        //闈為潤榛?锛堥厤鍚堥潤榛樹娇鐢紝鍏堝彂閫侀潤榛橈紝鎺ヨЕ闈欓粯鍒欎細涓嬪彂涓€涓?锛?
        private final  static String nonSilent = "3";
        //闈欓粯
        private final  static String silent = "4";
    }
}

