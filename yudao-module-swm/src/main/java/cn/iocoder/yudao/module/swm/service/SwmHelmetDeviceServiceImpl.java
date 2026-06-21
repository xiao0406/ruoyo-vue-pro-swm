package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDevicePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDeviceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmHelmetDeviceMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.HELMET_DEVICE_NOT_EXISTS;

/**
 * 安全帽设备 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmHelmetDeviceServiceImpl implements SwmHelmetDeviceService {

    @Resource
    private SwmHelmetDeviceMapper swmHelmetDeviceMapper;

    @Override
    public String createHelmetDevice(SwmHelmetDeviceSaveReqVO createReqVO) {
        // 插入安全帽设备
        SwmHelmetDeviceDO helmetDevice = BeanUtils.toBean(createReqVO, SwmHelmetDeviceDO.class);
        swmHelmetDeviceMapper.insert(helmetDevice);
        return helmetDevice.getId();
    }

    @Override
    public void updateHelmetDevice(SwmHelmetDeviceSaveReqVO updateReqVO) {
        // 校验存在
        validateHelmetDeviceExists(updateReqVO.getId());

        // 更新安全帽设备
        SwmHelmetDeviceDO updateObj = BeanUtils.toBean(updateReqVO, SwmHelmetDeviceDO.class);
        swmHelmetDeviceMapper.updateById(updateObj);
    }

    @Override
    public void deleteHelmetDevice(String id) {
        // 校验存在
        validateHelmetDeviceExists(id);

        // 删除安全帽设备
        swmHelmetDeviceMapper.deleteById(id);
    }

    @Override
    public SwmHelmetDeviceDO getHelmetDevice(String id) {
        return swmHelmetDeviceMapper.selectById(id);
    }

    @Override
    public PageResult<SwmHelmetDeviceDO> getHelmetDevicePage(SwmHelmetDevicePageReqVO pageReqVO) {
        return swmHelmetDeviceMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validateHelmetDeviceExists(String id) {
        if (swmHelmetDeviceMapper.selectById(id) == null) {
            throw exception(HELMET_DEVICE_NOT_EXISTS);
        }
    }

}
