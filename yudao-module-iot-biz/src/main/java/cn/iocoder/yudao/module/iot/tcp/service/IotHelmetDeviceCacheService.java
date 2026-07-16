package cn.iocoder.yudao.module.iot.tcp.service;

import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class IotHelmetDeviceCacheService {

    @Resource
    private RedisService redisService;

    public void updateMacPersonMappingWithPersonId(String macAddress, String identityCard) {
        if (macAddress == null || macAddress.isBlank() || identityCard == null || identityCard.isBlank()) {
            return;
        }
        redisService.hset(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, macAddress, identityCard);
        redisService.hset(SwmRedisKeyConstants.GlobalKey.PERSON_DEVICE_MAP, identityCard, macAddress);
    }
}
