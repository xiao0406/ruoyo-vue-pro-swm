package cn.iocoder.yudao.module.swm.enums;

/**
 * SWM Redis Key 常量
 *
 * 迁移自 JeeSite: com.jeesite.modules.constant.SwmRedisConstant
 */
public interface SwmRedisKeyConstants {

    String SEPAR = ":";
    String SWM_PREFIX = "SWM:";
    String IOT_PREFIX = "iot:";

    /**
     * 全局 Redis Key（设备-人员映射等）
     */
    interface GlobalKey {
        String DEVICE_TO_CORP = "swm:device_to_corp";
        String DEVICE_PERSON_MAP = "swm:device_person_map";
        String PERSON_DEVICE_MAP = "swm:person_device_map";
        String ACTIVE_PERSON_CACHE_KEY = "swm:active_person_cache";
        String IDENTITY_CARD_MAP_KEY = "swm:identity_card_map";
        String BATTERY_WARN_KEY_PREFIX = "swm:battery_warn:";
        String KLT_DEVICE_ID_ALL = "swm:klt_device_id_all";
        String ZT_DEVICE_ID_ALL = "swm:zt_device_id_all";
    }

    /**
     * SWM 模块 Redis Key
     */
    interface SwmKey {
        String AREA_CACHE = "swm:area_cache";
        String IS_PROD_VOICE_ALARM = "swm:is_prod_voice_alarm";
        String HAZARD_IS_ALARM_BEACON = "swm:hazard_is_alarm_beacon";
        String PRODUCTION_VOICE_ALARM_INTERVAL = "swm:production_voice_alarm_interval";
        String MAC_TO_HAZARD_INFO = "swm:mac_to_hazard_info";
        String VOICE_TEMPLATE_CACHE = "swm:voice_template_cache";
        String ALARM_CONFIG = "swm:alarm_config";
        String AREA_ID_CACHE_KEY = "swm:area_id_cache";
        String BEACON_MAC_CACHE_KEY = "swm:beacon_mac_cache";
        String MAJOR_MINOR_TO_MAC = "swm:major_minor_to_mac";
        String ZT_DEVICE_BATTERY = "swm:zt_device_battery";
        String ZT_DEVICE_BLE_DATA = "swm:zt_device_ble_data";
        String ZT_DEVICE_MESSAGE_DATA = "swm:zt_device_message_data";
    }

    /**
     * IoT 模块 Redis Key
     */
    interface IotKey {
        String AREA_CACHE_KEY_PREFIX = "iot:area:";
        String AREA_ALL_CACHE_KEY = "iot:area:all";
        String AREA_NAME_TO_ID_CACHE_KEY = "iot:area:name_to_id";
        String BEACON_CACHE_KEY_PREFIX = "iot:beacon:";
        String BEACON_CACHE_NAME_TO_IDS = "iot:beacon:name_to_ids";
        String BEACON_ALL_CACHE_KEY = "iot:beacon:all";
        String BEACON_ALL_AREA_IDS = "iot:beacon:all_area_ids";
        String HELMET_MAC_CACHE_KEY = "iot:helmet_mac";
        String ONLINE_DEVICES_KEY = "iot:online_devices";
        String DEVICE_TTL_KEY_PREFIX = "iot:device_ttl:";
    }

}
