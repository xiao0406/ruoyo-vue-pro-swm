package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
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
 * 设备在线状态处理器
 * 处理设备在线状态MQTT消息
 *
 * @author wxy
 * @date 2026-04-03
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class DeviceRunStatusHandler implements MqttBusinessHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRunStatusHandler.class);
    private static final String LOG_PREFIX = "【中泰-MQTT消息-设备状态】- ";


    // 缓存：key = sipAccount，value = 上一次发送时间戳
    private static final ConcurrentHashMap<String, Long> runStatusMap = new ConcurrentHashMap<>();

    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;
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
        // 检查主题是否以 /status/online 结尾
        if (!topic.contains(MqttConstants.STATUS +"/"+ MqttConstants.RUN_STATUS)) {
            return false;
        }
        return true;
    }

    @Override
    public void handle(String topic, MqttMessage message){
        // 解析消息内容
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.info(LOG_PREFIX+"处理设备运行状态消息: topic={}, payload={}", topic, payload);
        try {
            //定位数据实体类
            TcpMessageData messageData = new TcpMessageData();
            DeviceStatusDto deviceStatusDto = null;

            // 1.先校验设备是否入库
            if (!MqttConstants.verifyDeviceExists(topic, messageData, redisService, deviceCorpMappingCache)){
                return;
            }

            String deviceId = messageData.getDeviceId();

            // 2. 解析消息内容
            // 2.1. 解析 MQTT 消息体
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // 2.2. 转换为告警 DTO
            deviceStatusDto = JSONUtil.toBean(payload, DeviceStatusDto.class);
            if (cn.hutool.core.util.ObjectUtil.isEmpty(deviceStatusDto)){
                return;
            }

            // ==============检验数据是否重复发送
            Long now = deviceStatusDto.getTime();
            Long lastTime = runStatusMap.get(deviceId);
            if (lastTime != null && lastTime.equals(now)){
                logger.info(LOG_PREFIX+"处理设备运行状态消息: 设备ID={}，数据重复发送，忽略", deviceId);
                return ;
            }else{
                // 添加到缓存
                runStatusMap.put(deviceId, now);
            }
            //3 拿到设备状态，然后放入缓存
            String deviceStatus = deviceStatusDto.getValue();
            // 过期时间,3分钟
            long expirationTime = 60 * 3;
            //静默,24小时
            long silentTime = 60*60*24;
            if (StrUtil.isNotEmpty(deviceStatus)){
                if (DeviceStatusDto.online.equals(deviceStatus)){
                    logger.info(LOG_PREFIX+"处理设备上线消息: 设备ID={}", deviceId);
                    //说明设备存在，将设备在线状态放入redis中
                    this.getRedisUtil().setZTDeviceOnline(deviceId,expirationTime);
                    //删除静默缓存
                    this.removeLongTimeStaticRedisData(deviceId);
                }else if (DeviceStatusDto.offline.equals(deviceStatus)){
                    logger.info(LOG_PREFIX+"处理设备离线消息: 设备ID={}", deviceId);
                    //说明设备不存在，移除redis
                    this.getRedisUtil().removeDeviceOnline(deviceId);
                    //删除静默缓存
                    this.removeLongTimeStaticRedisData(deviceId);
                }else if (DeviceStatusDto.nonSilent.equals(deviceStatus)){
                    logger.info(LOG_PREFIX+"处理设备非静默消息: 设备ID={}", deviceId);
                    //说明设备存在，将设备在线状态放入redis中,并且要触发静默报警
                    this.getRedisUtil().refreshDeviceOnlineByTime(deviceId,expirationTime);
                    //删除静默缓存
                    this.removeLongTimeStaticRedisData(deviceId);
                }else if (DeviceStatusDto.silent.equals(deviceStatus)){

                    logger.info(LOG_PREFIX+"处理设备静默消息: 设备ID={}", deviceId);
                    //说明设备存在，将设备在线状态放入redis中,并且要触发静默报警(默认给4小时在线)
                    this.getRedisUtil().refreshDeviceOnlineByTime(deviceId,silentTime);
                    //写入静默专用reids
                    this.refreshDeviceInfo(deviceId, payload,silentTime);
                    // 触发静默报警
                    this.longTimeStaticProcessor(deviceId, messageData, payload,deviceStatusDto);
                }
            }
        }catch (Exception e){
            logger.error(LOG_PREFIX+"处理设备运行状态消息异常: topic={}, payload={}", topic, payload, e);
        }
    }


    /**
     * 静默报警处理逻辑
     */
    private void longTimeStaticProcessor(String deviceId, TcpMessageData messageData,String payload,DeviceStatusDto alarmDTO) {
        logger.info(LOG_PREFIX+"处理设备静默报警消息: 设备ID={}", deviceId);

        //1.从redis取出蓝牙数据，并且构建messageData
        List<Map<String, Object>> beacons = deviceSoSAlarmHandler.getIBeaconDataToRedis(deviceId);
        messageData.setBluetoothBeacons(beacons);
        messageData.setRawMessage(payload);
        //getScanTimestamp
        messageData.setScanTimestamp(alarmDTO.getTime());
        //3.检查是否需要报警
        boolean alarm = MqttConstants.isAlarm(AlarmConfigEnum.CSJ.getCode(), deviceId,redisService, deviceCorpMappingCache);
        if (alarm){
            //这里设置中泰设备标识
            messageData.setZTDevice(true);
            longTimeStaticTcpProcessor.process(messageData, null);
            logger.info(LOG_PREFIX+"处理设备静默报警消息: 设备ID={}，已处理", deviceId);
        }
    }

    /**
     * 只用于设备静默状态，因为静默状态设备不上报数据，所以需要定时上报数据
     * 中泰帽子-设备详细信息,key为设备ID，value为设备详细信息（TcpMessageData），主要用于设备静默状态，设备不上报
     * 数据，那么就需要用这个来定时上报数据
     */
    private void refreshDeviceInfo(String deviceId, String payload, long time) {
        TcpMessageData messageData = new TcpMessageData();
        //1.先获取设备的蓝牙数据
        List<Map<String, Object>> beacons = deviceSoSAlarmHandler.getIBeaconDataToRedis(deviceId);
        messageData.setRawMessage(payload);
        //getScanTimestamp
        messageData.setScanTimestamp(System.currentTimeMillis());
        messageData.setBluetoothBeacons(beacons);

        //2.拿到电量
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
        Integer batteryLevel = (Integer) redisService.hget(corpCode + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_BATTERY, deviceId);
        messageData.setBatteryLevel(batteryLevel);
        messageData.setDeviceId(deviceId);

        //3.放入redis
        redisService.hset(corpCode + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_MESSAGE_DATA, deviceId, messageData,time);
    }

    /**
     * 删除静默状态下的redis数据
     */
    public void removeLongTimeStaticRedisData(String deviceId) {
        redisService.hdel(deviceCorpMappingCache.getCorpCode(deviceId) + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_MESSAGE_DATA, deviceId);
    }


    /**
     * 设备运行状态DTO
     */
    @Data
    public class DeviceStatusDto {
        //设备标识
        private String sip;
        //时间戳（毫秒）
        private Long time;
        //类型：run_status-运行状态
        private String type;
        //状态值：value 在线状态值   0离线	1在线	3非静默	4静默
        private String value;

        //在线
        private final  static String online = "1";
        //离线
        private final  static String offline = "0";
        //非静默 （配合静默使用，先发送静默，接触静默则会下发一个3）
        private final  static String nonSilent = "3";
        //静默
        private final  static String silent = "4";
    }
}