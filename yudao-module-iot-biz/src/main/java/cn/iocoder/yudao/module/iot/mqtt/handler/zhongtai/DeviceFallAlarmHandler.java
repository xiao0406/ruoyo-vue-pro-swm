package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;


import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.AlarmConfigEnum;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.dto.zhongtai.ZhongTaiAlarmDto;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.processor.FallTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.SosTcpProcessor;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备跌落告警处理器
 * 处理设备跌落告警MQTT消息
 *
 * @author wxy
 * @date 2026-04-03
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class DeviceFallAlarmHandler implements MqttBusinessHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceFallAlarmHandler.class);

    private static final String LOG_PREFIX = "【中泰-MQTT消息-跌落告警】- ";

    // 缓存：key = sipAccount，value = 上一次发送时间戳
    private static final ConcurrentHashMap<String, Long> fallAlarmMap = new ConcurrentHashMap<>();

    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;

    @Resource
    private RedisService redisService;
    @Resource
    private FallTcpProcessor fallTcpProcessor;

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        if (topic == null || message == null) {
            logger.warn("{}canHandle检查失败: topic或message为空", LOG_PREFIX);
            return false;
        }

        boolean canHandle = topic.contains(MqttConstants.ALARM + "/" + MqttConstants.FALL);

        if (canHandle) {
            logger.info("{}canHandle检查通过: topic={}", LOG_PREFIX, topic);
        } else {
            logger.debug("{}canHandle检查未通过: topic={}, 期望包含: {}/{}",
                    LOG_PREFIX, topic, MqttConstants.ALARM, MqttConstants.SOS);
        }

        return canHandle;
    }

    /**
     * 处理 MQTT 告警消息
     */
    public void handle(String topic, MqttMessage message) {

        String payload = null;
        ZhongTaiAlarmDto alarmDTO = new ZhongTaiAlarmDto();
        TcpMessageData messageData = new TcpMessageData();

        try {
            // 解析 MQTT 消息体
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            logger.info("{}开始处理 中泰跌落告警消息，topic={}, payload={}", LOG_PREFIX, topic, payload);

            // 1.先校验设备是否入库
            if (!MqttConstants.verifyDeviceExists(topic, messageData, redisService, deviceCorpMappingCache)){
                return;
            }
            String deviceId = messageData.getDeviceId();

            // 2. 解析消息内容
            // 2.1. 解析 MQTT 消息体
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // 2.2. 转换为告警 DTO
            alarmDTO = JSONUtil.toBean(payload, ZhongTaiAlarmDto.class);
            if (ObjectUtil.isEmpty(alarmDTO)){
                return;
            }


            // ==============检验数据是否重复发送
            Long now = alarmDTO.getTime();
            Long lastTime = fallAlarmMap.get(deviceId);
            if (lastTime != null && lastTime.equals(now)){
                logger.info(LOG_PREFIX+"{}数据重复发送，忽略", deviceId);
                return ;
            }else{
                // 添加到缓存
                fallAlarmMap.put(deviceId, now);
            }

            //2.3 从redis取出蓝牙数据，并且构建messageData
            List<Map<String, Object>> beacons = this.getIBeaconDataToRedis(deviceId);
            messageData.setBluetoothBeacons(beacons);

            messageData.setRawMessage(payload);
            //getScanTimestamp
            messageData.setScanTimestamp(alarmDTO.getTime());

            //3.检查是否需要报警
            boolean alarm = MqttConstants.isAlarm(AlarmConfigEnum.DL.getCode(), deviceId,redisService, deviceCorpMappingCache);
            if (alarm){
                //这里设置中泰设备标识
                messageData.setZTDevice(true);
                fallTcpProcessor.process(messageData, null);
            }


        } catch (Exception e) {
            logger.error("{}处理 MQTT 告警消息异常，topic={}, payload={}, error={}", LOG_PREFIX,
                    topic, payload, e.getMessage(), e);

        }
    }


    /**
     * 从Redis获取设备蓝牙数据
     * @param deviceId 设备ID
     * @return 蓝牙数据列表，永不为 null
     */
    public List<Map<String, Object>> getIBeaconDataToRedis(String deviceId) {
        // 1. 空值校验
        if (StrUtil.isEmpty(deviceId)) {
            logger.warn("[获取蓝牙数据] deviceId 为空");
            return new ArrayList<>();
        }

        try {
            // 2. 从redis获取
            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
            Object cacheObj = redisService.hget(corpCode + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_BLE_DATA, deviceId);

            // 3. 安全类型转换（核心优化）
            if (cacheObj instanceof List<?>) {
                // 安全强转，避免类型转换异常
                return (List<Map<String, Object>>) cacheObj;
            }
        } catch (Exception e) {
            logger.error("[获取蓝牙数据] 设备[{}] 从Redis获取数据异常：", deviceId, e);
        }
        // 4. 不存在/异常时返回空集合
        return new ArrayList<>();
    }

}