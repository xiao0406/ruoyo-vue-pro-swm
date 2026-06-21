package cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "人员工作区域新增/修改 Request VO")
@Data
public class SwmPersonWorkAreaSaveReqVO {

    @Schema(description = "人员工作区域编号")
    private String id;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "身份证号不能为空")
    private String identityCard;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "区域ID")
    private String areaId;

    @Schema(description = "区域名称")
    private String areaName;

}
