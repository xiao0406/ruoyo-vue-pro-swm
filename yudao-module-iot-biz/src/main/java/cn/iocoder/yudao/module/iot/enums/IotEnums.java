package cn.iocoder.yudao.module.iot.enums;

/**
 * IoT 业务枚举集合
 *
 * 从 JeeSite 实体内联枚举提取为独立枚举类。
 */
public interface IotEnums {

    /**
     * TCP 发送状态
     * 迁移自: TcpSendMessageLog.SendStatusEnum
     */
    enum TcpSendStatusEnum {
        SUCCESS("success", "成功"),
        FAILED("failed", "失败");

        private final String value;
        private final String label;

        TcpSendStatusEnum(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    /**
     * 语音模板状态
     * 迁移自: VoiceTemplate.StatusEnum
     */
    enum VoiceTemplateStatusEnum {
        NORMAL("0", "正常"),
        DISABLED("1", "已禁用");

        private final String value;
        private final String label;

        VoiceTemplateStatusEnum(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    /**
     * 语音模板语言
     * 迁移自: VoiceTemplate.LanguageEnum
     */
    enum LanguageEnum {
        ZH_CN("zh-CN", "中文"),
        EN_US("en-US", "英文");

        private final String value;
        private final String label;

        LanguageEnum(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    /**
     * 召回结果
     * 迁移自: OneKeyRecall.RecallResultEnum
     */
    enum RecallResultEnum {
        IN_PROGRESS("0", "进行中"),
        SUCCESS("1", "成功"),
        FAILED("2", "失败");

        private final String value;
        private final String label;

        RecallResultEnum(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    /**
     * 疏散方案
     * 迁移自: OneKeyRecall.EvacuationPlanEnum
     */
    enum EvacuationPlanEnum {
        ALL("1", "全部"),
        BY_WORKSHOP("2", "按车间"),
        BY_GROUP("3", "按班组"),
        BY_AREA("4", "按区域"),
        BY_PERSONNEL("5", "按人员");

        private final String value;
        private final String label;

        EvacuationPlanEnum(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    /**
     * 推送方式
     * 迁移自: OneKeyRecall.PushMethodEnum
     */
    enum PushMethodEnum {
        DEVICE("1", "设备语音"),
        SMS("2", "短信");

        private final String value;
        private final String label;

        PushMethodEnum(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    /**
     * MQTT 订阅来源
     * 迁移自: SubscriptionSource
     */
    enum SubscriptionSourceEnum {
        CONFIG("CONFIG", "配置文件"),
        API("API", "API接口");

        private final String value;
        private final String label;

        SubscriptionSourceEnum(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    /**
     * MQTT Broker 角色
     * 迁移自: MqttClientProperties.BrokerRole
     */
    enum BrokerRoleEnum {
        SUBSCRIBE_ONLY("SUBSCRIBE_ONLY", "只订阅"),
        PUBLISH_ONLY("PUBLISH_ONLY", "只发布"),
        BOTH("BOTH", "发布+订阅");

        private final String value;
        private final String label;

        BrokerRoleEnum(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    /**
     * IoT 上报类型
     * 迁移自: DeviceBatteryStatusHandler.IotReportType
     */
    enum IotReportTypeEnum {
        ONLINE("online", "设备在线"),
        SILENT("silent", "设备静默"),
        BATTERY("battery", "电量上报");

        private final String type;
        private final String desc;

        IotReportTypeEnum(String type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public String getType() { return type; }
        public String getDesc() { return desc; }
    }

}
