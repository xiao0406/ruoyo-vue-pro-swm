/**
 * @author Shawn
 * @date 2023-08-26
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmSafetyHelmetOrder;

import java.util.List;

/**
 * 安全帽订购记录DAO接口
 */
@MyBatisDao
public interface SwmSafetyHelmetOrderDao extends CrudDao<SwmSafetyHelmetOrder> {

    /**
     * 根据人员ID查询订购记录
     */
    List<SwmSafetyHelmetOrder> findByPersonId(String personId);

    /**
     * 根据安全帽ID查询订购记录
     */
    List<SwmSafetyHelmetOrder> findByHelmetId(String helmetId);

    /**
     * 更新订购状态
     */
    int updateOrderStatus(SwmSafetyHelmetOrder order);
}