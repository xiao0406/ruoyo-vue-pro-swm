package cn.iocoder.yudao.module.swm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * SWM 模块错误码常量
 *
 * 错误码范围: 1-020-000-001 ~ 1-020-999-999
 */
public interface ErrorCodeConstants {

    // ========== 人员管理 1-020-000-xxx ==========
    ErrorCode PERSON_NOT_EXISTS = new ErrorCode(1_020_000_001, "人员不存在");
    ErrorCode PERSON_ID_CARD_DUPLICATE = new ErrorCode(1_020_000_002, "身份证号已存在");
    ErrorCode PERSON_HELMET_ALREADY_BOUND = new ErrorCode(1_020_000_003, "该人员已绑定安全帽");

    // ========== 安全帽设备 1-020-001-xxx ==========
    ErrorCode HELMET_DEVICE_NOT_EXISTS = new ErrorCode(1_020_001_001, "安全帽设备不存在");
    ErrorCode HELMET_DEVICE_ALREADY_BINDED = new ErrorCode(1_020_001_002, "安全帽已被绑定");
    ErrorCode HELMET_DEVICE_NOT_BINDED = new ErrorCode(1_020_001_003, "安全帽未绑定");

    // ========== 区域/信标 1-020-002-xxx ==========
    ErrorCode AREA_NOT_EXISTS = new ErrorCode(1_020_002_001, "区域不存在");
    ErrorCode BEACON_STATION_NOT_EXISTS = new ErrorCode(1_020_002_002, "信标站点不存在");

    // ========== 考勤管理 1-020-003-xxx ==========
    ErrorCode ATTENDANCE_NOT_EXISTS = new ErrorCode(1_020_003_001, "考勤记录不存在");

    // ========== 巡检管理 1-020-004-xxx ==========
    ErrorCode INSPECTION_PLAN_NOT_EXISTS = new ErrorCode(1_020_004_001, "巡检计划不存在");
    ErrorCode INSPECTION_LIST_NOT_EXISTS = new ErrorCode(1_020_004_002, "巡检单不存在");

    // ========== 告警管理 1-020-005-xxx ==========
    ErrorCode ALARM_CONFIG_NOT_EXISTS = new ErrorCode(1_020_005_001, "告警配置不存在");
    ErrorCode ALARM_LIGHT_NOT_EXISTS = new ErrorCode(1_020_005_002, "报警灯不存在");
    ErrorCode WARNING_NOT_EXISTS = new ErrorCode(1_020_005_003, "预警记录不存在");

    // ========== 隐患排查 1-020-006-xxx ==========
    ErrorCode HIDDEN_DANGER_NOT_EXISTS = new ErrorCode(1_020_006_001, "隐患记录不存在");
    ErrorCode HAZARD_SOURCE_NOT_EXISTS = new ErrorCode(1_020_006_002, "危险源不存在");
    ErrorCode DANGER_DISPOSAL_NOT_EXISTS = new ErrorCode(1_020_006_003, "隐患处置记录不存在");

    // ========== 安全教育 1-020-007-xxx ==========
    ErrorCode SAFETY_EDUCATION_NOT_EXISTS = new ErrorCode(1_020_007_001, "安全教育记录不存在");

    // ========== 一键召回 1-020-008-xxx ==========
    ErrorCode ONE_CLICK_RECALL_NOT_EXISTS = new ErrorCode(1_020_008_001, "召回记录不存在");

    // ========== 排班管理 1-020-009-xxx ==========
    ErrorCode PERSON_SCHEDULE_NOT_EXISTS = new ErrorCode(1_020_009_001, "排班记录不存在");

    // ========== 语音模板 1-020-011-xxx ==========
    ErrorCode VOICE_TEMPLATE_NOT_EXISTS = new ErrorCode(1_020_011_001, "语音模板不存在");

    // ========== 通用选项 1-020-010-xxx ==========
    ErrorCode COMMON_OPTIONS_NOT_EXISTS = new ErrorCode(1_020_010_001, "通用选项不存在");
    ErrorCode WORK_TYPE_NOT_EXISTS = new ErrorCode(1_020_010_002, "工种不存在");

}
