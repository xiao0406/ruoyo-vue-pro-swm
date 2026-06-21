package cn.iocoder.yudao.module.iot.enums;

/**
 * 告警配置枚举
 *
 * 迁移自 JeeSite: com.jeesite.modules.enums.AlarmConfigEnum
 */
public enum AlarmConfigEnum {

    TM("1", "脱帽报警"),
    DL("4", "跌落报警"),
    CSJ("6", "长时间静止报警"),
    YJ("7", "应急呼叫"),
    WX("8", "危险区域闯入提示"),

    // 三维告警
    CG("000", "三维-串岗"),
    LG("001", "三维-离岗"),
    JZ("002", "三维-静止"),
    CY("003", "三维-超员"),
    QY("004", "三维-缺员"),
    LX("005", "三维-离线"),
    YJJR("006", "三维-越界进入"),
    YJLK("007", "三维-越界离开"),
    ZL("008", "三维-滞留"),
    SOS("012", "三维-SOS求救"),
    DDL("013", "三维-低电量");

    private final String code;
    private final String name;

    AlarmConfigEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据 code 查找枚举
     */
    public static AlarmConfigEnum fromCode(String code) {
        for (AlarmConfigEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
