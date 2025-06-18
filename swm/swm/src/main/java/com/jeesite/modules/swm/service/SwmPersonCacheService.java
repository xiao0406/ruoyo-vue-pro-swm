package com.jeesite.modules.swm.service;

import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.dao.SwmPersonDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人员Redis缓存服务
 * 用于缓存在职人员的基本信息
 * 
 * @author Shawn
 * @date 2025-01-15
 */
@Service
@Slf4j
public class SwmPersonCacheService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SwmPersonDao swmPersonDao;

    /**
     * 在职人员缓存的Redis Key
     */
    private static final String ACTIVE_PERSON_CACHE_KEY = "SWM:ACTIVE_PERSON_CACHE";

    /**
     * 身份证到人员ID映射的Redis Key
     */
    private static final String IDENTITY_CARD_MAP_KEY = "SWM:IDENTITY_CARD_MAP";

    /**
     * 程序启动时初始化在职人员缓存
     * 延迟初始化，避免循环依赖问题
     */
    @PostConstruct
    public void initActivePersonCache() {
        try {
            log.info("开始初始化在职人员缓存...");

            // 检查Redis连接状态
            if (!isRedisAvailable()) {
                log.warn("Redis连接不可用，跳过人员缓存初始化");
                return;
            }

            // 查询所有在职人员
            SwmPerson queryCondition = new SwmPerson();
            queryCondition.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
            List<SwmPerson> activePersons = swmPersonDao.findList(queryCondition);

            if (activePersons == null || activePersons.isEmpty()) {
                log.warn("未查询到在职人员数据");
                return;
            }

            // 清除旧缓存
            clearActivePersonCache();

            // 批量添加到缓存
            Map<String, Object> personCacheMap = new HashMap<>();
            Map<String, Object> identityCardMap = new HashMap<>();

            for (SwmPerson person : activePersons) {
                // 构建缓存的人员信息（只包含指定字段）
                Map<String, Object> personInfo = buildPersonCacheInfo(person);

                // 使用人员ID作为Redis Hash的field
                personCacheMap.put(person.getId(), personInfo);

                // 建立身份证到人员ID的映射
                if (person.getIdentityCard() != null && !person.getIdentityCard().trim().isEmpty()) {
                    identityCardMap.put(person.getIdentityCard(), person.getId());
                }
            }

            // 批量存储到Redis
            if (!personCacheMap.isEmpty()) {
                redisService.hmset(ACTIVE_PERSON_CACHE_KEY, personCacheMap);
                log.info("成功缓存{}条在职人员信息", personCacheMap.size());
            }

            // 存储身份证映射
            if (!identityCardMap.isEmpty()) {
                redisService.hmset(IDENTITY_CARD_MAP_KEY, identityCardMap);
                log.info("成功缓存{}条身份证映射信息", identityCardMap.size());
            }

            log.info("在职人员缓存初始化完成，共{}条记录", activePersons.size());

        } catch (Exception e) {
            log.error("初始化在职人员缓存失败", e);
        }
    }

    /**
     * 构建缓存的人员信息（只包含指定字段）
     */
    private Map<String, Object> buildPersonCacheInfo(SwmPerson person) {
        Map<String, Object> personInfo = new HashMap<>();

        // 只缓存需要的字段
        personInfo.put("name", person.getName()); // 姓名
        personInfo.put("company", person.getCompany()); // 所属单位
        personInfo.put("department", person.getDepartment()); // 所属车间
        personInfo.put("prodLine", person.getProdLine()); // 产线
        personInfo.put("team", person.getTeam()); // 所属班组
        personInfo.put("jobType", person.getJobType()); // 工种
        personInfo.put("identityCard", person.getIdentityCard()); // 身份证号码

        return personInfo;
    }

    /**
     * 根据身份证号查询在职人员信息
     */
    public Map<String, Object> getActivePersonByIdentityCard(String identityCard) {
        if (identityCard == null || identityCard.trim().isEmpty()) {
            return null;
        }

        try {
            // 先从身份证映射中获取人员ID
            String personId = (String) redisService.hget(IDENTITY_CARD_MAP_KEY, identityCard);

            if (personId == null) {
                log.debug("缓存中未找到身份证{}对应的人员ID", identityCard);
                return null;
            }

            // 根据人员ID获取人员信息
            Map<String, Object> personInfo = (Map<String, Object>) redisService.hget(ACTIVE_PERSON_CACHE_KEY, personId);

            if (personInfo != null) {
                log.debug("从缓存中获取到身份证{}对应的人员信息：{}", identityCard, personInfo.get("name"));
            }

            return personInfo;

        } catch (Exception e) {
            log.error("从缓存中查询身份证{}对应的人员信息失败", identityCard, e);
            return null;
        }
    }

    /**
     * 根据人员ID获取缓存的人员信息
     */
    public Map<String, Object> getActivePersonById(String personId) {
        if (personId == null || personId.trim().isEmpty()) {
            return null;
        }

        try {
            return (Map<String, Object>) redisService.hget(ACTIVE_PERSON_CACHE_KEY, personId);
        } catch (Exception e) {
            log.error("从缓存中查询人员ID{}失败", personId, e);
            return null;
        }
    }

    /**
     * 获取所有在职人员的缓存信息
     */
    public Map<Object, Object> getAllActivePersons() {
        try {
            return redisService.hmget(ACTIVE_PERSON_CACHE_KEY);
        } catch (Exception e) {
            log.error("获取所有在职人员缓存失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 人员状态变更时更新缓存
     * 当人员从在职变为离职时，从缓存中移除
     * 当人员从离职变为在职时，添加到缓存中
     */
    public void updatePersonCache(SwmPerson person) {
        if (person == null || person.getId() == null) {
            return;
        }

        try {
            if (SwmPerson.PersonStatusEnum.ACTIVE.equals(person.getPersonnelStatus())) {
                // 在职状态，添加或更新缓存
                Map<String, Object> personInfo = buildPersonCacheInfo(person);
                redisService.hset(ACTIVE_PERSON_CACHE_KEY, person.getId(), personInfo);

                // 更新身份证映射
                if (person.getIdentityCard() != null && !person.getIdentityCard().trim().isEmpty()) {
                    redisService.hset(IDENTITY_CARD_MAP_KEY, person.getIdentityCard(), person.getId());
                }

                log.debug("更新在职人员缓存：{}", person.getName());

            } else {
                // 非在职状态，从缓存中移除
                redisService.hdel(ACTIVE_PERSON_CACHE_KEY, person.getId());

                // 移除身份证映射
                if (person.getIdentityCard() != null && !person.getIdentityCard().trim().isEmpty()) {
                    redisService.hdel(IDENTITY_CARD_MAP_KEY, person.getIdentityCard());
                }

                log.debug("从在职人员缓存中移除：{}", person.getName());
            }

        } catch (Exception e) {
            log.error("更新人员缓存失败，人员ID：{}", person.getId(), e);
        }
    }

    /**
     * 删除人员时从缓存中移除
     */
    public void removePersonFromCache(SwmPerson person) {
        if (person == null || person.getId() == null) {
            return;
        }

        try {
            redisService.hdel(ACTIVE_PERSON_CACHE_KEY, person.getId());

            if (person.getIdentityCard() != null && !person.getIdentityCard().trim().isEmpty()) {
                redisService.hdel(IDENTITY_CARD_MAP_KEY, person.getIdentityCard());
            }

            log.debug("从缓存中删除人员：{}", person.getName());

        } catch (Exception e) {
            log.error("从缓存中删除人员失败，人员ID：{}", person.getId(), e);
        }
    }

    /**
     * 清除所有在职人员缓存
     */
    public void clearActivePersonCache() {
        try {
            redisService.del(ACTIVE_PERSON_CACHE_KEY);
            redisService.del(IDENTITY_CARD_MAP_KEY);
            log.info("已清除所有在职人员缓存");
        } catch (Exception e) {
            log.error("清除在职人员缓存失败", e);
        }
    }

    /**
     * 重新加载在职人员缓存
     */
    public void reloadActivePersonCache() {
        log.info("手动重新加载在职人员缓存");
        initActivePersonCache();
    }

    /**
     * 检查Redis连接状态
     */
    public boolean isRedisAvailable() {
        try {
            redisService.set("SWM:PERSON_HEALTH_CHECK", "OK", 10);
            String result = (String) redisService.get("SWM:PERSON_HEALTH_CHECK");
            redisService.del("SWM:PERSON_HEALTH_CHECK");
            return "OK".equals(result);
        } catch (Exception e) {
            log.error("Redis连接检查失败", e);
            return false;
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            Map<Object, Object> personCache = redisService.hmget(ACTIVE_PERSON_CACHE_KEY);
            Map<Object, Object> identityCardCache = redisService.hmget(IDENTITY_CARD_MAP_KEY);

            stats.put("activePersonCount", personCache != null ? personCache.size() : 0);
            stats.put("identityCardMapCount", identityCardCache != null ? identityCardCache.size() : 0);
            stats.put("redisAvailable", isRedisAvailable());

        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }
}