package cn.iocoder.yudao.module.iot.mqtt.manager;

import cn.iocoder.yudao.module.iot.config.MqttClientProperties;
import cn.iocoder.yudao.module.iot.mqtt.entity.MqttSubscription;
import cn.iocoder.yudao.module.iot.mqtt.entity.SubscriptionSource;
import cn.iocoder.yudao.module.iot.mqtt.processor.MqttMessageProcessor;
import cn.iocoder.yudao.module.iot.mqtt.registry.MqttProcessorRegistry;
import cn.iocoder.yudao.module.iot.mqtt.service.MqttClientService;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MQTT订阅管理器
 * 支持动态订阅管理和配置热更新
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Component
@DependsOn("mqttProcessorRegistry")
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MqttSubscriptionManager {

    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriptionManager.class);

    /**
     * 订阅信息存储 - 线程安全
     */
    private final Map<String, MqttSubscription> subscriptions = new ConcurrentHashMap<>();

    /**
     * 主题到处理器的映射
     */
    private final Map<String, MqttMessageProcessor> topicProcessors = new ConcurrentHashMap<>();

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 防止重复初始化/并发重入
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Resource
    private MqttClientService mqttClientService;

    @Resource
    private MqttProcessorRegistry processorRegistry;

    @Resource
    private MqttClientProperties mqttClientProperties;

    @Qualifier("mqttExecutor")
    @Resource
    private ThreadPoolTaskExecutor mqttExecutor;

    @PostConstruct
    public void init() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        // 启动定时健康检查任务
        if (mqttClientProperties.getDynamicSubscriptions().getManagement().getHealthCheckInterval() > 0) {
            int interval = mqttClientProperties.getDynamicSubscriptions().getManagement().getHealthCheckInterval();
            scheduler.scheduleAtFixedRate(this::checkSubscriptionHealth, interval, interval, TimeUnit.SECONDS);
            logger.info("启动订阅健康检查任务，检查间隔: {} 秒", interval);
        }

        logger.info("MQTT订阅管理器初始化完成");
    }

    /**
     * API驱动的订阅添加
     *
     * @param topic         主题
     * @param qos           QoS级别
     * @param processorType 处理器类型
     * @return 是否成功
     */
    public boolean addSubscriptionViaApi(String topic, int qos, String processorType) {
        return addSubscriptionViaApi(topic, qos, processorType, null);
    }

    /**
     * API驱动的订阅添加（指定Broker地址）
     *
     * @param topic         主题
     * @param qos           QoS级别
     * @param processorType 处理器类型
     * @param brokerUrl     目标Broker地址，null表示所有可订阅的Broker
     * @return 是否成功
     */
    public boolean addSubscriptionViaApi(String topic, int qos, String processorType, String brokerUrl) {
        MqttMessageProcessor processor = processorRegistry.createProcessor(processorType);
        if (processor == null) {
            logger.error("未找到处理器类型: {}", processorType);
            return false;
        }

        return addSubscription(topic, qos, processor, SubscriptionSource.API, brokerUrl);
    }

    /**
     * 配置驱动的订阅添加
     *
     * @param topic         主题
     * @param qos           QoS级别
     * @param processorType 处理器类型
     * @return 是否成功
     */
    public boolean addSubscriptionViaConfig(String topic, int qos, String processorType) {
        return addSubscriptionViaConfig(topic, qos, processorType, null);
    }

    /**
     * 配置驱动的订阅添加（指定Broker地址）
     *
     * @param topic         主题
     * @param qos           QoS级别
     * @param processorType 处理器类型
     * @param brokerUrl     目标Broker地址，null表示所有可订阅的Broker
     * @return 是否成功
     */
    public boolean addSubscriptionViaConfig(String topic, int qos, String processorType, String brokerUrl) {
        if (processorRegistry == null) {
            logger.error("处理器注册中心未初始化，无法创建处理器: {}", processorType);
            return false;
        }

        MqttMessageProcessor processor = processorRegistry.createProcessor(processorType);
        if (processor == null) {
            logger.error("未找到处理器类型: {}", processorType);
            return false;
        }

        return addSubscription(topic, qos, processor, SubscriptionSource.CONFIG, brokerUrl);
    }

    /**
     * 统一的订阅添加方法
     */
    private boolean addSubscription(String topic, int qos, MqttMessageProcessor processor,
            SubscriptionSource source, String brokerUrl) {
        try {
            // 检查MQTT客户端连接状态
            if (!mqttClientService.isConnected()) {
                logger.warn("MQTT客户端未连接，无法订阅主题: {}", topic);
                return false;
            }

            // 并发下原子判重：先占位，避免重复 subscribe
            MqttSubscription newSub = new MqttSubscription(topic, qos, processor, LocalDateTime.now(), source);
            newSub.setBrokerUrl(brokerUrl);
            MqttSubscription existing = subscriptions.putIfAbsent(topic, newSub);
            if (existing != null) {
                if (isSameSubscription(existing, qos, processor, source, brokerUrl)) {
                    logger.warn("主题 {} 已经订阅，跳过重复订阅", topic);
                    return true;
                }

                logger.warn("主题 {} 已存在不同订阅配置，准备切换处理器: oldProcessor={}, newProcessor={}, oldSource={}, newSource={}",
                        topic, existing.getProcessorType(), newSub.getProcessorType(), existing.getSource(), source);

                return replaceSubscription(existing, newSub);
            }

            if (subscribeInternal(newSub)) {
                logger.info("成功添加MQTT订阅: topic={}, qos={}, source={}, brokerUrl={}", topic, qos, source, brokerUrl);
                return true;
            }

            subscriptions.remove(topic, newSub);
            topicProcessors.remove(topic, processor);
            return false;
        } catch (Exception e) {
            logger.error("添加MQTT订阅失败: topic={}, error={}", topic, e.getMessage(), e);
            subscriptions.remove(topic);
            return false;
        }
    }

    private boolean isSameSubscription(MqttSubscription existing, int qos, MqttMessageProcessor processor,
            SubscriptionSource source, String brokerUrl) {
        return existing.getQos() == qos
                && Objects.equals(existing.getProcessorType(), getProcessorType(processor))
                && existing.getSource() == source
                && Objects.equals(normalizeBroker(existing.getBrokerUrl()), normalizeBroker(brokerUrl));
    }

    private String normalizeBroker(String broker) {
        return broker == null || broker.trim().isEmpty() ? null : broker.trim();
    }

    private boolean replaceSubscription(MqttSubscription existing, MqttSubscription replacement) {
        String topic = replacement.getTopic();
        try {
            // 在订阅所绑定的客户端上取消订阅
            List<org.eclipse.paho.client.mqttv3.MqttClient> targets = getSubscriptionTargets(existing);
            for (org.eclipse.paho.client.mqttv3.MqttClient client : targets) {
                try {
                    if (client.isConnected()) {
                        client.unsubscribe(topic);
                    }
                } catch (Exception e) {
                    logger.warn("取消订阅失败: topic={}, broker={}, error={}", topic, client.getServerURI(), e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("切换订阅处理器前取消订阅失败: topic={}, error={}", topic, e.getMessage());
        }

        subscriptions.put(topic, replacement);
        if (subscribeInternal(replacement)) {
            logger.info("成功切换MQTT订阅处理器: topic={}, qos={}, source={}, processor={}",
                    replacement.getTopic(), replacement.getQos(), replacement.getSource(), replacement.getProcessorType());
            return true;
        }

        subscriptions.put(topic, existing);
        topicProcessors.put(topic, existing.getProcessor());
        try {
            subscribeInternal(existing);
        } catch (Exception e) {
            logger.error("恢复原订阅失败: topic={}, error={}", topic, e.getMessage(), e);
        }
        return false;
    }

    /**
     * 根据订阅获取目标客户端列表。
     * 若指定了 brokerUrl 则只返回该 Broker；否则返回所有可订阅的客户端。
     */
    private List<org.eclipse.paho.client.mqttv3.MqttClient> getSubscriptionTargets(MqttSubscription subscription) {
        String brokerUrl = subscription.getBrokerUrl();
        if (brokerUrl != null && !brokerUrl.trim().isEmpty()) {
            org.eclipse.paho.client.mqttv3.MqttClient client = mqttClientService.getClientByBroker(brokerUrl);
            if (client != null) {
                return Collections.singletonList(client);
            }
            return Collections.emptyList();
        }
        return mqttClientService.getSubscribableClients();
    }

    private boolean subscribeInternal(MqttSubscription subscription) {
        String topicFilter = subscription.getTopic();
        MqttMessageProcessor processor = subscription.getProcessor();
        try {
            List<org.eclipse.paho.client.mqttv3.MqttClient> targets = getSubscriptionTargets(subscription);
            boolean anySuccess = false;
            for (org.eclipse.paho.client.mqttv3.MqttClient client : targets) {
                if (client == null || !client.isConnected()) {
                    logger.warn("MQTT客户端未连接，跳过订阅: broker={}, topic={}", client != null ? client.getServerURI() : "null", topicFilter);
                    continue;
                }
                try {
                    client.subscribe(topicFilter, subscription.getQos(), (receivedTopic, message) -> {
                        logger.info("收到MQTT消息: topicFilter={}, receivedTopic={}, qos={}, retained={}, broker={}",
                                topicFilter, receivedTopic, message.getQos(), message.isRetained(), client.getServerURI());
                        processMessageAsync(topicFilter, receivedTopic, message, processor);
                    });
                    anySuccess = true;
                    logger.info("MQTT主题订阅已绑定消息处理器: topic={}, qos={}, processor={}, broker={}",
                            topicFilter, subscription.getQos(), subscription.getProcessorType(), client.getServerURI());
                } catch (Exception e) {
                    logger.error("订阅MQTT主题失败: topic={}, broker={}, error={}", topicFilter, client.getServerURI(), e.getMessage());
                }
            }

            if (anySuccess) {
                topicProcessors.put(topicFilter, processor);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("订阅MQTT主题失败: topic={}, error={}", topicFilter, e.getMessage(), e);
            return false;
        }
    }

    private String getProcessorType(MqttMessageProcessor processor) {
        if (processor == null) {
            return "unknown";
        }
        String className = processor.getClass().getSimpleName();
        if (className.endsWith("MessageProcessor")) {
            className = className.substring(0, className.length() - "MessageProcessor".length());
        }
        return className.toLowerCase();
    }

    /**
     * 移除订阅
     *
     * @param topic 主题
     * @return 是否成功
     */
    public boolean removeSubscription(String topic) {
        try {
            MqttSubscription subscription = subscriptions.get(topic);
            if (subscription == null) {
                logger.warn("主题 {} 未找到订阅信息", topic);
                return true;
            }

            // 在订阅所绑定的客户端上取消订阅
            List<org.eclipse.paho.client.mqttv3.MqttClient> targets = getSubscriptionTargets(subscription);
            for (org.eclipse.paho.client.mqttv3.MqttClient client : targets) {
                try {
                    if (client.isConnected()) {
                        client.unsubscribe(topic);
                    }
                } catch (Exception e) {
                    logger.warn("取消订阅失败: topic={}, broker={}, error={}", topic, client.getServerURI(), e.getMessage());
                }
            }

            // 移除本地记录
            subscriptions.remove(topic);
            topicProcessors.remove(topic);

            logger.info("成功移除MQTT订阅: topic={}", topic);
            return true;

        } catch (Exception e) {
            logger.error("移除MQTT订阅失败: topic={}, error={}", topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量添加订阅
     */
    public Map<String, Boolean> addSubscriptions(List<MqttClientProperties.SubscriptionConfig> configs,
            SubscriptionSource source) {
        Map<String, Boolean> results = new HashMap<>();

        for (MqttClientProperties.SubscriptionConfig config : configs) {
            boolean success;
            if (source == SubscriptionSource.CONFIG) {
                success = addSubscriptionViaConfig(config.getTopic(), config.getQos(), config.getProcessorType());
            } else {
                success = addSubscriptionViaApi(config.getTopic(), config.getQos(), config.getProcessorType());
            }
            results.put(config.getTopic(), success);
        }

        return results;
    }

    /**
     * 批量移除订阅
     */
    public Map<String, Boolean> removeSubscriptions(List<String> topics) {
        Map<String, Boolean> results = new HashMap<>();

        for (String topic : topics) {
            boolean success = removeSubscription(topic);
            results.put(topic, success);
        }

        return results;
    }

    /**
     * 异步处理消息
     */
    private void processMessageAsync(String topicFilter, String receivedTopic, MqttMessage message,
            MqttMessageProcessor processor) {
        if (processor == null) {
            logger.error("MQTT消息处理器为空，无法处理消息: topicFilter={}, receivedTopic={}", topicFilter, receivedTopic);
            return;
        }

        // 统计以"订阅过滤器 topicFilter"为 key（适配 +/# 通配符订阅）
        MqttSubscription subscription = subscriptions.get(topicFilter);
        if (subscription != null) {
            subscription.incrementMessageCount();
        }

        // 深拷贝 payload，避免异步处理中 message 被复用/变更
        final String topicCopy = receivedTopic;
        final byte[] payloadCopy = Arrays.copyOf(message.getPayload(), message.getPayload().length);
        final int qosCopy = message.getQos();
        final boolean retainedCopy = message.isRetained();

        // 使用线程池异步处理，避免阻塞MQTT线程
        mqttExecutor.submit(() -> {
            try {
                MqttMessage safeMessage = new MqttMessage(payloadCopy);
                safeMessage.setQos(qosCopy);
                safeMessage.setRetained(retainedCopy);
                processor.process(topicCopy, safeMessage);
            } catch (Exception e) {
                logger.error("处理MQTT消息失败: topic={}, error={}", topicCopy, e.getMessage(), e);
            }
        });
    }

    /**
     * 获取所有订阅信息
     */
    public List<MqttSubscription> getAllSubscriptions() {
        return new ArrayList<>(subscriptions.values());
    }

    /**
     * 获取订阅数量
     */
    public int getSubscriptionCount() {
        return subscriptions.size();
    }

    /**
     * 根据来源获取订阅
     */
    public List<MqttSubscription> getSubscriptionsBySource(SubscriptionSource source) {
        return subscriptions.values().stream()
                .filter(sub -> sub.getSource() == source)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * 检查订阅健康状态
     */
    private void checkSubscriptionHealth() {
        logger.debug("开始检查订阅健康状态，当前订阅数: {}", subscriptions.size());

        for (Map.Entry<String, MqttSubscription> entry : subscriptions.entrySet()) {
            String topic = entry.getKey();
            MqttSubscription subscription = entry.getValue();

            // 检查订阅是否还有效
            if (!isSubscriptionActive(topic)) {
                logger.warn("检测到订阅 {} 已失效，尝试重新订阅", topic);

                // 自动重新订阅
                if (mqttClientProperties.getDynamicSubscriptions().getManagement().isAutoResubscribe()) {
                    resubscribe(subscription);
                }
            }
        }
    }

    /**
     * 检查订阅是否活跃
     */
    private boolean isSubscriptionActive(String topic) {
        try {
            // 这里可以通过MQTT客户端检查订阅状态
            // 由于Paho客户端没有直接的方法，我们通过连接状态来判断
            return mqttClientService.isConnected();
        } catch (Exception e) {
            logger.error("检查订阅状态失败: topic={}", topic, e);
            return false;
        }
    }

    /**
     * 重新订阅
     */
    private void resubscribe(MqttSubscription subscription) {
        try {
            List<org.eclipse.paho.client.mqttv3.MqttClient> targets = getSubscriptionTargets(subscription);
            for (org.eclipse.paho.client.mqttv3.MqttClient client : targets) {
                if (client == null || !client.isConnected()) {
                    continue;
                }
                try {
                    client.subscribe(subscription.getTopic(), subscription.getQos(),
                            (receivedTopic, message) -> {
                                processMessageAsync(subscription.getTopic(), receivedTopic, message, subscription.getProcessor());
                            });
                } catch (Exception e) {
                    logger.error("重新订阅失败: topic={}, broker={}, error={}",
                            subscription.getTopic(), client.getServerURI(), e.getMessage());
                }
            }

            subscription.setLastResubscribeTime(LocalDateTime.now());
            subscription.incrementRetryCount();

            logger.info("重新订阅成功: topic={}", subscription.getTopic());

        } catch (Exception e) {
            logger.error("重新订阅失败: topic={}, error={}",
                    subscription.getTopic(), e.getMessage(), e);
        }
    }

    /**
     * 重新订阅所有订阅（用于重连后）
     */
    public void resubscribeAll() {
        logger.info("开始重新订阅所有主题，订阅数: {}", subscriptions.size());

        for (MqttSubscription subscription : subscriptions.values()) {
            resubscribe(subscription);
        }

        logger.info("重新订阅完成");
    }

    /**
     * 清理所有订阅
     */
    @PreDestroy
    public void cleanup() {
        logger.info("开始清理MQTT订阅管理器...");

        // 关闭定时任务
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 清理订阅信息
        subscriptions.clear();
        topicProcessors.clear();

        logger.info("MQTT订阅管理器清理完成");
    }
}
