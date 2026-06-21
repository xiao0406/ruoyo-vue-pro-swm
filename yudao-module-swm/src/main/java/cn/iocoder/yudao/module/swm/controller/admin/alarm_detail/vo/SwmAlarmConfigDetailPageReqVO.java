package cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "告警配置详情分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmAlarmConfigDetailPageReqVO extends PageParam {

    @Schema(description = "主表ID")
    private String mainId;

}
