package cn.iocoder.yudao.module.iot.controller.admin.tcp.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(name = "TCP消息日志分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TcpSendMessageLogPageReqVO extends PageParam {

    @Schema(description = "设备ID")
    private String deviceId;
    @Schema(description = "发送状态")
    private String sendStatus;
    @Schema(description = "身份证号")
    private String identityCard;
}
