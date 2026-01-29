package com.jeesite.modules.swm.service;

import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.dao.SwmPersonDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.jeesite.modules.sys.utils.DictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人员Redis缓存服务
 * 用于缓存在职人员的基本信息
 * 
 * @author Shawn
 * @date 2025-01-15
 */
@Service
@Slf4j
public class SwmPersonCacheService implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SwmPersonDao swmPersonDao;
    @Autowired
    private UserService userService;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initActivePersonCache();
    }

    /**
     * 程序启动时初始化在职人员缓存
     * 延迟初始化，避免循环依赖问题
     * 
     * @author Shawn
     * @date 2025/06/23 - 修改为使用自定义SQL获取完整信息
     */
//    @PostConstruct
    public void initActivePersonCache() {

        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }


        // 批量添加到缓存
        Map<String, Object> personCacheMap = new HashMap<>();
        Map<String, Object> identityCardMap = new HashMap<>();

        //为每个租户都生成排班计划
        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();
            //设置当前线程的租户信息
            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            XxlJobHelper.log("开始处理租户：{} ========================", corpCode);

            try {
                log.info("开始初始化在职人员缓存...");

                // 检查Redis连接状态
                if (!isRedisAvailable()) {
                    log.warn("Redis连接不可用，跳过人员缓存初始化");
                    return;
                }

                int random = new Random().nextInt(1_000_000);

                // 使用自定义SQL查询获取包含各表ID的完整人员信息
                List<Map<String, Object>> activePersonsWithIds = swmPersonDao.findActivePersonsWithIds(random,null);

                if (activePersonsWithIds == null || activePersonsWithIds.isEmpty()) {
                    log.warn("未查询到在职人员数据");
                    continue;
                }

                // 清除旧缓存
                clearActivePersonCache();

                for (Map<String, Object> personData : activePersonsWithIds) {
                    String personId = (String) personData.get("id");
                    String identityCard = (String) personData.get("identityCard");

                    // 构建缓存的人员信息（包含各表ID）
                    Map<String, Object> personInfo = buildPersonCacheInfoWithIds(personData);

                    // 使用人员ID作为Redis Hash的field
                    personCacheMap.put(personId, personInfo);

                    // 建立身份证到人员ID的映射
                    if (identityCard != null && !identityCard.trim().isEmpty()) {
                        // 检查身份证是否已存在，如果存在则记录警告
                        if (identityCardMap.containsKey(identityCard)) {
                            String existingPersonId = (String) identityCardMap.get(identityCard);
                            log.warn("发现重复身份证号码：{}，人员ID：{}，已存在人员ID：{}，将使用最新的人员记录",
                                    identityCard, personId, existingPersonId);
                        }
                        identityCardMap.put(identityCard, personId);
                    }
                }


                // 批量存储到Redis
                if (!personCacheMap.isEmpty()) {
                    redisService.hmset(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY, personCacheMap);
                    log.info("成功缓存{}条在职人员信息", personCacheMap.size());
                }
                // 批量存储到Redis
                if (!personCacheMap.isEmpty()) {
                    redisService.del(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY);
                    redisService.hmset(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY, personCacheMap);
                    log.info("成功缓存{}条在职人员信息", personCacheMap.size());
                }

                // 存储身份证映射
                if (!identityCardMap.isEmpty()) {
                    redisService.hmset(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY, identityCardMap);
                    log.info("成功缓存{}条身份证映射信息", identityCardMap.size());
                    // 存储身份证映射
                    if (!identityCardMap.isEmpty()) {
                        redisService.del(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY);
                        redisService.hmset(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY, identityCardMap);
                        log.info("成功缓存{}条身份证映射信息", identityCardMap.size());
                    }

                    log.info("在职人员缓存初始化完成，共{}条记录", activePersonsWithIds.size());
                }

                log.info("在职人员缓存初始化完成，共{}条记录", activePersonsWithIds.size());

            } catch (Exception e) {
                log.error("初始化在职人员缓存失败", e);
            }finally {
                CorpUtils.setCurrentCorpCode(null, null);
            }
        }

    }

    /**
     * 构建缓存的人员信息（只包含指定字段）
     */
    private Map<String, Object> buildPersonCacheInfo(SwmPerson person) {
        Map<String, Object> personInfo = new HashMap<>();
        SwmPerson swmPerson = new SwmPerson();
        swmPerson.setIdentityCard(person.getIdentityCard());
        List<SwmPerson> list = swmPersonDao.findList(swmPerson);
        if (list == null) {
            return null;
        }
        person = list.get(0);
        // 只缓存需要的字段
        personInfo.put("id", person.getId()); // 人员ID
        personInfo.put("name", person.getName()); // 姓名
        personInfo.put("company", person.getCompany()); // 所属单位
        personInfo.put("department", person.getDepartment()); // 所属车间
        personInfo.put("prodLine", person.getProdLine()); // 产线
        personInfo.put("team", person.getTeam()); // 所属班组
        personInfo.put("jobType", person.getJobType()); // 工种
        personInfo.put("identityCard", person.getIdentityCard()); // 身份证号码
        personInfo.put("gender", person.getGender()); // 性别
        personInfo.put("phoneNumber", person.getPhoneNumber()); // 手机号

        return personInfo;
    }

    /**
     * 构建缓存的人员信息（包含各表ID）
     * 
     * @param personData 包含各表ID的人员数据
     * @return 缓存用的人员信息Map
     * @author Shawn
     * @date 2025/06/23
     */
    private Map<String, Object> buildPersonCacheInfoWithIds(Map<String, Object> personData) {
        Map<String, Object> personInfo = new HashMap<>();

        // 基本字段
        personInfo.put("id", personData.get("id")); // 人员ID
        personInfo.put("name", personData.get("name")); // 姓名
        personInfo.put("company", personData.get("company")); // 所属单位
        personInfo.put("department", personData.get("department")); // 所属车间
        personInfo.put("prodLine", personData.get("prodLine")); // 产线
        personInfo.put("team", personData.get("team")); // 所属班组
        personInfo.put("jobType", personData.get("jobType")); // 工种
        personInfo.put("identityCard", personData.get("identityCard")); // 身份证号码
        if (personData.get("personType") != null) {
            String personType = personData.get("personType").toString();
            String dictLabel = DictUtils.getDictLabel("person_type_enum", personType, "未知类型");
            personInfo.put("personType", dictLabel);
        }


        // 关联表的ID字段 - 2025/06/23 Shawn 添加
        personInfo.put("workerArchiveId", personData.get("workerArchiveId")); // 工人档案ID (fms_worker.id)
        personInfo.put("officeCode", personData.get("officeCode")); // 组织编码 (js_sys_office.office_code)
        personInfo.put("positionArchiveId", personData.get("positionArchiveId")); // 车间ID (fms_position_archive.id)
        personInfo.put("workGroupId", personData.get("workGroupId")); // 班组ID (fms_work_group.id)
        personInfo.put("prodLineId", personData.get("prodLineId")); // 产线ID (fms_prod_line.id)
        personInfo.put("gender", personData.get("gender")); // 性别
        personInfo.put("phoneNumber", personData.get("phoneNumber")); // 手机号

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
            String personId = (String) redisService.hget(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY, identityCard);
            if (personId == null) {
                return null;
            }

            // 根据人员ID获取人员信息
            Map<String, Object> personInfo = (Map<String, Object>) redisService.hget(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY, personId);
            if (personInfo != null) {
            }

            return personInfo;
        } catch (Exception e) {
            log.error("从缓存中查询身份证{}对应的人员信息失败", identityCard, e);
            return null;
        }
    }

    /**
     * 批量根据身份证获取活跃人员信息
     *
     * @param identityCards 身份证集合
     * @return Map<身份证, 人员信息>
     */
    public Map<String, Map<String, Object>>  getActivePersonByIdentityCardBatch(List<String> identityCards) {
        if (identityCards == null || identityCards.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        try {
            // 先从身份证映射中批量获取人员ID
            Map<Object, Object> personIdMap = redisService.hmget(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY);

            for (String idCard : identityCards) {
                if (idCard == null || idCard.trim().isEmpty()) {
                    continue;
                }

                Object personIdObj = personIdMap.get(idCard);
                if (personIdObj == null) {
                    continue;
                }
                String personId = String.valueOf(personIdObj);

                // 根据人员ID获取人员信息
                Map<String, Object> personInfo = (Map<String, Object>) redisService.hget(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY, personId);
                if (personInfo != null) {
                    resultMap.put(idCard, personInfo);
                }
            }
        } catch (Exception e) {
            log.error("从缓存中批量查询身份证对应的人员信息失败", e);
        }

        return resultMap;
    }


    /**
     * 根据人员ID获取缓存的人员信息
     */
    public Map<String, Object> getActivePersonById(String personId) {
        if (personId == null || personId.trim().isEmpty()) {
            return null;
        }

        try {
            return (Map<String, Object>) redisService.hget(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY, personId);
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
            return redisService.hmget(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY);
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
                int random = new Random().nextInt(1_000_000);
                Map<String, Object> activePersonsWithIds = swmPersonDao.findActivePersonsWithIds(random, person.getIdentityCard()).get(0);
                Map<String, Object> personInfo = buildPersonCacheInfoWithIds(activePersonsWithIds);
                redisService.hset(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY, person.getId(), personInfo);

                // 更新身份证映射
                if (person.getIdentityCard() != null && !person.getIdentityCard().trim().isEmpty()) {
                    redisService.hset(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY, person.getIdentityCard(), person.getId());
                }



                log.debug("更新在职人员缓存：{}", person.getName());

            } else {
                // 非在职状态，从缓存中移除
                redisService.hdel(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY, person.getId());

                // 移除身份证映射
                if (person.getIdentityCard() != null && !person.getIdentityCard().trim().isEmpty()) {
                    redisService.hdel(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY, person.getIdentityCard());
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
            redisService.hdel(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY, person.getId());

            if (person.getIdentityCard() != null && !person.getIdentityCard().trim().isEmpty()) {
                redisService.hdel(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY, person.getIdentityCard());
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
            redisService.del(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY);
            redisService.del(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY);
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
            Map<Object, Object> personCache = redisService.hmget(SwmRedisConstant.RedisGlobalKey.ACTIVE_PERSON_CACHE_KEY);
            Map<Object, Object> identityCardCache = redisService.hmget(SwmRedisConstant.RedisGlobalKey.IDENTITY_CARD_MAP_KEY);

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