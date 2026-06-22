package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.module.iot.service.ExternalCoordinateService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 外部坐标服务实现
 */
@Service
@Validated
public class ExternalCoordinateServiceImpl implements ExternalCoordinateService {

    @Override
    public Object getExternalCoordinate(Object... args) {
        // TODO: 实现外部坐标逻辑
        return null;
    }
}
