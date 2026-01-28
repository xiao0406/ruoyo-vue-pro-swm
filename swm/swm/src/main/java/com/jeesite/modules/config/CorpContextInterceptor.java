package com.jeesite.modules.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.UserHelper;
import com.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;


/**
 * 所有的HTTP都走这里，拦截器设置租户信息
 */
@Component
@Slf4j
public class CorpContextInterceptor implements HandlerInterceptor {

    private static final String HEADER_CORP_CODE = "corpCode";
    private static final String SESSION_CORP_CODE = "corpCode";
    private static final String SESSION_CORP_NAME = "corpName";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 1. 先清理，防止线程复用带来的脏数据（非常关键）
        CorpUtils.setCurrentCorpCode(null,null);

        // 2. Header 优先（接口级切换）
        String headerCorpCode = request.getHeader(HEADER_CORP_CODE);
        if (StringUtils.isNotBlank(headerCorpCode)) {
            CorpUtils.setCurrentCorpCode(headerCorpCode, headerCorpCode);
            log.debug("使用 Header 租户: {}", headerCorpCode);
            return true;
        }

        // 3. Session 租户（switch 接口写入的）
        Session session = UserUtils.getSession();
        if (session != null) {
            String sessionCorpCode = (String) session.getAttribute(SESSION_CORP_CODE);
            String sessionCorpName = (String) session.getAttribute(SESSION_CORP_NAME);

            if (StringUtils.isNotBlank(sessionCorpCode)) {
                CorpUtils.setCurrentCorpCode(sessionCorpCode, sessionCorpName);
                log.debug("使用 Session 租户: {}", sessionCorpCode);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 4. 请求结束必须清理 ThreadLocal
        CorpUtils.setCurrentCorpCode(null,null);
    }
}
