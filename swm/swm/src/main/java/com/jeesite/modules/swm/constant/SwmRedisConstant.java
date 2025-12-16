package com.jeesite.modules.swm.constant;

/**
 * SWM模块Redis缓存常量
 * 
 * @author Shawn
 */
public class SwmRedisConstant {

    public static final String SEPAR = ":";
    public static final String SWM_PREFIX = "SWM:";
    public static final String IOT_PREFIX = "iot:";



    /**
     * 这里是缓存的所有租户数据
     */
    public static class RedisGlobalKey {
        /**
         * 设备与租户的映射关系
         */
        public static final String DEVICE_TO_CORP = SWM_PREFIX + "DEVICE_TO_CORP";


        /**
         * 设备ID到分配人员的映射缓存   HGETALL SWM:HELMET:DEVICE_PERSON_MAP
         */
        public static final String DEVICE_PERSON_MAP = SWM_PREFIX + "HELMET:DEVICE_PERSON_MAP";

        /**
         * 人员到设备ID的映射缓存   HGETALL SWM:HELMET:PERSON_DEVICE_MAP
         */
        public static final String PERSON_DEVICE_MAP = SWM_PREFIX + "HELMET:PERSON_DEVICE_MAP";

        /**
         * 在职人员缓存的Redis Key
         */
        public static final String ACTIVE_PERSON_CACHE_KEY = "SWM:ACTIVE_PERSON_CACHE";

        /**
         * 身份证到人员ID映射的Redis Key
         */
        public static final String IDENTITY_CARD_MAP_KEY = "SWM:IDENTITY_CARD_MAP";


    }


    /**
     * Swm模块Redis缓存常量
     */
    public class RedisSwmKey {

        /**
         * 生产区域缓存，List<String> 存的是生产区域的主键信息
         */
        public static final String AREA_CACHE = SEPAR + SWM_PREFIX + "AREA_CACHE";

        /**
         * 生产区域报警标识，值为1     GET IS_PROD_Voice_ALARM:安全区域id
         */
        public static final String IS_PROD_Voice_ALARM = SEPAR + SWM_PREFIX + "is_prod_voice_alarm";


        /**
         * 哪些信标标识为不用报警
         */
        public static final String Hazard_ISALARM_BEACON = SEPAR + SWM_PREFIX + "Hazard_ISALARM_BEACON";
        //增加生产区域语音报警间隔
        public static final String PRODUCTION_Voice_ALARM_INTERVAL = SEPAR + SWM_PREFIX + "PRODUCTION_Voice_ALARM_INTERVAL";


    }


    /**
     * Iot模块Redis缓存常量
     */
    public class RedisIotKey {


        // 区域缓存相关常量
        public static final String AREA_CACHE_KEY_PREFIX = ":iot:area:name:";
        public static final String AREA_ALL_CACHE_KEY = SEPAR + IOT_PREFIX + "area:all";
        public static final String AREA_NAME_TO_ID_CACHE_KEY = SEPAR + IOT_PREFIX + "area:name_to_id";


        // 信标基站相关常量
        public static final String BEACON_CACHE_KEY_PREFIX = SEPAR + IOT_PREFIX + "beacon:station:area:id:";  // 按区域ID存储
        public static final String BEACON_CACHE_NAME_TO_IDS = SEPAR + IOT_PREFIX + "beacon:station:name2ids:"; // 区域名称到ID映射
        public static final String BEACON_ALL_CACHE_KEY = SEPAR + IOT_PREFIX + "beacon:station:all";
        public static final String BEACON_ALL_AREA_IDS = SEPAR + IOT_PREFIX + "beacon:station:all_area_ids"; // 所有区域ID列表



        /**
         * Redis缓存Key - 头盔MAC地址到人员信息的映射
         */
        public static final String HELMET_MAC_CACHE_KEY = SEPAR + IOT_PREFIX + "helmet:mac_to_person";

        /**
         * 统一在线设备集合 key
         * SMEMBERS ZJGGJS:iot:devices:online
         */
        public static final String ONLINE_DEVICES_KEY = SEPAR+IOT_PREFIX + "devices:online";
        /**
         * 单个设备 TTL key 前缀
         */
        public static final String DEVICE_TTL_KEY_PREFIX = SEPAR+IOT_PREFIX + "device:ttl:";
    }


}