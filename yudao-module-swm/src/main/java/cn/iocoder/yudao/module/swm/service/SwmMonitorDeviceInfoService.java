package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo.SwmMonitorDeviceInfoPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo.SwmMonitorDeviceInfoSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmMonitorDeviceInfoDO;
import jakarta.validation.Valid;

/**
 * 监控设备 Service 接口
 */
public interface SwmMonitorDeviceInfoService {

    /**
     * 创建监控设备
     *
     * @param createReqVO 创建信息
     * @return 监控设备编号
     */
    String createMonitorDeviceInfo(@Valid SwmMonitorDeviceInfoSaveReqVO createReqVO);

    /**
     * 更新监控设备
     *
     * @param updateReqVO 更新信息
     */
    void updateMonitorDeviceInfo(@Valid SwmMonitorDeviceInfoSaveReqVO updateReqVO);

    /**
     * 删除监控设备
     *
     * @param id 监控设备编号
     */
    void deleteMonitorDeviceInfo(String id);

    /**
     * 获得监控设备
     *
     * @param id 监控设备编号
     * @return 监控设备
     */
    SwmMonitorDeviceInfoDO getMonitorDeviceInfo(String id);

    /**
     * 获得监控设备分页
     *
     * @param pageReqVO 分页查询
     * @return 监控设备分页
     */
    PageResult<SwmMonitorDeviceInfoDO> getMonitorDeviceInfoPage(SwmMonitorDeviceInfoPageReqVO pageReqVO);

}
