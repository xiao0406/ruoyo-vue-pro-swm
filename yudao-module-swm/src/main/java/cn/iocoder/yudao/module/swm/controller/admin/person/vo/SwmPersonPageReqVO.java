package cn.iocoder.yudao.module.swm.controller.admin.person.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.Size;

@Schema(description = "人员管理分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmPersonPageReqVO extends PageParam {

    @Schema(description = "姓名")
    @Size(max = 30, message = "姓名长度不能超过30个字符")
    private String name;

    @Schema(description = "人员编码")
    @Size(max = 30, message = "人员编码长度不能超过30个字符")
    private String personNumber;

    @Schema(description = "人员类型")
    @Size(max = 30, message = "人员类型长度不能超过30个字符")
    private String personType;

    @Schema(description = "所属单位")
    @Size(max = 100, message = "所属单位长度不能超过100个字符")
    private String company;

    @Schema(description = "所属车间")
    @Size(max = 100, message = "所属车间长度不能超过100个字符")
    private String department;

    @Schema(description = "人员状态")
    @Size(max = 30, message = "人员状态长度不能超过30个字符")
    private String personnelStatus;

    @Schema(description = "身份证号码")
    @Size(max = 30, message = "身份证号码长度不能超过30个字符")
    private String identityCard;

    @Schema(description = "手机号码")
    @Size(max = 30, message = "手机号码长度不能超过30个字符")
    private String phoneNumber;

}
