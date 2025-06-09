package com.jeesite.modules.swm.constant;

/**
 * SWM模块Redis缓存常量
 * 
 * @author Shawn
 */
public class SwmRedisConstant {

    /**
     * SWM模块统一前缀
     */
    public static final String SWM_PREFIX = "SWM:";

    /**
     * 头盔设备相关缓存
     */
    public static class Helmet {
        /** 设备ID到分配人员的映射缓存 */
        public static final String DEVICE_PERSON_MAP = SWM_PREFIX + "HELMET:DEVICE_PERSON_MAP";

        /** 人员到设备ID的映射缓存 */
        public static final String PERSON_DEVICE_MAP = SWM_PREFIX + "HELMET:PERSON_DEVICE_MAP";
    }

    /**
     * 缓存过期时间（秒）
     */
    public static class TTL {
        /** 映射关系缓存时间 - 永不过期 */
        public static final long MAPPING = -1;
    }
}