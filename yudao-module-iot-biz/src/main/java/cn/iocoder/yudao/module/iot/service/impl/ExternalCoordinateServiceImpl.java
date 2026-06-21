package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.module.iot.service.ExternalCoordinateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 外部坐标服务实现
 */
@Service
@Validated
@Slf4j
public class ExternalCoordinateServiceImpl implements ExternalCoordinateService {

    @Override
    public Object getExternalCoordinate(Object... args) {
        // TODO: 实现外部坐标逻辑
        return null;
    }
}
