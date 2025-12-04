package com.jeesite.modules.config;

import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.UserHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@Aspect
@Component
public class SwitchCorpAspect {

    @Around("@annotation(switchCorp)")
    public Object around(ProceedingJoinPoint joinPoint, SwitchCorp switchCorp) throws Throwable {
        // 获取请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 保存原始线程租户
        String originalCorpCode = CorpUtils.getCurrentCorpCode();
        String originalCorpName = CorpUtils.getCurrentCorpName();

        try {
            if (switchCorp.enable()) {
                String corpCode = request.getParameter("corpCode");
                if (corpCode != null && !corpCode.isEmpty()) {
                    String corpName = getCorpNameByCode(corpCode);
                    CorpUtils.setCurrentCorpCode(corpCode, corpName);
                    log.debug("动态切换租户：corpCode={}, corpName={}", corpCode, corpName);
                }
            }
            return joinPoint.proceed();
        } finally {
            CorpUtils.setCurrentCorpCode(originalCorpCode, originalCorpName);
        }
    }

    private String getCorpNameByCode(String corpCode) {
        Object cache = CorpUtils.getCache(corpCode);
        return cache != null ? cache.toString() : corpCode;
    }
}


