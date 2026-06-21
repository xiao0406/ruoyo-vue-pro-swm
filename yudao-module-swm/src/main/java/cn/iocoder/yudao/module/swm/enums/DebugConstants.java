package cn.iocoder.yudao.module.swm.enums;

/**
 * 调试模式常量
 *
 * 迁移自 JeeSite: com.jeesite.modules.constant.DebugConstant
 */
public class DebugConstants {

    /**
     * 是否开启调试模式
     */
    public static volatile boolean open = false;

    /**
     * 调试设备编号（仅在 open=true 时生效）
     */
    public static volatile String deviceNum = "";

}
