package cn.iocoder.yudao.module.iot.service;

import cn.iocoder.yudao.module.iot.dal.dataobject.IotCoordinateRequestDO;
import cn.iocoder.yudao.module.iot.util.R;

import java.util.Map;

/**
 * External coordinate service boundary.
 */
public interface ExternalCoordinateService {

    /**
     * Resolve external coordinate data.
     */
    Object getExternalCoordinate(Object... args);

    /**
     * Persist or expose coordinate request data through the migrated RuoYi boundary.
     */
    R<Map<String, Object>> saveCoordinateData(IotCoordinateRequestDO coordinateRequest);
}
