package cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "告警配置详情 Response VO")
@Data
public class SwmAlarmConfigDetailRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "角色")
    private String roles;

    @Schema(description = "用户")
    private String users;

    @Schema(description = "主表ID")
    private String mainId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remarks;

}
