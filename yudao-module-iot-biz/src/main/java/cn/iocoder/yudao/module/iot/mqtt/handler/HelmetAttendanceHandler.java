package cn.iocoder.yudao.module.iot.mqtt.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.iocoder.yudao.module.iot.service.HelmetDeviceCacheService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.iot.util.R;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 头盔考勤业务处理器
 * 处理头盔设备的考勤打卡MQTT消息
 *
 * @author Shawn
 * @date 2025-07-23
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class HelmetAttendanceHandler implements MqttBusinessHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelmetAttendanceHandler.class);

    @Resource
    private HelmetDeviceCacheService helmetDeviceCacheService;

    @Resource
    private SwmWarningManagementService swmWarningManagementService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        try {
            // 检查主题是否匹配
            if (!topic.contains("connect_packet/adv_publish")) {
                return false;
            }

            // 检查消息内容是否包含upload_datas字段
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            JsonNode rootNode = objectMapper.readTree(payload);

            return rootNode.has("upload_datas") && rootNode.get("upload_datas").isArray();

        } catch (Exception e) {
            logger.debug("判断是否为头盔考勤消息时发生异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void handle(String topic, MqttMessage message) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // 解析MQTT消息
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            JsonNode rootNode = objectMapper.readTree(payload);

            // 提取基础信息
            String deviceId = extractDeviceId(rootNode);
            if (StringUtils.isBlank(deviceId)) {
                logger.warn("MQTT消息中未找到有效的设备ID，跳过处理");
                return;
            }

            // 提取upload_datas数组
            JsonNode uploadDatasNode = rootNode.get("upload_datas");
            if (uploadDatasNode == null || !uploadDatasNode.isArray()) {
                logger.warn("MQTT消息中未找到upload_datas数组，跳过处理");
                return;
            }

            // 提取并去重MAC地址列表
            List<String> macAddresses = extractAndDeduplicateMacAddresses(uploadDatasNode);
            if (macAddresses.isEmpty()) {
                return; // 静默跳过，不打印日志
            }

            // 批量处理考勤打卡
            processAttendanceForMacAddresses(deviceId, macAddresses, startTime);

        } catch (Exception e) {
            logger.error("处理头盔考勤MQTT消息异常: topic={}, error={}", topic, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 提取设备ID
     * 优先从base_info.id获取，如果没有则尝试从其他字段获取
     */
    private String extractDeviceId(JsonNode rootNode) {
        // 优先从base_info.id获取
        if (rootNode.has("base_info")) {
            JsonNode baseInfoNode = rootNode.get("base_info");
            if (baseInfoNode.has("id")) {
                String deviceId = baseInfoNode.get("id").asText();
                if (StringUtils.isNotBlank(deviceId)) {
                    return deviceId;
                }
            }
        }

        // 如果base_info.id不存在，尝试从根节点的device_id字段获取
        if (rootNode.has("device_id")) {
            String deviceId = rootNode.get("device_id").asText();
            if (StringUtils.isNotBlank(deviceId)) {
                return deviceId;
            }
        }

        return null;
    }

    /**
     * 从upload_datas数组中提取并去重MAC地址列表
     */
    private List<String> extractAndDeduplicateMacAddresses(JsonNode uploadDatasNode) {
        Set<String> macSet = new LinkedHashSet<>(); // 使用LinkedHashSet保持顺序并去重

        for (JsonNode dataNode : uploadDatasNode) {
            if (dataNode.has("mac")) {
                String mac = dataNode.get("mac").asText();
                if (StringUtils.isNotBlank(mac)) {
                    macSet.add(mac.trim());
                }
            }
        }

        return new ArrayList<>(macSet);
    }

    /**
     * 批量处理MAC地址的考勤打卡
     */
    private void processAttendanceForMacAddresses(String deviceId, List<String> macAddresses, long startTime) {
        int successCount = 0;
        int validMacCount = 0;

        // 统计有效MAC数量（能找到人员信息的）
        for (String macAddress : macAddresses) {
            String identityCard = helmetDeviceCacheService.getPersonIdentityByMac(macAddress, deviceId);
            if (StringUtils.isNotBlank(identityCard)) {
                validMacCount++;
            }
        }

        // 如果没有有效MAC，静默返回
        if (validMacCount == 0) {
            return;
        }

        // 打印开始处理日志
        logger.info("头盔考勤处理: 设备ID={}, 去重后MAC={}, 有效={}",
                deviceId, macAddresses.size(), validMacCount);

        // 处理每个MAC地址
        for (String macAddress : macAddresses) {
            try {
                boolean success = processAttendanceForSingleMac(deviceId, macAddress);
                if (success) {
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("处理MAC地址 {} 的考勤打卡异常: {}", macAddress, e.getMessage(), e);
            }
        }

        // 计算耗时
        long duration = System.currentTimeMillis() - startTime;

        // 打印完成日志
        if (successCount > 0) {
            logger.info("头盔考勤完成: 成功={}, 耗时={}ms", successCount, duration);
        } else {
            logger.info("头盔考勤完成: 无成功打卡, 耗时={}ms", duration);
        }
    }

    /**
     * 处理单个MAC地址的考勤打卡
     */
    private boolean processAttendanceForSingleMac(String deviceId, String macAddress) {
        try {
            // 从Redis缓存中查询人员身份证号码
            String identityCard = helmetDeviceCacheService.getPersonIdentityByMac(macAddress,deviceId);

            if (StringUtils.isBlank(identityCard)) {
                // 静默跳过，不打印任何日志
                return false;
            }

            // 调用考勤打卡方法
            R<cn.hutool.json.JSONObject> result = swmWarningManagementService.updateAttendanceRecord(deviceId,
                    identityCard);

            if (result.getCode() == R.SUCCESS) {
                logger.info("考勤打卡成功: MAC={} → 身份证={}", macAddress, identityCard);
                return true;
            } else {
                logger.warn("考勤打卡失败: MAC={}, 身份证={}, 错误={}",
                        macAddress, identityCard, result.getMsg());
                return false;
            }

        } catch (Exception e) {
            logger.error("考勤打卡异常: MAC={}, 错误={}", macAddress, e.getMessage(), e);
            return false;
        }
    }
}
