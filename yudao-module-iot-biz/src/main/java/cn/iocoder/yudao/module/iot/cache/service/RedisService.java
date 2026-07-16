package cn.iocoder.yudao.module.iot.cache.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 服务封装
 */
@Component
public class RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 判断key是否存在
     *
     * @param key key
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取set集合
     *
     * @param key key
     * @return set集合
     */
    public Set<Object> sGet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public long sSet(String key, Object value) {
        Long added = redisTemplate.opsForSet().add(key, value);
        return added != null ? added : 0;
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取hash值
     *
     * @param key   key
     * @param field field
     * @return 值
     */
    public Object hget(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 设置hash值
     *
     * @param key   key
     * @param field field
     * @param value value
     */
    public void hset(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public void hset(String key, String field, Object value, long timeout) {
        redisTemplate.opsForHash().put(key, field, value);
        expire(key, timeout, TimeUnit.SECONDS);
    }

    public boolean setNxValue(String key, String value, long timeout) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public void hdel(String key, String field) {
        redisTemplate.opsForHash().delete(key, field);
    }

    /**
     * 删除key
     *
     * @param key key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除key（别名）
     *
     * @param key key
     */
    public void del(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 设置过期时间
     *
     * @param key     key
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    public void expire(String key, long timeout) {
        expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 从set集合中移除元素
     *
     * @param key   key
     * @param value value
     * @return 移除的数量
     */
    public long setRemove(String key, Object value) {
        Long removed = redisTemplate.opsForSet().remove(key, value);
        return removed != null ? removed : 0;
    }

}
