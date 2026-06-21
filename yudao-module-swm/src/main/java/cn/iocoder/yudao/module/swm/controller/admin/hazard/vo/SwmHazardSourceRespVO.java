package cn.iocoder.yudao.module.swm.controller.admin.hazard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "危险源 Response VO")
@Data
public class SwmHazardSourceRespVO {

    @Schema(description = "编号")
    private String id;

    @Schema(description = "危险源名称")
    private String hazardName;

    @Schema(description = "危险源类别")
    private String hazardCategory;

    @Schema(description = "所在位置")
    private String location;

    @Schema(description = "信标标识")
    private String beaconIdentifier;

    @Schema(description = "是否纳入巡检")
    private String isPatrolIncluded;

    @Schema(description = "巡检记录摘要")
    private String patrolRecordSummary;

    @Schema(description = "登记时间")
    private LocalDateTime registrationTime;

    @Schema(description = "危险源状态")
    private String hazardStatus;

    @Schema(description = "巡检频率(天)")
    private Integer frequencyDays;

    @Schema(description = "责任人ID")
    private String responsiblePersonId;

    @Schema(description = "责任人")
    private String responsiblePerson;

    @Schema(description = "首次检查时间")
    private LocalDateTime firstInspectionTime;

    @Schema(description = "语音模板ID")
    private String voiceTemplateId;

    @Schema(description = "信标标签")
    private String beaconTag;

    @Schema(description = "是否草稿")
    private String isDraft;

    @Schema(description = "过滤身份证号")
    private String filterIdentityCard;

    @Schema(description = "过滤人员")
    private String filterPersonnel;

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
