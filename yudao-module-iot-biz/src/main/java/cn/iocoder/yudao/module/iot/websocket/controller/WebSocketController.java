package cn.iocoder.yudao.module.iot.websocket.controller;

import cn.iocoder.yudao.module.iot.websocket.service.WebSocketService;
import cn.iocoder.yudao.module.iot.websocket.vo.Result;
import cn.iocoder.yudao.module.iot.websocket.dto.LocalRecordControlRequest;
import cn.iocoder.yudao.module.iot.websocket.dto.LocalVideoListRequest;
import cn.iocoder.yudao.module.iot.websocket.dto.ServerPushUploadRequest;
import cn.iocoder.yudao.module.iot.websocket.dto.ServerPushOpenRtspRequest;
import cn.iocoder.yudao.module.iot.websocket.dto.ServerPushStopRtspRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * WebSocket控制器
 * 提供HTTP接口来测试WebSocket功能
 *
 * @author Shawn
 * @date 2025-05-28
 */
@RestController
@RequestMapping("${adminPath}/api/websocket")
public class WebSocketController {

    @Resource
    private WebSocketService webSocketService;

    /**
     * 通过Handler方式发送消息给指定会话
     */
    @GetMapping("/send/session/{sessionId}")
    public Result<?> sendMessageToSession(
            @PathVariable("sessionId") String sessionId,
            @RequestParam("message") String message) {
        webSocketService.sendMessageToSession(sessionId, message);
        return Result.success("消息已发送");
    }

    /**
     * 通过Handler方式发送消息给指定设备ID
     *
     * @author Shawn
     * @date 2024-07-05
     */
    @GetMapping("/send/device/{deviceId}")
    public Result<?> sendMessageToDevice(
            @PathVariable("deviceId") String deviceId,
            @RequestParam("message") String message) {
        webSocketService.sendMessageToDevice(deviceId, message);
        return Result.success("消息已发送给设备");
    }

    /**
     * 通过Handler方式广播消息
     */
    @GetMapping("/broadcast/handler")
    public Result<?> broadcastMessageViaHandler(
            @RequestParam("message") String message) {
        webSocketService.broadcastMessageViaHandler(message);
        return Result.success("广播消息已发送");
    }

    /**
     * 通过Endpoint方式发送消息给指定用户
     */
    @GetMapping("/send/user/{userId}")
    public Result<?> sendMessageToUser(
            @PathVariable("userId") String userId,
            @RequestParam("message") String message) {
        webSocketService.sendMessageToUser(userId, message);
        return Result.success("消息已发送给用户");
    }

    /**
     * 通过Endpoint方式广播消息
     */
    @GetMapping("/broadcast/endpoint")
    public Result<?> broadcastMessageViaEndpoint(
            @RequestParam("message") String message) {
        webSocketService.broadcastMessageViaEndpoint(message);
        return Result.success("广播消息已发送");
    }

    /**
     * 获取当前在线用户数
     */
    @GetMapping("/online/count")
    public Result<?> getOnlineUserCount() {
        int count = webSocketService.getOnlineUserCount();
        return Result.success("当前在线用户数: " + count, count);
    }

    /**
     * 获取WebSocket连接状态详情
     *
     * @author Shawn
     * @date 2024-12-19
     */
    @GetMapping("/status/detail")
    public Result<?> getWebSocketStatusDetail() {
        try {
            Map<String, Object> statusDetail = webSocketService.getWebSocketStatusDetail();
            return Result.success("WebSocket连接状态详情", statusDetail);
        } catch (Exception e) {
            return Result.error("获取连接状态失败: " + e.getMessage());
        }
    }

    /**
     * 发送本地录制控制指令给指定设备
     * 后台发送指令控制设备开启/关闭本地录制
     *
     * @param request 本地录制控制请求参数
     * @return 操作结果
     *
     * @author Shawn
     * @date 2025-05-28
     */
    @PostMapping("/control/localrecord")
    public Result<?> sendLocalRecordControlCommand(@Valid @RequestBody LocalRecordControlRequest request) {

        try {
            webSocketService.sendLocalRecordControlCommand(
                    request.getDeviceId(),
                    request.getLocalRecordAsString(),
                    request.getRecordVideoName());
            return Result.success("本地录制控制指令已发送给设备: " + request.getDeviceId());
        } catch (Exception e) {
            return Result.error("发送指令失败: " + e.getMessage());
        }
    }

