package cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "处置记录分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmHandleRecordPageReqVO extends PageParam {

    @Schema(description = "处置记录名称")
    private String recordName;

    @Schema(description = "预警ID")
    private String warningId;

    @Schema(description = "处置状态")
    private String handleStatus;

}
