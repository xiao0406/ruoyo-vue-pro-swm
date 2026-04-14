package com.jeesite.modules.constant;

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

        //电量redis报警
        public static final String BATTERY_WARN_KEY_PREFIX = IOT_PREFIX + "battery:warn:";

        /**
         * 科利特所有设备编码，set结构
         */
        public static final String KLT_DEVICE_ID_ALL =  SWM_PREFIX + "KLT:DEVICE_ID_ALL";

        /**
         * 中泰所有设备编码，set结构
         */
        public static final String ZT_DEVICE_ID_ALL = SWM_PREFIX + "ZT:DEVICE_ID_ALL";

        /**
         * 中泰帽子-信标的mac地址和major和minor的缓存映射，hash结构，key为major和minor的组合，value为mac地址
         */
        public static final String MAJOR_MINOR_TO_MAC = SWM_PREFIX + "ZT:MAJOR_MINOR_TO_MAC";

        /**
         * 中泰帽子-设备电量，hash结构，key为设备ID，value为电量
         */
        public static final String ZT_DEVICE_BATTERY = SWM_PREFIX + "ZT:DEVICE_BATTERY";

        /**
         * 中泰帽子-设备蓝牙数据，hash结构，key为设备ID，value为蓝牙数据，有效期60秒
         */
        public static final String ZT_DEVICE_BLE_DATA = SWM_PREFIX + "ZT:DEVICE_BLE_DATA";


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

        /**
         * 危险源反向缓存，用mac地址作为key，value为危险源信息，用于iot服务的危险源报警
         */
        public static final String MAC_TO_HAZARD_INFO = SEPAR + SWM_PREFIX + "mac_to_hazard_info";


        /**
         * 语音模板缓存, hash结构，key为模板ID，value为模板名称
         */
        public static final String VOICE_TEMPLATE_CACHE = SEPAR + SWM_PREFIX + "VOICE_TEMPLATE_CACHE";

        /**
         * 报警配置缓存, hash结构，key为报警配置key，value为是否报警
         */
        public static final String ALARM_CONFIG = SEPAR + SWM_PREFIX + "ALARM_CONFIG";


        /**
         * 区域id -> 区域信息, hash结构，key为区域ID，value为区域详情
         */
        public static final String AREA_ID_CACHE_KEY = SEPAR + SWM_PREFIX + "area:id_to_info";

        /**
         * 信标mac地址到信标信息的映射
         */
        public static final String BEACON_MAC_CACHE_KEY = SEPAR + SWM_PREFIX + "beacon:mac_to_info";
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