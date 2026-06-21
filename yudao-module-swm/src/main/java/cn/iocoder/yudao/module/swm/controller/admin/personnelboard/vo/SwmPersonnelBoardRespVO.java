package cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "人员看板 Response VO")
@Data
public class SwmPersonnelBoardRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "组织")
    private String organization;

    @Schema(description = "车间")
    private String workshop;

    @Schema(description = "工序")
    private String process;

    @Schema(description = "班组")
    private String team;

    @Schema(description = "工作状态")
    private String workStatus;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "安全帽状态")
    private String helmetStatus;

    @Schema(description = "人员状态")
    private String personnelStatus;

    @Schema(description = "考勤次数")
    private Integer attendanceCount;

    @Schema(description = "工作时长")
    private BigDecimal workingHours;

    @Schema(description = "空闲时长")
    private BigDecimal idleHours;

    @Schema(description = "身份证号")
    private String idCard;

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

}
