package cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "人员工作区域 Response VO")
@Data
public class SwmPersonWorkAreaRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "身份证号")
    private String identityCard;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "区域ID")
    private String areaId;

    @Schema(description = "区域名称")
    private String areaName;

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
