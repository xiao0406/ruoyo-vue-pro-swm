package cn.iocoder.yudao.module.iot.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * 报警灯配置类
 * 所有配置都写死在代码中，不依赖外部配置文件
 *
 * @author Shawn
 */
@Data
@Configuration
public class AlarmLightConfig {

    /**
     * API基础地址（写死）
     */
    private final String baseUrl = "http://gateway.sgbjxt.com/tgCloud/api";

    /**
     * 登录接口路径（写死）
     */
    private final String loginPath = "/login";

    /**
     * 语音播报接口路径（写死）
     */
    private final String voicePath = "/foreign/instruction/text/to/speech";

    /**
     * 登录账号（写死）
     */
    private final String account = "zjgggf_admin";

    /**
     * 登录密码（写死，已MD5加密）
     */
    private final String password = "4ce4dead23190802a3097bce1c61ca03";

    /**
     * 默认设备IMEI号（写死）
     */
    private final String defaultImei = "8673390759655860";

    /**
     * Token缓存时间（分钟）（写死）
     */
    private final Integer tokenCacheMinutes =10;

    /**
     * 连接超时时间（毫秒）（写死）
     */
    private final Integer connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）（写死）
     */
    private final Integer readTimeout = 30000;

    /**
     * 最大重试次数（写死）
     */
    private final Integer maxRetryTimes = 3;

    /**
     * 默认播放参数配置（写死）
     */
    @Data
    public static class DefaultVoiceParams {
        /**
         * 播放状态（写死）
         */
        private final Integer status = 3;

        /**
         * 播放音量 有效范围0-10（写死）
         */
        private final Integer volume = 3;

        /**
         * 音调（写死）
         */
        private final Integer savePitch = 1;

        /**
         * 播放模式 1循环播放 2播放一次（写死）
         */
        private final Integer playMode = 2;

        /**
         * 闪烁模式（写死）
         */
        private final Integer flashMode = 0;

        /**
         * 发声选择 1男声 0女声（写死）
         */
        private final Integer voiceSelection = 0;

        /**
         * 文本格式（写死）
         */
        private final Integer textFormat = 0;

        /**
         * 是否保存 0不保存 1保存（写死）
         */
        private final Integer whetherSave = 0;
    }

    /**
     * 默认播放参数（写死）
     */
    private final DefaultVoiceParams defaultParams = new DefaultVoiceParams();

    /**
     * 获取完整的登录URL
     */
    public String getLoginUrl() {
        return baseUrl + loginPath;
    }

    /**
     * 获取完整的语音播报URL
     */
    public String getVoiceUrl() {
        return baseUrl + voicePath;
    }
}
