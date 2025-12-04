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
        /** 设备ID到分配人员的映射缓存   SWM:HELMET:DEVICE_PERSON_MAP*/
        public static final String DEVICE_PERSON_MAP = SWM_PREFIX + "HELMET:DEVICE_PERSON_MAP";

        /** 人员到设备ID的映射缓存   HGETALL SWM:HELMET:PERSON_DEVICE_MAP */
        public static final String PERSON_DEVICE_MAP = SWM_PREFIX + "HELMET:PERSON_DEVICE_MAP";
    }

    /**
     * 区域数据相关缓存（IOT模块初始化）
     */
    public static class Area {
        /** 所有区域数据缓存 */
        public static final String ALL_AREA_DATA = "iot:area:all";
    }

    /**
     * 危险源管理（危险源是否报警）
     */
    public static class HazardSource {
        /** 哪些信标标识为不用报警 */
        public static final String Hazard_ISALARM_BEACON = "Hazard_ISALARM_BEACON";
    }

    /**
     * 缓存过期时间（秒）
     */
    public static class TTL {
        /** 映射关系缓存时间 - 永不过期 */
        public static final long MAPPING = -1;
    }

    /**
     * 设备信息
     */
    public static class Device {
        /**
         * Redis在线设备集合Key
         * SMEMBERS online_devices
         */
        public static final String ONLINE_DEVICES_KEY = "iot:devices:online";

        /**
         * 设备与租户的映射关系
         */
        public static final String DEVICE_TO_CORP =SWM_PREFIX + "DEVICE_TO_CORP";
    }

    /**
     * 工作区域缓存服务
     */
    public static class AreaCache {
        /** 哪些信标标识为不用报警 */
        public static final String AREA_CACHE =SWM_PREFIX + "AREA_CACHE";
    }

}