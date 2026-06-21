package cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Schema(description = "人员退场分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmPersonDeparturePageReqVO extends PageParam {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "身份证号")
    private String identityCard;

    @Schema(description = "退场类型")
    private String departureType;

    @Schema(description = "退场日期 - 开始")
    private LocalDate departureDateStart;

    @Schema(description = "退场日期 - 结束")
    private LocalDate departureDateEnd;

}
