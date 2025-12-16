package com.jeesite.modules.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.UserHelper;
import com.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class CorpContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 优先使用前端传参
//        String paramCorpCode = request.getParameter("corpCode");
        String paramCorpCode = request.getHeader("corpCode"); // 前端自定义 header

        // 默认用登录用户
        User user = UserUtils.getUser();
        String corpCode = (user != null) ? user.getCorpCode() : null;
        String corpName = (user != null) ? user.getCorpName() : null;

        if (StringUtils.isNotBlank(paramCorpCode)) {
            corpCode = paramCorpCode;
            corpName = paramCorpCode; // 可以用 corpCode 填充名字，也可以前端传名字
        }

        if (StringUtils.isNotBlank(corpCode)) {
            CorpUtils.setCurrentCorpCode(corpCode, corpName);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 清理 ThreadLocal
        CorpUtils.removeCurrentCorpCode(null);
    }
}