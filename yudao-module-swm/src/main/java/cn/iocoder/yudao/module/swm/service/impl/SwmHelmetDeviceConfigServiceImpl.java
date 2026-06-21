package cn.iocoder.yudao.module.swm.service.impl;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmHelmetDeviceMapper;
import cn.iocoder.yudao.module.swm.service.SwmHelmetDeviceConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Validated
public class SwmHelmetDeviceConfigServiceImpl implements SwmHelmetDeviceConfigService {

    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    @Resource
    private SwmHelmetDeviceMapper swmHelmetDeviceMapper;

    @Override
    public String getConfig(String deviceCode) {
        if (StringUtils.isBlank(deviceCode)) {
            return null;
        }
        String cached = configCache.get(deviceCode);
        if (StringUtils.isNotBlank(cached)) {
            return cached;
        }
        SwmHelmetDeviceDO device = swmHelmetDeviceMapper.selectOne(
                new LambdaQueryWrapper<SwmHelmetDeviceDO>()
                        .eq(SwmHelmetDeviceDO::getDeviceId, deviceCode)
                        .last("LIMIT 1"));
        if (device == null) {
            return null;
        }
        String config = JSONUtil.toJsonStr(device);
        configCache.put(deviceCode, config);
        return config;
    }

    @Override
    public void refreshCache() {
        configCache.clear();
        for (SwmHelmetDeviceDO device : swmHelmetDeviceMapper.selectList(new LambdaQueryWrapper<>())) {
            if (StringUtils.isNotBlank(device.getDeviceId())) {
                configCache.put(device.getDeviceId(), JSONUtil.toJsonStr(device));
            }
        }
    }

}
