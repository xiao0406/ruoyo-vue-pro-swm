package cn.iocoder.yudao.module.iot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
        // 初始化 ThreadPoolTaskExecutor 对象
        // executor.initialize();
        return executor;
    }

    /**
     * TCP消息业务处理线程池
     * 处理：调用定位引擎、保存TDengine、报警判断等 I/O 密集型业务
     * 1000+ TCP 设备，32核CPU，2:1 线程超配
     *
     * 拒绝策略说明：使用 DiscardPolicy 而非 CallerRunsPolicy。
     * 当线程池满载时直接丢弃新任务，避免阻塞 Netty EventLoop 线程
     * （CallerRunsPolicy 会在 EventLoop 线程上执行业务逻辑，导致
     * 该线程上的所有 TCP 通道无法读取数据，进而触发 IdleStateHandler
     * 断开设备连接）。
     * TCP 设备会自动重发消息，丢几条消息比全部设备掉线好。
     */
    @Bean("tcpMessageExecutor")
    public Executor tcpMessageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(64);
        executor.setMaxPoolSize(128);
        executor.setQueueCapacity(2048);
        executor.setThreadNamePrefix("tcp-biz-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * TCP设备心跳续期专用线程池（轻量级）
     * 与业务处理线程池隔离，即使业务处理堆积，设备在线状态也能正常续期
     */
    @Bean("tcpHeartbeatExecutor")
    public Executor tcpHeartbeatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(4096);
        executor.setThreadNamePrefix("tcp-hb-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }

    /**
     * TCP原始消息入库线程池
     * 专用于保存原始消息到TDengine，与业务处理线程池隔离
     * 确保即使定位引擎响应慢或业务堆积，原始消息也能正常入库
     *
     * 拒绝策略：DiscardPolicy，与 tcpMessageExecutor 保持一致，
     * 避免阻塞 Netty EventLoop 线程。
     */
    @Bean("tcpSaveExecutor")
    public Executor tcpSaveExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(4096);
        executor.setThreadNamePrefix("tcp-save-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean("mqttExecutor")
    @ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor mqttExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("mqtt-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean("mqttProcessorExecutor")
    @ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor mqttProcessorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(12);
        executor.setMaxPoolSize(24);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("mqtt-dispatch-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
