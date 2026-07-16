package cn.iocoder.yudao.module.iot.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * TCP 发送消息日志 DO
 * 表: iot_tcp_send_message_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("iot_tcp_send_message_log")
public class TcpSendMessageLogDO extends IotBaseDO {
    private String deviceId;
    private String sendMessage;
    /** 发送状态（枚举 IotEnums.TcpSendStatusEnum） */
    private String sendStatus;
    private String errorMessage;
    private String identityCard;
    private String personName;
}
