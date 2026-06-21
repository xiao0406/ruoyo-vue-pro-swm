package cn.iocoder.yudao.module.swm.controller.admin.worktype.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "工种管理分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmWorkTypePageReqVO extends PageParam {

    @Schema(description = "工种名称")
    private String workType;

    @Schema(description = "工种编码")
    private String workTypeCode;

}
