package cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "一键召回分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmOneClickRecallPageReqVO extends PageParam {

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "撤离方案")
    private String evacuationPlan;

    @Schema(description = "召回结果")
    private String recallResult;

}
