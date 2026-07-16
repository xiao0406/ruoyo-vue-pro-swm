package cn.iocoder.yudao.module.iot.mqtt.processor.zhongtai;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.HelmetAttendanceHandler;
import cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai.*;
import cn.iocoder.yudao.module.iot.mqtt.processor.MqttMessageProcessor;
import cn.iocoder.yudao.module.iot.service.RawMessageTdEngineService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * MQTT消息分发处理器
 * 根据消息内容特征将消息路由到不同的业务处理器
 *
 * @author Shawn
 * @date 2025-07-23
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class ZhongtaiMessageDispatcherProcessor implements MqttMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ZhongtaiMessageDispatcherProcessor.class);
    private static final String LOG_PREFIX = "【中泰-MQTT消息-消息分发】- ";


    @Resource
    private DeviceRunStatusHandler deviceRunStatusHandler;

    @Resource
    private DeviceBatteryStatusHandler deviceBatteryStatusHandler;

    @Resource
    private DeviceIbeaconStatusHandler deviceIbeaconStatusHandler;

    @Resource
    private DeviceSoSAlarmHandler deviceSoSAlarmHandler;

    @Resource
    private DeviceUnbonnetAlarmHandler deviceUnbonnetAlarmHandler;

    @Resource
    private DeviceFallAlarmHandler deviceFallAlarmHandler;

    @Resource
    private DeviceGpsLocationHandler deviceGpsLocationHandler;

    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;

    @Resource
    private RedisService redisService;

    @Qualifier("mqttProcessorExecutor")
    @Resource
    private ThreadPoolTaskExecutor mqttProcessorExecutor;
    @Resource
    private RawMessageTdEngineService rawMessageTdEngineService;

    @Override
    public void process(String topic, MqttMessage message) {

        try {
            // 深拷贝，避免 MQTT message 被复用
            final String topicCopy = topic;
            final byte[] payloadCopy = Arrays.copyOf(message.getPayload(), message.getPayload().length);
            logger.info(LOG_PREFIX + "收到待分发MQTT消息: topic={}, payloadBytes={}", topicCopy, payloadCopy.length);

            mqttProcessorExecutor.execute(() -> {
                try {
                    MqttMessage safeMessage = new MqttMessage(payloadCopy);

                    // 1. 校验设备
                    TcpMessageData messageData = new TcpMessageData();
                    if (!MqttConstants.verifyDeviceExists(topicCopy, messageData, redisService, deviceTenantMappingCache)) {
                        return;
                    }

                    // 2. 分发处理
                    handleSafely("设备运行状态", () -> {
                        if (deviceRunStatusHandler.canHandle(topicCopy, safeMessage)) {
                            deviceRunStatusHandler.handle(topicCopy, safeMessage);
                        }
                    });

                    handleSafely("设备电量", () -> {
                        if (deviceBatteryStatusHandler.canHandle(topicCopy, safeMessage)) {
                            deviceBatteryStatusHandler.handle(topicCopy, safeMessage);
                        }
                    });

                    handleSafely("设备蓝牙", () -> {
                        if (deviceIbeaconStatusHandler.canHandle(topicCopy, safeMessage)) {
                            deviceIbeaconStatusHandler.handle(topicCopy, safeMessage);
                        }
                    });

                    handleSafely("设备GPS", () -> {
                        if (deviceGpsLocationHandler.canHandle(topicCopy, safeMessage)) {
                            deviceGpsLocationHandler.handle(topicCopy, safeMessage);
                        }
                    });

                    handleSafely("设备SOS", () -> {
                        if (deviceSoSAlarmHandler.canHandle(topicCopy, safeMessage)) {
                            deviceSoSAlarmHandler.handle(topicCopy, safeMessage);
                        }
                    });

                    handleSafely("设备脱帽", () -> {
                        if (deviceUnbonnetAlarmHandler.canHandle(topicCopy, safeMessage)) {
                            deviceUnbonnetAlarmHandler.handle(topicCopy, safeMessage);
                        }
                    });

                    handleSafely("设备跌倒", () -> {
                        if (deviceFallAlarmHandler.canHandle(topicCopy, safeMessage)) {
                            deviceFallAlarmHandler.handle(topicCopy, safeMessage);
                        }
                    });


                    handleSafely("插入日志", () -> {
                        String payload = new String(payloadCopy, StandardCharsets.UTF_8);
                        this.insertLog(messageData.getDeviceId(), topicCopy, payload);
                    });

                } catch (Exception e) {
                    logger.error(LOG_PREFIX + "MQTT消息处理异常 topic={}", topicCopy, e);
                }
            });

        } catch (Exception e) {
            logger.error(LOG_PREFIX + "MQTT消息分发入口异常 topic={}", topic, e);
        }
    }

    /**
     * 通用安全执行方法
     */
    private void handleSafely(String desc, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            logger.error(LOG_PREFIX + "{}处理异常: {}", desc, e.getMessage(), e);
        }
    }

    /**
     * 插入日志
     * @param deviceId
     * @param sessionId
     * @param message
     */
    private void insertLog(String deviceId, String sessionId, String message) {
            R<JSONObject> result = rawMessageTdEngineService.saveRawMessageData(deviceId, sessionId, message);
    }
}
