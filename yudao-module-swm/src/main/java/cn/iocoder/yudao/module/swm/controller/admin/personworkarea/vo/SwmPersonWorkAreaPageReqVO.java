package cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "人员工作区域分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmPersonWorkAreaPageReqVO extends PageParam {

    @Schema(description = "身份证号")
    private String identityCard;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "区域ID")
    private String areaId;

}
