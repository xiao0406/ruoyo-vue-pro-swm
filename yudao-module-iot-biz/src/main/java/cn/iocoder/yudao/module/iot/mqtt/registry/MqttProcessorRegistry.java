package cn.iocoder.yudao.module.iot.mqtt.registry;

import cn.iocoder.yudao.module.iot.mqtt.processor.*;
//import cn.iocoder.yudao.module.iot.mqtt.processor.LoggingMessageProcessor;
import cn.iocoder.yudao.module.iot.mqtt.processor.zhongtai.ZhongtaiMessageDispatcherProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * MQTT消息处理器注册中心
 * 管理所有可用的消息处理器
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MqttProcessorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(MqttProcessorRegistry.class);

    /**
     * 处理器存储映射
     */
    private final Map<String, MqttMessageProcessor> processors = new HashMap<>();

    @Resource
    private MessageDispatcherProcessor messageDispatcherProcessor;
    @Resource
    private ZhongtaiMessageDispatcherProcessor zhongtaiMessageDispatcherProcessor;

    @PostConstruct
    public void init() {
        // 注册默认处理器
        registerDefaultProcessors();
        logger.info("MQTT消息处理器注册中心初始化完成，已注册处理器: {}", processors.keySet());
    }

    /**
     * 注册默认处理器
     */
    private void registerDefaultProcessors() {
        registerProcessor("default", new DefaultMessageProcessor());
        registerProcessor("logging", new LoggingMessageProcessor());
        registerProcessor("custom", new CustomMessageProcessor());
        //注释掉，现在不用
//        registerProcessor("message_dispatcher", messageDispatcherProcessor);
        //中泰消息分发器
        registerProcessor("zhongtai_message_dispatcher", zhongtaiMessageDispatcherProcessor);
    }

    /**
     * 注册处理器
     *
     * @param type      处理器类型
     * @param processor 处理器实例
     */
    public void registerProcessor(String type, MqttMessageProcessor processor) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("处理器类型不能为空");
        }

        if (processor == null) {
            throw new IllegalArgumentException("处理器实例不能为空");
        }

        processors.put(type, processor);
        logger.info("注册MQTT消息处理器: type={}, class={}", type, processor.getClass().getSimpleName());
    }

    /**
     * 创建处理器实例
     *
     * @param type 处理器类型
     * @return 处理器实例，如果类型不存在则返回null
     */
    public MqttMessageProcessor createProcessor(String type) {
        if (type == null || type.trim().isEmpty()) {
            logger.warn("处理器类型为空，返回默认处理器");
            return processors.get("default");
        }

        MqttMessageProcessor processor = processors.get(type);
        if (processor == null) {
            logger.warn("未找到处理器类型: {}，返回默认处理器", type);
            return processors.get("default");
        }

        return processor;
    }

    /**
     * 获取所有处理器类型
     *
     * @return 处理器类型集合
     */
    public Set<String> getProcessorTypes() {
        return processors.keySet();
    }

    /**
     * 检查处理器类型是否存在
     *
     * @param type 处理器类型
     * @return 是否存在
     */
    public boolean hasProcessor(String type) {
        return processors.containsKey(type);
    }

    /**
     * 移除处理器
     *
     * @param type 处理器类型
     * @return 被移除的处理器实例，如果不存在则返回null
     */
    public MqttMessageProcessor removeProcessor(String type) {
        MqttMessageProcessor removed = processors.remove(type);
        if (removed != null) {
            logger.info("移除MQTT消息处理器: type={}", type);
        }
        return removed;
    }

    /**
     * 获取已注册的处理器数量
     *
     * @return 处理器数量
     */
    public int getProcessorCount() {
        return processors.size();
    }
}
