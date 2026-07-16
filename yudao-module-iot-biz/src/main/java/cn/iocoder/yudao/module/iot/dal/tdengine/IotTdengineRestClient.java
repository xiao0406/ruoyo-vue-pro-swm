package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.swm.api.enums.TenantDbEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * IoT TDengine REST client.
 */
@Slf4j
@Component
public class IotTdengineRestClient {

    @Value("${tdengine.url}")
    private String url;

    @Value("${tdengine.authorization}")
    private String authorization;

    @Value("${tdengine.dbname}")
    private String dbname;

    public CommonResult<JSONObject> executeTDengineSQL(String sql) {
        Long tenantId = TenantContextHolder.getTenantId();
        return executeByTenantId(sql, tenantId);
    }

    public CommonResult<JSONObject> executeByTenantId(String sql, Long tenantId) {
        String realSql = resolveTenantSql(sql, tenantId);
        log.info("Execute IoT TDengine SQL: {}", realSql);
        try {
            String result = HttpRequest.post(url)
                    .header("Authorization", authorization)
                    .body(realSql)
                    .execute()
                    .body();
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (!"succ".equals(jsonObject.getStr("status"))
                    && (jsonObject.getInt("code") == null || jsonObject.getInt("code") != 0)) {
                log.error("TDengine SQL failed. sql={}, result={}", realSql, result);
                return CommonResult.error(1, jsonObject.getStr("desc"));
            }
            return CommonResult.success(jsonObject);
        } catch (Exception e) {
            log.error("Execute TDengine SQL exception: {}", realSql, e);
            return CommonResult.error(1, "SQL execute exception: " + e.getMessage());
        }
    }

    private String resolveTenantSql(String sql, Long tenantId) {
        if (tenantId == null) {
            return sql;
        }
        String tenantDbName = TenantDbEnum.getDbNameByTenantId(tenantId);
        if (tenantDbName == null || tenantDbName.equals(dbname)) {
            return sql;
        }
        return sql.replace(dbname, tenantDbName);
    }
}
