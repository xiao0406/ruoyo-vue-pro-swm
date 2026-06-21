package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 区域管理 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.entity.SwmArea
 * 表: swm_area
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_area")
public class SwmAreaDO extends SwmBaseDO {

    /** 区域名称 */
    private String areaName;
    /** 区域类型 */
    private String areaType;
    /** 区域颜色 */
    private String areaColor;
    /** 车间ID */
    private String workShop;
    /** 语音提示 */
    private String voicePrompt;
    /** 文件路径 */
    private String filePath;
    /** 关联的信标ID列表 */
    private String bIds;
    /** 是否大屏展示 */
    private Boolean isScreenShow;

}
