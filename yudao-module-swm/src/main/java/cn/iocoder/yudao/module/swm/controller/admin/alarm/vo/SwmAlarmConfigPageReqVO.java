package cn.iocoder.yudao.module.swm.controller.admin.alarm.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "告警配置分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmAlarmConfigPageReqVO extends PageParam {

    @Schema(description = "报警名称")
    private String alarmName;

    @Schema(description = "报警唯一标识key")
    private String alarmKey;

    @Schema(description = "是否报警")
    private Integer enableAlarm;

}
