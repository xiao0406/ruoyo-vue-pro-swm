package cn.iocoder.yudao.module.iot.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * JSON 响应构建工具类
 */
public class JsonResponseBuilder {

    /**
     * 构建CA登录响应
     */
    public static String buildCaLoginResponse(String deviceId) {
        JSONObject response = new JSONObject();
        response.set("cmd", "login");
        response.set("code", 0);
        response.set("msg", "success");
        response.set("deviceId", deviceId);
        return response.toString();
    }

    /**
     * 构建CA位置上报响应
     */
    public static String buildCaReportLocationResponse() {
        JSONObject response = new JSONObject();
        response.set("cmd", "report_location");
        response.set("code", 0);
        response.set("msg", "success");
        return response.toString();
    }

    /**
     * 构建CA SIP SOS响应
     */
    public static String buildCaSipSosResponse() {
        JSONObject response = new JSONObject();
        response.set("cmd", "sip_sos");
        response.set("code", 0);
        response.set("msg", "success");
        return response.toString();
    }

    /**
     * 构建服务端推送MA广播响应
     */
    public static String buildServerPushMaBroadcastResponse() {
        JSONObject response = new JSONObject();
        response.set("cmd", "server_push_ma_broadcast");
        response.set("code", 0);
        response.set("msg", "success");
        return response.toString();
    }

    /**
     * 构建服务端推送MA广播响应（带URL）
     */
    public static String buildServerPushMaBroadcastResponse(String url) {
        JSONObject response = new JSONObject();
        response.set("cmd", "server_push_ma_broadcast");
        response.set("code", 0);
        response.set("msg", "success");
        response.set("url", url);
        return response.toString();
    }

    /**
     * 构建服务端推送开启RTSP命令
     */
    public static String buildServerPushOpenRtspCommand(String pushUrl) {
        JSONObject response = new JSONObject();
        response.set("cmd", "server_push_open_rtsp");
        response.set("pushUrl", pushUrl);
        return response.toString();
    }

    /**
     * 构建服务端推送停止RTSP命令
     */
    public static String buildServerPushStopRtspCommand() {
        JSONObject response = new JSONObject();
        response.set("cmd", "server_push_stop_rtsp");
        return response.toString();
    }

    /**
     * 构建CA SOS响应
     */
    public static String buildCaSosResponse() {
        JSONObject response = new JSONObject();
        response.set("cmd", "ca_sos");
        response.set("code", 0);
        response.set("msg", "success");
        return response.toString();
    }

    /**
     * 构建CA上传照片响应
     */
    public static String buildCaUploadPhotoResponse() {
        JSONObject response = new JSONObject();
        response.set("cmd", "ca_upload_photo");
        response.set("code", 0);
        response.set("msg", "success");
        return response.toString();
    }

    /**
     * 构建推送到客户端响应
     */
    public static String buildPushToClientResponse() {
        JSONObject response = new JSONObject();
        response.set("cmd", "push_to_client");
        response.set("code", 0);
        response.set("msg", "success");
        return response.toString();
    }

    /**
     * 构建切换本地录制响应
     */
    public static String buildToggleLocalRecordResponse() {
        JSONObject response = new JSONObject();
        response.set("cmd", "toggle_local_record");
        response.set("code", 0);
        response.set("msg", "success");
        return response.toString();
    }

}
