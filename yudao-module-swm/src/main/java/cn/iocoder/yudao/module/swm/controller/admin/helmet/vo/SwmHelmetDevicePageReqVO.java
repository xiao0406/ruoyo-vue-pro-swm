package cn.iocoder.yudao.module.swm.controller.admin.helmet.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "安全帽设备分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmHelmetDevicePageReqVO extends PageParam {

    @Schema(description = "头盔编号")
    private String deviceId;

    @Schema(description = "头盔类型")
    private String helmetType;

    @Schema(description = "绑定人员")
    private String assignedPerson;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "所属车间")
    private String assignedWorkshop;

    @Schema(description = "所属工序")
    private String assignedProcess;

    @Schema(description = "使用状态")
    private String usageStatus;

    @Schema(description = "设备来源")
    private String deviceSource;

}
