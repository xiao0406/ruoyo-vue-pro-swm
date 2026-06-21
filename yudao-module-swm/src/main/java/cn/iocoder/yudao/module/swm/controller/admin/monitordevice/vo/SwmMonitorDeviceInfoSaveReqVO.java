package cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "监控设备新增/修改 Request VO")
@Data
public class SwmMonitorDeviceInfoSaveReqVO {

    @Schema(description = "监控设备编号")
    private String id;

    @Schema(description = "记录类型")
    private String recType;

    @Schema(description = "父节点ID")
    private String parentId;

    @Schema(description = "树排序")
    private String treeSort;

    @Schema(description = "树叶子节点")
    private String treeLeaf;

    @Schema(description = "父节点名称")
    private String parentName;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设备名称不能为空")
    private String name;

    @Schema(description = "设备编码")
    private String code;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "RTSP端口")
    private String rtspPort;

    @Schema(description = "管理员账号")
    private String adminUser;

    @Schema(description = "管理员密码")
    private String adminPassword;

    @Schema(description = "RTSP地址")
    private String rtspUri;

    @Schema(description = "通道号")
    private String channel;

    @Schema(description = "子类型")
    private String subtype;

    @Schema(description = "资源来源")
    private String loadSource;

    @Schema(description = "流地址")
    private String streamUrl;

    @Schema(description = "摄像机索引编码")
    private String cameraIndexCode;

}
