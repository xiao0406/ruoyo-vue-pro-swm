package cn.iocoder.yudao.module.swm.controller.admin.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "每日考勤 Response VO")
@Data
public class SwmDailyAttendanceRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "员工ID")
    private String employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "身份证号")
    private String identityCard;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "人员类型")
    private String personType;

    @Schema(description = "考勤日期")
    private LocalDate attendanceDate;

    @Schema(description = "工作时间段")
    private String workTimeRange;

    @Schema(description = "班次")
    private String classes;

    @Schema(description = "上班打卡时间")
    private LocalDateTime clockInTime;

    @Schema(description = "上班打卡日期")
    private LocalDateTime clockInDate;

    @Schema(description = "午休结束时间")
    private LocalDateTime noonEndTime;

    @Schema(description = "午休结束日期")
    private LocalDateTime noonEndDate;

    @Schema(description = "下午开始时间")
    private LocalDateTime afterStartTime;

    @Schema(description = "下午开始日期")
    private LocalDateTime afterStartDate;

    @Schema(description = "下班打卡时间")
    private LocalDateTime clockOutTime;

    @Schema(description = "下班打卡日期")
    private LocalDateTime clockOutDate;

    @Schema(description = "打卡开始时间")
    private LocalDateTime clockStartTime;

    @Schema(description = "打卡结束时间")
    private LocalDateTime clockEndTime;

    @Schema(description = "排班时长")
    private BigDecimal scheduledHours;

    @Schema(description = "休息时间")
    private BigDecimal restTime;

    @Schema(description = "实际时长")
    private BigDecimal actualHours;

    @Schema(description = "空闲时长")
    private BigDecimal idleHours;

    @Schema(description = "有效工作时长")
    private BigDecimal effectiveWorkHours;

    @Schema(description = "日效率")
    private BigDecimal dailyEfficiency;

    @Schema(description = "日达成率")
    private BigDecimal dailyAchievementRate;

    @Schema(description = "考勤是否正常（0正常 1异常）")
    private String attendanceNormal;

    @Schema(description = "当前位置（0工作区 1休息区）")
    private String currentPosition;

    @Schema(description = "是否触发未下班补偿且未恢复")
    private Boolean pendingClockOutCompensate;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "租户编号")
    private Long tenantId;

}
