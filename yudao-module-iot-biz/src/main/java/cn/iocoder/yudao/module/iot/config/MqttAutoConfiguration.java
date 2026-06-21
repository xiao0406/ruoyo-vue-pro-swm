package cn.iocoder.yudao.module.iot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT自动配置类
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Configuration
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MqttAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MqttAutoConfiguration.class);

    /**
     * 创建MQTT消息处理线程池配置Bean
     *
     * @param properties MQTT配置属性
     * @return 线程池配置
     */
    @Bean("mqttMessageExecutor")
    public org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor mqttMessageExecutor(
            MqttClientProperties properties) {

        MqttClientProperties.ThreadPool config = properties.getThreadPool();

        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor =
            new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(config.getCorePoolSize());

        // 最大线程数
        executor.setMaxPoolSize(config.getMaxPoolSize());

        // 队列容量
        executor.setQueueCapacity(config.getQueueCapacity());

        // 线程名前缀
        executor.setThreadNamePrefix(config.getThreadNamePrefix());

        // 线程空闲时间
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(config.isAllowCoreThreadTimeout());

        // 拒绝策略：当队列满时在调用线程中执行
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(30);

        // 初始化线程池
        executor.initialize();

        logger.info("MQTT消息处理线程池配置完成: core={}, max={}, queue={}, prefix={}",
            config.getCorePoolSize(), config.getMaxPoolSize(),
            config.getQueueCapacity(), config.getThreadNamePrefix());

        return executor;
    }
}
