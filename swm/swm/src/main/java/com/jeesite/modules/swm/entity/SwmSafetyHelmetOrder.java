/**
 * @author Shawn
 * @date 2023-08-26
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 安全帽订购记录实体类
 */
@Table(name = "swm_safety_helmet_order", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键", isPK = true),
        @Column(name = "person_id", attrName = "personId", label = "人员ID"),
        @Column(name = "device_id", attrName = "deviceId", label = "安全帽ID"),
        @Column(name = "order_by", attrName = "orderByPerson", label = "订购人"),
        @Column(name = "order_date", attrName = "orderDate", label = "订购时间"),
        @Column(name = "helmet_model", attrName = "helmetModel", label = "安全帽型号"),
        @Column(name = "helmet_color", attrName = "helmetColor", label = "安全帽颜色"),
        @Column(name = "quantity", attrName = "quantity", label = "订购数量"),
        @Column(name = "order_status", attrName = "orderStatus", label = "订购状态"),
        @Column(name = "received_date", attrName = "receivedDate", label = "领取时间"),
        @Column(name = "received_sign", attrName = "receivedSign", label = "领取签名"),
        @Column(name = "bind_time", attrName = "bindTime", label = "绑定时间"),
        @Column(name = "unbind_time", attrName = "unbindTime", label = "解绑时间"),
        @Column(name = "bind_duration", attrName = "bindDuration", label = "绑定时长(天)"),
        @Column(name = "usage_status", attrName = "usageStatus", label = "使用状态"),
        @Column(name = "binder", attrName = "binder", label = "绑定人员"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
})
public class SwmSafetyHelmetOrder extends DataEntity<SwmSafetyHelmetOrder> {

    private static final long serialVersionUID = 1L;

    // 订购状态枚举
    public static final class OrderStatusEnum {
        public static final String PENDING = "0"; // 待处理
        public static final String PROCESSED = "1"; // 已处理
        public static final String DELIVERED = "2"; // 已发放

        /**
         * 获取订购状态文本
         */
        public static String getText(String status) {
            if (PENDING.equals(status)) {
                return "待处理";
            } else if (PROCESSED.equals(status)) {
                return "已处理";
            } else if (DELIVERED.equals(status)) {
                return "已发放";
            } else {
                return "未知状态";
            }
        }
    }
    
    // 使用状态枚举
    public static final class UsageStatusEnum {
        public static final String USING = "0"; // 使用中
        public static final String UNBINDED = "1"; // 已解绑

        /**
         * 获取使用状态文本
         */
        public static String getText(String status) {
            if (USING.equals(status)) {
                return "使用中";
            } else if (UNBINDED.equals(status)) {
                return "已解绑";
            } else {
                return "未知状态";
            }
        }
    }

    private String personId; // 人员ID
    private String deviceId; // 安全帽ID
    private String orderByPerson; // 订购人
    private Date orderDate; // 订购时间
    private String helmetModel; // 安全帽型号
    private String helmetColor; // 安全帽颜色
    private Integer quantity; // 订购数量
    private String orderStatus; // 订购状态
    private Date receivedDate; // 领取时间
    private String receivedSign; // 领取签名
    private Date bindTime; // 绑定时间
    private Date unbindTime; // 解绑时间
    private Integer bindDuration; // 绑定时长(天)
    private String usageStatus; // 使用状态
    private String binder; // 绑定人员

    public SwmSafetyHelmetOrder() {
        this(null);
    }

    public SwmSafetyHelmetOrder(String id) {
        super(id);
    }

    @NotBlank(message = "人员ID不能为空")
    @Length(min = 0, max = 64, message = "人员ID长度不能超过64个字符")
    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @Length(min = 0, max = 50, message = "安全帽ID长度不能超过50个字符")
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @NotBlank(message = "订购人不能为空")
    @Length(min = 0, max = 64, message = "订购人长度不能超过64个字符")
    public String getOrderByPerson() {
        return orderByPerson;
    }

    public void setOrderByPerson(String orderByPerson) {
        this.orderByPerson = orderByPerson;
    }

    @NotNull(message = "订购时间不能为空")
    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    @Length(min = 0, max = 100, message = "安全帽型号长度不能超过100个字符")
    public String getHelmetModel() {
        return helmetModel;
    }

    public void setHelmetModel(String helmetModel) {
        this.helmetModel = helmetModel;
    }

    @Length(min = 0, max = 50, message = "安全帽颜色长度不能超过50个字符")
    public String getHelmetColor() {
        return helmetColor;
    }

    public void setHelmetColor(String helmetColor) {
        this.helmetColor = helmetColor;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @NotBlank(message = "订购状态不能为空")
    @Length(min = 0, max = 20, message = "订购状态长度不能超过20个字符")
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    @Length(min = 0, max = 100, message = "领取签名长度不能超过100个字符")
    public String getReceivedSign() {
        return receivedSign;
    }

    public void setReceivedSign(String receivedSign) {
        this.receivedSign = receivedSign;
    }
    
    public Date getBindTime() {
        return bindTime;
    }

    public void setBindTime(Date bindTime) {
        this.bindTime = bindTime;
    }

    public Date getUnbindTime() {
        return unbindTime;
    }

    public void setUnbindTime(Date unbindTime) {
        this.unbindTime = unbindTime;
    }

    public Integer getBindDuration() {
        return bindDuration;
    }

    public void setBindDuration(Integer bindDuration) {
        this.bindDuration = bindDuration;
    }

    @NotBlank(message = "使用状态不能为空")
    @Length(min = 0, max = 20, message = "使用状态长度不能超过20个字符")
    public String getUsageStatus() {
        return usageStatus;
    }

    public void setUsageStatus(String usageStatus) {
        this.usageStatus = usageStatus;
    }

    @Length(min = 0, max = 64, message = "绑定人员长度不能超过64个字符")
    public String getBinder() {
        return binder;
    }

    public void setBinder(String binder) {
        this.binder = binder;
    }

    /**
     * 获取订购状态文本
     */
    public String getOrderStatusText() {
        return OrderStatusEnum.getText(this.orderStatus);
    }
    
    /**
     * 获取使用状态文本
     */
    public String getUsageStatusText() {
        return UsageStatusEnum.getText(this.usageStatus);
    }
}