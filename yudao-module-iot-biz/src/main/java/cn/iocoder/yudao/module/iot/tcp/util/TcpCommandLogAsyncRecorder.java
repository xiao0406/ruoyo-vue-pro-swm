package cn.iocoder.yudao.module.iot.tcp.util;

import cn.iocoder.yudao.module.swm.service.TcpDeviceCommandLogService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * TCP设备指令日志异步记录工具类
 * 统一处理TCP指令日志的异步记录逻辑，消除重复代码
 *
 * @author Shawn
 * @date 2025-10-02
 */
@Component
public class TcpCommandLogAsyncRecorder {

    private static final Logger logger = LoggerFactory.getLogger(TcpCommandLogAsyncRecorder.class);

    @Resource
    private TcpDeviceCommandLogService tcpDeviceCommandLogService;

    /**
     * 异步保存TCP指令日志（成功场景）
     *
     * @param deviceId 设备ID
     * @param sendMessage 下发的消息内容
     * @author Shawn
     * @date 2025-10-02
     */
    public void saveAsync(String deviceId, String sendMessage) {
        saveAsync(deviceId, sendMessage, null);
    }

    /**
     * 异步保存TCP指令日志（带错误信息）
     *
     * @param deviceId 设备ID
     * @param sendMessage 下发的消息内容
     * @param errorMessage 错误信息
     * @author Shawn
     * @date 2025-10-02
     */
    public void saveAsync(String deviceId, String sendMessage, String errorMessage) {
        CompletableFuture.runAsync(() -> {
            try {
                tcpDeviceCommandLogService.saveTcpCommandLog(deviceId, sendMessage, errorMessage);
            } catch (Exception e) {
                // 日志记录失败不影响业务，只记录错误日志
                logger.error("保存TCP指令日志失败，deviceId: {}, 错误: {}", deviceId, e.getMessage());
            }
        }).exceptionally(throwable -> {
            logger.error("异步保存TCP指令日志失败，deviceId: {}", deviceId, throwable);
            return null;
        });
    }
}
