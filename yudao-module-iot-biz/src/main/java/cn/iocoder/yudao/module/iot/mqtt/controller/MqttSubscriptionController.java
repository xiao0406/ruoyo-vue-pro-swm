package cn.iocoder.yudao.module.iot.mqtt.controller;

import cn.iocoder.yudao.module.iot.mqtt.dto.SubscriptionRequest;
import cn.iocoder.yudao.module.iot.mqtt.entity.MqttSubscription;
import cn.iocoder.yudao.module.iot.mqtt.entity.SubscriptionSource;
import cn.iocoder.yudao.module.iot.mqtt.manager.MqttSubscriptionManager;
import cn.iocoder.yudao.module.iot.mqtt.registry.MqttProcessorRegistry;
import cn.iocoder.yudao.module.iot.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MQTT订阅管理控制器
 * 提供订阅的增删改查API接口
 *
 * @author Shawn
 * @date 2025-07-21
 */
@RestController
@RequestMapping("${adminPath}/api/mqtt/subscription")
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MqttSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriptionController.class);

    @Resource
    private MqttSubscriptionManager subscriptionManager;

    @Resource
    private MqttProcessorRegistry processorRegistry;

    /**
     * 添加订阅
     *
     * @param request 订阅请求
     * @return 操作结果
     */
    @PostMapping("/add")
    public R<Boolean> addSubscription(@RequestBody @Valid SubscriptionRequest request) {
        logger.info("收到添加订阅请求: topic={}, qos={}, processorType={}",
                request.getTopic(), request.getQos(), request.getProcessorType());

        try {
            boolean success = subscriptionManager.addSubscriptionViaApi(
                    request.getTopic(),
                    request.getQos(),
                    request.getProcessorType());

            if (success) {
                logger.info("添加订阅成功: topic={}", request.getTopic());
                return R.ok(true);
            } else {
                logger.error("添加订阅失败: topic={}", request.getTopic());
                return R.fail("订阅失败");
            }
        } catch (Exception e) {
            logger.error("添加订阅异常: topic={}, error={}", request.getTopic(), e.getMessage(), e);
            return R.fail("订阅异常: " + e.getMessage());
        }
    }

    /**
     * 删除订阅
     *
     * @param topic 主题（URL编码）
     * @return 操作结果
     */
    @DeleteMapping("/remove/{topic}")
    public R<Boolean> removeSubscription(@PathVariable String topic) {
        try {
            // URL解码
            topic = URLDecoder.decode(topic, "UTF-8");
            logger.info("收到删除订阅请求: topic={}", topic);

            boolean success = subscriptionManager.removeSubscription(topic);

            if (success) {
                logger.info("删除订阅成功: topic={}", topic);
                return R.ok(true);
            } else {
                logger.error("删除订阅失败: topic={}", topic);
                return R.fail("取消订阅失败");
            }
        } catch (Exception e) {
            logger.error("删除订阅异常: topic={}, error={}", topic, e.getMessage(), e);
            return R.fail("取消订阅异常: " + e.getMessage());
        }
    }

    /**
     * 获取所有订阅信息
     *
     * @return 订阅列表
     */
    @GetMapping("/list")
    public R<List<MqttSubscription>> getSubscriptions() {
        try {
            List<MqttSubscription> subscriptions = subscriptionManager.getAllSubscriptions();
            logger.info("获取订阅列表成功，数量: {}", subscriptions.size());
            return R.ok(subscriptions);
        } catch (Exception e) {
            logger.error("获取订阅列表异常: {}", e.getMessage(), e);
            return R.fail("获取订阅列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据来源获取订阅信息
     *
     * @param source 订阅来源 (CONFIG/API)
     * @return 订阅列表
     */
    @GetMapping("/list/{source}")
    public R<List<MqttSubscription>> getSubscriptionsBySource(@PathVariable String source) {
        try {
            SubscriptionSource subscriptionSource = SubscriptionSource.valueOf(source.toUpperCase());
            List<MqttSubscription> subscriptions = subscriptionManager.getSubscriptionsBySource(subscriptionSource);

            logger.info("获取{}来源的订阅列表成功，数量: {}", source, subscriptions.size());
            return R.ok(subscriptions);
        } catch (IllegalArgumentException e) {
            logger.error("无效的订阅来源: {}", source);
            return R.fail("无效的订阅来源: " + source);
        } catch (Exception e) {
            logger.error("获取订阅列表异常: source={}, error={}", source, e.getMessage(), e);
            return R.fail("获取订阅列表失败: " + e.getMessage());
        }
    }

    /**
     * 批量添加订阅
     *
     * @param requests 订阅请求列表
     * @return 操作结果映射
     */
    @PostMapping("/batch/add")
    public R<Map<String, Boolean>> addSubscriptions(@RequestBody @Valid List<SubscriptionRequest> requests) {
        logger.info("收到批量添加订阅请求，数量: {}", requests.size());

        try {
            Map<String, Boolean> results = new HashMap<>();

            for (SubscriptionRequest request : requests) {
                boolean success = subscriptionManager.addSubscriptionViaApi(
                        request.getTopic(),
                        request.getQos(),
                        request.getProcessorType());
                results.put(request.getTopic(), success);
            }

            long successCount = results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
            logger.info("批量添加订阅完成: 总数={}, 成功={}, 失败={}",
                    requests.size(), successCount, requests.size() - successCount);

            return R.ok(results);
        } catch (Exception e) {
            logger.error("批量添加订阅异常: {}", e.getMessage(), e);
            return R.fail("批量添加订阅失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除订阅
     *
     * @param topics 主题列表
     * @return 操作结果映射
     */
    @PostMapping("/batch/remove")
    public R<Map<String, Boolean>> removeSubscriptions(@RequestBody List<String> topics) {
        logger.info("收到批量删除订阅请求，数量: {}", topics.size());

        try {
            Map<String, Boolean> results = subscriptionManager.removeSubscriptions(topics);

            long successCount = results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
            logger.info("批量删除订阅完成: 总数={}, 成功={}, 失败={}",
                    topics.size(), successCount, topics.size() - successCount);

            return R.ok(results);
        } catch (Exception e) {
            logger.error("批量删除订阅异常: {}", e.getMessage(), e);
            return R.fail("批量删除订阅失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的处理器类型
     *
     * @return 处理器类型集合
     */
    @GetMapping("/processor-types")
    public R<Set<String>> getProcessorTypes() {
        try {
            Set<String> types = processorRegistry.getProcessorTypes();
            logger.info("获取处理器类型成功，数量: {}", types.size());
            return R.ok(types);
        } catch (Exception e) {
            logger.error("获取处理器类型异常: {}", e.getMessage(), e);
            return R.fail("获取处理器类型失败: " + e.getMessage());
        }
    }

    /**
     * 获取订阅统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public R<Map<String, Object>> getStatistics() {
        try {
            List<MqttSubscription> allSubscriptions = subscriptionManager.getAllSubscriptions();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalSubscriptions", allSubscriptions.size());

            // 按来源分组统计
            Map<String, Long> sourceStats = allSubscriptions.stream()
                    .collect(Collectors.groupingBy(
                            sub -> sub.getSource().name(),
                            Collectors.counting()));
            statistics.put("subscriptionsBySource", sourceStats);

            // 按处理器类型分组统计
            Map<String, Long> processorStats = allSubscriptions.stream()
                    .collect(Collectors.groupingBy(
                            MqttSubscription::getProcessorType,
                            Collectors.counting()));
            statistics.put("subscriptionsByProcessor", processorStats);

            // 消息接收统计
            long totalMessages = allSubscriptions.stream()
                    .mapToLong(MqttSubscription::getMessageCount)
                    .sum();
            statistics.put("totalMessagesReceived", totalMessages);

            logger.info("获取订阅统计信息成功");
            return R.ok(statistics);
        } catch (Exception e) {
            logger.error("获取订阅统计信息异常: {}", e.getMessage(), e);
            return R.fail("获取统计信息失败: " + e.getMessage());
        }
    }
}
