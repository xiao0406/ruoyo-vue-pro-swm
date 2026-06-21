package cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Schema(description = "人员看板新增/修改 Request VO")
@Data
public class SwmPersonnelBoardSaveReqVO {

    @Schema(description = "人员看板编号")
    private String id;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
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

}
