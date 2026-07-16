package cn.iocoder.yudao.module.swm.service;

import java.util.Map;

/**
 * 安全帽设备缓存 Service 接口
 */
public interface HelmetDeviceCacheService {

    /**
     * 获取设备缓存信息
     *
     * @param deviceCode 设备编号
     * @return 设备缓存数据
     */
    Map<String, Object> getDeviceCache(String deviceCode);

    /**
     * 更新设备缓存
     *
     * @param deviceCode 设备编号
     * @param data       缓存数据
     */
    void updateDeviceCache(String deviceCode, Map<String, Object> data);

    /**
     * 清除设备缓存
     *
     * @param deviceCode 设备编号
     */
    void evictDeviceCache(String deviceCode);

    default void updateMacPersonMappingWithPersonId(String mac, String personId) {
    }

}
