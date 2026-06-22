package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.iot.service.IotDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class IotDeviceServiceImpl implements IotDeviceService {

    @Resource
    private IotDeviceMapper iotDeviceMapper;

    @Override
    public String createIotDevice(IotDeviceSaveReqVO reqVO) {
        IotDeviceDO device = BeanUtils.toBean(reqVO, IotDeviceDO.class);
        iotDeviceMapper.insert(device);
        return device.getId();
    }

    @Override
    public void updateIotDevice(IotDeviceSaveReqVO reqVO) {
        validateIotDeviceExists(reqVO.getId());
        IotDeviceDO device = BeanUtils.toBean(reqVO, IotDeviceDO.class);
        iotDeviceMapper.updateById(device);
    }

    @Override
    public void deleteIotDevice(String id) {
        validateIotDeviceExists(id);
        iotDeviceMapper.deleteById(id);
    }

    @Override
    public IotDeviceDO getIotDevice(String id) {
        return iotDeviceMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceDO> getIotDevicePage(IotDevicePageReqVO pageReqVO) {
        return iotDeviceMapper.selectPage(pageReqVO);
    }

    private void validateIotDeviceExists(String id) {
        if (iotDeviceMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.IOT_DEVICE_NOT_EXISTS);
        }
    }
}
