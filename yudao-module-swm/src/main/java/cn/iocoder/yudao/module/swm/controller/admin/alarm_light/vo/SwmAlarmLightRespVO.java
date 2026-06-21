package cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "报警灯 Response VO")
@Data
public class SwmAlarmLightRespVO {

    @Schema(description = "报警灯编号")
    private String id;

    @Schema(description = "报警灯名称")
    private String lightName;

    @Schema(description = "SN编码")
    private String snCode;

    @Schema(description = "是否启用报警")
    private String enableAlarm;

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
