package com.jeesite.modules.vo;

import lombok.Data;

import java.util.Map;

/**
 * 设备实时数据
 * @author: cjie
 * @date: 2025/5/28
 */
@Data
public class DeviceDataDTO {
    /**
     * 设备编号
     */
    private String deviceNum;

    /**
     * 设备数据
     */
    private Map<String, Object> data;
}
