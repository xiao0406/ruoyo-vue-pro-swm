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
        TenantContext.clear();
        CorpUtils.removeCurrentCorpCode(null);

        // 3. Session 租户（switch 接口写入的）
        Session session = UserUtils.getSession();
        if (session != null) {
            String corpCode = (String) session.getAttribute(SESSION_CORP_CODE);
            String corpName = (String) session.getAttribute(SESSION_CORP_NAME);

            if (StringUtils.isNotBlank(corpCode)) {
                TenantContext.set(corpCode);
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                log.debug("使用 Session 租户: {}", corpCode);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 4. 请求结束必须清理 ThreadLocal
        TenantContext.clear();
        CorpUtils.removeCurrentCorpCode(null);
    }
}
