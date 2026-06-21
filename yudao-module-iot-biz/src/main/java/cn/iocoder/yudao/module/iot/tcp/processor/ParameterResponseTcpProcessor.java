package cn.iocoder.yudao.module.iot.tcp.processor;

import cn.iocoder.yudao.module.swm.service.SwmHelmetDeviceService;
import cn.iocoder.yudao.module.swm.service.SwmHelmetDeviceConfigService;
import cn.iocoder.yudao.module.swm.service.HelmetDeviceCacheService;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceConfigDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 设备参数应答TCP消息处理器
 * 处理设备发送的PSA类型应答消息，格式：$xx,PSA,yyyy,zz,#
 *
 * @author Shawn
 * @date 2025-01-22
 */
@Component
public class ParameterResponseTcpProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ParameterResponseTcpProcessor.class);

    @Resource
    private SwmHelmetDeviceService swmHelmetDeviceService;

    @Resource
    private SwmHelmetDeviceConfigService swmHelmetDeviceConfigService;

    @Resource
    private HelmetDeviceCacheService helmetDeviceCacheService;

    /**
     * PSA应答消息的正则表达式
     * 格式：$xx,PSA,yyyy,zz,#
     * 或者：$xx,PSA,yyyy,zz,data,#（带数据的应答）
     */
    private static final Pattern PSA_PATTERN = Pattern.compile(
            "^\\$([0-9A-Fa-f]+),PSA,([0-9A-Za-z]+),([A-Z]+)(?:,([^,#]+))?,#$");

    /**
     * 判断是否可以处理该消息
     * 检查消息是否符合PSA应答格式
     *
     * @param rawMessage 原始TCP消息
     * @return true表示可以处理（PSA应答消息）
     */
    public boolean canProcess(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return false;
        }

        String message = rawMessage.trim();

        // 检查基本格式：以$开头，以#结尾，包含PSA
        if (!message.startsWith("$") || !message.endsWith("#") || !message.contains("PSA")) {
            return false;
        }

        // 使用正则表达式进行详细验证
        Matcher matcher = PSA_PATTERN.matcher(message);
        boolean canProcess = matcher.matches();

        if (canProcess) {
            logger.debug("检测到PSA应答消息: {}", message);
        }

        return canProcess;
    }

    /**
     * 处理PSA应答消息
     *
     * @param rawMessage 原始TCP消息
     */
    public void process(String rawMessage) {
        try {
            logger.info("开始处理PSA应答消息: {}", rawMessage);

            // 解析消息
            PSAResponse response = parseMessage(rawMessage);
            if (response == null) {
                logger.warn("PSA消息解析失败: {}", rawMessage);
                return;
            }

            logger.info("PSA消息解析成功: 设备号={}, 参数类型={}, 数据={}",
                    response.getDeviceId(), response.getParameterType(), response.getData());

            // 根据参数类型进行处理
            switch (response.getParameterType()) {
                case "GM":
                    handleGetMacAddress(response);
                    break;
                case "IP":
                    handleServerIpResponse(response);
                    break;
                case "PORT":
                    handleServerPortResponse(response);
                    break;
                case "TM":
                    handleGroupDurationResponse(response);
                    break;
                case "CS":
                    handleNormalBeaconCsResponse(response);
                    break;
                case "SCS":
                    handleSpecialBeaconCsResponse(response);
                    break;
                case "SL":
                    handleDeepSleepResponse(response);
                    break;
                case "LM":
                    handleLocationModeResponse(response);
                    break;
                case "SW":
                    handleBluetoothScanWindowResponse(response);
                    break;
                case "SN":
                    handleBluetoothScanDurationResponse(response);
                    break;
                case "TI":
                    handleSendIntervalResponse(response);
                    break;
                case "AR":
                    handleHazardRetriggerIntervalResponse(response);
                    break;
                case "WI":
                    handleSleepWakeupTimeResponse(response);
                    break;
                case "ID":
                case "SID":
                    handleBeaconFilterNameResponse(response);
                    break;
                case "PS":
                    handleOffAlarmIntervalResponse(response);
                    break;
                default:
                    logger.info("暂不支持的参数类型: {}, 设备号: {}",
                            response.getParameterType(), response.getDeviceId());
                    break;
            }

        } catch (Exception e) {
            logger.error("处理PSA应答消息时发生异常: {}", rawMessage, e);
        }
    }

    /**
     * 解析PSA应答消息
     *
     * @param rawMessage 原始消息
     * @return 解析后的PSA应答对象，解析失败返回null
     */
    private PSAResponse parseMessage(String rawMessage) {
        if (rawMessage == null) {
            return null;
        }

        String message = rawMessage.trim();
        Matcher matcher = PSA_PATTERN.matcher(message);

        if (!matcher.matches()) {
            logger.warn("PSA消息格式不匹配: {}", message);
            return null;
        }

        try {
            String length = matcher.group(1);
            String deviceId = matcher.group(2);
            String parameterType = matcher.group(3);
            String data = matcher.group(4); // 可能为null

            PSAResponse response = new PSAResponse();
            response.setLength(length);
            response.setDeviceId(deviceId);
            response.setParameterType(parameterType);
            response.setData(data);
            response.setRawMessage(rawMessage);

            return response;

        } catch (Exception e) {
            logger.error("解析PSA消息时发生异常: {}", message, e);
            return null;
        }
    }

    /**
     * 处理获取MAC地址的应答
     *
     * @param response PSA应答对象
     */
    private void handleGetMacAddress(PSAResponse response) {
        String deviceId = response.getDeviceId();
        String macAddress = response.getData();

        try {
            logger.info("处理获取MAC地址应答: 设备号={}, MAC地址={}", deviceId, macAddress);

            // 验证MAC地址格式
            if (macAddress == null || macAddress.trim().isEmpty()) {
                logger.warn("MAC地址为空: 设备号={}", deviceId);
                return;
            }

            // 验证MAC地址格式（12位十六进制字符）
            String cleanMac = macAddress.trim().toUpperCase();
            if (!cleanMac.matches("^[0-9A-F]{12}$")) {
                logger.warn("MAC地址格式不正确: 设备号={}, MAC地址={}", deviceId, macAddress);
                return;
            }

            // 查询设备是否存在
            SwmHelmetDevice device = swmHelmetDeviceService.getByDeviceId(deviceId);
            if (device == null) {
                logger.warn("设备不存在: 设备号={}", deviceId);
                return;
            }

            // 更新MAC地址
            device.setMacAddress(cleanMac);
            swmHelmetDeviceService.update(device);

            logger.info("MAC地址更新成功: 设备号={}, MAC地址={}", deviceId, cleanMac);

            // 增量更新iot:helmet:mac_to_person缓存
            try {
                String identityCard = device.getAssignedPerson();
                if (identityCard != null && !identityCard.trim().isEmpty()) {
                    // 使用增量更新方法更新缓存（自动查询personId）
                    helmetDeviceCacheService.updateMacPersonMappingWithPersonId(cleanMac, identityCard);
                    logger.info("MAC地址缓存增量更新成功: 设备号={}, MAC={}, 身份证={}", deviceId, cleanMac, identityCard);
                } else {
                    logger.debug("设备未绑定人员，跳过缓存更新: 设备号={}, MAC={}", deviceId, cleanMac);
                }
            } catch (Exception e) {
                logger.error("MAC地址缓存增量更新失败: 设备号={}, MAC={}, 错误={}", deviceId, cleanMac, e.getMessage(), e);
                // 缓存更新失败不影响主业务流程，只记录错误日志
            }

        } catch (Exception e) {
            logger.error("处理获取MAC地址应答时发生异常: 设备号={}, MAC地址={}",
                    deviceId, macAddress, e);
        }
    }

    /**
     * 处理服务器IP设置应答
     *
     * @param response PSA应答对象
     */
    private void handleServerIpResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "serverIp", "IP设置");
    }

    /**
     * 处理端口设置应答
     *
     * @param response PSA应答对象
     */
    private void handleServerPortResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "serverPort", "端口设置");
    }

    /**
     * 处理每组时长设置应答
     *
     * @param response PSA应答对象
     */
    private void handleGroupDurationResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "groupDuration", "每组时长设置");
    }

    /**
     * 处理普通信标CS设置应答
     *
     * @param response PSA应答对象
     */
    private void handleNormalBeaconCsResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "normalBeaconCs", "普通信标CS设置");
    }

    /**
     * 处理特殊信标CS设置应答
     *
     * @param response PSA应答对象
     */
    private void handleSpecialBeaconCsResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "specialBeaconCs", "特殊信标CS设置");
    }

    /**
     * 处理深度休眠设置应答
     *
     * @param response PSA应答对象
     */
    private void handleDeepSleepResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "deepSleepDuration", "深度休眠设置");
    }

    /**
     * 处理定位模式设置应答
     *
     * @param response PSA应答对象
     */
    private void handleLocationModeResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "locationMode", "定位模式设置");
    }

    /**
     * 处理蓝牙扫描窗口设置应答
     *
     * @param response PSA应答对象
     */
    private void handleBluetoothScanWindowResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "bluetoothScanWindow", "蓝牙扫描窗口设置");
    }

    /**
     * 处理蓝牙持续扫描时间设置应答
     *
     * @param response PSA应答对象
     */
    private void handleBluetoothScanDurationResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "bluetoothScanDuration", "蓝牙持续扫描时间设置");
    }

    /**
     * 处理发送间隔设置应答
     *
     * @param response PSA应答对象
     */
    private void handleSendIntervalResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "sendInterval", "发送间隔设置");
    }

    /**
     * 处理危险源重新触发间隔设置应答
     *
     * @param response PSA应答对象
     */
    private void handleHazardRetriggerIntervalResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "hazardRetriggerInterval", "危险源重新触发间隔设置");
    }

    /**
     * 处理休眠唤醒时间设置应答
     *
     * @param response PSA应答对象
     */
    private void handleSleepWakeupTimeResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "sleepWakeupTime", "休眠唤醒时间设置");
    }

    /**
     * 处理信标名称过滤设置应答
     *
     * @param response PSA应答对象
     */
    private void handleBeaconFilterNameResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "beaconFilterName", "信标名称过滤设置");
    }

    /**
     * 处理脱帽报警时间间隔设置应答
     *
     * @param response PSA应答对象
     */
    private void handleOffAlarmIntervalResponse(PSAResponse response) {
        updateConfigField(response.getDeviceId(), "hatOffAlarmInterval", "脱帽报警时间间隔");
    }

    /**
     * 通用的配置字段更新方法
     * 从配置表同步到设备表，使用单字段更新避免更新整个实体
     *
     * @param deviceId 设备ID
     * @param fieldName 字段名称
     * @param description 操作描述
     */
    private void updateConfigField(String deviceId, String fieldName, String description) {
        try {
            logger.info("处理{}应答: 设备号={}", description, deviceId);

            // 1. 查询配置表获取配置信息
            SwmHelmetDeviceConfig config = swmHelmetDeviceConfigService.getByDeviceId(deviceId);
            if (config == null) {
                logger.warn("{}失败，配置不存在: 设备号={}", description, deviceId);
                return;
            }

            // 2. 根据字段名称调用对应的单字段更新方法
            boolean updated = false;
            switch (fieldName) {
                case "serverIp":
                    updated = swmHelmetDeviceService.updateServerIp(deviceId, config.getServerIp());
                    break;
                case "serverPort":
                    updated = swmHelmetDeviceService.updateServerPort(deviceId, config.getServerPort());
                    break;
                case "groupDuration":
                    updated = swmHelmetDeviceService.updateGroupDuration(deviceId, config.getGroupDuration());
                    break;
                case "normalBeaconCs":
                    updated = swmHelmetDeviceService.updateNormalBeaconCs(deviceId, config.getNormalBeaconCs());
                    break;
                case "specialBeaconCs":
                    updated = swmHelmetDeviceService.updateSpecialBeaconCs(deviceId, config.getSpecialBeaconCs());
                    break;
                case "deepSleepDuration":
                    updated = swmHelmetDeviceService.updateDeepSleepDuration(deviceId, config.getDeepSleepDuration());
                    break;
                case "locationMode":
                    updated = swmHelmetDeviceService.updateLocationMode(deviceId, config.getLocationMode());
                    break;
                case "bluetoothScanWindow":
                    updated = swmHelmetDeviceService.updateBluetoothScanWindow(deviceId, config.getBluetoothScanWindow());
                    break;
                case "bluetoothScanDuration":
                    updated = swmHelmetDeviceService.updateBluetoothScanDuration(deviceId, config.getBluetoothScanDuration());
                    break;
                case "sendInterval":
                    updated = swmHelmetDeviceService.updateSendInterval(deviceId, config.getSendInterval());
                    break;
                case "hazardRetriggerInterval":
                    updated = swmHelmetDeviceService.updateHazardRetriggerInterval(deviceId, config.getHazardRetriggerInterval());
                    break;
                case "sleepWakeupTime":
                    updated = swmHelmetDeviceService.updateSleepWakeupTime(deviceId, config.getSleepWakeupTime());
                    break;
                case "beaconFilterName":
                    updated = swmHelmetDeviceService.updateBeaconFilterName(deviceId, config.getBeaconFilterName());
                    break;
                case "hatOffAlarmInterval":
                    updated = swmHelmetDeviceService.updatehatOffAlarmInterval(deviceId, config.getHatOffAlarmInterval());
                    break;
                default:
                    logger.warn("未知的配置字段: {}", fieldName);
                    break;
            }

            // 3. 记录更新结果
            if (updated) {
                logger.info("{}成功: 设备号={}, 字段={}", description, deviceId, fieldName);
            } else {
                logger.warn("{}失败: 设备号={}, 字段={}", description, deviceId, fieldName);
            }

        } catch (Exception e) {
            logger.error("{}时发生异常: 设备号={}, 字段={}", description, deviceId, fieldName, e);
        }
    }

    /**
     * PSA应答消息数据结构
     */
    private static class PSAResponse {
        private String length; // 数据长度
        private String deviceId; // 设备编号
        private String parameterType; // 参数类型
        private String data; // 数据内容（可选）
        private String rawMessage; // 原始消息

        // Getters and Setters
        public String getLength() {
            return length;
        }

        public void setLength(String length) {
            this.length = length;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getParameterType() {
            return parameterType;
        }

        public void setParameterType(String parameterType) {
            this.parameterType = parameterType;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getRawMessage() {
            return rawMessage;
        }

        public void setRawMessage(String rawMessage) {
            this.rawMessage = rawMessage;
        }
    }
}
