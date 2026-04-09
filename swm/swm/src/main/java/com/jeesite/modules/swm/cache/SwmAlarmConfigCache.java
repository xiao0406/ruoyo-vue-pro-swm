package com.jeesite.modules.swm.cache;

import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import com.jeesite.modules.swm.service.SwmAlarmConfigService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * 是否报警缓存服务
 */
@Service
@Slf4j
public class SwmAlarmConfigCache implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;

    @Lazy
    @Autowired
    private SwmAlarmConfigService alarmConfigService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initAreaCache();
    }

    public void initAreaCache() {

        log.info("开始初始化设备缓存（全局）...");

        //清理缓存
        redisService.del(SwmRedisConstant.RedisSwmKey.ALARM_CONFIG);

        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            log.warn("没有租户信息");
            return;
        }

        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();

            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            TenantContext.set(corpCode);

            log.info("处理租户：{}", corpCode);

            try {
                //查询当前租户下的语音模板列表
                SwmAlarmConfig alarmConfig = new SwmAlarmConfig();
                alarmConfig.setRandom(new Random().nextInt(1_000_000));
                List<SwmAlarmConfig> list = alarmConfigService.findList(alarmConfig);
                if (CollectionUtils.isEmpty(list)){
                    continue;
                }
                for (SwmAlarmConfig config : list) {
                    redisService.hset(corpCode +  SwmRedisConstant.RedisSwmKey.ALARM_CONFIG, config.getAlarmKey(), config.getEnableAlarm());
                }

            } catch (Exception e) {
                log.error("租户处理失败: {}", corpCode, e);
            } finally {
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }
        log.info("设备缓存初始化完成");
    }

    /**
     * 修改方法调用逻辑
     */
    public void update(SwmAlarmConfig alarmConfig) {
        String corpCode = CorpUtils.getCurrentCorpCode();
        redisService.hset(corpCode +  SwmRedisConstant.RedisSwmKey.ALARM_CONFIG, alarmConfig.getAlarmKey(), alarmConfig.getEnableAlarm());
    }

    /**
     * 删除方法调用逻辑
     */
    public void delete(SwmAlarmConfig alarmConfig) {
        String corpCode = CorpUtils.getCurrentCorpCode();
        redisService.hdel(corpCode +  SwmRedisConstant.RedisSwmKey.ALARM_CONFIG, alarmConfig.getAlarmKey());
    }
}
