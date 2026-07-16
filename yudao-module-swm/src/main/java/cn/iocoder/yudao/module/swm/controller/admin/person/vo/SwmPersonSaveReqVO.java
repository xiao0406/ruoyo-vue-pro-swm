package cn.iocoder.yudao.module.swm.controller.admin.person.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "人员管理新增/修改 Request VO")
@Data
public class SwmPersonSaveReqVO {

    @Schema(description = "人员编号")
    private String id;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "人员编码")
    private String personNumber;

    @Schema(description = "年龄")
    private String age;

    @Schema(description = "人员类型")
    private String personType;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "所属单位")
    private String company;

    @Schema(description = "所属车间")
    private String department;

    @Schema(description = "产线")
    private String prodLine;

    @Schema(description = "所属工序")
    private String workProcess;

    @Schema(description = "所属班组")
    private String team;

    @Schema(description = "所属工种")
    private String jobType;

    @Schema(description = "部门")
    private String dept;

    @Schema(description = "职务")
    private String position;

    @Schema(description = "关联安全帽编号")
    private String safetyHelmetId;

    @Schema(description = "人员状态")
    private String personnelStatus;

    @Schema(description = "入场安全教育")
    private String safetyEducation;

    @Schema(description = "身份证号码")
    private String identityCard;

    @Schema(description = "手机号码")
    private String phoneNumber;

    @Schema(description = "紧急联系人")
    private String urgentPerson;

    @Schema(description = "紧急联系人手机号")
    private String urgentPhoneNumber;

    @Schema(description = "是否归还安全帽")
    private String helmetReturned;

    @Schema(description = "离职类型")
    private String departureType;

    @Schema(description = "离职原因")
    private String departureReason;

    @Schema(description = "离职时间")
    private LocalDate departureDate;

    @Schema(description = "是否厂内员工")
    private String isExternalPersonnel;

    @Schema(description = "血型")
    private String bloodType;

}
