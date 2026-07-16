package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyHelmetOrderDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmSafetyHelmetOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.HELMET_DEVICE_NOT_EXISTS;

/**
 * 安全帽订单 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmSafetyHelmetOrderServiceImpl implements SwmSafetyHelmetOrderService {

    @Resource
    private SwmSafetyHelmetOrderMapper swmSafetyHelmetOrderMapper;

    @Override
    public String createSafetyHelmetOrder(SwmSafetyHelmetOrderSaveReqVO createReqVO) {
        // 插入安全帽订单
        SwmSafetyHelmetOrderDO safetyHelmetOrder = BeanUtils.toBean(createReqVO, SwmSafetyHelmetOrderDO.class);
        swmSafetyHelmetOrderMapper.insert(safetyHelmetOrder);
        return safetyHelmetOrder.getId();
    }

    @Override
    public void updateSafetyHelmetOrder(SwmSafetyHelmetOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateSafetyHelmetOrderExists(updateReqVO.getId());

        // 更新安全帽订单
        SwmSafetyHelmetOrderDO updateObj = BeanUtils.toBean(updateReqVO, SwmSafetyHelmetOrderDO.class);
        swmSafetyHelmetOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteSafetyHelmetOrder(String id) {
        // 校验存在
        validateSafetyHelmetOrderExists(id);

        // 删除安全帽订单
        swmSafetyHelmetOrderMapper.deleteById(id);
    }

    @Override
    public SwmSafetyHelmetOrderDO getSafetyHelmetOrder(String id) {
        return swmSafetyHelmetOrderMapper.selectById(id);
    }

    @Override
    public PageResult<SwmSafetyHelmetOrderDO> getSafetyHelmetOrderPage(SwmSafetyHelmetOrderPageReqVO pageReqVO) {
        LambdaQueryWrapper<SwmSafetyHelmetOrderDO> wrapper = new LambdaQueryWrapper<SwmSafetyHelmetOrderDO>()
                .eq(pageReqVO.getPersonId() != null && !pageReqVO.getPersonId().isBlank(),
                        SwmSafetyHelmetOrderDO::getPersonId, pageReqVO.getPersonId())
                .like(pageReqVO.getDeviceId() != null && !pageReqVO.getDeviceId().isBlank(),
                        SwmSafetyHelmetOrderDO::getDeviceId, pageReqVO.getDeviceId())
                .eq(pageReqVO.getOrderStatus() != null && !pageReqVO.getOrderStatus().isBlank(),
                        SwmSafetyHelmetOrderDO::getOrderStatus, pageReqVO.getOrderStatus())
                .eq(pageReqVO.getHelmetModel() != null && !pageReqVO.getHelmetModel().isBlank(),
                        SwmSafetyHelmetOrderDO::getHelmetModel, pageReqVO.getHelmetModel())
                .orderByDesc(SwmSafetyHelmetOrderDO::getCreateTime);
        return swmSafetyHelmetOrderMapper.selectPage(pageReqVO, wrapper);
    }

    private void validateSafetyHelmetOrderExists(String id) {
        if (swmSafetyHelmetOrderMapper.selectById(id) == null) {
            throw exception(HELMET_DEVICE_NOT_EXISTS);
        }
    }

}
