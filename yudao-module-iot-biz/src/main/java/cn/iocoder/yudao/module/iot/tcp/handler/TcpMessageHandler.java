package cn.iocoder.yudao.module.iot.tcp.handler;

import cn.iocoder.yudao.module.iot.service.RawMessageTdEngineService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.parser.TcpMessageParser;
import cn.iocoder.yudao.module.iot.tcp.processor.AlarmZeroTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.FallTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.HazardSourceTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.HelmetOffTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.LongTimeStaticTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.ParameterResponseTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.SosTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.service.HelmetDeviceRegistrationService;
import cn.iocoder.yudao.module.iot.tcp.session.DeviceSession;
import cn.iocoder.yudao.module.iot.tcp.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class TcpMessageHandler extends SimpleChannelInboundHandler<String> {

    private SessionManager sessionManager;

    @Resource
    private RawMessageTdEngineService rawMessageTdEngineService;
    @Resource
    private TcpMessageParser tcpMessageParser;
    @Resource
    private AlarmZeroTcpProcessor alarmZeroTcpProcessor;
    @Resource
    private SosTcpProcessor sosTcpProcessor;
    @Resource
    private FallTcpProcessor fallTcpProcessor;
    @Resource
    private HelmetOffTcpProcessor helmetOffTcpProcessor;
    @Resource
    private LongTimeStaticTcpProcessor longTimeStaticTcpProcessor;
    @Resource
    private HazardSourceTcpProcessor hazardSourceTcpProcessor;
    @Resource
    private ParameterResponseTcpProcessor parameterResponseTcpProcessor;
    @Resource
    private HelmetDeviceRegistrationService helmetDeviceRegistrationService;
    @Resource(name = "tcpSaveExecutor")
    private Executor tcpSaveExecutor;

    private final AtomicLong saveSuccessCount = new AtomicLong();
    private final AtomicLong saveFailureCount = new AtomicLong();

    public TcpMessageHandler() {
    }

    public TcpMessageHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        DeviceSession session = new DeviceSession();
        session.setChannel(ctx.channel());
        session.setConnectTime(LocalDateTime.now());
        session.setLastActiveTime(LocalDateTime.now());
        session.setClientAddress(String.valueOf(ctx.channel().remoteAddress()));
        sessionManager.addSession(ctx.channel().id().asShortText(), session);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionManager.removeSession(ctx.channel().id().asShortText());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) {
        String sessionId = ctx.channel().id().asShortText();
        DeviceSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            session.setLastActiveTime(LocalDateTime.now());
            session.incrementMessageCount();
            session.setLastMessage(message);
        }

        processMessage(ctx, message, session);
        saveRawMessage(sessionId, message, session);
    }

    private void processMessage(ChannelHandlerContext ctx, String message, DeviceSession session) {
        if (message == null || message.isBlank()) {
            return;
        }
        if (parameterResponseTcpProcessor.canProcess(message)) {
            parameterResponseTcpProcessor.process(message);
            return;
        }
        TcpMessageData tcpData = tcpMessageParser.parseMessage(message);
        if (tcpData == null) {
            return;
        }
        if (session != null) {
            session.setDeviceId(tcpData.getDeviceId());
            sessionManager.updateDeviceMapping(session.getSessionId(), tcpData.getDeviceId());
            helmetDeviceRegistrationService.registerDeviceIfNeeded(tcpData, session);
        }
        if (sosTcpProcessor.canProcess(tcpData)) {
            sosTcpProcessor.process(tcpData, ctx);
        }
        if (fallTcpProcessor.canProcess(tcpData)) {
            fallTcpProcessor.process(tcpData, ctx);
        }
        if (helmetOffTcpProcessor.canProcess(tcpData)) {
            helmetOffTcpProcessor.process(tcpData, ctx);
        }
        if (longTimeStaticTcpProcessor.canProcess(tcpData)) {
            longTimeStaticTcpProcessor.process(tcpData, ctx);
        }
        if (hazardSourceTcpProcessor.canProcess(tcpData)) {
            hazardSourceTcpProcessor.process(tcpData, ctx);
        }
        if (alarmZeroTcpProcessor.canProcess(tcpData)) {
            alarmZeroTcpProcessor.process(tcpData);
        }
    }

    private void saveRawMessage(String sessionId, String message, DeviceSession session) {
        Runnable task = () -> {
            try {
                String deviceId = session != null && session.getDeviceId() != null
                        ? session.getDeviceId() : extractDeviceIdFromMessage(message);
                rawMessageTdEngineService.saveRawMessageData(deviceId, sessionId, message);
                saveSuccessCount.incrementAndGet();
            } catch (Exception ex) {
                saveFailureCount.incrementAndGet();
                log.error("Save TCP raw message failed, sessionId={}", sessionId, ex);
            }
        };
        if (tcpSaveExecutor != null) {
            tcpSaveExecutor.execute(task);
        } else {
            task.run();
        }
    }

    private String extractDeviceIdFromMessage(String message) {
        if (message == null || !message.startsWith("$") || !message.endsWith("#")) {
            return "unknown_device";
        }
        String[] parts = message.substring(1, message.length() - 1).split(",");
        return parts.length >= 3 ? parts[2] : "unknown_device";
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event && event.state() == IdleState.READER_IDLE) {
            ctx.close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("TCP connection exception", cause);
        ctx.close();
    }

    public long getSaveSuccessCount() {
        return saveSuccessCount.get();
    }

    public long getSaveFailureCount() {
        return saveFailureCount.get();
    }
}
