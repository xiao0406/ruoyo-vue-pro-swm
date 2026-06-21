package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyHelmetOrderDO;
import jakarta.validation.Valid;

/**
 * 安全帽订单 Service 接口
 */
public interface SwmSafetyHelmetOrderService {

    /**
     * 创建安全帽订单
     *
     * @param createReqVO 创建信息
     * @return 安全帽订单编号
     */
    String createSafetyHelmetOrder(@Valid SwmSafetyHelmetOrderSaveReqVO createReqVO);

    /**
     * 更新安全帽订单
     *
     * @param updateReqVO 更新信息
     */
    void updateSafetyHelmetOrder(@Valid SwmSafetyHelmetOrderSaveReqVO updateReqVO);

    /**
     * 删除安全帽订单
     *
     * @param id 安全帽订单编号
     */
    void deleteSafetyHelmetOrder(String id);

    /**
     * 获得安全帽订单
     *
     * @param id 安全帽订单编号
     * @return 安全帽订单
     */
    SwmSafetyHelmetOrderDO getSafetyHelmetOrder(String id);

    /**
     * 获得安全帽订单分页
     *
     * @param pageReqVO 分页查询
     * @return 安全帽订单分页
     */
    PageResult<SwmSafetyHelmetOrderDO> getSafetyHelmetOrderPage(SwmSafetyHelmetOrderPageReqVO pageReqVO);

}
