package com.jeesite.modules.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.modules.sys.entity.Role;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.entity.UserRole;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.RoleUtils;
import com.jeesite.modules.sys.utils.UserHelper;
import com.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 所有的HTTP都走这里，拦截器设置租户信息
 */
@Component
@Slf4j
public class CorpContextInterceptor implements HandlerInterceptor {

    /**
     * Session中当前切换租户
     */
    private static final String SESSION_CORP_CODE = "corpCode";
    private static final String SESSION_CORP_NAME = "corpName";

    /**
     * 允许切换租户的角色编码
     */
    private static final String SWITCH_ROLE_CODE = "AQM_ADMIN";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 1. 先清理，防止线程复用带来的脏数据（非常关键）
        TenantContext.clear();
        CorpUtils.removeCurrentCorpCode(null);

        // 2. 账号自身租户只作为兜底；如果 Session 中已有切换租户，则优先使用 Session 租户
        User currentUser = getCurrentUserSafely();

        if (currentUser == null) {
            log.debug("当前请求无登录用户");
            return true;
        }

        // 用户自身所属租户
        String userCorpCode = currentUser.getCorpCode();
        String userCorpName = currentUser.getCorpName();

        // =========================================================
        // 3. 判断是否允许切换租户
        //    只有 AQM_ADMIN 才允许
        // =========================================================
        boolean canSwitchCorp = hasSwitchCorpPermission(currentUser);

        Session session = UserUtils.getSession();

        String corpCode;
        String corpName;

        // =========================================================
        // 4. 超级管理员：允许从Session读取当前切换租户
        // =========================================================
        if (canSwitchCorp) {

            corpCode = null;
            corpName = null;

            if (session != null) {
                corpCode = (String) session.getAttribute(SESSION_CORP_CODE);
                corpName = (String) session.getAttribute(SESSION_CORP_NAME);
            }

            // Session中没有租户，则默认使用自身租户
            if (StringUtils.isBlank(corpCode)) {

                corpCode = userCorpCode;
                corpName = userCorpName;

                if (session != null) {
                    session.setAttribute(SESSION_CORP_CODE, corpCode);
                    session.setAttribute(SESSION_CORP_NAME, corpName);
                }

                log.debug("管理员首次进入，使用默认租户: {}", corpCode);
            }

        } else {

            // =========================================================
            // 5. 普通用户：强制使用自身租户
            //    禁止读取Session租户（防止串租户）
            // =========================================================
            corpCode = userCorpCode;
            corpName = userCorpName;

            // 强制覆盖Session，避免残留脏数据
            if (session != null) {
                session.setAttribute(SESSION_CORP_CODE, corpCode);
                session.setAttribute(SESSION_CORP_NAME, corpName);
            }

            log.debug("普通用户固定租户: {}", corpCode);
        }

        // =========================================================
        // 6. 设置当前请求租户上下文
        // =========================================================
        if (StringUtils.isNotBlank(corpCode)) {

            TenantContext.set(corpCode);

            CorpUtils.setCurrentCorpCode(corpCode, corpName);

            log.info(
                    "当前请求租户: corpCode={}, userCode={}, sessionId={}, canSwitchCorp={}",
                    corpCode,
                    currentUser.getUserCode(),
                    request.getSession().getId(),
                    canSwitchCorp
            );
        }

        return true;
    }

    /**
     * 判断是否拥有租户切换权限
     */
    private boolean hasSwitchCorpPermission(User user) {

        try {

            List<Role> roleList = user.getRoleList();

            if (roleList == null || roleList.isEmpty()) {
                return false;
            }

            return roleList.stream()
                    .anyMatch(role ->
                            SWITCH_ROLE_CODE.equals(role.getRoleCode())
                    );

        } catch (Exception e) {

            log.error("判断租户切换权限失败", e);

            return false;
        }
    }

    /**
     * 安全获取当前用户
     */
    private User getCurrentUserSafely() {

        try {
            return UserUtils.getUser();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        // =========================================================
        // 请求结束必须清理ThreadLocal
        // 防止线程复用导致串租户
        // =========================================================
        TenantContext.clear();

        CorpUtils.removeCurrentCorpCode(null);
    }
}
