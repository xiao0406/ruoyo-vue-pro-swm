package cn.iocoder.yudao.module.iot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TCP服务器自动配置类
 */
@Configuration
@EnableConfigurationProperties(TcpServerProperties.class)
@ConditionalOnProperty(prefix = "iot.tcp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TcpServerAutoConfiguration {

    /**
     * TCP服务器配置Bean
     */
    @Bean
    public TcpServerProperties tcpServerProperties() {
        return new TcpServerProperties();
    }
}
