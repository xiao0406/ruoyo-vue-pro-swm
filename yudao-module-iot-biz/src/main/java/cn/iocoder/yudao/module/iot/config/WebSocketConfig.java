package cn.iocoder.yudao.module.iot.config;

import cn.iocoder.yudao.module.iot.websocket.handler.WebSocketMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket配置类
 *
 * @author Shawn
 * @date 2023-11-01
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，并设置允许跨域访问
        registry.addHandler(webSocketMessageHandler(), "/iot/websocket/message")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor()); // 添加HTTP会话拦截器
    }

    @Bean
    public WebSocketMessageHandler webSocketMessageHandler() {
        return new WebSocketMessageHandler();
    }

    /**
     * 注入ServerEndpointExporter，这个bean会自动注册使用了@ServerEndpoint注解的WebSocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 配置WebSocket容器，设置消息大小限制
     * 解决base64等大消息传输问题
     *
     * @author Shawn
     * @date 2025-06-10
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        // 设置文本消息缓冲区大小为50MB
        container.setMaxTextMessageBufferSize(50 * 1024 * 1024);

        // 设置二进制消息缓冲区大小为50MB
        container.setMaxBinaryMessageBufferSize(50 * 1024 * 1024);

        logger.info("WebSocket容器配置完成 - 消息缓冲区大小: 50MB");

        return container;
    }

    /**
     * 自定义Tomcat配置，设置连接超时参数
     *
     * @author Shawn
     * @date 2024-12-19
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers((Connector connector) -> {
                // 设置连接超时时间为10分钟（600秒）
                connector.setProperty("connectionTimeout", "600000");
                // 设置Keep-Alive超时时间为1小时
                connector.setProperty("keepAliveTimeout", "3600000");
                // 设置最大100000
                connector.setProperty("maxKeepAliveRequests", "100000");
                // 设置socket超时时间,1小时
                connector.setProperty("soTimeout", "3600000");
                // 禁用连接超时（设置为-1表示无限制）
                connector.setProperty("disableUploadTimeout", "false");
                // 设置异步请求超时时间为1小时
                connector.setAsyncTimeout(3600000L);

                logger.info("Tomcat连接器配置完成 - connectionTimeout: 600秒, keepAliveTimeout: 3600秒, asyncTimeout: 3600秒");
            });
        };
    }
}
