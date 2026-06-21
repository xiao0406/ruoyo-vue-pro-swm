package cn.iocoder.yudao.module.iot.mqtt.service;

import cn.iocoder.yudao.module.iot.config.MqttClientProperties;
import cn.iocoder.yudao.module.iot.mqtt.dto.PublishRequest;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MQTT消息发布服务
 * 支持同步和异步消息发布
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Service
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MqttPublishService {

    private static final Logger logger = LoggerFactory.getLogger(MqttPublishService.class);

    @Resource
    private MqttClientService mqttClientService;

    @Resource
    private MqttClientProperties mqttClientProperties;

    @Qualifier("mqttExecutor")
    @Resource
    private ThreadPoolTaskExecutor mqttExecutor;

    /**
     * 发布消息统计
     */
    private final AtomicLong publishSuccessCount = new AtomicLong(0);
    private final AtomicLong publishFailureCount = new AtomicLong(0);

    /**
     * 发布消息
     *
     * @param topic    主题
     * @param payload  消息内容
     * @param qos      QoS级别
     * @param retained 是否保留
     * @return 是否成功
     */
    public boolean publish(String topic, String payload, int qos, boolean retained) {
        try {
            if (!mqttClientService.isConnected()) {
                logger.warn("所有MQTT客户端未连接，发布失败: topic={}", topic);
                publishFailureCount.incrementAndGet();
                return false;
            }

            if (topic == null || topic.trim().isEmpty()) {
                logger.error("发布消息失败：主题不能为空");
                publishFailureCount.incrementAndGet();
                return false;
            }

            if (payload == null) {
                payload = "";
            }

            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(qos);
            message.setRetained(retained);

            boolean anySuccess = false;
            for (MqttClient client : mqttClientService.getPublishableClients()) {
                try {
                    if (client != null && client.isConnected()) {
                        client.publish(topic, message);
                        anySuccess = true;
                    }
                } catch (MqttException e) {
                    logger.error("发布消息到Broker失败: broker={}, topic={}, error={}", client.getServerURI(), topic, e.getMessage());
                }
            }

            if (anySuccess) {
                if (mqttClientProperties.getLogging().isLogPublishedMessages()) {
                    logger.info("发布MQTT消息成功: topic={}, qos={}, retained={}, payload={}",
                            topic, qos, retained, truncateMessage(payload));
                }
                publishSuccessCount.incrementAndGet();
                return true;
            } else {
                publishFailureCount.incrementAndGet();
                return false;
            }

        } catch (Exception e) {
            logger.error("发布MQTT消息异常: topic={}, error={}", topic, e.getMessage(), e);
            publishFailureCount.incrementAndGet();
            return false;
        }
    }

    /**
     * 发布消息（使用默认QoS）
     *
     * @param topic   主题
     * @param payload 消息内容
     * @return 是否成功
     */
    public boolean publish(String topic, String payload) {
        return publish(topic, payload, mqttClientProperties.getPublish().getDefaultQos(),
                mqttClientProperties.getPublish().isRetained());
    }

    /**
     * 异步发布消息
     *
     * @param topic    主题
     * @param payload  消息内容
     * @param qos      QoS级别
     * @param retained 是否保留
     * @return CompletableFuture
     */
    public CompletableFuture<Boolean> publishAsync(String topic, String payload, int qos, boolean retained) {
        return CompletableFuture.supplyAsync(() -> publish(topic, payload, qos, retained), mqttExecutor);
    }

    /**
     * 批量发布消息
     *
     * @param requests 发布请求列表
     * @return 发布结果映射
     */
    public Map<String, Boolean> publishBatch(List<PublishRequest> requests) {
        Map<String, Boolean> results = new HashMap<>();

        if (requests == null || requests.isEmpty()) {
            logger.warn("批量发布消息：请求列表为空");
            return results;
        }

        for (PublishRequest request : requests) {
            boolean success = publish(request.getTopic(), request.getPayload(),
                    request.getQos(), request.isRetained());
            results.put(request.getTopic(), success);
        }

        logger.info("批量发布消息完成：总数={}, 成功={}, 失败={}",
                requests.size(),
                results.values().stream().mapToInt(b -> b ? 1 : 0).sum(),
                results.values().stream().mapToInt(b -> b ? 0 : 1).sum());

        return results;
    }

    /**
     * 异步批量发布消息
     *
     * @param requests 发布请求列表
     * @return CompletableFuture
     */
    public CompletableFuture<Map<String, Boolean>> publishBatchAsync(List<PublishRequest> requests) {
        return CompletableFuture.supplyAsync(() -> publishBatch(requests), mqttExecutor);
    }

    /**
     * 发布JSON消息
     *
     * @param topic       主题
     * @param jsonPayload JSON消息内容
     * @param qos         QoS级别
     * @return 是否成功
     */
    public boolean publishJson(String topic, String jsonPayload, int qos) {
        return publish(topic, jsonPayload, qos, false);
    }

    /**
     * 发布二进制消息
     *
     * @param topic         主题
     * @param binaryPayload 二进制消息内容
     * @param qos           QoS级别
     * @param retained      是否保留
     * @return 是否成功
     */
    public boolean publishBinary(String topic, byte[] binaryPayload, int qos, boolean retained) {
        try {
            if (!mqttClientService.isConnected()) {
                logger.error("所有MQTT客户端未连接，无法发布二进制消息");
                publishFailureCount.incrementAndGet();
                return false;
            }

            if (topic == null || topic.trim().isEmpty()) {
                logger.error("发布二进制消息失败：主题不能为空");
                publishFailureCount.incrementAndGet();
                return false;
            }

            if (binaryPayload == null) {
                binaryPayload = new byte[0];
            }

            MqttMessage message = new MqttMessage(binaryPayload);
            message.setQos(qos);
            message.setRetained(retained);

            boolean anySuccess = false;
            for (MqttClient client : mqttClientService.getPublishableClients()) {
                try {
                    if (client != null && client.isConnected()) {
                        client.publish(topic, message);
                        anySuccess = true;
                    }
                } catch (MqttException e) {
                    logger.error("发布二进制消息到Broker失败: broker={}, topic={}, error={}", client.getServerURI(), topic, e.getMessage());
                }
            }

            if (anySuccess) {
                logger.info("发布MQTT二进制消息成功: topic={}, qos={}, retained={}, size={} bytes",
                        topic, qos, retained, binaryPayload.length);
                publishSuccessCount.incrementAndGet();
                return true;
            }

            publishFailureCount.incrementAndGet();
            return false;

        } catch (Exception e) {
            logger.error("发布MQTT二进制消息失败: topic={}, error={}", topic, e.getMessage(), e);
            publishFailureCount.incrementAndGet();
            return false;
        }
    }

    /**
     * 获取发布成功次数
     */
    public long getPublishSuccessCount() {
        return publishSuccessCount.get();
    }

    /**
     * 获取发布失败次数
     */
    public long getPublishFailureCount() {
        return publishFailureCount.get();
    }

    /**
     * 重置统计计数器
     */
    public void resetCounters() {
        publishSuccessCount.set(0);
        publishFailureCount.set(0);
        logger.info("MQTT发布统计计数器已重置");
    }

    /**
     * 截断消息内容用于日志
     */
    private String truncateMessage(String message) {
        if (message == null) {
            return "null";
        }

        int maxLength = mqttClientProperties.getLogging().getMaxMessageLength();
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...";
    }
}
