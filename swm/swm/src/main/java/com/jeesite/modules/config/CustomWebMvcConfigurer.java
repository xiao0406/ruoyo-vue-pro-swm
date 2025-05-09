package com.jeesite.modules.config;

import com.jeesite.autoconfigure.sys.NoRepeatSubmitInterceptor;
import com.jeesite.modules.cache.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @Author：cuihu
 * @Package：com.jeesite.modules.config
 * @Project：cscec-jessite-cloud-qms1
 * @name：CustomWebMvcConfigurer
 * @Date：2024/12/9 18:38
 */
@Component
public class CustomWebMvcConfigurer extends WebMvcConfigurerAdapter {

    @Autowired
    private RedisService redisService;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NoRepeatSubmitInterceptor(redisService));
    }
}