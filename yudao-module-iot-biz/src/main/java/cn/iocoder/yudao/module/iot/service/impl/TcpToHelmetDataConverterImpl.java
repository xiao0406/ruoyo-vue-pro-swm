package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.module.iot.service.TcpToHelmetDataConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TcpToHelmetDataConverterImpl implements TcpToHelmetDataConverter {

    @Override
    public List<Map<String, Object>> convertBeaconData(List<Map<String, Object>> tcpBeaconData) {
        if (tcpBeaconData == null) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> beacon : tcpBeaconData) {
            Map<String, Object> converted = new HashMap<>(beacon);
            result.add(converted);
        }
        return result;
    }
}
