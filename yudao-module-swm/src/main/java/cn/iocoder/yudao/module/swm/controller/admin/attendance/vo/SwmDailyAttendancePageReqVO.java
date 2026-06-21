package cn.iocoder.yudao.module.swm.controller.admin.attendance.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Schema(description = "每日考勤分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmDailyAttendancePageReqVO extends PageParam {

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

    @Schema(description = "考勤日期（开始）")
    private LocalDate beginAttendanceDate;

    @Schema(description = "考勤日期（结束）")
    private LocalDate endAttendanceDate;

    @Schema(description = "班次")
    private String classes;

    @Schema(description = "考勤是否正常（0正常 1异常）")
    private String attendanceNormal;

}
