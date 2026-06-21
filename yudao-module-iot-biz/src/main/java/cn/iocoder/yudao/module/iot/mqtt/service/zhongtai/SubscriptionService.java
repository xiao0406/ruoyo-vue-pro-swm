package cn.iocoder.yudao.module.iot.mqtt.service.zhongtai;

import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.manager.MqttSubscriptionManager;
import cn.iocoder.yudao.module.iot.mqtt.service.MqttClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 动态 MQTT 订阅服务
 * 支持基于通配符的动态订阅管理
 *
 * @author wxy
 * @date 2025-07-21
 */
@Service
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class SubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    private static final String LOG_PREFIX = "【中泰-MQTT消息-订阅服务】- ";

    @Resource
    private MqttClientService mqttClientService;

    @Resource
    private MqttSubscriptionManager subscriptionManager;

    //消息分发处理器
    private final static String processorType = "zhongtai_message_dispatcher";

    /**
     * 中台订阅固定使用 brokers[].name = imdm 的 Broker。
     */
    private static final String TARGET_BROKER = "imdm";

    /**
     * 应用就绪后初始化动态订阅（须在 {@link MqttClientService} 完成连接之后执行）。
     * 说明：若放在 {@code @PostConstruct}，会早于 MQTT 连接而误判"未连接"并跳过全部通配符订阅。
     */
    @Order(2)
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info(LOG_PREFIX + "动态 MQTT 订阅服务初始化...");

        if (!mqttClientService.isConnected()) {
            logger.error(LOG_PREFIX + "MQTT 客户端未连接，无法初始化动态订阅（请检查连接配置与 broker 可用性）");
            return;
        }

        initializeDynamicSubscriptions();
    }


    /**
     * 初始化动态订阅
     */
    private void initializeDynamicSubscriptions() {
        logger.info(LOG_PREFIX + "开始初始化动态订阅...");

        // 设备在线状态
        subscribeToAllDeviceOnlineStatus();

        logger.info("开始初始化动态订阅...");
        subscribeToAllDeviceRunStatus();
        // 设备电量信息
        subscribeToAllDeviceBatteryStatus();
        // 设备蓝牙信息
        subscribeToAllDeviceIbeaconStatus();
        //设备GPS定位
        subscribeToAllDeviceLocations();
        // 设备SOS告警
        subscribeToAllDeviceAlarms(MqttConstants.SOS);
        //设备脱帽报警
        subscribeToAllDeviceAlarms(MqttConstants.UNBONNET);
        //设备掉落报警
        subscribeToAllDeviceAlarms(MqttConstants.FALL);


        logger.info(LOG_PREFIX + "动态订阅初始化完成，当前订阅数：{}", subscriptionManager.getSubscriptionCount());
    }

    /**
     * 订阅所有设备的在线状态
     * 使用通配符主题：/cmt/IoT/pub/+/+/status/online
     */
    public boolean subscribeToAllDeviceOnlineStatus() {
        String topic = MqttConstants.getOnlineStatusWildcardTopic();
        int qos = 1; // 使用 QoS 1，确保消息至少送达一次
        logger.info(LOG_PREFIX + "订阅所有设备在线状态：topic={}, qos={}, processor={}, broker={}", topic, qos, processorType, TARGET_BROKER);

        return mqttClientService.subscribe(topic, qos, processorType, TARGET_BROKER);
    }


    /**
     * 订阅特定用户的设备在线状态
     *
     * @param userSip 用户 SIP
     * @return 是否成功
     */
    public boolean subscribeToDeviceOnlineStatus(String userSip) {
        String topic = MqttConstants.getOnlineStatusTopic(userSip);
        int qos = 1;
        logger.info(LOG_PREFIX + "订阅用户 {} 的设备在线状态：topic={}", userSip, topic);

        return mqttClientService.subscribe(topic, qos, processorType, TARGET_BROKER);
    }


    /**
     * 订阅所有设备的运行状态
     */
    public boolean subscribeToAllDeviceRunStatus() {
        String wildcardTopic = MqttConstants.getRunStatusWildcardTopic();
        int qos = 1;
        logger.info(LOG_PREFIX + "订阅所有设备运行状态：topic={}, qos={}, processor={}",
                wildcardTopic, qos, processorType, TARGET_BROKER);

        return mqttClientService.subscribe(wildcardTopic, qos, processorType, TARGET_BROKER);
    }

    /**
     * 订阅所有设备的电量信息
     */
    public boolean subscribeToAllDeviceBatteryStatus() {
        String wildcardTopic = MqttConstants.getSensorWildcardTopic(MqttConstants.BATTERY);
        int qos = 1;
        logger.info(LOG_PREFIX + "订阅所有设备电量信息：topic={}, qos={}, processor={}",
                wildcardTopic, qos, processorType, TARGET_BROKER);

        return mqttClientService.subscribe(wildcardTopic, qos, processorType, TARGET_BROKER);
    }

    /**
     * 订阅所有设备的蓝牙信息
     */
    public boolean subscribeToAllDeviceIbeaconStatus() {
        String wildcardTopic = MqttConstants.getSensorWildcardTopic(MqttConstants.IBEACON);
        int qos = 1;

        logger.info(LOG_PREFIX + "订阅所有设备的蓝牙信息：topic={}, qos={}, processor={}",
                wildcardTopic, qos, processorType, TARGET_BROKER);

        return mqttClientService.subscribe(wildcardTopic, qos, processorType, TARGET_BROKER);
    }

    /**
     * 订阅所有设备的 GPS 定位信息
     */
    public boolean subscribeToAllDeviceLocations() {
        String wildcardTopic = MqttConstants.getLocationWildcardTopic(MqttConstants.GPS);
        int qos = 1;

        logger.info(LOG_PREFIX + "订阅所有设备定位信息：topic={}, qos={}, processor={}",
                wildcardTopic, qos, processorType, TARGET_BROKER);

        return mqttClientService.subscribe(wildcardTopic, qos, processorType, TARGET_BROKER);
    }

    /**
     * 订阅所有设备的 告警
     */
    public boolean subscribeToAllDeviceAlarms(String alarmType) {
        String wildcardTopic = MqttConstants.getAlarmWildcardTopic(alarmType);
        int qos = 2; // SOS 告警使用 QoS 1 确保送达

        logger.info(LOG_PREFIX + "订阅所有设备的告警：topic={}, qos={}, processor={},报警类型={}",
                wildcardTopic, qos, processorType, alarmType);

        return mqttClientService.subscribe(wildcardTopic, qos, processorType, TARGET_BROKER);
    }

    /**
     * 自定义订阅
     *
     * @param topic         主题（支持通配符）
     * @param qos           QoS 级别
     * @param processorType 处理器类型
     * @return 是否成功
     */
    public boolean subscribeCustom(String topic, int qos, String processorType) {
        logger.info(LOG_PREFIX + "自定义订阅：topic={}, qos={}, processor={}, broker={}", topic, qos, processorType, TARGET_BROKER);

        return mqttClientService.subscribe(topic, qos, processorType, TARGET_BROKER);
    }

}
