package cn.iocoder.yudao.module.iot.tcp.server;

import cn.iocoder.yudao.module.iot.config.TcpServerProperties;
import cn.iocoder.yudao.module.iot.tcp.handler.TcpChannelInitializer;
import cn.iocoder.yudao.module.iot.tcp.session.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Netty TCP服务器
 */
@Component
public class NettyTcpServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyTcpServer.class);

    @Resource
    private TcpServerProperties tcpServerProperties;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private cn.iocoder.yudao.module.iot.tcp.util.TcpCommandLogAsyncRecorder commandLogRecorder;

    private final SessionManager sessionManager;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private boolean isRunning = false;

    // 静态实例，供其他服务使用
    private static NettyTcpServer instance;

    public NettyTcpServer() {
        this.sessionManager = new SessionManager();
        instance = this;
        logger.info("NettyTcpServer 创建 SessionManager 实例");
    }

    // 静态方法获取实例
    public static NettyTcpServer getInstance() {
        return instance;
    }

    @PostConstruct
    public void init() {
        // 设置TCP指令日志记录器到SessionManager
        sessionManager.setCommandLogRecorder(commandLogRecorder);

        if (tcpServerProperties.isEnabled()) {
            startServer();
        } else {
            logger.info("TCP服务器已禁用，跳过启动");
        }
    }

    /**
     * 启动TCP服务器
     */
    public void startServer() {
        if (isRunning) {
            logger.warn("TCP服务器已经在运行中");
            return;
        }

        try {
            bossGroup = new NioEventLoopGroup(tcpServerProperties.getBossThread());
            workerGroup = new NioEventLoopGroup(tcpServerProperties.getWorkerThread());

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, tcpServerProperties.getSoBacklog())
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, tcpServerProperties.isSoKeepAlive())
                    .childOption(ChannelOption.TCP_NODELAY, tcpServerProperties.isTcpNoDelay())
                    .childOption(ChannelOption.SO_RCVBUF, tcpServerProperties.getReceiveBufferSize())
                    .childOption(ChannelOption.SO_SNDBUF, tcpServerProperties.getSendBufferSize())
                    .childHandler(new TcpChannelInitializer(sessionManager, tcpServerProperties, applicationContext));

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(tcpServerProperties.getPort()).sync();

            if (future.isSuccess()) {
                serverChannel = future.channel();
                isRunning = true;
                logger.info("TCP服务器启动成功，端口：{}", tcpServerProperties.getPort());

                // 添加关闭监听器
                serverChannel.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
                    logger.info("TCP服务器已关闭");
                    isRunning = false;
                });

            } else {
                logger.error("TCP服务器启动失败：{}", future.cause().getMessage());
                shutdown();
            }

        } catch (Exception e) {
            logger.error("TCP服务器启动异常", e);
            shutdown();
        }
    }

    /**
     * 停止TCP服务器
     */
    @PreDestroy
    public void shutdown() {
        if (!isRunning) {
            return;
        }

        logger.info("正在关闭TCP服务器...");

        try {
            // 关闭服务器通道
            if (serverChannel != null) {
                serverChannel.close().sync();
            }

            // 清理会话
            if (sessionManager != null) {
                sessionManager.closeAllSessions();
            }

        } catch (InterruptedException e) {
            logger.error("关闭TCP服务器时发生异常", e);
            Thread.currentThread().interrupt();
        } finally {
            // 优雅关闭事件循环组
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            isRunning = false;
            logger.info("TCP服务器已关闭");
        }
    }

    /**
     * 重启TCP服务器
     */
    public void restart() {
        logger.info("重启TCP服务器...");
        shutdown();
        try {
            Thread.sleep(1000); // 等待1秒确保资源释放
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        startServer();
    }

    /**
     * 获取服务器运行状态
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return sessionManager != null ? sessionManager.getSessionCount() : 0;
    }

    /**
     * 获取SessionManager实例
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
