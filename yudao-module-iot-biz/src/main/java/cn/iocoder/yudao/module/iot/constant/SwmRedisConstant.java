package cn.iocoder.yudao.module.iot.constant;

import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;

/**
 * IoT 模块 Redis Key 常量
 * 桥接 SWM API 中的 SwmRedisKeyConstants，保持旧代码兼容
 */
public class SwmRedisConstant {

    /**
     * 全局 Redis Key（设备-人员映射等）
     * 对应 SwmRedisKeyConstants.GlobalKey
     */
    public static final class RedisGlobalKey {
        public static final String DEVICE_PERSON_MAP = SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP;
        public static final String PERSON_DEVICE_MAP = SwmRedisKeyConstants.GlobalKey.PERSON_DEVICE_MAP;
        public static final String ACTIVE_PERSON_CACHE_KEY = SwmRedisKeyConstants.GlobalKey.ACTIVE_PERSON_CACHE_KEY;
        public static final String IDENTITY_CARD_MAP_KEY = SwmRedisKeyConstants.GlobalKey.IDENTITY_CARD_MAP_KEY;
        public static final String BATTERY_WARN_KEY_PREFIX = SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX;
        public static final String KLT_DEVICE_ID_ALL = SwmRedisKeyConstants.GlobalKey.KLT_DEVICE_ID_ALL;
        public static final String ZT_DEVICE_ID_ALL = SwmRedisKeyConstants.GlobalKey.ZT_DEVICE_ID_ALL;
    }
}
