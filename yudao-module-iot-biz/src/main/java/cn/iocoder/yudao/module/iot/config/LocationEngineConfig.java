package cn.iocoder.yudao.module.iot.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import org.apache.commons.lang3.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 定位引擎配置类
 * 负责读取和验证定位引擎相关配置
 *
 * @author Shawn
 * @date 2025-07-23
 */
@Component
public class LocationEngineConfig {
private static final Logger log = LoggerFactory.getLogger(LocationEngineConfig.class);

    // 原有配置保持不变
    @Value("${JIAI.url:http://58.240.212.6:8094}")
    private String jiaiBaseUrl;

    // 新增配置
    @Value("${iot.location.engine-type:jiai}")
    private String engineType;

    @Value("${iot.location.chat.base-url:}")
    private String chatBaseUrl;


    @Value("${iot.location.chat.endpoint:/chat/getCoordinate}")
    private String chatEndpoint;

    @Value("${iot.location.chat.timeout:5000}")
    private int chatTimeout;

    @Value("${iot.location.history.enabled:false}")
    private boolean historyEnabled;

    @Value("${iot.location.history.seconds:5}")
    private int historySeconds;

    @Value("${iot.location.history.query-timeout:3000}")
    private int historyQueryTimeout;

    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;

    /**
     * 配置初始化和验证
     */
    @PostConstruct
    public void init() {
        log.info("初始化定位引擎配置...");
        validateConfiguration();
        logConfiguration();
    }

    /**
     * 验证配置的有效性
     */
    private void validateConfiguration() {
        // 验证引擎类型
        if (!"jiai".equals(engineType) && !"chat".equals(engineType)) {
            log.warn("无效的引擎类型: {}, 将使用默认值: jiai", engineType);
            engineType = "jiai";
        }

        // 验证Chat引擎配置
        if ("chat".equals(engineType)) {
            if (StringUtils.isBlank(chatBaseUrl)) {
                log.error("Chat引擎已启用但未配置base-url，将降级到JIAI引擎");
                engineType = "jiai";
            } else {
                // 验证URL格式
                if (!chatBaseUrl.startsWith("http://") && !chatBaseUrl.startsWith("https://")) {
                    log.warn("Chat引擎base-url格式可能不正确: {}", chatBaseUrl);
                }

                // 验证超时时间
                if (chatTimeout <= 0 || chatTimeout > 60000) {
                    log.warn("Chat引擎超时时间不合理: {}ms, 将使用默认值: 5000ms", chatTimeout);
                    chatTimeout = 5000;
                }
            }
        }

        // 验证JIAI引擎配置
        if (StringUtils.isBlank(jiaiBaseUrl)) {
            log.error("JIAI引擎base-url未配置，使用默认值");
            jiaiBaseUrl = "http://58.240.212.6:8094";
        }

        // 验证历史坐标查询配置
        if (historyEnabled) {
            if (historySeconds <= 0 || historySeconds > 3600) {
                log.warn("历史坐标查询时间间隔不合理: {}秒, 将使用默认值: 5秒", historySeconds);
                historySeconds = 5;
            }

            if (historyQueryTimeout <= 0 || historyQueryTimeout > 30000) {
                log.warn("历史坐标查询超时时间不合理: {}ms, 将使用默认值: 3000ms", historyQueryTimeout);
                historyQueryTimeout = 3000;
            }
        }
    }

    /**
     * 记录当前配置信息
     */
    private void logConfiguration() {
        log.info("定位引擎配置信息:");
        log.info("  引擎类型: {}", engineType);
        log.info("  JIAI引擎地址: {}", jiaiBaseUrl);

        if ("chat".equals(engineType)) {
            log.info("  Chat引擎地址: {}", chatBaseUrl);
            log.info("  Chat引擎端点: {}", chatEndpoint);
            log.info("  Chat引擎超时: {}ms", chatTimeout);
        }

        log.info("  历史坐标查询: {}", historyEnabled ? "启用" : "禁用");
        if (historyEnabled) {
            log.info("  历史坐标时间间隔: {}秒", historySeconds);
            log.info("  历史坐标查询超时: {}ms", historyQueryTimeout);
        }
    }

    /**
     * 检查配置是否有效
     *
     * @return true表示配置有效
     */
    public boolean isConfigurationValid() {
        if ("chat".equals(engineType)) {
            return StringUtils.isNotBlank(chatBaseUrl);
        }
        return StringUtils.isNotBlank(jiaiBaseUrl);
    }

    /**
     * 获取当前有效的引擎类型
     *
     * @return 引擎类型
     */
    public String getEffectiveEngineType() {
        if ("chat".equals(engineType) && StringUtils.isNotBlank(chatBaseUrl)) {
            return "chat";
        }
        return "jiai";
    }

    /**
     * 获取Chat引擎完整URL
     *
     * @return Chat引擎完整URL
     */
    public String getChatEngineUrl() {
        if (StringUtils.isBlank(chatBaseUrl)) {
            return null;
        }
        return chatBaseUrl + chatEndpoint;
    }

    /**
     * 获取JIAI引擎完整URL
     *
     * @return JIAI引擎完整URL
     */
    public String getJiaiEngineUrl() {
        return jiaiBaseUrl + "/JIAI/location/";
    }

    // Getter方法
    public String getJiaiBaseUrl() { return jiaiBaseUrl; }
    public String getEngineType() { return engineType; }
    public String getChatBaseUrl() { return chatBaseUrl; }
    public String getChatEndpoint() { return chatEndpoint; }
    public int getChatTimeout() { return chatTimeout; }
    public boolean isHistoryEnabled() { return historyEnabled; }
    public int getHistorySeconds() { return historySeconds; }
    public int getHistoryQueryTimeout() { return historyQueryTimeout; }

    // Setter方法（用于测试）
    public void setEngineType(String engineType) { this.engineType = engineType; }
    public void setChatBaseUrl(String chatBaseUrl) { this.chatBaseUrl = chatBaseUrl; }
    public void setHistoryEnabled(boolean historyEnabled) { this.historyEnabled = historyEnabled; }
    public void setHistorySeconds(int historySeconds) { this.historySeconds = historySeconds; }

    @Override
    public String toString() {
        return "LocationEngineConfig{" +
                "jiaiBaseUrl='" + jiaiBaseUrl + '\'' +
                ", engineType='" + engineType + '\'' +
                ", chatBaseUrl='" + chatBaseUrl + '\'' +
                ", chatEndpoint='" + chatEndpoint + '\'' +
                ", chatTimeout=" + chatTimeout +
                ", historyEnabled=" + historyEnabled +
                ", historySeconds=" + historySeconds +
                ", historyQueryTimeout=" + historyQueryTimeout +
                '}';
    }
}
