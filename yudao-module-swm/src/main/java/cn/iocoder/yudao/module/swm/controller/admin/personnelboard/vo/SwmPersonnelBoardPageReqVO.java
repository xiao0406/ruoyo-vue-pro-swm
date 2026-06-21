package cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "人员看板分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmPersonnelBoardPageReqVO extends PageParam {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "组织")
    private String organization;

    @Schema(description = "工作状态")
    private String workStatus;

    @Schema(description = "人员状态")
    private String personnelStatus;

    @Schema(description = "身份证号")
    private String idCard;

}
