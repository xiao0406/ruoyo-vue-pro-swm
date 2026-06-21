package cn.iocoder.yudao.module.iot.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotDeviceDO;
import jakarta.validation.Valid;

public interface IotDeviceService {
    String createIotDevice(@Valid IotDeviceSaveReqVO createReqVO);
    void updateIotDevice(@Valid IotDeviceSaveReqVO updateReqVO);
    void deleteIotDevice(String id);
    IotDeviceDO getIotDevice(String id);
    PageResult<IotDeviceDO> getIotDevicePage(IotDevicePageReqVO pageReqVO);
}
