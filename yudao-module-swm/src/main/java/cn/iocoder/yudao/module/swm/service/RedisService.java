package cn.iocoder.yudao.module.swm.service;

import java.util.Map;

/**
 * Redis 缓存 Service — 兼容 JeeSite 自定义 RedisService
 * 建议后续迁移到 Yudao 的 RedisService（yudao-spring-boot-starter-redis）
 */
public interface RedisService {
    Object get(String key);
    void set(String key, Object value);
    void set(String key, Object value, long timeout);
    void delete(String key);
    Boolean hasKey(String key);
    default Object hget(String key, String field) { return null; }
    default Map<Object, Object> hmget(String key) { return Map.of(); }
    default void hdel(String key, String field) {}
    default void del(String key) { delete(key); }
    default void sSet(String key, String value) {}
    default void expire(String key, long timeout) {}
    default void setRemove(String key, String value) {}
}
