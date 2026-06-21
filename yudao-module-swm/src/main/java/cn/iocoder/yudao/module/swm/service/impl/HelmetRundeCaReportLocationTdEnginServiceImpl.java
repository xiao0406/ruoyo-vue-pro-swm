package cn.iocoder.yudao.module.swm.service.impl;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.swm.dal.tdengine.TdengineRestClient;
import cn.iocoder.yudao.module.swm.service.HelmetRundeCaReportLocationTdEnginService;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全帽定位数据 TDengine 服务实现
 */
@Slf4j
@Service
public class HelmetRundeCaReportLocationTdEnginServiceImpl implements HelmetRundeCaReportLocationTdEnginService {

    @Resource
    private TdengineRestClient tdengineRestClient;

    @Override
    public CommonResult<Map<String, Object>> getFirstAndLastTimeByIdCardAndDate(String idCard, String dateStr, String workTimeRange) {
        try {
            String sql = String.format(
                "SELECT first(ts) as firstTime, last(ts) as lastTime FROM helmet_location WHERE id_card='%s' AND ts>='%s' AND ts<='%s 23:59:59'",
                idCard, dateStr, dateStr);
            CommonResult<JSONObject> result = tdengineRestClient.executeByCurrentTenant(sql);
            if (result.getCode() != 0) {
                return CommonResult.error(result.getCode(), result.getMsg());
            }
            JSONObject data = result.getData();
            Map<String, Object> resultMap = new HashMap<>();
            if (data != null && data.containsKey("data")) {
                JSONArray rows = data.getJSONArray("data");
                if (rows != null && rows.size() > 0) {
                    JSONArray row = rows.getJSONArray(0);
                    resultMap.put("firstTime", row.getStr(0));
                    resultMap.put("lastTime", row.getStr(1));
                }
            }
            return CommonResult.success(resultMap);
        } catch (Exception e) {
            log.error("查询安全帽定位首末时间失败, idCard={}, date={}", idCard, dateStr, e);
            return CommonResult.error(500, "查询失败: " + e.getMessage());
        }
    }
}
