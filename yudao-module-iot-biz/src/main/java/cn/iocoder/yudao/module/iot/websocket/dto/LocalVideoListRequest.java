package cn.iocoder.yudao.module.iot.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 设备本地视频列表查询请求参数
 *
 * @author Shawn
 * @date 2024-07-05
 */
public class LocalVideoListRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    /**
     * 开始时间
     * 格式：yyyyMMddHHmmss
     */
    @NotBlank(message = "开始时间不能为空")
    @Pattern(regexp = "^\\d{14}$", message = "开始时间格式不正确，应为14位数字，格式：yyyyMMddHHmmss")
    private String startTime;

    /**
     * 结束时间
     * 格式：yyyyMMddHHmmss
     */
    @NotBlank(message = "结束时间不能为空")
    @Pattern(regexp = "^\\d{14}$", message = "结束时间格式不正确，应为14位数字，格式：yyyyMMddHHmmss")
    private String endTime;

    /**
     * 视频类型
     */
    @NotBlank(message = "视频类型不能为空")
    private String videoType;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }
}
