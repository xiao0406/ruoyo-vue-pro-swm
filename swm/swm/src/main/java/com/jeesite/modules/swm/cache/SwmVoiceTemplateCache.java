package com.jeesite.modules.swm.cache;

import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmVoiceTemplate;
import com.jeesite.modules.swm.service.SwmVoiceTemplateService;
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
 * 语音模板缓存服务
 */

@Service
@Slf4j
public class SwmVoiceTemplateCache implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private UserService userService;
    @Autowired
    @Lazy
    private SwmVoiceTemplateService voiceTemplateService;
    @Autowired
    private RedisService redisService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initAreaCache();
    }

    public void initAreaCache() {

        log.info("开始初始化设备缓存（全局）...");

        //清理缓存
        redisService.del(SwmRedisConstant.RedisSwmKey.VOICE_TEMPLATE_CACHE);

        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            log.warn("没有租户信息");
            return;
        }

        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();

//            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            TenantContext.set(corpCode);

            log.info("处理租户：{}", corpCode);

            try {
                //查询当前租户下的语音模板列表
                SwmVoiceTemplate template = new SwmVoiceTemplate();
                template.setRandom(new Random().nextInt(1_000_000));
                List<SwmVoiceTemplate> list = voiceTemplateService.findList(template);
                if (CollectionUtils.isEmpty(list)){
                    continue;
                }
                for (SwmVoiceTemplate voiceTemplate : list) {
                    if (voiceTemplate.getTemplateId() == null){
                        continue;
                    }
                    redisService.hset(corpCode +  SwmRedisConstant.RedisSwmKey.VOICE_TEMPLATE_CACHE, voiceTemplate.getTemplateId(), voiceTemplate.getTemplateName());
                }

            } catch (Exception e) {
                log.error("租户处理失败: {}", corpCode, e);
            } finally {
//                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }
        log.info("设备缓存初始化完成");
    }



    /**
     * 删除方法调用逻辑
     */
    public void delete(SwmVoiceTemplate voiceTemplate) {
        if (voiceTemplate.getTemplateId() == null){
            return;
        }
        redisService.hdel(voiceTemplate.getCorpCode() +  SwmRedisConstant.RedisSwmKey.VOICE_TEMPLATE_CACHE, voiceTemplate.getTemplateId());
    }
}
