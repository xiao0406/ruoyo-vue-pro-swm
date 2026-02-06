package com.jeesite.modules.swm.util;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 签名工具类（SHA256 实现）
 * 说明：
 * - 签名算法：SHA256
 * - 签名内容：原始字符串（如 "AppId+Timestamp+AppSecret+Nonce"）
 * - 密钥：APP_SECRET（需 Base64 或 HEX 编码？此处默认为明文字符串）
 */
public class SignatureUtil {


    /**
     * 核心工具方法：纯SHA256哈希计算，返回小写十六进制字符串（第三方签名要求）
     *
     * @param content 待哈希的字符串
     * @return SHA256加密后的小写字符串，失败抛出运行时异常
     */
    public static String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 按UTF-8编码转字节数组（统一编码，避免不同环境哈希结果不一致）
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            // 转小写十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA256是JDK自带算法，不会抛出此异常，直接封装为运行时异常
            throw new RuntimeException("SHA256算法初始化失败", e);
        }
    }

    /**
     * 判断第三方响应是否业务成功
     *
     * @param jsonStr 响应体字符串
     * @return true 表示业务成功（isSuccess=true）
     */
    public static boolean isBusinessSuccess(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) return false;
        try {
            JSONObject json = JSONObject.parseObject(jsonStr);
            return Boolean.TRUE.equals(json.getBoolean("isSuccess"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 提取错误消息（用于日志）
     */
    public static String getErrorMessage(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) return "空响应";
        try {
            JSONObject json = JSONObject.parseObject(jsonStr);
            String msg = json.getString("message");
            return msg != null ? msg : "code=" + json.getIntValue("code") + ", isSuccess=" + json.getBoolean("isSuccess");
        } catch (Exception e) {
            return "解析失败: " + jsonStr;
        }
    }
}

