package cn.iocoder.yudao.module.swm.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import java.util.List;
import java.util.Map;

/**
 * TDengine 时序数据库 Service
 */
public interface TDengineService {

    String executeSql(String sql);

    List<Map<String, Object>> query(String sql);

    long selectCount(String sql);

    /**
     * 按租户 ID 执行 TDengine SQL
     * @param sql SQL 语句
     * @param tenantId 租户 ID（用于确定 TDengine 数据库）
     */
    CommonResult<JSONObject> executeByTenantId(String sql, Long tenantId);

    /**
     * 按当前租户上下文执行 TDengine SQL
     */
    CommonResult<JSONObject> executeByCurrentTenant(String sql);

    /**
     * @deprecated 使用 {@link #executeByTenantId(String, Long)} 替代
     */
    @Deprecated
    CommonResult<JSONObject> executeTDengineSQLByXXJOB(String sql, Long tenantIdStr);

    default cn.iocoder.yudao.module.swm.util.R<JSONObject> executeTDengineSQLByDeviceId(String sql, String deviceId) {
        CommonResult<JSONObject> result = executeByCurrentTenant(sql);
        if (result.isSuccess()) {
            return cn.iocoder.yudao.module.swm.util.R.ok(result.getData());
        }
        return cn.iocoder.yudao.module.swm.util.R.fail(result.getCode(), result.getMsg());
    }
}
