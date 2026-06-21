package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;


import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.AlarmConfigEnum;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.dto.zhongtai.ZhongTaiAlarmDto;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.processor.HelmetOffTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.SosTcpProcessor;
import jakarta.annotation.Resource;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脱帽/戴帽告警
 * 处理设备脱帽/戴帽告警MQTT消息
 *
 * @author wxy
 * @date 2026-04-03
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class DeviceUnbonnetAlarmHandler implements MqttBusinessHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceUnbonnetAlarmHandler.class);

    private static final String LOG_PREFIX = "【中泰-MQTT消息-脱帽报警】- ";
    // 缓存：key = sipAccount，value = 上一次发送时间戳
    private static final ConcurrentHashMap<String, Long> unbonnetAlarmMap = new ConcurrentHashMap<>();

    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;

    @Resource
    private RedisService redisService;
    @Resource
    private HelmetOffTcpProcessor helmetOffTcpProcessor;
    @Resource
    private DeviceSoSAlarmHandler deviceSoSAlarmHandler;

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        if (topic == null || message == null) {
            logger.warn("{}canHandle检查失败: topic或message为空", LOG_PREFIX);
            return false;
        }

        boolean canHandle = topic.contains(MqttConstants.ALARM + "/" + MqttConstants.UNBONNET);

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
            logger.info("{}开始处理 脱帽中泰告警消息，topic={}, payload={}", LOG_PREFIX, topic, payload);

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
            Long lastTime = unbonnetAlarmMap.get(deviceId);
            if (lastTime != null && lastTime.equals(now)){
                logger.info(LOG_PREFIX+"处理设备运行状态消息: 设备ID={}，数据重复发送，忽略", deviceId);
                return ;
            }else{
                // 添加到缓存
                unbonnetAlarmMap.put(deviceId, now);
            }

            //判断是否为脱帽，不是则返回
            if (!"0".equals(alarmDTO.getValue())){
                return;
            }
            //2.3 从redis取出蓝牙数据，并且构建messageData
            List<Map<String, Object>> beacons = deviceSoSAlarmHandler.getIBeaconDataToRedis(deviceId);
            messageData.setBluetoothBeacons(beacons);

            messageData.setRawMessage(payload);
            //getScanTimestamp
            messageData.setScanTimestamp(alarmDTO.getTime());

            //3.检查是否需要报警
            boolean alarm = MqttConstants.isAlarm(AlarmConfigEnum.TM.getCode(), deviceId,redisService, deviceCorpMappingCache);
            if (alarm){
                //这里设置中泰设备标识
                messageData.setZTDevice(true);
                helmetOffTcpProcessor.process(messageData, null);
            }

        } catch (Exception e) {
            logger.error("{}处理 MQTT 告警消息异常，topic={}, payload={}, error={}", LOG_PREFIX,
                    topic, payload, e.getMessage(), e);

        }
    }



}