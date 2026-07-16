package cn.iocoder.yudao.module.swm.service;

/**
 * 安全帽设备配置 Service 接口
 */
public interface SwmHelmetDeviceConfigService {

    /**
     * 根据设备编号获取配置
     *
     * @param deviceCode 设备编号
     * @return 配置 JSON 字符串
     */
    String getConfig(String deviceCode);

    /**
     * 刷新设备配置缓存
     */
    void refreshCache();

    default <T extends cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceConfigDO> T getByDeviceId(String deviceId) {
        return null;
    }

}
