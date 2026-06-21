package cn.iocoder.yudao.module.iot.mqtt.controller;

import cn.iocoder.yudao.module.iot.mqtt.dto.PublishRequest;
import cn.iocoder.yudao.module.iot.mqtt.service.MqttPublishService;
import cn.iocoder.yudao.module.iot.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MQTT发布管理控制器
 * 提供消息发布API接口
 *
 * @author Shawn
 * @date 2025-07-21
 */
@RestController
@RequestMapping("${adminPath}/api/mqtt/publish")
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MqttPublishController {

    private static final Logger logger = LoggerFactory.getLogger(MqttPublishController.class);

    @Resource
    private MqttPublishService publishService;

    /**
     * 发布消息
     *
     * @param request 发布请求
     * @return 操作结果
     */
    @PostMapping("/send")
    public R<Boolean> publishMessage(@RequestBody @Valid PublishRequest request) {
        logger.info("收到发布消息请求: topic={}, qos={}, retained={}, payloadLength={}",
                request.getTopic(), request.getQos(), request.isRetained(),
                request.getPayload() != null ? request.getPayload().length() : 0);

        try {
            boolean success = publishService.publish(
                    request.getTopic(),
                    request.getPayload(),
                    request.getQos(),
                    request.isRetained());

            if (success) {
                logger.info("发布消息成功: topic={}", request.getTopic());
                return R.ok(true);
            } else {
                logger.error("发布消息失败: topic={}", request.getTopic());
                return R.fail("消息发布失败");
            }
        } catch (Exception e) {
            logger.error("发布消息异常: topic={}, error={}", request.getTopic(), e.getMessage(), e);
            return R.fail("消息发布异常: " + e.getMessage());
        }
    }

    /**
     * 批量发布消息
     *
     * @param requests 发布请求列表
     * @return 操作结果映射
     */
    @PostMapping("/batch/send")
    public R<Map<String, Boolean>> publishMessages(@RequestBody @Valid List<PublishRequest> requests) {
        logger.info("收到批量发布消息请求，数量: {}", requests.size());

        try {
            Map<String, Boolean> results = publishService.publishBatch(requests);

            long successCount = results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
            logger.info("批量发布消息完成: 总数={}, 成功={}, 失败={}",
                    requests.size(), successCount, requests.size() - successCount);

            return R.ok(results);
        } catch (Exception e) {
            logger.error("批量发布消息异常: {}", e.getMessage(), e);
            return R.fail("批量发布消息失败: " + e.getMessage());
        }
    }

    /**
     * 发布简单文本消息
     *
     * @param topic    主题
     * @param message  消息内容
     * @param qos      QoS级别（可选，默认1）
     * @param retained 是否保留（可选，默认false）
     * @return 操作结果
     */
    @PostMapping("/send/text")
    public R<Boolean> publishTextMessage(
            @RequestParam String topic,
            @RequestParam String message,
            @RequestParam(defaultValue = "1") int qos,
            @RequestParam(defaultValue = "false") boolean retained) {

        logger.info("收到发布文本消息请求: topic={}, qos={}, retained={}, messageLength={}",
                topic, qos, retained, message != null ? message.length() : 0);

        try {
            boolean success = publishService.publish(topic, message, qos, retained);

            if (success) {
                logger.info("发布文本消息成功: topic={}", topic);
                return R.ok(true);
            } else {
                logger.error("发布文本消息失败: topic={}", topic);
                return R.fail("消息发布失败");
            }
        } catch (Exception e) {
            logger.error("发布文本消息异常: topic={}, error={}", topic, e.getMessage(), e);
            return R.fail("消息发布异常: " + e.getMessage());
        }
    }

    /**
     * 发布JSON消息
     *
     * @param topic       主题
     * @param jsonPayload JSON消息内容
     * @param qos         QoS级别（可选，默认1）
     * @return 操作结果
     */
    @PostMapping("/send/json")
    public R<Boolean> publishJsonMessage(
            @RequestParam String topic,
            @RequestBody String jsonPayload,
            @RequestParam(defaultValue = "1") int qos) {

        logger.info("收到发布JSON消息请求: topic={}, qos={}, payloadLength={}",
                topic, qos, jsonPayload != null ? jsonPayload.length() : 0);

        try {
            boolean success = publishService.publishJson(topic, jsonPayload, qos);

            if (success) {
                logger.info("发布JSON消息成功: topic={}", topic);
                return R.ok(true);
            } else {
                logger.error("发布JSON消息失败: topic={}", topic);
                return R.fail("JSON消息发布失败");
            }
        } catch (Exception e) {
            logger.error("发布JSON消息异常: topic={}, error={}", topic, e.getMessage(), e);
            return R.fail("JSON消息发布异常: " + e.getMessage());
        }
    }

    /**
     * 异步发布消息
     *
     * @param request 发布请求
     * @return 操作结果
     */
    @PostMapping("/send/async")
    public R<String> publishMessageAsync(@RequestBody @Valid PublishRequest request) {
        logger.info("收到异步发布消息请求: topic={}, qos={}, retained={}",
                request.getTopic(), request.getQos(), request.isRetained());

        try {
            CompletableFuture<Boolean> future = publishService.publishAsync(
                    request.getTopic(),
                    request.getPayload(),
                    request.getQos(),
                    request.isRetained());

            // 异步处理，立即返回
            future.whenComplete((success, throwable) -> {
                if (throwable != null) {
                    logger.error("异步发布消息失败: topic={}, error={}",
                            request.getTopic(), throwable.getMessage(), throwable);
                } else if (success) {
                    logger.info("异步发布消息成功: topic={}", request.getTopic());
                } else {
                    logger.error("异步发布消息失败: topic={}", request.getTopic());
                }
            });

            return R.ok("异步发布已提交");
        } catch (Exception e) {
            logger.error("提交异步发布消息异常: topic={}, error={}", request.getTopic(), e.getMessage(), e);
            return R.fail("提交异步发布失败: " + e.getMessage());
        }
    }

    /**
     * 异步批量发布消息
     *
     * @param requests 发布请求列表
     * @return 操作结果
     */
    @PostMapping("/batch/send/async")
    public R<String> publishMessagesAsync(@RequestBody @Valid List<PublishRequest> requests) {
        logger.info("收到异步批量发布消息请求，数量: {}", requests.size());

        try {
            CompletableFuture<Map<String, Boolean>> future = publishService.publishBatchAsync(requests);

            // 异步处理，立即返回
            future.whenComplete((results, throwable) -> {
                if (throwable != null) {
                    logger.error("异步批量发布消息失败: error={}", throwable.getMessage(), throwable);
                } else {
                    long successCount = results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
                    logger.info("异步批量发布消息完成: 总数={}, 成功={}, 失败={}",
                            requests.size(), successCount, requests.size() - successCount);
                }
            });

            return R.ok("异步批量发布已提交");
        } catch (Exception e) {
            logger.error("提交异步批量发布消息异常: {}", e.getMessage(), e);
            return R.fail("提交异步批量发布失败: " + e.getMessage());
        }
    }

    /**
     * 获取发布统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public R<Map<String, Object>> getPublishStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("publishSuccessCount", publishService.getPublishSuccessCount());
            statistics.put("publishFailureCount", publishService.getPublishFailureCount());
            statistics.put("totalPublishCount",
                    publishService.getPublishSuccessCount() + publishService.getPublishFailureCount());

            logger.info("获取发布统计信息成功");
            return R.ok(statistics);
        } catch (Exception e) {
            logger.error("获取发布统计信息异常: {}", e.getMessage(), e);
            return R.fail("获取发布统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 重置发布统计计数器
     *
     * @return 操作结果
     */
    @PostMapping("/statistics/reset")
    public R<Boolean> resetPublishStatistics() {
        try {
            publishService.resetCounters();
            logger.info("重置发布统计计数器成功");
            return R.ok(true);
        } catch (Exception e) {
            logger.error("重置发布统计计数器异常: {}", e.getMessage(), e);
            return R.fail("重置统计计数器失败: " + e.getMessage());
        }
    }
}
