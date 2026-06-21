package cn.iocoder.yudao.module.iot.mqtt.controller;

import cn.iocoder.yudao.module.iot.config.MqttClientProperties;
import cn.iocoder.yudao.module.iot.mqtt.dto.MqttStatistics;
import cn.iocoder.yudao.module.iot.mqtt.dto.MqttStatus;
import cn.iocoder.yudao.module.iot.mqtt.entity.MqttSubscription;
import cn.iocoder.yudao.module.iot.mqtt.manager.MqttSubscriptionManager;
import cn.iocoder.yudao.module.iot.mqtt.service.MqttClientService;
import cn.iocoder.yudao.module.iot.mqtt.service.MqttPublishService;
import cn.iocoder.yudao.module.iot.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MQTT监控管理控制器
 * 提供MQTT状态监控和管理API接口
 *
 * @author Shawn
 * @date 2025-07-21
 */
@RestController
@RequestMapping("${adminPath}/api/mqtt/monitor")
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MqttMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(MqttMonitorController.class);

    @Resource
    private MqttClientService mqttClientService;

    @Resource
    private MqttSubscriptionManager subscriptionManager;

    @Resource
    private MqttPublishService publishService;

    @Resource
    private MqttClientProperties mqttClientProperties;

    /**
     * 获取MQTT连接状态
     *
     * @return MQTT状态信息
     */
    @GetMapping("/status")
    public R<MqttStatus> getStatus() {
        try {
            MqttStatus status = new MqttStatus();
            status.setConnected(mqttClientService.isConnected());
            status.setBrokerUrl(mqttClientProperties.getBrokerUrl());
            status.setClientId(mqttClientService.getClientId());
            status.setSubscriptionCount(subscriptionManager.getSubscriptionCount());
            status.setConnectTime(mqttClientService.getConnectTime());
            status.setReconnectAttempts(mqttClientService.getReconnectAttempts());
            status.setLastReconnectTime(mqttClientService.getLastReconnectTime());
            status.setPublishSuccessCount(publishService.getPublishSuccessCount());
            status.setPublishFailureCount(publishService.getPublishFailureCount());

            // 计算运行时间
            if (status.getConnectTime() != null) {
                Duration duration = Duration.between(status.getConnectTime(), LocalDateTime.now());
                status.setUptime(duration.getSeconds());
            }

            // 计算总接收消息数
            List<MqttSubscription> subscriptions = subscriptionManager.getAllSubscriptions();
            long totalMessages = subscriptions.stream()
                    .mapToLong(MqttSubscription::getMessageCount)
                    .sum();
            status.setTotalMessagesReceived(totalMessages);

            logger.info("获取MQTT状态成功: connected={}, subscriptions={}",
                    status.isConnected(), status.getSubscriptionCount());

            return R.ok(status);
        } catch (Exception e) {
            logger.error("获取MQTT状态异常: {}", e.getMessage(), e);
            return R.fail("获取MQTT状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取详细统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public R<MqttStatistics> getStatistics() {
        try {
            MqttStatistics statistics = new MqttStatistics();

            List<MqttSubscription> subscriptions = subscriptionManager.getAllSubscriptions();

            // 基础统计
            long totalReceived = subscriptions.stream()
                    .mapToLong(MqttSubscription::getMessageCount)
                    .sum();
            statistics.setTotalMessagesReceived(totalReceived);
            statistics.setTotalMessagesSent(publishService.getPublishSuccessCount());
            statistics.setActiveSubscriptions(subscriptions.size());

            // 连接运行时间
            if (mqttClientService.getConnectTime() != null) {
                Duration duration = Duration.between(mqttClientService.getConnectTime(), LocalDateTime.now());
                statistics.setConnectionUptime(duration.getSeconds());
            }

            // 各主题消息计数
            Map<String, Long> topicCounts = subscriptions.stream()
                    .filter(sub -> sub.getMessageCount() > 0)
                    .collect(Collectors.toMap(
                            MqttSubscription::getTopic,
                            MqttSubscription::getMessageCount));
            statistics.setTopicMessageCounts(topicCounts);

            // 各处理器类型消息计数
            Map<String, Long> processorCounts = subscriptions.stream()
                    .collect(Collectors.groupingBy(
                            MqttSubscription::getProcessorType,
                            Collectors.summingLong(MqttSubscription::getMessageCount)));
            statistics.setProcessorMessageCounts(processorCounts);

            // 计算平均值
            if (statistics.getConnectionUptime() > 0) {
                double minutes = statistics.getConnectionUptime() / 60.0;
                statistics.setAverageMessagesPerMinute(totalReceived / minutes);
                statistics.setAverageSentPerMinute(statistics.getTotalMessagesSent() / minutes);
            }

            statistics.setLastUpdateTime(LocalDateTime.now());

            logger.info("获取MQTT统计信息成功");
            return R.ok(statistics);
        } catch (Exception e) {
            logger.error("获取MQTT统计信息异常: {}", e.getMessage(), e);
            return R.fail("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 手动重连MQTT
     *
     * @return 操作结果
     */
    @PostMapping("/reconnect")
    public R<Boolean> reconnect() {
        logger.info("收到手动重连MQTT请求");

        try {
            boolean success = mqttClientService.reconnect();

            if (success) {
                logger.info("手动重连MQTT成功");
                return R.ok(true);
            } else {
                logger.error("手动重连MQTT失败");
                return R.fail("重连失败");
            }
        } catch (Exception e) {
            logger.error("手动重连MQTT异常: {}", e.getMessage(), e);
            return R.fail("重连异常: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public R<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();

            boolean connected = mqttClientService.isConnected();
            health.put("status", connected ? "UP" : "DOWN");
            health.put("broker", mqttClientProperties.getBrokerUrl());
            health.put("clientId", mqttClientService.getClientId());
            health.put("subscriptions", subscriptionManager.getSubscriptionCount());
            health.put("timestamp", LocalDateTime.now());

            if (connected) {
                health.put("connectTime", mqttClientService.getConnectTime());
                health.put("reconnectAttempts", mqttClientService.getReconnectAttempts());
            }

            logger.debug("MQTT健康检查: status={}", connected ? "UP" : "DOWN");
            return R.ok(health);
        } catch (Exception e) {
            logger.error("MQTT健康检查异常: {}", e.getMessage(), e);
            return R.fail("健康检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置信息
     *
     * @return 配置信息
     */
    @GetMapping("/config")
    public R<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("enabled", mqttClientProperties.isEnabled());
            config.put("brokerUrl", mqttClientProperties.getBrokerUrl());
            config.put("clientId", mqttClientProperties.getClientId());
            config.put("cleanSession", mqttClientProperties.isCleanSession());
            config.put("connectionTimeout", mqttClientProperties.getConnectionTimeout());
            config.put("keepAliveInterval", mqttClientProperties.getKeepAliveInterval());
            config.put("automaticReconnect", mqttClientProperties.isAutomaticReconnect());
            config.put("dynamicSubscriptionsEnabled",
                    mqttClientProperties.getDynamicSubscriptions().isEnabled());

            logger.info("获取MQTT配置信息成功");
            return R.ok(config);
        } catch (Exception e) {
            logger.error("获取MQTT配置信息异常: {}", e.getMessage(), e);
            return R.fail("获取配置信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    @GetMapping("/system")
    public R<Map<String, Object>> getSystemInfo() {
        try {
            Map<String, Object> system = new HashMap<>();

            // JVM信息
            Runtime runtime = Runtime.getRuntime();
            system.put("jvmTotalMemory", runtime.totalMemory());
            system.put("jvmFreeMemory", runtime.freeMemory());
            system.put("jvmUsedMemory", runtime.totalMemory() - runtime.freeMemory());
            system.put("jvmMaxMemory", runtime.maxMemory());

            // 线程信息
            system.put("activeThreads", Thread.activeCount());

            // 系统时间
            system.put("systemTime", LocalDateTime.now());

            logger.info("获取系统信息成功");
            return R.ok(system);
        } catch (Exception e) {
            logger.error("获取系统信息异常: {}", e.getMessage(), e);
            return R.fail("获取系统信息失败: " + e.getMessage());
        }
    }
}
