package cn.iocoder.yudao.module.swm.controller.admin.inspectionplan.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(name = "巡检计划分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SwmInspectionPlanPageReqVO extends PageParam {
    @Schema(description = "计划名称") private String planName;
    @Schema(description = "巡检类型") private String inspectionType;
    @Schema(description = "巡检状态") private String planStatus;
}
