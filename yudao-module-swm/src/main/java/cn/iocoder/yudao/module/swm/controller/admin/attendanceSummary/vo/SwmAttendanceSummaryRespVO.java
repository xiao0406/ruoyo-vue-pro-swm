package cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "考勤汇总 Response VO")
@Data
public class SwmAttendanceSummaryRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "员工ID")
    private String employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "身份证号")
    private String identityCard;

    @Schema(description = "phone number")
    private String phoneNumber;
    @Schema(description = "部门")
    private String department;

    @Schema(description = "工段")
    private String workProcess;

    @Schema(description = "班组")
    private String team;

    @Schema(description = "工种")
    private String jobType;

    @Schema(description = "班次")
    private String workShift;

    @Schema(description = "月份")
    private String month;

    @Schema(description = "排班天数")
    private BigDecimal scheduledDays;

    @Schema(description = "实际天数")
    private BigDecimal actualDays;

    @Schema(description = "实际出勤天数")
    private BigDecimal actualAttendanceDays;

    @Schema(description = "出勤率")
    private BigDecimal attendanceRate;

    @Schema(description = "月出勤率")
    private BigDecimal monthlyAttendanceRate;

    @Schema(description = "排班时长")
    private BigDecimal scheduledHours;

    @Schema(description = "实际时长")
    private BigDecimal actualHours;

    @Schema(description = "出勤达成率")
    private BigDecimal attendanceAchievementRate;

    @Schema(description = "空闲时长")
    private BigDecimal idleHours;

    @Schema(description = "效率")
    private BigDecimal efficiency;

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
