package cn.iocoder.yudao.module.iot.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * IoT 模块错误码常量
 *
 * 错误码范围: 1-025-000-001 ~ 1-025-999-999
 */
public interface ErrorCodeConstants {

    // ========== 设备管理 1-025-000-xxx ==========
    ErrorCode IOT_DEVICE_NOT_EXISTS = new ErrorCode(1_025_000_001, "IoT设备不存在");
    ErrorCode IOT_FILE_UPLOAD_NOT_EXISTS = new ErrorCode(1_025_000_002, "文件上传记录不存在");

    // ========== 语音告警 1-025-001-xxx ==========
    ErrorCode VOICE_TEMPLATE_NOT_EXISTS = new ErrorCode(1_025_001_001, "语音模板不存在");
    ErrorCode ALARM_LIGHT_NOT_EXISTS = new ErrorCode(1_025_001_002, "报警灯不存在");

    // ========== TCP 通信 1-025-002-xxx ==========
    ErrorCode TCP_SEND_MESSAGE_LOG_NOT_EXISTS = new ErrorCode(1_025_002_001, "TCP消息日志不存在");

}
