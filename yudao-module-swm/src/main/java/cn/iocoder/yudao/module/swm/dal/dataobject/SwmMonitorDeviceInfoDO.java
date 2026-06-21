package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 监控设备信息 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmMonitorDeviceInfo
 * 表: js_sys_monitor_device_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("js_sys_monitor_device_info")
public class SwmMonitorDeviceInfoDO extends SwmBaseDO {

    private String recType;
    private String parentId;
    private String treeSort;
    private String treeLeaf;
    private String parentName;
    private String name;
    private String code;
    private String deviceType;
    private String ip;
    private String rtspPort;
    private String adminUser;
    private String adminPassword;
    private String rtspUri;
    private String channel;
    private String subtype;
    private String loadSource;
    private String streamUrl;
    private String cameraIndexCode;

}
