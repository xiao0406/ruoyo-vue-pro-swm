package cn.iocoder.yudao.module.iot.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 本地录制控制请求DTO
 *
 * @author Shawn
 * @date 2025-05-28
 */
public class LocalRecordControlRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    /**
     * 本地录制状态 (0:关闭, 1:开启)
     */
    @NotNull(message = "本地录制状态不能为空")
    @Min(value = 0, message = "本地录制状态只能是0或1")
    @Max(value = 1, message = "本地录制状态只能是0或1")
    private Integer localRecord;

    /**
     * 录制视频文件名
     */
    @NotBlank(message = "录制视频文件名不能为空")
    private String recordVideoName;

    public LocalRecordControlRequest() {
    }

    public LocalRecordControlRequest(String deviceId, Integer localRecord, String recordVideoName) {
        this.deviceId = deviceId;
        this.localRecord = localRecord;
        this.recordVideoName = recordVideoName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getLocalRecord() {
        return localRecord;
    }

    public void setLocalRecord(Integer localRecord) {
        this.localRecord = localRecord;
    }

    /**
     * 获取本地录制状态的字符串形式
     * 用于WebSocket消息发送
     */
    public String getLocalRecordAsString() {
        return localRecord != null ? localRecord.toString() : "0";
    }

    public String getRecordVideoName() {
        return recordVideoName;
    }

    public void setRecordVideoName(String recordVideoName) {
        this.recordVideoName = recordVideoName;
    }

    @Override
    public String toString() {
        return "LocalRecordControlRequest{" +
                "deviceId='" + deviceId + '\'' +
                ", localRecord=" + localRecord +
                ", recordVideoName='" + recordVideoName + '\'' +
                '}';
    }
}
