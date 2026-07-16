package cn.iocoder.yudao.module.iot.service;

/**
 * 安全帽设备缓存服务接口
 */
public interface HelmetDeviceCacheService {

    default String getPersonIdentityByMac(String mac, String defaultValue) {
        return defaultValue;
    }
}
