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
        this.save(order);
    }
}