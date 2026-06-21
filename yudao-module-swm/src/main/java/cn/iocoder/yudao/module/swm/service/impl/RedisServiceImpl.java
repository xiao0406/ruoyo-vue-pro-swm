package cn.iocoder.yudao.module.swm.service.impl;
import cn.iocoder.yudao.module.swm.service.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
@Service @Slf4j
public class RedisServiceImpl implements RedisService {
    @Resource private StringRedisTemplate stringRedisTemplate;
    @Override public Object get(String key) { return stringRedisTemplate.opsForValue().get(key); }
    @Override public void set(String key, Object value) { stringRedisTemplate.opsForValue().set(key, String.valueOf(value)); }
    @Override public void set(String key, Object value, long timeout) { stringRedisTemplate.opsForValue().set(key, String.valueOf(value), timeout, TimeUnit.SECONDS); }
    @Override public void delete(String key) { stringRedisTemplate.delete(key); }
    @Override public Boolean hasKey(String key) { return stringRedisTemplate.hasKey(key); }
}
