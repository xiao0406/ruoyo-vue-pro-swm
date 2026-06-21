package cn.iocoder.yudao.module.swm.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 签名工具类（SHA256 实现）
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.util.SignatureUtil
 * 已将 fastjson 替换为 Jackson
 */
public class SignatureUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * SHA256 哈希计算，返回小写十六进制字符串
     */
    public static String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA256算法初始化失败", e);
        }
    }

    /**
     * 判断第三方响应是否业务成功
     */
    public static boolean isBusinessSuccess(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) return false;
        try {
            JsonNode json = OBJECT_MAPPER.readTree(jsonStr);
            JsonNode isSuccess = json.get("isSuccess");
            return isSuccess != null && isSuccess.asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 提取错误消息（用于日志）
     */
    public static String getErrorMessage(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) return "空响应";
        try {
            JsonNode json = OBJECT_MAPPER.readTree(jsonStr);
            JsonNode msg = json.get("message");
            if (msg != null) return msg.asText();
            return "code=" + json.path("code").asInt() + ", isSuccess=" + json.path("isSuccess").asBoolean();
        } catch (Exception e) {
            return "解析失败: " + jsonStr;
        }
    }

}
