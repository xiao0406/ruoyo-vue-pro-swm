package cn.iocoder.yudao.module.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MQTT客户端配置属性类
 * 支持Nacos配置中心动态配置
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Component
@ConfigurationProperties(prefix = "iot.mqtt")
@Data
public class MqttClientProperties {

    /**
     * 是否启用MQTT客户端
     */
    private boolean enabled;

    /**
     * MQTT Broker地址（单地址，兼容模式，共用全局clientId/username/password）
     */
    private String brokerUrl;

    /**
     * 多Broker并发连接列表（每个Broker独立的认证信息）
     * 配置此项后，每个Broker创建独立客户端同时连接接收数据。
     * 若同时配置了 brokerUrl，brokers 优先。
     */
    private List<BrokerConfig> brokers = new ArrayList<>();

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 是否在配置的客户端ID后追加运行时唯一后缀，避免多实例或本地重复启动时互相踢下线。
     */
    private boolean appendUniqueSuffixToClientId = true;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 清除会话标志，cleanSession=false ：确保Broker在客户端断开连接后保留消息，当客户端重新连接时，Broker会将这些消息发送给客户端。
     */
    private boolean cleanSession = false;

    /**
     * 连接超时时间（秒）
     */
    private int connectionTimeout = 30;

    /**
     * 心跳间隔（秒）
     */
    private int keepAliveInterval = 30;

    /**
     * 最大飞行中消息数
     */
    private int maxInflight = 200;

    /**
     * 自动重连
     */
    private boolean automaticReconnect = true;

    /**
     * 最大重连延迟（毫秒）
     */
    private int maxReconnectDelay = 128000;

    /**
     * 线程池配置
     */
    private ThreadPool threadPool = new ThreadPool();

    /**
     * 动态订阅配置
     */
    private DynamicSubscriptions dynamicSubscriptions = new DynamicSubscriptions();

    /**
     * 发布配置
     */
    private Publish publish = new Publish();

    /**
     * 日志配置
     */
    private Logging logging = new Logging();

    /**
     * 线程池配置类
     */
    @Data
    public static class ThreadPool {
        private int corePoolSize = 20;
        private int maxPoolSize = 100;
        private int queueCapacity = 10000;
        private String threadNamePrefix = "mqtt-processor-";
        private int keepAliveSeconds = 600;
        private boolean allowCoreThreadTimeout = true;
    }

    /**
     * Broker角色枚举
     */
    public enum BrokerRole {
        /** 只订阅，不发布 */
        SUBSCRIBE_ONLY,
        /** 只发布，不订阅 */
        PUBLISH_ONLY,
        /** 发布+订阅（默认，兼容存量配置） */
        BOTH
    }

    /**
     * 单个Broker配置（含独立认证信息）
     */
    @Data
    public static class BrokerConfig {
        /** 别名，用于代码中按名称查找Broker（可选） */
        private String name;
        private String url;
        private String clientId;
        private String username;
        private String password;
        /** Broker角色，默认BOTH兼容存量配置 */
        private BrokerRole role = BrokerRole.BOTH;
    }

    /**
     * 动态订阅配置类
     */
    @Data
    public static class DynamicSubscriptions {
        private boolean enabled = true;
        private List<SubscriptionConfig> initialSubscriptions = new ArrayList<>();
        private Management management = new Management();

        /**
         * 订阅管理配置类
         */
        @Data
        public static class Management {
            private int healthCheckInterval = 300;
            private boolean autoResubscribe = true;
            private int maxRetryAttempts = 3;
            private int retryDelay = 5000;
        }
    }

    /**
     * 订阅配置类
     */
    @Data
    public static class SubscriptionConfig {
        private String topic;
        private int qos;
        private String processorType;
    }

    /**
     * 发布配置类
     */
    @Data
    public static class Publish {
        private int defaultQos = 1;
        private boolean retained = false;
        private int timeout = 5000;
        private boolean asyncEnabled = true;
    }

    /**
     * 日志配置类
     */
    @Data
    public static class Logging {
        private boolean enabled = true;
        private String logLevel = "INFO";
        private boolean logReceivedMessages = true;
        private boolean logPublishedMessages = true;
        private int maxMessageLength = 1000;
    }
}
