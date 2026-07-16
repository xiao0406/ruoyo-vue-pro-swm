package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDevicePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDeviceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 安全帽设备 Service 接口
 */
public interface SwmHelmetDeviceService {

    /**
     * 创建安全帽设备
     *
     * @param createReqVO 创建信息
     * @return 设备编号
     */
    String createHelmetDevice(@Valid SwmHelmetDeviceSaveReqVO createReqVO);

    /**
     * 更新安全帽设备
     *
     * @param updateReqVO 更新信息
     */
    void updateHelmetDevice(@Valid SwmHelmetDeviceSaveReqVO updateReqVO);

    /**
     * 删除安全帽设备
     *
     * @param id 设备编号
     */
    void deleteHelmetDevice(String id);

    /**
     * 获得安全帽设备
     *
     * @param id 设备编号
     * @return 安全帽设备
     */
    SwmHelmetDeviceDO getHelmetDevice(String id);

    /**
     * 获得安全帽设备分页
     *
     * @param pageReqVO 分页查询
     * @return 安全帽设备分页
     */
    PageResult<SwmHelmetDeviceDO> getHelmetDevicePage(SwmHelmetDevicePageReqVO pageReqVO);

    SwmHelmetDeviceDO getByDeviceId(String deviceId);

    List<SwmHelmetDeviceDO> findAvailableHelmets(String keyword);

    boolean assignPerson(String deviceId, String personId, String personName, String binder);

    boolean unassignPerson(String deviceId);

    default void save(SwmHelmetDeviceDO device) {
    }

    default void update(SwmHelmetDeviceDO device) {
    }

    default boolean updateServerIp(String deviceId, String value) {
        return false;
    }

    default boolean updateServerPort(String deviceId, String value) {
        return false;
    }

    default boolean updateGroupDuration(String deviceId, String value) {
        return false;
    }

    default boolean updateNormalBeaconCs(String deviceId, String value) {
        return false;
    }

    default boolean updateSpecialBeaconCs(String deviceId, String value) {
        return false;
    }

    default boolean updateDeepSleepDuration(String deviceId, String value) {
        return false;
    }

    default boolean updateLocationMode(String deviceId, String value) {
        return false;
    }

    default boolean updateBluetoothScanWindow(String deviceId, String value) {
        return false;
    }

    default boolean updateBluetoothScanDuration(String deviceId, String value) {
        return false;
    }

    default boolean updateSendInterval(String deviceId, String value) {
        return false;
    }

    default boolean updateHazardRetriggerInterval(String deviceId, String value) {
        return false;
    }

    default boolean updateSleepWakeupTime(String deviceId, String value) {
        return false;
    }

    default boolean updateBeaconFilterName(String deviceId, String value) {
        return false;
    }

    default boolean updatehatOffAlarmInterval(String deviceId, String value) {
        return false;
    }

}
