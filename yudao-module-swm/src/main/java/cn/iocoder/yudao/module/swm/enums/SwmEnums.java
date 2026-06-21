package cn.iocoder.yudao.module.swm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SWM 业务枚举集合
 *
 * 从 JeeSite 实体内联枚举提取为独立枚举类。
 * 原始来源：SwmPerson、SwmHelmetDevice、SwmBeaconStation 等实体类。
 */
public interface SwmEnums {

    // ========== 人员相关枚举 ==========

    /**
     * 人员类型
     * 迁移自: SwmPerson.PersonTypeEnum
     */
    @Getter
    @AllArgsConstructor
    enum PersonTypeEnum {
        WORKER("0", "工人"),
        MANAGER("1", "管理人员"),
        TEAM_LEADER("2", "班组长"),
        SPECIAL_TRADES("3", "特殊工种");
        private final String value;
        private final String label;
    }

    /**
     * 人员状态
     * 迁移自: SwmPerson.PersonStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum PersonStatusEnum {
        INACTIVE("0", "未激活"),
        ACTIVE("1", "已激活");
        private final String value;
        private final String label;
    }

    /**
     * 安全教育完成状态
     * 迁移自: SwmPerson.SafetyEducationEnum / SwmSafetyEducation.StatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum SafetyEducationStatusEnum {
        NOT_STARTED("0", "未开始"),
        COMPLETED("1", "已完成");
        private final String value;
        private final String label;
    }

    /**
     * 安全帽归还状态
     * 迁移自: SwmPerson.HelmetReturnedEnum
     */
    @Getter
    @AllArgsConstructor
    enum HelmetReturnedEnum {
        YES("1", "已归还"),
        NO("0", "未归还");
        private final String value;
        private final String label;
    }

    /**
     * 退场类型
     * 迁移自: SwmPerson.DepartureTypeEnum
     */
    @Getter
    @AllArgsConstructor
    enum DepartureTypeEnum {
        NORMAL("1", "正常退场"),
        ABNORMAL("0", "异常退场");
        private final String value;
        private final String label;
    }

    // ========== 安全帽设备相关枚举 ==========

    /**
     * 运动状态
     * 迁移自: SwmHelmetDevice.MotionStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum MotionStatusEnum {
        STATIC("0", "静止"),
        MOVING("1", "运动"),
        CHARGING("2", "充电");
        private final String value;
        private final String label;
    }

    /**
     * 安全帽类型
     * 迁移自: SwmHelmetDevice.HelmetTypeEnum
     */
    @Getter
    @AllArgsConstructor
    enum HelmetTypeEnum {
        PORTABLE("1", "便携式"),
        HEADBAND("2", "头箍式"),
        INTEGRATED("3", "一体式");
        private final String value;
        private final String label;
    }

    /**
     * 使用状态
     * 迁移自: SwmHelmetDevice.UsageStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum HelmetUsageStatusEnum {
        UNBINDED("0", "未绑定"),
        IN_USE("1", "使用中");
        private final String value;
        private final String label;
    }

    /**
     * 定位模式
     * 迁移自: SwmHelmetDevice.LocationModeEnum
     */
    @Getter
    @AllArgsConstructor
    enum LocationModeEnum {
        GPS_BEIDOU("1", "GPS/北斗"),
        GPS_BEIDOU_BT("2", "GPS/北斗+蓝牙"),
        BT_GPS_BEIDOU("3", "蓝牙+GPS/北斗"),
        BT("4", "蓝牙");
        private final String value;
        private final String label;
    }

    // ========== 信标相关枚举 ==========

    /**
     * 信标类型
     * 迁移自: SwmBeaconStation.BeaconTypeEnum
     */
    @Getter
    @AllArgsConstructor
    enum BeaconTypeEnum {
        CONVENTION("1", "常规"),
        FENCE("2", "围栏"),
        DANGEROUS_SOURCE("3", "危险源"),
        CAMERA("4", "摄像头");
        private final String value;
        private final String label;
    }

    /**
     * 信标状态
     * 迁移自: SwmBeaconStation.BeaconStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum BeaconStatusEnum {
        ONLINE("1", "在线"),
        OFFLINE("2", "离线");
        private final String value;
        private final String label;
    }

    /**
     * 信标部署状态
     * 迁移自: SwmBeaconStation.DeployStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum DeployStatusEnum {
        NOT_DEPLOYED("0", "未部署"),
        DEPLOYED("1", "已部署");
        private final String value;
        private final String label;
    }

    // ========== 危险源/隐患相关枚举 ==========

    /**
     * 危险源状态
     * 迁移自: SwmHazardSource.HazardSourceStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum HazardSourceStatusEnum {
        WAIT("0", "待处理"),
        IN_PROGRESS("1", "处理中"),
        COMPLETED("2", "已完成"),
        CANCELLED("3", "已取消");
        private final String value;
        private final String label;
    }

    // ========== 预警管理枚举 ==========

    /**
     * 预警类型
     * 迁移自: SwmWarningManagement.WarningTypeEnum
     */
    @Getter
    @AllArgsConstructor
    enum WarningTypeEnum {
        ACTIVE("1", "主动预警"),
        PASSIVE("2", "被动预警");
        private final String value;
        private final String label;
    }

