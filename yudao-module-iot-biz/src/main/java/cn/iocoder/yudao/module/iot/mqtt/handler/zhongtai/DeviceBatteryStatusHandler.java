package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.config.SpringContextHolder;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.tcp.handler.TcpMessageHandler;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.session.SessionManager;
import cn.iocoder.yudao.module.iot.tcp.util.RedisUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备电量信息处理器
 * 处理设备电量信息MQTT消息
 *
 * @author wxy
 * @date 2026-04-03
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class DeviceBatteryStatusHandler implements MqttBusinessHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceBatteryStatusHandler.class);
    private static final String LOG_PREFIX = "【中泰-MQTT消息-设备电量】- ";
    // 低电量语音模板
    private static final String Low_Battery_VoiceTemplate_ID = "1936227999566286848";
    // 缓存：key = sipAccount，value = 上一次发送时间戳
    private static final ConcurrentHashMap<String, Long> batteryStatusMap = new ConcurrentHashMap<>();

    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;

    @Resource
    private RedisService redisService;
    @Resource
    @Lazy
    private DeviceSoSAlarmHandler soSAlarmHandler;



    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        // 检查主题是否包含 /sensor/battery
        if (!topic.contains(MqttConstants.SENSOR +"/"+ MqttConstants.BATTERY)) {
            return false;
        }
        return true;
    }

    @Override
    public void handle(String topic, MqttMessage message) {
        // 解析消息内容
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.info( LOG_PREFIX +"处理设备电量信息消息: topic={}, payload={}", topic, payload);

        //定位数据实体类
        TcpMessageData messageData = new TcpMessageData();
        DeviceBatteryDto deviceBatteryDto = null;

        try {
            // 1.先校验设备是否入库
            if (!MqttConstants.verifyDeviceExists(topic, messageData, redisService, deviceCorpMappingCache)){
                return;
            }

            String deviceId = messageData.getDeviceId();

            // 2. 解析消息内容
            // 1. 先转成 JSONObject
            JSONObject jsonObj = JSONUtil.parseObj(payload);

            // 2. 手动把 value 字符串转成对象
            String valueStr = jsonObj.getStr("value");
            IotDeviceValue value = JSONUtil.toBean(valueStr, IotDeviceValue.class);

            // 3. 再组装回 DTO
            deviceBatteryDto = JSONUtil.toBean(jsonObj, DeviceBatteryDto.class);
            deviceBatteryDto.setValue(value);  // 手动设置进去
            if (ObjectUtil.isEmpty(deviceBatteryDto)){
                return;
            }
            // 2.3 判断是否是电量上报类型
            if (!deviceBatteryDto.isBatteryType()){
                return;
            }


            // ==============检验数据是否重复发送
            Long now = deviceBatteryDto.getTime();
            Long lastTime = batteryStatusMap.get(deviceId);
            if (lastTime != null && lastTime.equals(now)){
                logger.info( LOG_PREFIX +"设备 [{}] 电量信息已存在，已忽略", deviceId);
                return ;
            }else{
                // 添加到缓存
                batteryStatusMap.put(deviceId, now);
            }

            //3.获取电量
            Integer battery = deviceBatteryDto.getValue().getBattery();
            //4.放入redis缓存，hash结构，key为设备ID，value为电量
            if (battery == null || battery <= 0){
                return;
            }
            //5.放入redis缓存，hash结构，key为设备ID，value为电量,缓存时间为300秒·
            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
            redisService.hset(corpCode + SwmRedisKeyConstants.RedisSwmKey.ZT_DEVICE_BATTERY, deviceId, battery);

            // 设备电量 >= 40%，清空所有提醒
            if (battery >= 40) {
                clearBatteryWarnRedis(deviceId);
                return;
            }

            //5.判断电量，20，30，40电量均下发一次语音报警
            checkAndSendBatteryWarn(deviceId, battery, 20);
            checkAndSendBatteryWarn(deviceId, battery, 30);
            checkAndSendBatteryWarn(deviceId, battery, 40);
        }catch (Exception e){
            logger.error( LOG_PREFIX +"处理设备电量信息消息异常: topic={}, payload={}", topic, payload, e);
        }
    }



    /**
     * 清理设备电量提醒
     * @param deviceId
     */
    private void clearBatteryWarnRedis(String deviceId) {
        redisService.del(SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX + deviceId + ":20");
        redisService.del(SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX + deviceId + ":30");
        redisService.del(SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX + deviceId + ":40");

        logger.info( LOG_PREFIX +"设备 [{}] 电量>=40%，已清空低电量提醒记录", deviceId);
    }

    /**
     * 发送语音电量提示
     * @param deviceId
     * @param batteryLevel
     * @param threshold
     */
    public void checkAndSendBatteryWarn(String deviceId, int batteryLevel, int threshold) {

        if (batteryLevel >= threshold) {
            return;
        }

        //电量redis
        String redisKey = SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX + deviceId + ":" + threshold;
        // 已提醒过
        if (redisService.hasKey(redisKey)) {
            return;
        }
        // =============== 语音报警redis =============
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
        String voiceTemplateCacheKey  = corpCode +  SwmRedisKeyConstants.RedisSwmKey.VOICE_TEMPLATE_CACHE;

        // 查询语音模板内容，redis里查
        Object hget = redisService.hget(voiceTemplateCacheKey, Low_Battery_VoiceTemplate_ID);
        String templateNames = null;
        if (hget != null){
            templateNames = (String) hget;
        }

        if (StringUtils.isBlank(templateNames)) {
            logger.warn( LOG_PREFIX +"语音模板内容为空: ID={}", Low_Battery_VoiceTemplate_ID);
            return;
        }
        // 发送语音
        JSONObject result = soSAlarmHandler.sendVoiceCommand(deviceId, templateNames);
        //发送成功写进缓存
        if(!result.getBool("success")){
            return;
        }
        // 记录 Redis
        redisService.set(redisKey, "1");
        logger.info( LOG_PREFIX +"设备 [{}] 触发 {}% 电量提醒，当前电量：{}%", deviceId, threshold, batteryLevel);
    }




    /**
     * IoT 设备上报消息实体
     * 主题示例：/cmt/IoT/pub/8/6700/sensor/battery
     */
    @Data
    public class DeviceBatteryDto {

        //上报用户的sip号码（设备唯一标识）
        private String sip;
        //时间戳（毫秒）
        private Long time;
        //数据类型：online-在线；silent-静默；battery-电量
        private String type;

        //电量/信号 时使用：battery、signal
        private IotDeviceValue value;

        //判断是否是电量上报类型
        public boolean isBatteryType() {
            return IotReportType.BATTERY.getType().equals(this.type);
        }

    }

    /**
     * 设备上报的 value 字段实体
     */
    @Data
    public class IotDeviceValue {

        //电池电量 0~100，100=满电
        private Integer battery;
        //信号强度 1~4，值越大信号越好
        private Integer signal;
    }

    /**
     * IoT 上报类型枚举
     */
    @Getter
    @AllArgsConstructor
    public enum IotReportType {

        ONLINE("online", "设备在线"),
        SILENT("silent", "设备静默"),
        BATTERY("battery", "电量上报");

        private final String type;
        private final String desc;
    }
}