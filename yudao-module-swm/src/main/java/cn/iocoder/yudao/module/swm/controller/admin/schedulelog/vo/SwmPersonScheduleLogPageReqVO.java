package cn.iocoder.yudao.module.swm.controller.admin.schedulelog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "排班日志分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmPersonScheduleLogPageReqVO extends PageParam {

    @Schema(description = "操作人")
    private String operateUser;

    @Schema(description = "人员ID")
    private String personId;

    @Schema(description = "目标班次")
    private String targetClasses;

}