    /**
     * 处理状态（通用）
     * 迁移自: SwmWarningManagement.HandleStatusEnum / SwmHandleRecord.HandleStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum HandleStatusEnum {
        UNHANDLED("0", "未处理"),
        HANDLED("1", "已处理"),
        DRAFT("2", "草稿");
        private final String value;
        private final String label;
    }

    // ========== 一键召回枚举 ==========

    /**
     * 召回结果
     * 迁移自: SwmOneClickRecall.RecallResultEnum
     */
    @Getter
    @AllArgsConstructor
    enum RecallResultEnum {
        IN_PROGRESS("0", "进行中"),
        SUCCESS("1", "成功"),
        FAILED("2", "失败");
        private final String value;
        private final String label;
    }

    /**
     * 疏散方案
     * 迁移自: SwmOneClickRecall.EvacuationPlanEnum
     */
    @Getter
    @AllArgsConstructor
    enum EvacuationPlanEnum {
        ALL("1", "全部"),
        BY_WORKSHOP("2", "按车间"),
        BY_TEAM("3", "按班组"),
        BY_AREA("4", "按区域"),
        BY_PERSON("5", "按人员"),
        BY_PERSON_TYPE("6", "按人员类型");
        private final String value;
        private final String label;
    }

    /**
     * 推送方式
     * 迁移自: SwmOneClickRecall.PushMethodEnum
     */
    @Getter
    @AllArgsConstructor
    enum PushMethodEnum {
        DEVICE("1", "设备语音"),
        SMS("2", "短信");
        private final String value;
        private final String label;
    }

    // ========== 排班相关枚举 ==========

    /**
     * 班次类型
     * 迁移自: SwmScheduleTime.ShiftTypeEnum
     */
    @Getter
    @AllArgsConstructor
    enum ShiftTypeEnum {
        MORNING("1", "早班"),
        MIDDLE("2", "中班"),
        NIGHT("3", "晚班");
        private final String value;
        private final String label;
    }

    // ========== 巡检相关枚举 ==========

    /**
     * 巡检计划状态
     * 迁移自: SwmInspectionPlan.PlanStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum PlanStatusEnum {
        OPEN("open", "进行中"),
        PAUSE("pause", "已暂停");
        private final String value;
        private final String label;
    }

    /**
     * 巡检单状态
     * 迁移自: SwmInspectionList.InspectionListStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum InspectionListStatusEnum {
        WAIT("0", "待处理"),
        IN_PROGRESS("1", "处理中"),
        COMPLETED("2", "已完成"),
        CANCELLED("3", "已取消");
        private final String value;
        private final String label;
    }

    // ========== 安全教育枚举 ==========

    /**
     * 教育类型
     * 迁移自: SwmSafetyEducation.EducationTypeEnum
     */
    @Getter
    @AllArgsConstructor
    enum EducationTypeEnum {
        ENTRY("1", "入场教育"),
        WEEKLY("2", "周教育"),
        MONTHLY("3", "月教育"),
        QUARTERLY("4", "季度教育"),
        SPECIAL("5", "专项教育");
        private final String value;
        private final String label;
    }

    /**
     * 参与类型
     * 迁移自: SwmSafetyEducation.ParticipationTypeEnum
     */
    @Getter
    @AllArgsConstructor
    enum ParticipationTypeEnum {
        TEAM("1", "班组"),
        PROCESS("2", "工序"),
        WORKSHOP("3", "车间");
        private final String value;
        private final String label;
    }

    // ========== 安全帽订单枚举 ==========

    /**
     * 订单状态
     * 迁移自: SwmSafetyHelmetOrder.OrderStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum OrderStatusEnum {
        PENDING("0", "待处理"),
        PROCESSED("1", "已处理"),
        DELIVERED("2", "已发货");
        private final String value;
        private final String label;
    }

    // ========== 看板枚举 ==========

    /**
     * 工作状态
     * 迁移自: SwmPersonnelBoard.WorkStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum WorkStatusEnum {
        WORKING("1", "在岗"),
        RESTING("0", "休息");
        private final String value;
        private final String label;
    }

    /**
     * 安全帽状态（看板用）
     * 迁移自: SwmPersonnelBoard.HelmetStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum HelmetStatusEnum {
        NORMAL("1", "正常"),
        OFF("0", "离线");
        private final String value;
        private final String label;
    }

    /**
     * 人员状态（看板用）
     * 迁移自: SwmPersonnelBoard.PersonnelStatusEnum
     */
    @Getter
    @AllArgsConstructor
    enum PersonnelStatusEnum {
        NORMAL("1", "正常"),
        IDLE("0", "空闲");
        private final String value;
        private final String label;
    }

    // ========== 报警灯枚举 ==========

    /**
     * 是否启用报警
     * 迁移自: SwmAlarmLight.EnableAlarmEnum
     */
    @Getter
    @AllArgsConstructor
    enum EnableAlarmEnum {
        YES("1", "是"),
        NO("0", "否");
        private final String value;
        private final String label;
    }

}
