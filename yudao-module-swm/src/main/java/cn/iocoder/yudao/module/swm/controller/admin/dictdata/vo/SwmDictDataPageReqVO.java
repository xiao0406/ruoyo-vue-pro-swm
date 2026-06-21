package cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "字典数据分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmDictDataPageReqVO extends PageParam {

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "是否系统字典")
    private String isSys;

}
