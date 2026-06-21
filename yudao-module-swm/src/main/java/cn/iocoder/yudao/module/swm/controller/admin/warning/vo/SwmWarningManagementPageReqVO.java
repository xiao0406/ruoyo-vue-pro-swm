package cn.iocoder.yudao.module.swm.controller.admin.warning.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "预警管理分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmWarningManagementPageReqVO extends PageParam {

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "预警性质")
    private String warningType;

    @Schema(description = "处置状态")
    private String handleStatus;

    @Schema(description = "告警类型")
    private String type;

    @Schema(description = "区域")
    private String area;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "告警开始时间")
    private LocalDateTime beginAlarmTime;

    @Schema(description = "告警结束时间")
    private LocalDateTime endAlarmTime;

}
