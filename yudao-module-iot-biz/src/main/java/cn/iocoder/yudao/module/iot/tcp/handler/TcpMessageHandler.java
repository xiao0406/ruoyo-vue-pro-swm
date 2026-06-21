package cn.iocoder.yudao.module.iot.tcp.handler;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.iocoder.yudao.module.swm.service.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.swm.api.constant.TdengineSuperTableConstants;
import cn.iocoder.yudao.module.swm.api.enums.AlarmConfigEnum;
import cn.iocoder.yudao.module.swm.service.RedisService;
import cn.iocoder.yudao.module.swm.api.enums.MqttSendPositionParamEnum;
import cn.iocoder.yudao.module.iot.mqtt.service.MqttPublishService;
import cn.iocoder.yudao.module.swm.service.RawMessageTdEngineService;
import cn.iocoder.yudao.module.swm.service.TDengineService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.parser.TcpMessageParser;
import cn.iocoder.yudao.module.iot.tcp.processor.AlarmZeroTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.SosTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.FallTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.HelmetOffTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.LongTimeStaticTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.HazardSourceTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.processor.ParameterResponseTcpProcessor;
import cn.iocoder.yudao.module.iot.tcp.session.DeviceSession;
import cn.iocoder.yudao.module.iot.tcp.session.SessionManager;
import cn.iocoder.yudao.module.iot.tcp.service.HelmetDeviceRegistrationService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo.SwmAlarmConfigDetailRespVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TCP消息处理器
 */
