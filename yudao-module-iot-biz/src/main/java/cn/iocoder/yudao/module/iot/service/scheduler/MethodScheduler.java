package cn.iocoder.yudao.module.iot.service.scheduler;

import org.springframework.stereotype.Component;

/**
 * 方法调度器
 */
@Component
public class MethodScheduler {

    public MethodScheduler() {
    }

    public MethodScheduler(Integer intervalSeconds, Integer retryCount) {
    }

    public void scheduleMethod(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }
}
