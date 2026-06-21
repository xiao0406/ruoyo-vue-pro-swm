package cn.iocoder.yudao.module.iot.websocket.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 服务端控制设备开启推流请求DTO
 * 用于9.1.1功能：后台发送指令控制设备开启推流
 *
 * @author Shawn
 * @date 2024-07-06
 */
public class ServerPushOpenRtspRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    /**
     * 推流地址
     */
    @NotBlank(message = "推流地址不能为空")
    private String pushUrl;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }
}
