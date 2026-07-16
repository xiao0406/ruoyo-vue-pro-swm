package cn.iocoder.yudao.module.swm.controller.admin.helmet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(description = "安全帽设备新增/修改 Request VO")
@Data
public class SwmHelmetDeviceSaveReqVO {

    @Schema(description = "安全帽设备编号")
    private String id;

    @Schema(description = "头盔编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "头盔编号不能为空")
    private String deviceId;

    @Schema(description = "头盔类型")
    private String helmetType;

    @Schema(description = "头盔电量")
    private Integer batteryLevel;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "MAC地址")
    private String macAddress;

    @Schema(description = "绑定人员")
    private String assignedPerson;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "手机号码")
    private String personPhone;

    @Schema(description = "Bind time")
    private LocalDateTime bindTime;

    @Schema(description = "Unbind time")
    private LocalDateTime unbindTime;

    @Schema(description = "所属车间")
    private String assignedWorkshop;

    @Schema(description = "所属工序")
    private String assignedProcess;

    @Schema(description = "所属班组")
    private String assignedTeam;

    @Schema(description = "运动状态")
    private String motionStatus;

    @Schema(description = "使用状态")
    private String usageStatus;

    @Schema(description = "服务器IP")
    private String serverIp;

    @Schema(description = "端口Port")
    private String serverPort;

    @Schema(description = "蓝牙扫描窗口(秒)")
    private Integer bluetoothScanWindow;

    @Schema(description = "每组时长(秒)")
    private Integer groupDuration;

    @Schema(description = "普通信标CS")
    private Integer normalBeaconCs;

    @Schema(description = "特殊信标CS")
    private Integer specialBeaconCs;

    @Schema(description = "定位模式")
    private String locationMode;

    @Schema(description = "深度休眠时长(分钟)")
    private Integer deepSleepDuration;

    @Schema(description = "蓝牙扫描持续时间窗口")
    private Integer bluetoothScanDuration;

    @Schema(description = "发送间隔(秒)")
    private Integer sendInterval;

    @Schema(description = "危险源重新触发间隔(秒)")
    private Integer hazardRetriggerInterval;

    @Schema(description = "休眠唤醒时间(秒)")
    private Integer sleepWakeupTime;

    @Schema(description = "接收信标(名称)")
    private String beaconFilterName;

    @Schema(description = "脱帽报警时间间隔")
    private String hatOffAlarmInterval;

    @Schema(description = "设备来源")
    private String deviceSource;

    @Schema(description = "IMEI")
    private String imei;

    @Schema(description = "帽子颜色")
    private String deviceColor;

}
