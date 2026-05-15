package com.jeesite.modules.config;

import com.jeesite.modules.sys.utils.CorpUtils;
import org.springframework.core.task.TaskDecorator;

/**
 * 线程上下文复制
 */
public class CorpContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 1. 从提交任务的线程中获取租户；TenantContext 优先，CorpUtils 兜底
        String tenantCorpCode = TenantContext.get();
        String taskCorpCode = tenantCorpCode != null && !tenantCorpCode.isEmpty() ? tenantCorpCode : CorpUtils.getCurrentCorpCode();
        String taskCorpName = CorpUtils.getCurrentCorpName();

        return () -> {
            // 2. 先保存异步线程原有上下文，避免任务执行完成后把外层上下文清空
            String oldTenantCorpCode = TenantContext.get();
            String oldCorpCode = CorpUtils.getCurrentCorpCode();
            String oldCorpName = CorpUtils.getCurrentCorpName();
            try {
                // 3. 执行任务前先清理线程池复用带来的旧上下文，再设置提交任务时的租户
                TenantContext.clear();
                CorpUtils.removeCurrentCorpCode(null);
                if (taskCorpCode != null && !taskCorpCode.isEmpty()) {
                    TenantContext.set(taskCorpCode);
                    CorpUtils.setCurrentCorpCode(taskCorpCode, taskCorpName);
                }
                runnable.run();
            } finally {
                // 4. 任务结束后先清理当前任务租户，再恢复进入任务前的线程上下文
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
                if (oldTenantCorpCode != null && !oldTenantCorpCode.isEmpty()) {
                    TenantContext.set(oldTenantCorpCode);
                }
                if (oldCorpCode != null && !oldCorpCode.isEmpty()) {
                    CorpUtils.setCurrentCorpCode(oldCorpCode, oldCorpName);
                }
            }
        };
    }
}


