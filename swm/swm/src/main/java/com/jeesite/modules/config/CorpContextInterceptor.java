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

/**
 * 所有的HTTP都走这里，拦截器设置租户信息
 */
@Component
@Slf4j
public class CorpContextInterceptor implements HandlerInterceptor {

    private static final String SESSION_CORP_CODE = "corpCode";
    private static final String SESSION_CORP_NAME = "corpName";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 1. 先清理，防止线程复用带来的脏数据（非常关键）
        TenantContext.clear();
        CorpUtils.removeCurrentCorpCode(null);

        // 2. 账号自身租户只作为兜底；如果 Session 中已有切换租户，则优先使用 Session 租户
        User currentUser = getCurrentUserSafely();
        String userCorpCode = currentUser != null ? currentUser.getCorpCode() : null;
        String userCorpName = currentUser != null ? currentUser.getCorpName() : null;

        // 3. Session 租户（switch 接口写入的）。允许账号归属租户和当前切换租户不一致。
        Session session = UserUtils.getSession();
        String corpCode = null;
        String corpName = null;
        if (session != null) {
            corpCode = (String) session.getAttribute(SESSION_CORP_CODE);
            corpName = (String) session.getAttribute(SESSION_CORP_NAME);
        }

        // 没有切换租户时，使用账号自身租户兜底，并同步回 Session，避免后续请求为空
        if (StringUtils.isBlank(corpCode) && StringUtils.isNotBlank(userCorpCode)) {
            corpCode = userCorpCode;
            corpName = userCorpName;
            if (session != null) {
                session.setAttribute(SESSION_CORP_CODE, corpCode);
                session.setAttribute(SESSION_CORP_NAME, corpName);
            }
            log.debug("使用账号租户兜底: {}", corpCode);
        }

        if (StringUtils.isNotBlank(corpCode)) {
            TenantContext.set(corpCode);
            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            log.debug("当前请求租户: {}", corpCode);
        }

        return true;
    }

    private User getCurrentUserSafely() {
        try {
            return UserHelper.getUser();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 4. 请求结束必须清理 ThreadLocal
        TenantContext.clear();
        CorpUtils.removeCurrentCorpCode(null);
    }
}
