package cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "监控设备 Response VO")
@Data
public class SwmMonitorDeviceInfoRespVO {

    @Schema(description = "主键")
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

    @Schema(description = "设备名称")
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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remarks;

}