    /**
     * 发送查询设备本地视频列表指令
     * 服务端通过websocket发送指令，获取设备本地存储的视频列表
     * 8.1服务端websocket发送指令，查看设备本地视频列表
     *
     * @param request 查询本地视频列表请求参数
     * @return 操作结果
     *
     * @author Shawn
     * @date 2025-05-28
     */
    @PostMapping("/control/localvideolist")
    public Result<?> getLocalVideoList(@Valid @RequestBody LocalVideoListRequest request) {
        try {
            webSocketService.sendGetLocalVideoListCommand(
                    request.getDeviceId(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getVideoType());
            return Result.success("查询本地视频列表指令已发送给设备: " + request.getDeviceId());
        } catch (Exception e) {
            return Result.error("发送指令失败: " + e.getMessage());
        }
    }

    /**
     * 8.3 服务端选择文件上传
     *
     * @param request 上传请求参数
     * @return 操作结果
     * @author Shawn
     * @date 2025-05-28
     */
    @PostMapping("/control/serverpushupload")
    public Result<?> serverPushUploadVideo(@Valid @RequestBody ServerPushUploadRequest request) {
        try {
            webSocketService.sendServerPushUploadCommand(
                    request.getDeviceId(),
                    request.getTalkbackId(),
                    request.getFileList());
            return Result.success("服务端选择文件上传指令已发送给设备: " + request.getDeviceId());
        } catch (Exception e) {
            return Result.error("发送指令失败: " + e.getMessage());
        }
    }

    /**
     * 9.1.1 后台发送指令控制设备开启推流
     * 向设备发送开启RTSP推流的命令
     *
     * @param request 开启推流请求参数
     * @return 操作结果
     * @author Shawn
     * @date 2025-05-28
     */
    @PostMapping("/control/openpushstream")
    public Result<?> openPushStream(@Valid @RequestBody ServerPushOpenRtspRequest request) {
        try {
            webSocketService.sendOpenPushStreamCommand(
                    request.getDeviceId(),
                    request.getPushUrl());
            return Result.success("开启推流指令已发送给设备: " + request.getDeviceId());
        } catch (Exception e) {
            return Result.error("发送指令失败: " + e.getMessage());
        }
    }

    /**
     * 9.2 后台发送指令控制设备关闭推流
     * 向设备发送数据 {"cmd": "server_push_stop_rtsp"}
     *
     * @param request 关闭推流请求参数
     * @return 操作结果
     * @author Shawn
     * @date 2025-05-28
     */
    @PostMapping("/control/closepushstream")
    public Result<?> closePushStream(@Valid @RequestBody ServerPushStopRtspRequest request) {
        try {
            webSocketService.sendClosePushStreamCommand(request.getDeviceId());
            return Result.success("关闭推流指令已发送给设备: " + request.getDeviceId());
        } catch (Exception e) {
            return Result.error("发送指令失败: " + e.getMessage());
        }
    }

    /**
     * 强制清理指定设备的WebSocket连接
     *
     * @param deviceId 设备ID
     * @return 操作结果
     * @author Shawn
     * @date 2025-01-04
     */
    @PostMapping("/manage/cleanup/device/{deviceId}")
    public Result<?> forceCleanupDeviceConnection(@PathVariable("deviceId") String deviceId) {
        try {
            boolean success = webSocketService.forceCleanupDeviceConnection(deviceId);
            if (success) {
                return Result.success("设备连接清理成功: " + deviceId);
            } else {
                return Result.error("设备连接不存在或清理失败: " + deviceId);
            }
        } catch (Exception e) {
            return Result.error("清理设备连接失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发WebSocket连接清理
     * 清理所有无效连接和过期会话
     *
     * @return 清理统计信息
     * @author Shawn
     * @date 2025-01-04
     */
    @PostMapping("/manage/cleanup/all")
    public Result<?> triggerConnectionCleanup() {
        try {
            Map<String, Object> cleanupResult = webSocketService.triggerConnectionCleanup();
            return Result.success("WebSocket连接清理完成", cleanupResult);
        } catch (Exception e) {
            return Result.error("触发连接清理失败: " + e.getMessage());
        }
    }

    /**
     * 获取WebSocket连接健康状态
     * 包含详细的连接信息和清理状态
     *
     * @return WebSocket连接健康状态
     * @author Shawn
     * @date 2025-01-04
     */
    @GetMapping("/manage/health")
    public Result<?> getConnectionHealth() {
        try {
            Map<String, Object> statusDetail = webSocketService.getWebSocketStatusDetail();

            // 添加健康状态评估
            Map<String, Object> healthInfo = new HashMap<>();
            int totalConnections = (Integer) statusDetail.get("totalConnections");
            int activeConnections = (Integer) statusDetail.get("activeConnections");
            int deviceConnections = (Integer) statusDetail.get("deviceConnections");

            // 计算连接健康指标
            double activeRatio = totalConnections > 0 ? (double) activeConnections / totalConnections : 1.0;
            double deviceRatio = totalConnections > 0 ? (double) deviceConnections / totalConnections : 0.0;

            String healthStatus;
            if (activeRatio >= 0.8 && deviceRatio >= 0.7) {
                healthStatus = "健康";
            } else if (activeRatio >= 0.6 && deviceRatio >= 0.5) {
                healthStatus = "良好";
            } else if (activeRatio >= 0.4 && deviceRatio >= 0.3) {
                healthStatus = "警告";
            } else {
                healthStatus = "异常";
            }

            healthInfo.put("healthStatus", healthStatus);
            healthInfo.put("activeRatio", Math.round(activeRatio * 100) / 100.0);
            healthInfo.put("deviceRatio", Math.round(deviceRatio * 100) / 100.0);
            healthInfo.put("recommendations",
                    generateHealthRecommendations(activeRatio, deviceRatio, totalConnections));

            statusDetail.put("healthInfo", healthInfo);

            return Result.success("WebSocket连接健康状态", statusDetail);
        } catch (Exception e) {
            return Result.error("获取连接健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 生成健康状态建议
     *
     * @param activeRatio      活跃连接比例
     * @param deviceRatio      设备连接比例
     * @param totalConnections 总连接数
     * @return 建议列表
     * @author Shawn
     * @date 2025-01-04
     */
    private List<String> generateHealthRecommendations(double activeRatio, double deviceRatio, int totalConnections) {
        List<String> recommendations = new ArrayList<>();

        if (activeRatio < 0.6) {
            recommendations.add("活跃连接比例较低，建议执行连接清理");
        }

        if (deviceRatio < 0.5) {
            recommendations.add("设备连接比例较低，可能存在未正确登录的连接");
        }

        if (totalConnections > 1000) {
            recommendations.add("连接数量较多，建议监控服务器性能");
        }

        if (totalConnections == 0) {
            recommendations.add("当前无活跃连接，请检查设备连接状态");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("连接状态良好，无需特殊处理");
        }

        return recommendations;
    }
}