public class TcpMessageHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(TcpMessageHandler.class);

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

    @Qualifier("tcpMessageExecutor")
    @Resource
    private Executor tcpMessageExecutor;

    @Qualifier("tcpHeartbeatExecutor")
    @Resource
    private Executor tcpHeartbeatExecutor;

    @Qualifier("tcpSaveExecutor")
    @Resource
    private Executor tcpSaveExecutor;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private RedisService redisService;
    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;
    @Value("${tdengine.dbname}")
    private String dbname;
    @Resource
    private TDengineService tdengineService;

    // 低电量语音模板
    private static final String Low_Battery_VoiceTemplate_ID = "1936227999566286848";

    // 统计计数器
    private final AtomicLong saveSuccessCount = new AtomicLong(0);
    private final AtomicLong saveFailureCount = new AtomicLong(0);

    // MQTT 是附加能力，未启用时不应影响 TCP 连接建立
    @Resource
    private MqttPublishService publishService;

    public TcpMessageHandler() {
        // Spring Bean 默认构造函数
    }

    public TcpMessageHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // 设置SessionManager的方法，用于在Spring容器中设置
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        logger.info("设备连接建立：{}", clientAddress);

        // 创建设备会话
        DeviceSession session = new DeviceSession();
        session.setChannel(ctx.channel());
        session.setConnectTime(LocalDateTime.now());
        session.setLastActiveTime(LocalDateTime.now());
        session.setClientAddress(clientAddress);

        // 添加到会话管理器
        sessionManager.addSession(ctx.channel().id().asShortText(), session);

        // 发送设备已连接语音提醒
        sendConnectVoiceImmediately(ctx);

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        logger.info("设备连接断开：{}", clientAddress);

        // 从会话管理器中移除
        sessionManager.removeSession(ctx.channel().id().asShortText());

        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        String sessionId = ctx.channel().id().asShortText();
        String clientAddress = ctx.channel().remoteAddress().toString();

        logger.info("收到设备消息 [{}] [{}]: {}", sessionId, clientAddress, message);

        // 发送mqtt消息,用于第三方解析
        boolean success = publishService.publish(
                MqttSendPositionParamEnum.POSITION_PARAM_ENUM.getTopic(),
                message,
                MqttSendPositionParamEnum.POSITION_PARAM_ENUM.getQos(),
                MqttSendPositionParamEnum.POSITION_PARAM_ENUM.isRetained());

        if (success) {
            logger.debug("MQTT-发布TCP消息成功: topic={}", MqttSendPositionParamEnum.POSITION_PARAM_ENUM.getTopic());
        } else {
            logger.debug("MQTT-发布TCP消息失败: topic={}", MqttSendPositionParamEnum.POSITION_PARAM_ENUM.getTopic());
        }

        // 更新会话活跃时间
        DeviceSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            session.setLastActiveTime(LocalDateTime.now());
            session.incrementMessageCount();

            // 续期 Redis 在线状态（使用独立心跳线程池，与业务处理隔离）
            if (session.getDeviceId() != null) {
                String deviceId = session.getDeviceId();
                tcpHeartbeatExecutor.execute(() -> {
                    try {
                        sessionManager.refreshOnlineStatus(deviceId);
                    } catch (Exception e) {
                        logger.warn("异步刷新设备 [{}] 在线状态失败: {}", deviceId, e.getMessage());
                    }
                });
            }
        }

        // 处理接收到的消息
        processMessage(ctx, message, session);

        // 异步保存消息到TDengine
        saveMessageToTDengineAsync(sessionId, message, session);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                String clientAddress = ctx.channel().remoteAddress().toString();
                logger.warn("设备连接超时，关闭连接：{}", clientAddress);
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        logger.error("TCP连接异常 [{}]: {}", clientAddress, cause.getMessage(), cause);
        ctx.close();
    }

    /**
     * 处理接收到的消息
     */
    private void processMessage(ChannelHandlerContext ctx, String message, DeviceSession session) {
        try {
            // 基础消息验证
            if (message == null || message.trim().isEmpty()) {
                logger.warn("收到空消息，忽略处理");
                return;
            }

            // 获取sessionId
            String sessionId = ctx.channel().id().asShortText();

            // 更新会话信息
            if (session != null) {
                session.setLastMessage(message);

                // 从消息中提取设备ID（不管是什么类型的消息）
                String deviceId = extractDeviceIdFromMessage(message);
                if (deviceId != null) {
                    session.setDeviceId(deviceId);
                    logger.info("从消息中提取到设备ID：{} -> {}", session.getClientAddress(), deviceId);

                    // 更新设备ID到会话的映射关系
                    sessionManager.updateDeviceMapping(sessionId, deviceId);
                    logger.info("更新设备映射关系：设备ID[{}] -> 会话ID[{}]", deviceId, sessionId);

                    // 如果是注册消息，发送注册应答
                    if (message.startsWith("$") && message.contains(",R,")) {
                        sendRegisterResponse(deviceId);
                        logger.info("设备注册成功：{} -> {}", session.getClientAddress(), deviceId);
                    }
                }
            }

            // 实时处理TCP消息业务逻辑
            processTcpMessageImmediately(ctx, message, session);

        } catch (Exception e) {
            logger.error("处理消息时发生异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 从消息中提取设备ID
     * 消息格式：$xx,yy,设备ID,#（第三位永远是设备ID，不管第二位是什么）
     */
    private String extractDeviceIdFromMessage(String message) {
        try {
            if (message.startsWith("$") && message.endsWith("#")) {
                String[] parts = message.substring(1, message.length() - 1).split(",");
                if (parts.length >= 3) {
                    return parts[2]; // 第三位永远是设备ID
                }
            }
        } catch (Exception e) {
            logger.error("解析消息中的设备ID失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 发送注册应答
     * 应答格式：$xx,RA,0,y,#\n
     */
    private void sendRegisterResponse(String deviceId) {
        String response = "$08,RA,0,1,#\n";
        boolean success = sessionManager.sendMessageToDevice(deviceId, response);
        if (success) {
            logger.info("发送注册应答给设备：{}", deviceId);
        } else {
            logger.error("发送注册应答失败，设备：{}", deviceId);
        }
    }

    /**
     * 异步保存消息到TDengine
     *
     * @param sessionId 会话ID
     * @param message   消息内容
     * @param session   设备会话
     */
    private void saveMessageToTDengineAsync(String sessionId, String message, DeviceSession session) {
        // 使用CompletableFuture异步执行，不阻塞TCP处理线程
        CompletableFuture.runAsync(() -> {
            try {
                String deviceId = getDeviceIdForSaving(session, message);

                R<JSONObject> result = rawMessageTdEngineService.saveRawMessageData(
                        deviceId, sessionId, message);

                if (result.getCode() == R.SUCCESS) {
                    saveSuccessCount.incrementAndGet();
                    logger.info("TCP消息保存成功 - SessionId: {}, DeviceId: {}, MessageLength: {}",
                            sessionId, deviceId, message.length());
                } else {
                    saveFailureCount.incrementAndGet();
                    logger.warn("TCP消息保存失败 - SessionId: {}, DeviceId: {}, 错误: {}",
                            sessionId, deviceId, result.getMsg());
                }
            } catch (Exception e) {
                saveFailureCount.incrementAndGet();
                // 保存失败不影响业务，只记录日志
                logger.error("保存TCP消息到TDengine异常 - SessionId: {}, 错误: {}",
                        sessionId, e.getMessage(), e);
            }
        }, tcpSaveExecutor).exceptionally(throwable -> {
            saveFailureCount.incrementAndGet();
            logger.error("异步保存TCP消息失败 - SessionId: {}", sessionId, throwable);
            return null;
        });
    }

    /**
     * 获取用于保存的设备ID
     *
     * @param session 设备会话
     * @param message 消息内容
     * @return 设备ID
     */
    private String getDeviceIdForSaving(DeviceSession session, String message) {
        // 优先级1：已注册的设备ID
        if (session != null && StringUtils.isNotBlank(session.getDeviceId())) {
            return session.getDeviceId();
        }

        // 优先级2：从消息中解析设备ID
        String deviceIdFromMessage = extractDeviceIdFromMessage(message);
        if (StringUtils.isNotBlank(deviceIdFromMessage)) {
            return deviceIdFromMessage;
        }

        // 优先级3：使用客户端地址作为设备ID
        if (session != null && StringUtils.isNotBlank(session.getClientAddress())) {
            return session.getClientAddress().replaceAll("[^a-zA-Z0-9_]", "_");
        }

        // 优先级4：默认值
        return "unknown_device";
    }

    /**
     * 获取保存成功次数
     */
    public long getSaveSuccessCount() {
        return saveSuccessCount.get();
    }

    /**
     * 获取保存失败次数
     */
    public long getSaveFailureCount() {
        return saveFailureCount.get();
    }

    /**
     * 实时处理TCP消息业务逻辑
     * 类似WebSocket的处理方式，收到消息后立即处理
     *
     * @param ctx     通道上下文
     * @param message TCP消息内容
     * @param session 设备会话
     */
    private void processTcpMessageImmediately(ChannelHandlerContext ctx, String message, DeviceSession session) {
        // 异步处理，避免阻塞TCP接收线程
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 基础验证
                if (StringUtils.isBlank(message)) {
                    logger.debug("TCP消息为空，跳过业务处理");
                    return;
                }

                // 2. 优先检查PSA应答消息
                if (parameterResponseTcpProcessor.canProcess(message)) {
                    logger.info("检测到PSA应答消息，使用参数应答处理器: {}", message);
                    parameterResponseTcpProcessor.process(message);
                    return;
                }

                // 3. 过滤：只处理第二位是'S'的消息
                if (!isTypeSMessage(message)) {
                    logger.info("非S类型TCP消息，跳过业务处理: {}", message.substring(0, Math.min(50, message.length())));
                    return;
                }

                logger.info("开始处理S类型TCP消息，长度: {}", message.length());

                // 4. 解析TCP消息
                TcpMessageData tcpData = tcpMessageParser.parseMessage(message);
                if (tcpData == null) {
                    logger.warn("TCP消息解析失败，跳过业务处理: {}", message);
                    return;
                }

                // 5. 检查并注册新设备（基于Session级别的检查）
                helmetDeviceRegistrationService.registerDeviceIfNeeded(tcpData, session);

                // 6. 首次连接设备下发GET指令（在设备注册之后）
                checkAndSendFirstConnectCommand(ctx, tcpData.getDeviceId(), session);

//                //保存电量：helmet_runde_ca_report_location
//                alarmZeroTcpProcessor.saveHelmetRundeData(tcpData.getDeviceId(),tcpData);

                // 7. 根据报警值选择处理器
                // 先检查是否包含SOS报警（优先级高）
                if (sosTcpProcessor.canProcess(tcpData)) {
                    logger.info("使用SOS报警处理器处理TCP消息: 设备ID={}, 报警值={}",
                            tcpData.getDeviceId(), tcpData.getAlarmValue());

                    // 执行SOS报警处理（发送语音等）
                    boolean alarm = isAlarm(AlarmConfigEnum.YJ.getCode(), tcpData.getDeviceId());
                    if (alarm){
                        sosTcpProcessor.process(tcpData, ctx);
                    }
                }

                // 检查是否包含跌落报警
                if (fallTcpProcessor.canProcess(tcpData)) {
                    logger.info("使用跌落报警处理器处理TCP消息: 设备ID={}, 报警值={}",
                            tcpData.getDeviceId(), tcpData.getAlarmValue());

                    // 执行跌落报警处理（发送语音等）
                    boolean alarm = isAlarm(AlarmConfigEnum.DL.getCode(), tcpData.getDeviceId());
                    if (alarm){
                        fallTcpProcessor.process(tcpData, ctx);
                    }
                }

                // 检查是否包含脱帽报警
                if (helmetOffTcpProcessor.canProcess(tcpData)) {
                    logger.info("使用脱帽报警处理器处理TCP消息: 设备ID={}, 报警值={}",
                            tcpData.getDeviceId(), tcpData.getAlarmValue());

                    // 执行脱帽报警处理（发送语音等）
                    boolean alarm = isAlarm(AlarmConfigEnum.TM.getCode(), tcpData.getDeviceId());
                    if (alarm){
                        boolean hasRecentAlarm = hasRecentAlarmInTenMinutes(tcpData.getDeviceId(), AlarmConfigEnum.TM.getName());
                        // 最近10分钟 没有告警 → 才处理
                        if (!hasRecentAlarm) {
                            helmetOffTcpProcessor.process(tcpData, ctx);
                        }
                    }
                }

                // 检查是否包含长时间静止报警
                if (longTimeStaticTcpProcessor.canProcess(tcpData)) {
                    logger.info("使用长时间静止报警处理器处理TCP消息: 设备ID={}, 报警值={}",
                            tcpData.getDeviceId(), tcpData.getAlarmValue());
                    // 执行长时间静止报警处理（发送语音等）
                    boolean alarm = isAlarm(AlarmConfigEnum.CSJ.getCode(), tcpData.getDeviceId());
                    if (alarm){
                        boolean hasRecentAlarm = hasRecentAlarmInTenMinutes(tcpData.getDeviceId(), AlarmConfigEnum.CSJ.getName());
                        // 最近10分钟 没有告警 → 才处理
                        if (!hasRecentAlarm) {
                            longTimeStaticTcpProcessor.process(tcpData, ctx);
                        }
                    }
                }

                // 检查是否包含危险源报警
                if (hazardSourceTcpProcessor.canProcess(tcpData)) {
                    logger.info("使用危险源报警处理器处理TCP消息: 设备ID={}, 报警值={}",
                            tcpData.getDeviceId(), tcpData.getAlarmValue());
                    // 执行危险源报警处理（语音下发将由WebSocket层处理）
                    boolean alarm = isAlarm(AlarmConfigEnum.WX.getCode(), tcpData.getDeviceId());
                    if (alarm){
                        hazardSourceTcpProcessor.process(tcpData, ctx);
                    }
                }

                // 再检查其他处理器，计算轨迹
                if (alarmZeroTcpProcessor.canProcess(tcpData)) {
                    logger.info("使用报警值0处理器处理TCP消息: 设备ID={}, 报警值={}",
                            tcpData.getDeviceId(), tcpData.getAlarmValue());

                    // 执行业务处理（调用定位引擎 + 保存helmet表）
                    alarmZeroTcpProcessor.process(tcpData);

                    logger.info("TCP消息业务处理完成: 设备ID={}", tcpData.getDeviceId());
                } else if (!sosTcpProcessor.canProcess(tcpData) && !fallTcpProcessor.canProcess(tcpData) &&
                        !helmetOffTcpProcessor.canProcess(tcpData) && !longTimeStaticTcpProcessor.canProcess(tcpData) &&
                        !hazardSourceTcpProcessor.canProcess(tcpData)) {
                    // 如果都不是已支持的报警类型，则记录日志
                    logger.info("暂不支持的报警值，跳过业务处理: 设备ID={}, 报警值={}",
                            tcpData.getDeviceId(), tcpData.getAlarmValue());
                }

                //增加低电量提醒，电量低于20%，30%，40%各自提醒一次，每次提醒都放入redis里，只个电量只提醒一次
                Integer batteryLevel = tcpData.getBatteryLevel();
                String deviceId = tcpData.getDeviceId();
                if (batteryLevel == null) {
                    return;
                }

                // 设备电量 >= 40%，清空所有提醒
                if (batteryLevel >= 40) {
                    clearBatteryWarnRedis(deviceId);
                    return;
                }

                // 设备电量 < 40%，按挡位提醒（从低到高）
                checkAndSendBatteryWarn(deviceId, batteryLevel, 20);

                checkAndSendBatteryWarn(deviceId, batteryLevel, 30);

                checkAndSendBatteryWarn(deviceId, batteryLevel, 40);

            } catch (Exception e) {
                logger.error("TCP消息业务处理时发生异常: {}", e.getMessage(), e);
            }
        }, tcpMessageExecutor).exceptionally(throwable -> {
            logger.error("TCP消息异步业务处理失败", throwable);
            return null;
        });
    }


    /**
     * 查询设备在【最近10分钟内】是否已存在相同类型的告警
     * @param deviceId 设备ID
     * @param warningContent 告警类型
     * @return true=10分钟内已报警（不重复报），false=未报警（可以报警）
     */
    public boolean hasRecentAlarmInTenMinutes(String deviceId, String warningContent) {
        try {
            // 1. 查询绑定身份证（和你原来逻辑一致）
            Object cardObj = redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
            String idCard = cardObj != null ? String.valueOf(cardObj).trim() : null;

            if (idCard == null || idCard.isEmpty()) {
                logger.warn("设备 {} 未绑定身份证", deviceId);
                return false;
            }

            // 2. 库名 + 表名
            String dbName = deviceCorpMappingCache.getDbName(deviceId);
            String tableName = "swm_warning_management_" + deviceId + "_" + idCard.toLowerCase();

            // 3. 时间：当前时间 -10分钟
            Date date = new Date();
            String startTimeStr = DateUtil.format(DateUtil.offsetMinute(date, -10), DatePattern.NORM_DATETIME_PATTERN);
            String endTimeStr = DateUtil.format(date, DatePattern.NORM_DATETIME_PATTERN);

            // 4. SQL：严格按你的格式！count(1)，不加 AS count
            String sql = "SELECT count(1) FROM " + dbName + "." + tableName +
                    " WHERE warning_content = '" + warningContent + "'" +
                    " AND create_date >= '" + startTimeStr + "'" +
                    " AND create_date <= '" + endTimeStr + "'";

            logger.info("查询10分钟内告警SQL: {}", sql);

            Long count = tdengineService.selectCount(sql);

            logger.info("查询10分钟内告警结果: {}", count);

            // 有数据 = 10分钟内已报警
            return  count > 0;

        } catch (Exception e) {
            logger.error("查询最近10分钟告警异常, deviceId={}, warningContent={}", deviceId, warningContent, e);
            return false;
        }
    }


    /**
     * 清理设备电量提醒
     * @param deviceId
     */
    private void clearBatteryWarnRedis(String deviceId) {
        redisService.del(SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX + deviceId + ":20");
        redisService.del(SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX + deviceId + ":30");
        redisService.del(SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX + deviceId + ":40");

        logger.info("设备 [{}] 电量>=40%，已清空低电量提醒记录", deviceId);
    }


    /**
     * 发送语音电量提示
     * @param deviceId
     * @param batteryLevel
     * @param threshold
     */
    public void checkAndSendBatteryWarn(String deviceId, int batteryLevel, int threshold) {


        if (batteryLevel >= threshold) {
            return;
        }

        String redisKey = SwmRedisKeyConstants.GlobalKey.BATTERY_WARN_KEY_PREFIX + deviceId + ":" + threshold;

        // 已提醒过
        if (redisService.hasKey(redisKey)) {
            return;
        }

        // 记录 Redis
        redisService.set(redisKey, "1");

        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);

        // 查询语音模板内容
        String voiceTemplateCacheKey  = corpCode +  SwmRedisKeyConstants.SwmKey.VOICE_TEMPLATE_CACHE;
        Object hget = redisService.hget(voiceTemplateCacheKey, Low_Battery_VoiceTemplate_ID);
        String voiceText = null;
        if (hget != null){
            voiceText = (String) hget;
        }

        if (StringUtils.isBlank(voiceText)) {
            logger.warn("语音模板内容为空: ID={}", Low_Battery_VoiceTemplate_ID);
            return;
        }

        // 调用语音报警服务下发语音
        logger.info("准备下发低电量语音报警: 设备ID={}, 语音内容={}", deviceId, voiceText);

        // 6. 构建并发送语音指令
        String voiceCommand = buildVoiceCommand(voiceText);
        sendVoiceCommand(deviceId, voiceCommand);
        logger.info("设备 [{}] 触发 {}% 电量提醒，当前电量：{}%", deviceId, threshold, batteryLevel);
    }


    /**
     * 判断是否需要报警
     * @param alarmKey
     * @return
     */
    public boolean isAlarm(String alarmKey,String deviceId){
        boolean isAlarm = true;
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
        String redisKey = corpCode + SwmRedisKeyConstants.SwmKey.ALARM_CONFIG;

        //1 -是  0-否
        Object hget = redisService.hget(redisKey, alarmKey);
        if (hget != null){
            Integer enableAlarm = (Integer) hget;
            if (enableAlarm == 0){
                isAlarm = false;
            }
        }


//        String sql = "select enable_alarm from swm_alarm_config where alarm_key = ? and `status` = '0' and corp_code = ?";
//        List<String> results = jdbcTemplate.queryForList(sql, String.class, alarmKey,corpCode);
//        if (results.size() > 0 && results.contains("0")){
//            isAlarm = false;
//        }
        return isAlarm ;
    }

    /**
     * 判断是否为S类型消息
     * 消息格式：$xx,S,设备ID,...
     *
     * @param message TCP消息
     * @return true表示是S类型消息
     */
    private boolean isTypeSMessage(String message) {
        try {
            if (StringUtils.isBlank(message) || !message.startsWith("$")) {
                return false;
            }

            // 查找第一个逗号后的字符
            int firstComma = message.indexOf(',');
            if (firstComma == -1 || firstComma + 1 >= message.length()) {
                return false;
            }

            int secondComma = message.indexOf(',', firstComma + 1);
            if (secondComma == -1) {
                return false;
            }

            // 提取第二位字段
            String commandType = message.substring(firstComma + 1, secondComma);
            return "S".equals(commandType.trim());

        } catch (Exception e) {
            logger.debug("判断S类型消息时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为首次连接设备，如果是则下发MAC地址获取指令
     * 在设备注册入库之后执行，不管设备是否为新设备都下发
     *
     * @param ctx      ChannelHandlerContext
     * @param deviceId 设备ID
     * @param session  设备会话
     */
    private void checkAndSendFirstConnectCommand(ChannelHandlerContext ctx, String deviceId, DeviceSession session) {
        try {
            // 检查是否已经下发过MAC地址获取指令
            if (session.isFirstConnectMacCommandSent()) {
                logger.debug("设备[{}]已下发过MAC地址获取指令，跳过", deviceId);
                return;
            }

            // 首次连接的设备，下发MAC地址获取指令（在设备注册之后）
            sendFirstConnectCommand(deviceId);
            logger.info("首次连接设备[{}]，已下发MAC地址获取指令", deviceId);

            // 标记已下发MAC地址获取指令，避免重复下发
            session.setFirstConnectMacCommandSent(true);

        } catch (Exception e) {
            logger.error("下发首次连接MAC地址获取指令时发生异常，设备ID: {}", deviceId, e);
            // 即使下发失败，也要标记为已尝试，避免重复尝试
            session.setFirstConnectMacCommandSent(true);
        }
    }

    /**
     * 发送首次连接MAC地址获取指令给设备
     * 指令格式：$OD,PS,0,GM,GET,#\n
     *
     * @param deviceId 设备ID
     */
    private void sendFirstConnectCommand(String deviceId) {
        String command = "$OD,PS,0,GM,GET,#\n";
        boolean success = sessionManager.sendMessageToDevice(deviceId, command);
        if (success) {
            logger.info("向首次连接设备[{}]发送MAC地址获取指令: {}", deviceId, command.trim());
        } else {
            logger.error("发送首次连接MAC地址获取指令失败，设备ID: {}", deviceId);
        }
    }

    /**
     * 连接建立时立即发送语音提醒
     * 不依赖设备ID，直接通过Channel发送
     *
     * @param ctx ChannelHandlerContext
     */
    private void sendConnectVoiceImmediately(ChannelHandlerContext ctx) {
        try {
            // 构建语音指令
            String text = "设备已连接";
            String dataSection = "PS,0,TTSD," + text + ",#";

            // 计算UTF-8字节长度
            int byteLength = dataSection.getBytes(StandardCharsets.UTF_8).length;

            // 转换为十六进制字符串（大写）
            String hexLength = Integer.toHexString(byteLength).toUpperCase();

            // 构建完整指令
            String voiceCommand = "$" + hexLength + "," + dataSection + "\n";

            logger.info("连接建立，立即发送语音提醒: {}", voiceCommand.trim());

            // 直接通过Channel发送
            ctx.channel().writeAndFlush(voiceCommand);

        } catch (Exception e) {
            // 捕获所有异常，确保不影响连接建立
            logger.error("发送连接语音提醒时发生异常，但不影响连接", e);
        }
    }

    /**
     * 构建语音指令
     * 格式: $xx,PS,0,TTSD,zzzz,#\n
     *
     * @param text 语音内容
     * @return 完整的语音指令
     */
    private String buildVoiceCommand(String text) {
        // 构建需要计算长度的部分: PS,0,TTSD,[语音内容],#
        String dataSection = "PS,0,TTSD," + text + ",#";

        // 计算UTF-8字节长度
        int byteLength = dataSection.getBytes(StandardCharsets.UTF_8).length;

        // 转换为十六进制字符串（大写）
        String hexLength = Integer.toHexString(byteLength).toUpperCase();

        // 构建完整指令
        String command = "$" + hexLength + "," + dataSection + "\n";

        logger.debug("构建语音指令: 文本='{}', UTF-8字节长度={}, 十六进制长度={}",
                text, byteLength, hexLength);

        return command;
    }

    /**
     * 发送语音指令给设备
     *
     * @param deviceId 设备ID
     * @param voiceCommand 语音指令
     */
    private void sendVoiceCommand(String deviceId, String voiceCommand) {
        cn.iocoder.yudao.module.iot.tcp.session.SessionManager sessionManager =
                cn.iocoder.yudao.module.iot.tcp.server.NettyTcpServer.getInstance().getSessionManager();
        boolean success = sessionManager.sendMessageToDevice(deviceId, voiceCommand);
        if (success) {
            logger.debug("语音指令发送成功, deviceId: {}", deviceId);
        } else {
            logger.warn("语音指令发送失败, deviceId: {}", deviceId);
        }
    }
}
