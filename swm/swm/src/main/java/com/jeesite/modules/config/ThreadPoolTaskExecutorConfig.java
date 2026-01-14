package com.jeesite.modules.config;

import com.jeesite.modules.sys.utils.CorpUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lipeng
 * @Description: 服务模块线程池配置
 * @date 2022/5/7 10:25
 */
@Configuration
@EnableAsync
public class ThreadPoolTaskExecutorConfig {

    /**
     * 服务模块线程池配置
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(10);
        // 设置最大线程数
        executor.setMaxPoolSize(40);
        // 设置队列容量
        executor.setQueueCapacity(10);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("qms-");
        // 设置拒绝策略。如果队列满了的拒绝策略。此处使用直接在当前线程调用的策略模式。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //初始化 ThreadPoolTaskExecutor 对象
        // executor.initialize();
        return executor;
    }



    @Bean("swmExecutor")
    public ThreadPoolTaskExecutor swmExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(24);
        // 设置最大线程数
        executor.setMaxPoolSize(47);
        // 设置队列容量
        executor.setQueueCapacity(200);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("swm-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 新增参数：线程上下文复制 ，解决异步任务，多租户失效问题
        executor.setTaskDecorator(new CorpContextTaskDecorator());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 初始化线程池
        executor.initialize();
        return executor;
    }



}
