package cn.iocoder.yudao.module.iot.controller.admin.tcp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(name = "TCP消息日志 Response VO")
@Data
public class TcpSendMessageLogRespVO {

    @Schema(description = "主键")
    private String id;
    @Schema(description = "设备ID")
    private String deviceId;
    @Schema(description = "发送消息")
    private String sendMessage;
    @Schema(description = "发送状态")
    private String sendStatus;
    @Schema(description = "错误消息")
    private String errorMessage;
    @Schema(description = "身份证号")
    private String identityCard;
    @Schema(description = "人员姓名")
    private String personName;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
