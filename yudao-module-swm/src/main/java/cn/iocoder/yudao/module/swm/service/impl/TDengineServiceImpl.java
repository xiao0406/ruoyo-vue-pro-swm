package cn.iocoder.yudao.module.swm.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.swm.dal.tdengine.TdengineRestClient;
import cn.iocoder.yudao.module.swm.service.TDengineService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TDengineServiceImpl implements TDengineService {

    @Resource
    private TdengineRestClient tdengineRestClient;

    @Override
    public String executeSql(String sql) {
        CommonResult<JSONObject> result = tdengineRestClient.executeTDengineSQL(sql);
        result.checkError();
        return result.getData().toString();
    }

    @Override
    public List<Map<String, Object>> query(String sql) {
        CommonResult<JSONObject> result = tdengineRestClient.executeTDengineSQL(sql);
        result.checkError();
        return toRows(result.getData());
    }

    @Override
    public long selectCount(String sql) {
        List<Map<String, Object>> rows = query(sql);
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            return 0L;
        }
        Object value = rows.get(0).values().iterator().next();
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    @Override
    public CommonResult<JSONObject> executeByTenantId(String sql, Long tenantId) {
        return tdengineRestClient.executeByTenantId(sql, tenantId);
    }

    @Override
    public CommonResult<JSONObject> executeByCurrentTenant(String sql) {
        Long tenantId = TenantContextHolder.getTenantId();
        return tdengineRestClient.executeByTenantId(sql, tenantId);
    }

    @SuppressWarnings("deprecation")
    @Override
    public CommonResult<JSONObject> executeTDengineSQLByXXJOB(String sql, Long tenantIdStr) {
        return tdengineRestClient.executeTDengineSQLByXXJOB(sql, tenantIdStr);
    }

    private List<Map<String, Object>> toRows(JSONObject response) {
        JSONArray heads = response.getJSONArray("head");
        JSONArray rows = response.getJSONArray("data");
        if (heads == null || rows == null) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            JSONArray row = rows.getJSONArray(i);
            Map<String, Object> item = new LinkedHashMap<>();
            for (int j = 0; j < heads.size(); j++) {
                item.put(heads.getStr(j), row.get(j));
            }
            result.add(item);
        }
        return result;
    }

}
