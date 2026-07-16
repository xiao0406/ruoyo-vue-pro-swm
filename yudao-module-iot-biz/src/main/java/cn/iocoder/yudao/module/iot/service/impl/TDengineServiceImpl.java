package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotTdengineRestClient;
import cn.iocoder.yudao.module.iot.service.TDengineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * IOT TDengine facade backed by the migrated TDengine REST client.
 */
@Slf4j
@Service
public class TDengineServiceImpl implements TDengineService {

    @Resource
    private IotTdengineRestClient tdengineRestClient;

    @Override
    public R<JSONObject> executeTDengineSQL(String sql) {
        return toR(tdengineRestClient.executeTDengineSQL(sql));
    }

    @Override
    public R<JSONObject> executeTDengineSQLByTenantId(String sql, Long tenantId) {
        return toR(tdengineRestClient.executeByTenantId(sql, tenantId));
    }

    @Override
    public R<JSONObject> executeTDengineSQLByDeviceId(String sql, String deviceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            return executeTDengineSQLByTenantId(sql, tenantId);
        }
        log.debug("TDengine SQL for device {} uses default database because tenant context is empty", deviceId);
        return executeTDengineSQL(sql);
    }

    private R<JSONObject> toR(CommonResult<JSONObject> result) {
        if (result == null) {
            return R.fail("TDengine returned empty result");
        }
        if (result.isSuccess()) {
            return R.ok(result.getData());
        }
        return R.fail(result.getCode(), result.getMsg());
    }

}
