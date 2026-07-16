package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;

/**
 * IOT TDengine access facade.
 */
public interface TDengineService {

    R<JSONObject> executeTDengineSQL(String sql);

    R<JSONObject> executeTDengineSQLByTenantId(String sql, Long tenantId);

    /**
     * Transitional device entry point used by migrated MQTT/TCP handlers.
     */
    R<JSONObject> executeTDengineSQLByDeviceId(String sql, String deviceId);

    default Long selectCount(String sql) {
        R<JSONObject> result = executeTDengineSQL(sql);
        if (result == null || result.getCode() != R.SUCCESS || result.getData() == null) {
            return 0L;
        }
        Object count = result.getData().get("count");
        if (count instanceof Number number) {
            return number.longValue();
        }
        return count == null ? 0L : Long.parseLong(String.valueOf(count));
    }

}
