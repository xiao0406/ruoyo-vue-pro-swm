package cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "人员排班 Response VO")
@Data
public class SwmPersonScheduleRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "月份")
    private String month;

    @Schema(description = "班次")
    private String classes;

    @Schema(description = "班次文本")
    private String classesText;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "员工ID")
    private String employeeId;

    @Schema(description = "班组名称")
    private String workGroupName;

    @Schema(description = "人员ID")
    private String personId;

    @Schema(description = "组织")
    private String organization;

    @Schema(description = "车间")
    private String workshop;

    @Schema(description = "工序")
    private String process;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "人员类型")
    private String personType;

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
