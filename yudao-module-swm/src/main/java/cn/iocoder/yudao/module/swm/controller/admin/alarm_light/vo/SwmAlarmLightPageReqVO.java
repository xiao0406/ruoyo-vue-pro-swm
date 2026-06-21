package cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "报警灯分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmAlarmLightPageReqVO extends PageParam {

    @Schema(description = "报警灯名称")
    private String lightName;

    @Schema(description = "SN编码")
    private String snCode;

    @Schema(description = "是否启用报警")
    private String enableAlarm;

}
