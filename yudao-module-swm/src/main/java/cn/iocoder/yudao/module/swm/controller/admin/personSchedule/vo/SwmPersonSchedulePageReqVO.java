package cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "人员排班分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmPersonSchedulePageReqVO extends PageParam {

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "月份")
    private String month;

    @Schema(description = "班次")
    private String classes;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "员工ID")
    private String employeeId;

    @Schema(description = "人员ID")
    private String personId;

    @Schema(description = "组织")
    private String organization;

    @Schema(description = "车间")
    private String workshop;

    @Schema(description = "工序")
    private String process;

}
