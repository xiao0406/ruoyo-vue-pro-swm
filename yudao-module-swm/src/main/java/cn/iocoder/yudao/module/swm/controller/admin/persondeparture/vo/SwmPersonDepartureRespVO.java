package cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "人员退场 Response VO")
@Data
public class SwmPersonDepartureRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "姓名")
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
