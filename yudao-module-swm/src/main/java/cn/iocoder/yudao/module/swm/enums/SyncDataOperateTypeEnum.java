package cn.iocoder.yudao.module.swm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据同步操作类型枚举
 *
 * 迁移自 JeeSite: com.jeesite.modules.enums.SyncDataOperateTypeEnum
 */
@Getter
@AllArgsConstructor
public enum SyncDataOperateTypeEnum {

    // ========== 安帽设备操作 ==========
    HELMET_ADD("HELMET_ADD", "新增安全帽"),
    HELMET_EDIT("HELMET_EDIT", "编辑安全帽"),
    HELMET_UNBIND("HELMET_UNBIND", "解绑安全帽"),
    HELMET_DELETE("HELMET_DELETE", "删除安全帽"),
    HELMET_IMPORT("HELMET_IMPORT", "导入安全帽"),
    HELMET_FULL_SYNC("HELMET_FULL_SYNC", "全量同步安全帽"),

    // ========== 人员操作 ==========
    PERSON_BATCH_DELETE("PERSON_BATCH_DELETE", "批量删除人员"),
    PERSON_IMPORT("PERSON_IMPORT", "导入人员"),
    PERSON_COMPLETE_EDUCATION("PERSON_COMPLETE_EDUCATION", "完成安全教育"),
    PERSON_BIND_HELMET("PERSON_BIND_HELMET", "绑定安全帽"),
    PERSON_ADD("PERSON_ADD", "新增人员"),
    PERSON_EDIT("PERSON_EDIT", "编辑人员"),
    PERSON_DEPARTURE("PERSON_DEPARTURE", "人员退场"),
    PERSON_FULL_SYNC("PERSON_FULL_SYNC", "全量同步人员"),

    // ========== 信标操作 ==========
    BEACON_ADD("BEACON_ADD", "新增信标"),
    BEACON_EDIT("BEACON_EDIT", "编辑信标"),
    BEACON_DELETE("BEACON_DELETE", "删除信标"),
    BEACON_IMPORT("BEACON_IMPORT", "导入信标"),
    BEACON_FULL_SYNC("BEACON_FULL_SYNC", "全量同步信标");

    private final String code;
    private final String description;

}
