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
    public List<SwmSafetyHelmetOrder> findByHelmetId(String helmetId) {
        return dao.findByHelmetId(helmetId);
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
    public void createBindingOrder(String personId, String personName, String helmetId, String currentUser) {
        SwmSafetyHelmetOrder order = new SwmSafetyHelmetOrder();
        order.setPersonId(personId);
        order.setHelmetId(helmetId);
        order.setOrderByPerson(currentUser);
        order.setOrderDate(new Date());
        order.setQuantity(1);
        order.setOrderStatus(SwmSafetyHelmetOrder.OrderStatusEnum.DELIVERED); // 直接设为已发放状态
        order.setReceivedDate(new Date()); // 领取时间设为当前时间
        order.setReceivedSign(personName); // 使用人员姓名作为签名
        order.setRemarks("系统自动创建的安全帽绑定记录");

        this.save(order);
    }
}