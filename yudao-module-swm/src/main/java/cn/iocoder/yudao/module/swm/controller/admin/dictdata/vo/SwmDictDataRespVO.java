package cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "字典数据 Response VO")
@Data
public class SwmDictDataRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "字典图标")
    private String dictIcon;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "是否系统字典")
    private String isSys;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "CSS样式")
    private String cssStyle;

    @Schema(description = "CSS类名")
    private String cssClass;

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
