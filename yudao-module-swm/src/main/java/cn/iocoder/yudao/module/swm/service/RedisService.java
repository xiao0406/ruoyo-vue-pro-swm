package cn.iocoder.yudao.module.swm.service;
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
}
