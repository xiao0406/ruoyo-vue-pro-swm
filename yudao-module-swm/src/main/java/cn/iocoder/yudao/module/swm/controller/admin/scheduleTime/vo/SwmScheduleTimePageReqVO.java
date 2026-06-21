package cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "排班时间分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmScheduleTimePageReqVO extends PageParam {

    @Schema(description = "班次类型")
    private String shiftType;

    @Schema(description = "休息天数")
    private String restDays;

}
