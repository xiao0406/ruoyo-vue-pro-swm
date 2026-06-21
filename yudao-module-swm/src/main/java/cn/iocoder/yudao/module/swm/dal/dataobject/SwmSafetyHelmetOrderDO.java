package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 安全帽订单 DO
 * 表: swm_safety_helmet_order
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_safety_helmet_order")
public class SwmSafetyHelmetOrderDO extends SwmBaseDO {
    private String personId;
    private String deviceId;
    private String orderByPerson;
    private LocalDateTime orderDate;
    private String helmetModel;
    private String helmetColor;
    private Integer quantity;
    /** 订单状态（枚举 SwmEnums.OrderStatusEnum） */
    private String orderStatus;
    private LocalDateTime receivedDate;
    private String receivedSign;
    private LocalDateTime bindTime;
    private LocalDateTime unbindTime;
    private Integer bindDuration;
    private String usageStatus;
    private String binder;
}
