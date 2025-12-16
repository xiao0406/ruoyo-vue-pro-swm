package com.jeesite.modules.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.stereotype.Component;

@Component
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CorpContextInterceptor corpContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(corpContextInterceptor)
                .addPathPatterns("/**"); // 拦截所有请求，可根据需要调整路径
    }
}