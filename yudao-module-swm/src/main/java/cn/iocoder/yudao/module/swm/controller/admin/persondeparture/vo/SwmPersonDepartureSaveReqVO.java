package cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "人员退场新增/修改 Request VO")
@Data
public class SwmPersonDepartureSaveReqVO {

    @Schema(description = "人员退场编号")
    private String id;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "人员类型")
    private String personType;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "公司")
    private String company;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "工序")
    private String workProcess;

    @Schema(description = "班组")
    private String team;

    @Schema(description = "工种")
    private String jobType;

    @Schema(description = "安全帽编号")
    private String safetyHelmetId;

    @Schema(description = "安全教育")
    private String safetyEducation;

    @Schema(description = "身份证号")
    private String identityCard;

    @Schema(description = "手机号")
    private String phoneNumber;

    @Schema(description = "人员状态")
    private String personnelStatus;

    @Schema(description = "安全帽是否归还")
    private String helmetReturned;

    @Schema(description = "退场类型")
    private String departureType;

    @Schema(description = "退场原因")
    private String departureReason;

    @Schema(description = "退场日期")
    private LocalDate departureDate;

}
