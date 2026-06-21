package cn.iocoder.yudao.module.iot.tcp.handler;

import cn.iocoder.yudao.module.iot.config.TcpServerProperties;
import cn.iocoder.yudao.module.iot.tcp.session.SessionManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * TCP通道初始化器
 */
public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final SessionManager sessionManager;
    private final TcpServerProperties tcpServerProperties;
    private final ApplicationContext applicationContext;

    public TcpChannelInitializer(SessionManager sessionManager,
            TcpServerProperties tcpServerProperties,
            ApplicationContext applicationContext) {
        this.sessionManager = sessionManager;
        this.tcpServerProperties = tcpServerProperties;
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 心跳检测处理器
        pipeline.addLast("idleStateHandler", new IdleStateHandler(
                tcpServerProperties.getHeartbeatInterval() / 1000, 0, 0, TimeUnit.SECONDS));

        // 智能帧解码器：支持多种消息格式
        // 1. 优先检查是否有换行符分隔的消息
        // 2. 如果没有换行符，则在连接空闲时处理缓冲区中的数据
        pipeline.addLast("frameDecoder", new SmartFrameDecoder(
                tcpServerProperties.getMaxFrameLength()));

        // 字符串解码器
        pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));

        // 字符串编码器
        pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));

        // 为每个连接创建新的TCP消息处理器实例
        TcpMessageHandler handler = new TcpMessageHandler(sessionManager);
        // 手动注入Spring依赖
        applicationContext.getAutowireCapableBeanFactory().autowireBean(handler);

        // TCP消息处理器
        pipeline.addLast("tcpMessageHandler", handler);
    }
}
