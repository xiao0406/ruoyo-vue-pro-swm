package cn.iocoder.yudao.module.iot.mqtt.dto.zhongtai;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 中泰报警信息
 * （1）SOS告警 ： value 值：0脱帽，1戴帽
 * （2）脱帽/戴帽告警 value 值：0脱帽，1戴帽
 * （3）告警跌倒  没有value，收到即报警
 *
 *
 */
@Data
@Schema(description = "中泰mqtt报警信息")
public class ZhongTaiAlarmDto {

    @Schema(description = "报警类型，例如：SOS")
    private String alarmType;

    @Schema(description = "设备号")
    private String sip;

    @Schema(description = "上报时间戳")
    private Long time;

    @Schema(description = "类型，例如 alarm")
    private String type;

    @Schema(description = "报警值")
    private String value;

    @Schema(description = "GPS 信息")
    private GpsInfo gps;

    // ============= GPS 嵌套对象 =============
    @Data
    public static class GpsInfo {
        private String satellites;  // 卫星数
        private String hig;
        private Long serverTime;
        private BigDecimal lon;     // 经度
        private Long time;
        private Integer dingweiStatus;  // 定位状态
        private String type;
        private Integer battery;    // 电量
        private String user;
        private BigDecimal lat;     // 纬度

        // 速度、方向
        private GpsExtraInfo info;
    }

    // ============= GPS 额外信息（速度、方向） =============
    @Data
    public static class GpsExtraInfo {
        private String speed;
        private String direction;
    }

}
