package cn.iocoder.yudao.module.swm.controller.admin.joblog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "任务日志分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmJobLogPageReqVO extends PageParam {

    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "执行状态")
    private String executeStatus;

}
