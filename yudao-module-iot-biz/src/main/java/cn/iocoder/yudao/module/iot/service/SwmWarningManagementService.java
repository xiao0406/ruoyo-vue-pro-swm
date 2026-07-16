package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;

/**
 * SWM 预警管理服务接口（IoT 版）
 */
public interface SwmWarningManagementService {

    R<JSONObject> saveCustomWarningToSwmWarningManagement(String deviceId, String idCard,
                                                          String warningContent, String warningType,
                                                          String warningLevel, String warningSource,
                                                          String warningTime, boolean autoHandle,
                                                          String areaId, String areaName,
                                                          String lng, String lat);

    R<JSONObject> saveCustomWarningToSwmWarningManagement(String deviceId, String idCard,
                                                          String warningContent, String warningType,
                                                          String warningLevel, String warningSource,
                                                          String warningTime);

    R<JSONObject> updateAttendanceRecord(String deviceId, String idCard);
}
