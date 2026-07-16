package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotCoordinateRequestDO;
import cn.iocoder.yudao.module.iot.service.ExternalCoordinateService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * External coordinate service implementation.
 */
@Service
@Validated
public class ExternalCoordinateServiceImpl implements ExternalCoordinateService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public Object getExternalCoordinate(Object... args) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", "local");
        result.put("args", args);
        return result;
    }

    @Override
    public R<Map<String, Object>> saveCoordinateData(IotCoordinateRequestDO coordinateRequest) {
        if (coordinateRequest == null) {
            return R.fail("coordinateRequest cannot be null");
        }
        Map<String, Object> payload = toPayload(coordinateRequest);
        R<JSONObject> writeResult = payloadWriter.savePayload(
                "external_coordinate_data", coordinateRequest.getElderId(), coordinateRequest.getType(), payload);
        payload.put("persisted", writeResult.getCode() == R.SUCCESS);
        payload.put("message", writeResult.getMsg());
        return writeResult.getCode() == R.SUCCESS ? R.ok(payload) : R.fail(writeResult.getMsg());
    }

    private Map<String, Object> toPayload(IotCoordinateRequestDO coordinateRequest) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("elderId", coordinateRequest.getElderId());
        result.put("x", coordinateRequest.getX());
        result.put("originalX", coordinateRequest.getOriginalX());
        result.put("scaleX", coordinateRequest.getScaleX());
        result.put("y", coordinateRequest.getY());
        result.put("originalY", coordinateRequest.getOriginalY());
        result.put("scaleY", coordinateRequest.getScaleY());
        result.put("address", coordinateRequest.getAddress());
        result.put("mapId", coordinateRequest.getMapId());
        result.put("orgCd", coordinateRequest.getOrgCd());
        result.put("type", coordinateRequest.getType());
        result.put("appId", coordinateRequest.getAppId());
        result.put("warningId", coordinateRequest.getWarningId());
        result.put("timeStr", coordinateRequest.getTimeStr());
        result.put("nearestBeacon", coordinateRequest.getNearestBeacon());
        result.put("usedBeacons", coordinateRequest.getUsedBeacons());
        return result;
    }
}
