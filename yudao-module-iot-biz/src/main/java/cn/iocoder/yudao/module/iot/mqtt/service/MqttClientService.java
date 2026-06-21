package cn.iocoder.yudao.module.iot.mqtt.service;

import cn.iocoder.yudao.module.iot.config.MqttClientProperties;
import cn.iocoder.yudao.module.iot.mqtt.manager.MqttSubscriptionManager;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT客户端核心服务
 * 管理多个MQTT客户端连接生命周期，支持同时连接多个Broker
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Service
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MqttClientService {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);

    @Resource
    private MqttClientProperties mqttClientProperties;

    @Resource
    private MqttSubscriptionManager subscriptionManager;

    /** 管理的所有MQTT客户端实例 */
    private final List<MqttClient> mqttClients = new ArrayList<>();

    /** Broker URL → 配置映射（用于角色判断和按URL查找） */
    private final Map<String, MqttClientProperties.BrokerConfig> brokerConfigByUrl = new ConcurrentHashMap<>();

    /** Broker name -> 配置映射（用于按配置别名查找） */
    private final Map<String, MqttClientProperties.BrokerConfig> brokerConfigByName = new ConcurrentHashMap<>();

    /** 各客户端连接时间 */
    private final Map<String, LocalDateTime> connectTimes = new ConcurrentHashMap<>();

    /** 各客户端最后重连时间 */
    private final Map<String, LocalDateTime> lastReconnectTimes = new ConcurrentHashMap<>();

    /** 各客户端重连次数 */
    private final Map<String, Integer> reconnectAttemptCounts = new ConcurrentHashMap<>();

    /**
     * 尽早建立连接：其他 Bean 的 {@code @PostConstruct}（如中泰动态订阅）会依赖已连接状态。
     * 配置驱动的 initialSubscriptions 仍在 {@link ApplicationReadyEvent} 中初始化，确保处理器等已就绪。
     */
    @PostConstruct
    public void initConnection() {
        if (!mqttClientProperties.isEnabled()) {
            logger.info("MQTT客户端已禁用，跳过连接初始化");
            return;
        }

        List<MqttClientProperties.BrokerConfig> brokerConfigs = getBrokerConfigs();
        if (brokerConfigs.isEmpty()) {
            logger.error("未配置MQTT Broker地址，无法初始化客户端");
            return;
        }

        boolean appendSuffix = mqttClientProperties.isAppendUniqueSuffixToClientId();
        String uuidSuffix = appendSuffix ? "-" + UUID.randomUUID().toString().substring(0, 8) : "";

        for (int i = 0; i < brokerConfigs.size(); i++) {
            MqttClientProperties.BrokerConfig config = brokerConfigs.get(i);
            String clientId = config.getClientId();
            if (clientId == null || clientId.trim().isEmpty()) {
                clientId = "iot-client-iot-swm";
            }
            clientId = clientId + uuidSuffix;
            try {
                logger.info("创建MQTT客户端[{}]: clientId={}, broker={}, username={}",
                        i, clientId, config.getUrl(), config.getUsername());
                MqttClient client = new MqttClient(config.getUrl(), clientId, new MemoryPersistence());
                registerCallback(client);
                connectClient(client, config);
                mqttClients.add(client);
                brokerConfigByUrl.put(config.getUrl(), config);
                if (config.getName() != null && !config.getName().trim().isEmpty()) {
                    brokerConfigByName.put(config.getName().trim(), config);
                }
                logger.info("MQTT客户端[{}]初始化完成", i);
            } catch (Exception e) {
                logger.error("MQTT客户端[{}]创建失败: broker={}, error={}", i, config.getUrl(), e.getMessage(), e);
            }
        }

        logger.info("MQTT客户端初始化完成，成功创建 {} / {} 个客户端", mqttClients.size(), brokerConfigs.size());
    }

    @Order(1)
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!mqttClientProperties.isEnabled()) {
            logger.info("MQTT客户端已禁用，跳过配置订阅初始化");
            return;
        }

        try {
            logger.info("应用启动完成，开始初始化配置驱动的MQTT订阅...");
            initializeConfigSubscriptions();
        } catch (Exception e) {
            logger.error("应用启动后初始化配置订阅失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取所有Broker配置（优先brokers列表，兼容单地址brokerUrl）
     */
    private List<MqttClientProperties.BrokerConfig> getBrokerConfigs() {
        List<MqttClientProperties.BrokerConfig> configs = mqttClientProperties.getBrokers();
        if (configs != null && !configs.isEmpty()) {
            return configs;
        }
        // 兼容单地址配置：用全局字段合成一个 BrokerConfig
        String singleUrl = mqttClientProperties.getBrokerUrl();
        if (singleUrl != null && !singleUrl.trim().isEmpty()) {
            MqttClientProperties.BrokerConfig fallback = new MqttClientProperties.BrokerConfig();
            fallback.setUrl(singleUrl.trim());
            fallback.setClientId(mqttClientProperties.getClientId());
            fallback.setUsername(mqttClientProperties.getUsername());
            fallback.setPassword(mqttClientProperties.getPassword());
            return Collections.singletonList(fallback);
        }
        return Collections.emptyList();
    }

    /**
     * 注册回调到指定客户端
     */
    private void registerCallback(MqttClient client) {
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                connectTimes.put(serverURI, LocalDateTime.now());

                if (reconnect) {
                    lastReconnectTimes.put(serverURI, LocalDateTime.now());
                    reconnectAttemptCounts.merge(serverURI, 1, Integer::sum);
                    logger.info("MQTT重连成功: serverURI={}, clientId={}, 重连次数={}",
                            serverURI, client.getClientId(), reconnectAttemptCounts.getOrDefault(serverURI, 0));
                    CompletableFuture.runAsync(() -> subscriptionManager.resubscribeAll());
                } else {
                    logger.info("MQTT连接回调触发: serverURI={}, clientId={}", serverURI, client.getClientId());
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                logger.error("MQTT连接丢失: serverURI={}, clientId={}, 自动重连: {}, 原因: {}",
                        client.getServerURI(), client.getClientId(),
                        mqttClientProperties.isAutomaticReconnect(), cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                logger.debug("收到MQTT消息: topic={}, messageId={}, broker={}", topic, message.getId(), client.getServerURI());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.debug("消息发送完成: messageId={}", token.getMessageId());
            }
        });
    }

    /**
     * 连接单个客户端到Broker
     */
    private void connectClient(MqttClient client, MqttClientProperties.BrokerConfig config) throws MqttException {
        if (client.isConnected()) {
            return;
        }
        MqttConnectOptions options = createConnectOptions(config);
        logger.info("正在连接MQTT Broker: {}, clientId={}", config.getUrl(), client.getClientId());
        client.connect(options);

        if (client.isConnected()) {
            logger.info("MQTT连接建立成功: {}", config.getUrl());
        } else {
            logger.error("MQTT连接失败: {}", config.getUrl());
        }
    }

    /**
     * 创建连接选项
     */
    private MqttConnectOptions createConnectOptions(MqttClientProperties.BrokerConfig config) {
        MqttConnectOptions options = new MqttConnectOptions();

        options.setCleanSession(mqttClientProperties.isCleanSession());
        options.setConnectionTimeout(mqttClientProperties.getConnectionTimeout());
        options.setKeepAliveInterval(mqttClientProperties.getKeepAliveInterval());
        options.setMaxInflight(mqttClientProperties.getMaxInflight());
        options.setAutomaticReconnect(mqttClientProperties.isAutomaticReconnect());

        // 使用每个Broker各自的认证信息
        if (config.getUsername() != null && !config.getUsername().trim().isEmpty()) {
            options.setUserName(config.getUsername());
        }
        if (config.getPassword() != null && !config.getPassword().trim().isEmpty()) {
            options.setPassword(config.getPassword().toCharArray());
        }

        if (mqttClientProperties.isAutomaticReconnect()) {
            options.setMaxReconnectDelay(mqttClientProperties.getMaxReconnectDelay());
        }

        return options;
    }

    /**
     * 初始化配置驱动的订阅
     */
    private void initializeConfigSubscriptions() {
        if (!mqttClientProperties.getDynamicSubscriptions().isEnabled()) {
            logger.info("动态订阅功能已禁用");
            return;
        }

        List<MqttClientProperties.SubscriptionConfig> initialSubscriptions = mqttClientProperties
                .getDynamicSubscriptions().getInitialSubscriptions();
        if (initialSubscriptions != null && !initialSubscriptions.isEmpty()) {
            logger.info("开始初始化配置驱动的订阅，数量: {}", initialSubscriptions.size());

            for (MqttClientProperties.SubscriptionConfig config : initialSubscriptions) {
                boolean success = subscriptionManager.addSubscriptionViaConfig(
                        config.getTopic(), config.getQos(), config.getProcessorType());

                if (success) {
                    logger.info("初始化订阅成功: topic={}, qos={}, processor={}",
                            config.getTopic(), config.getQos(), config.getProcessorType());
                } else {
                    logger.error("初始化订阅失败: topic={}", config.getTopic());
                }
            }

            logger.info("配置驱动的订阅初始化完成");
        } else {
            logger.info("未配置初始订阅列表");
        }
    }

    /**
     * 订阅主题（在所有可订阅的客户端上订阅）
     */
    public boolean subscribe(String topic, int qos, String processorType) {
        return subscriptionManager.addSubscriptionViaApi(topic, qos, processorType);
    }

    /**
     * 订阅主题（指定Broker地址）
     *
     * @param brokerUrl 目标Broker地址，null表示所有可订阅的Broker
     */
    public boolean subscribe(String topic, int qos, String processorType, String brokerUrl) {
        return subscriptionManager.addSubscriptionViaApi(topic, qos, processorType, brokerUrl);
    }

    /**
     * 取消订阅
     */
    public boolean unsubscribe(String topic) {
        return subscriptionManager.removeSubscription(topic);
    }

    /**
     * 发布消息（在所有已连接的客户端上发布）
     */
    public boolean publish(String topic, String payload, int qos) {
        return publishOnAllClients(topic, payload, qos, false);
    }

    public boolean publish(String topic, String payload, int qos, boolean retained) {
        return publishOnAllClients(topic, payload, qos, retained);
    }

    public boolean publish(String topic, String payload) {
        return publishOnAllClients(topic, payload,
                mqttClientProperties.getPublish().getDefaultQos(),
                mqttClientProperties.getPublish().isRetained());
    }

    /**
     * 在所有可发布的客户端上发布消息
     */
    private boolean publishOnAllClients(String topic, String payload, int qos, boolean retained) {
        boolean anySuccess = false;
        for (MqttClient client : getPublishableClients()) {
            try {
                if (client == null || !client.isConnected()) {
                    continue;
                }
                MqttMessage message = new MqttMessage(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                message.setQos(qos);
                message.setRetained(retained);
                client.publish(topic, message);
                anySuccess = true;
            } catch (MqttException e) {
                logger.error("发布消息失败: broker={}, topic={}, error={}", client.getServerURI(), topic, e.getMessage());
            }
        }
        return anySuccess;
    }

    /**
     * 是否任意一个客户端已连接
     */
    public boolean isConnected() {
        return mqttClients.stream().anyMatch(c -> c != null && c.isConnected());
    }

    /**
     * 获取首个客户端ID（兼容旧接口）
     */
    public String getClientId() {
        return mqttClients.isEmpty() ? null : mqttClients.get(0).getClientId();
    }

    /**
     * 获取首个已连接的客户端实例（兼容旧接口）
     */
    public MqttClient getMqttClient() {
        return mqttClients.stream()
                .filter(c -> c != null && c.isConnected())
                .findFirst()
                .orElse(mqttClients.isEmpty() ? null : mqttClients.get(0));
    }

    /**
     * 获取所有客户端实例
     */
    public List<MqttClient> getAllMqttClients() {
        return Collections.unmodifiableList(mqttClients);
    }

    /**
     * 按Broker URL查找客户端实例
     */
    public MqttClient getClientByUrl(String brokerUrl) {
        if (brokerUrl == null || brokerUrl.trim().isEmpty()) {
            return null;
        }
        return mqttClients.stream()
                .filter(c -> c != null && c.isConnected()
                        && c.getServerURI().equals(brokerUrl))
                .findFirst().orElse(null);
    }

    /**
     * 按Broker URL或配置name查找客户端实例。
     */
    public MqttClient getClientByBroker(String broker) {
        if (broker == null || broker.trim().isEmpty()) {
            return null;
        }
        String target = broker.trim();
        MqttClient byUrl = getClientByUrl(target);
        if (byUrl != null) {
            return byUrl;
        }
        MqttClientProperties.BrokerConfig config = brokerConfigByName.get(target);
        if (config == null || config.getUrl() == null) {
            return null;
        }
        return getClientByUrl(config.getUrl());
    }

    /**
     * 获取可订阅的客户端列表（角色为 SUBSCRIBE_ONLY 或 BOTH）
     */
    public List<MqttClient> getSubscribableClients() {
        return mqttClients.stream()
                .filter(c -> c != null && c.isConnected() && isSubscribable(c.getServerURI()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取可发布的客户端列表（角色为 PUBLISH_ONLY 或 BOTH）
     */
    public List<MqttClient> getPublishableClients() {
        return mqttClients.stream()
                .filter(c -> c != null && c.isConnected() && isPublishable(c.getServerURI()))
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean isSubscribable(String brokerUrl) {
        MqttClientProperties.BrokerConfig cfg = brokerConfigByUrl.get(brokerUrl);
        if (cfg == null || cfg.getRole() == null) {
            return true; // 兼容：未知角色默认可订阅
        }
        return cfg.getRole() == MqttClientProperties.BrokerRole.BOTH
                || cfg.getRole() == MqttClientProperties.BrokerRole.SUBSCRIBE_ONLY;
    }

    private boolean isPublishable(String brokerUrl) {
        MqttClientProperties.BrokerConfig cfg = brokerConfigByUrl.get(brokerUrl);
        if (cfg == null || cfg.getRole() == null) {
            return true; // 兼容：未知角色默认可发布
        }
        return cfg.getRole() == MqttClientProperties.BrokerRole.BOTH
                || cfg.getRole() == MqttClientProperties.BrokerRole.PUBLISH_ONLY;
    }

    public LocalDateTime getConnectTime() {
        return connectTimes.values().stream().findFirst().orElse(null);
    }

    public LocalDateTime getLastReconnectTime() {
        return lastReconnectTimes.values().stream().findFirst().orElse(null);
    }

    public int getReconnectAttempts() {
        return reconnectAttemptCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 手动重连所有客户端
     */
    public boolean reconnect() {
        List<MqttClientProperties.BrokerConfig> brokerConfigs = getBrokerConfigs();
        boolean allSuccess = true;
        for (int i = 0; i < mqttClients.size(); i++) {
            MqttClient client = mqttClients.get(i);
            MqttClientProperties.BrokerConfig config = i < brokerConfigs.size() ? brokerConfigs.get(i) : brokerConfigs.get(0);
            try {
                if (client.isConnected()) {
                    client.disconnect();
                    Thread.sleep(1000);
                }
                connectClient(client, config);
            } catch (Exception e) {
                logger.error("手动重连失败: broker={}", config.getUrl(), e);
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    /**
     * 断开所有连接
     */
    @PreDestroy
    public void disconnect() {
        for (MqttClient client : mqttClients) {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                    logger.info("MQTT客户端已断开连接: {}", client.getServerURI());
                }
            } catch (Exception e) {
                logger.error("断开MQTT连接失败: {}", client.getServerURI(), e);
            } finally {
                try {
                    client.close();
                } catch (Exception e) {
                    logger.error("关闭MQTT客户端失败: {}", client.getServerURI(), e);
                }
            }
        }
        mqttClients.clear();
        brokerConfigByUrl.clear();
        brokerConfigByName.clear();
    }
}
