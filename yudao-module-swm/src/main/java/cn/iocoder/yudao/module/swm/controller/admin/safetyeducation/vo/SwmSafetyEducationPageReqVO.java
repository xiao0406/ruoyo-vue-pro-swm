package cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(name = "安全教育分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SwmSafetyEducationPageReqVO extends PageParam {
    @Schema(description = "主题") private String theme;
    @Schema(description = "安全教育类型") private String safetyEducationType;
    @Schema(description = "状态") private String safetyStatus;
}
