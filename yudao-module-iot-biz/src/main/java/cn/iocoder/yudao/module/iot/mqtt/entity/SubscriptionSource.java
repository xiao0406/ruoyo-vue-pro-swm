package cn.iocoder.yudao.module.iot.mqtt.entity;

/**
 * 订阅来源枚举
 *
 * @author Shawn
 * @date 2025-07-21
 */
public enum SubscriptionSource {

    /**
     * 配置文件驱动的订阅
     */
    CONFIG("配置文件"),

    /**
     * REST API驱动的订阅
     */
    API("API接口");

    private final String description;

    SubscriptionSource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
