package cn.iocoder.yudao.module.iot.websocket.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 服务端控制设备关闭推流请求DTO
 * 用于9.2功能：后台发送指令控制设备关闭推流
 *
 * @author Shawn
 * @date 2025-05-28
 */
public class ServerPushStopRtspRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
