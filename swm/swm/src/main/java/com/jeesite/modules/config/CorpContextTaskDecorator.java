package com.jeesite.modules.config;

import com.jeesite.modules.sys.utils.CorpUtils;
import org.springframework.core.task.TaskDecorator;

/**
 * 线程上下文复制
 */
public class CorpContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 1. 从提交任务的线程中获取租户
        String corpCode = CorpUtils.getCurrentCorpCode();
        String corpName = CorpUtils.getCurrentCorpName();

        return () -> {
            try {
                // 2. 设置到异步线程
                if (corpCode != null && !corpCode.isEmpty()) {
                    CorpUtils.setCurrentCorpCode(corpCode, corpName);
                    TenantContext.set(corpCode);
                }
                runnable.run();
            } finally {
                // 3. 清理线程上下文
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        };
    }
}


