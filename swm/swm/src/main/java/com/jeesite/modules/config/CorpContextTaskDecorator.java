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
                if (corpCode != null) {
                    CorpUtils.setCurrentCorpCode(corpCode, corpName);
                }
                runnable.run();
            } finally {
                // 3. 必须清理，防止线程复用污染
                CorpUtils.removeCurrentCorpCode( null);
            }
        };
    }
}

