/**
 * @author Shawn
 * @date 2023-08-26
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmSafetyHelmetOrderDao;
import com.jeesite.modules.swm.entity.SwmSafetyHelmetOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 安全帽订购记录服务类
 */
@Service
@Transactional(readOnly = true)
public class SwmSafetyHelmetOrderService extends CrudService<SwmSafetyHelmetOrderDao, SwmSafetyHelmetOrder> {

    /**
     * 获取单条数据
     */
    @Override
    public SwmSafetyHelmetOrder get(String id) {
        return super.get(id);
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmSafetyHelmetOrder> findPage(SwmSafetyHelmetOrder order) {
        return super.findPage(order);
    }

    /**
     * 保存数据
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmSafetyHelmetOrder order) {
        // 设置默认值
        if (order.getIsNewRecord()) {
            // 订购时间
            if (order.getOrderDate() == null) {
                order.setOrderDate(new Date());
            }

            // 订购人
            if (StringUtils.isBlank(order.getOrderByPerson())) {
                order.setOrderByPerson("系统管理员");
            }

            // 订购状态
            if (StringUtils.isBlank(order.getOrderStatus())) {
                order.setOrderStatus(SwmSafetyHelmetOrder.OrderStatusEnum.PENDING); // 默认状态为待处理
            }

            // 订购数量
            if (order.getQuantity() == null) {
                order.setQuantity(1); // 默认数量为1
            }
            
            // 使用状态
            if (StringUtils.isBlank(order.getUsageStatus())) {
                order.setUsageStatus(SwmSafetyHelmetOrder.UsageStatusEnum.USING); // 默认使用中 - 值为"0"
            }
        }

        super.save(order);
    }

    /**
     * 删除数据
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmSafetyHelmetOrder order) {
        super.delete(order);
    }

    /**
     * 根据人员ID查询订购记录
     */
    public List<SwmSafetyHelmetOrder> findByPersonId(String personId) {
        return dao.findByPersonId(personId);
    }

    /**
     * 根据安全帽ID查询订购记录
     */
    public List<SwmSafetyHelmetOrder> findByDeviceId(String deviceId) {
        return dao.findByDeviceId(deviceId);
    }

    /**
     * 更新订购状态
     */
    @Transactional(readOnly = false)
    public int updateOrderStatus(SwmSafetyHelmetOrder order) {
        order.setUpdateDate(new Date());
        return dao.updateOrderStatus(order);
    }

    /**
     * 创建安全帽绑定订购记录
     */
    @Transactional(readOnly = false)
    public void createBindingOrder(String personId, String personName, String deviceId, String currentUser) {
        SwmSafetyHelmetOrder order = new SwmSafetyHelmetOrder();
        order.setPersonId(personId);
        order.setDeviceId(deviceId);
        order.setOrderByPerson(currentUser);
        order.setOrderDate(new Date());
        order.setOrderStatus(SwmSafetyHelmetOrder.OrderStatusEnum.DELIVERED);
        order.setReceivedDate(new Date());
        order.setReceivedSign(personName);
        order.setQuantity(1);
        order.setHelmetModel("标准型");
        order.setHelmetColor("白色");
        order.setRemarks("系统自动创建的绑定记录");
        
        // 设置绑定时间和使用状态
        Date now = new Date();
        order.setBindTime(now);
        order.setUsageStatus(SwmSafetyHelmetOrder.UsageStatusEnum.USING); // 已更新为"0"
        
        this.save(order);
    }
    
    /**
     * 创建安全帽绑定订购记录（包含身份证信息）
     */
    @Transactional(readOnly = false)
    public void createBindingOrder(String personId, String personName, String deviceId, String currentUser, String identityCard) {
        SwmSafetyHelmetOrder order = new SwmSafetyHelmetOrder();
        order.setPersonId(personId);
        order.setDeviceId(deviceId);
        order.setOrderByPerson(currentUser);
        order.setOrderDate(new Date());
        order.setOrderStatus(SwmSafetyHelmetOrder.OrderStatusEnum.DELIVERED);
        order.setReceivedDate(new Date());
        order.setReceivedSign(personName);
        order.setQuantity(1);
        order.setHelmetModel("标准型");
        order.setHelmetColor("白色");
        order.setRemarks("系统自动创建的绑定记录");
        
        // 设置绑定时间和使用状态
        Date now = new Date();
        order.setBindTime(now);
        order.setUsageStatus(SwmSafetyHelmetOrder.UsageStatusEnum.USING);
        // 设置绑定人员(身份证)
        order.setBinder(identityCard);
        
        this.save(order);
    }
    
    /**
     * 绑定安全帽
     */
    @Transactional(readOnly = false)
    public void bindHelmet(String orderId) {
        SwmSafetyHelmetOrder order = super.get(orderId);
        if (order != null) {
            Date now = new Date();
            order.setBindTime(now);
            order.setUsageStatus(SwmSafetyHelmetOrder.UsageStatusEnum.USING);
            super.save(order);
        }
    }
    
    /**
     * 绑定安全帽（带身份证信息）
     */
    @Transactional(readOnly = false)
    public void bindHelmet(String orderId, String binder) {
        SwmSafetyHelmetOrder order = super.get(orderId);
        if (order != null) {
            Date now = new Date();
            order.setBindTime(now);
            order.setUsageStatus(SwmSafetyHelmetOrder.UsageStatusEnum.USING);
            if (binder != null && !binder.isEmpty()) {
                order.setBinder(binder);
            }
            super.save(order);
        }
    }
    
    /**
     * 解绑安全帽
     */
    @Transactional(readOnly = false)
    public void unbindHelmet(String orderId) {
        SwmSafetyHelmetOrder order = super.get(orderId);
        if (order != null && SwmSafetyHelmetOrder.UsageStatusEnum.USING.equals(order.getUsageStatus())) {
            Date now = new Date();
            order.setUnbindTime(now);
            order.setUsageStatus(SwmSafetyHelmetOrder.UsageStatusEnum.UNBINDED);
            
            // 计算绑定时长（天）
            if (order.getBindTime() != null) {
                // 计算毫秒差
                long durationMillis = now.getTime() - order.getBindTime().getTime();
                // 转换为天数（向上取整）
                int durationDays = (int) Math.ceil(durationMillis / (1000.0 * 60 * 60 * 24));
                order.setBindDuration(durationDays);
            }
            
            super.save(order);
        }
    }
    
    /**
     * 根据设备ID查询当前使用中的订单
     */
    public SwmSafetyHelmetOrder findActiveOrderByDeviceId(String deviceId) {
        List<SwmSafetyHelmetOrder> orders = dao.findByDeviceId(deviceId);
        if (orders != null && !orders.isEmpty()) {
            for (SwmSafetyHelmetOrder order : orders) {
                if (SwmSafetyHelmetOrder.UsageStatusEnum.USING.equals(order.getUsageStatus())) {
                    return order;
                }
            }
        }
        return null;
    }
    
    /**
     * 根据人员ID查询当前使用中的订单
     */
    public List<SwmSafetyHelmetOrder> findActiveOrdersByPersonId(String personId) {
        List<SwmSafetyHelmetOrder> allOrders = dao.findByPersonId(personId);
        if (allOrders != null && !allOrders.isEmpty()) {
            allOrders.removeIf(order -> !SwmSafetyHelmetOrder.UsageStatusEnum.USING.equals(order.getUsageStatus()));
        }
        return allOrders;
    }
}