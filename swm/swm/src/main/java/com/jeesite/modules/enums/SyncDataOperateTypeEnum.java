package com.jeesite.modules.enums;

import lombok.Getter;

/**
 * 同步数据操作类型枚举
 *
 * @author fangxiaolong
 * @date 2026-01-21
 */
@Getter
public enum SyncDataOperateTypeEnum {

    // ===================== 设备操作类型（HELMET_前缀） =====================
    /** 新增安全帽设备 */
    HELMET_ADD("HELMET_ADD", "新增安全帽设备"),
    /** 编辑安全帽设备 */
    HELMET_EDIT("HELMET_EDIT", "编辑安全帽设备"),
    /** 解绑安全帽设备 */
    HELMET_UNBIND("HELMET_UNBIND", "解绑安全帽设备"),
    /** 删除安全帽设备 */
    HELMET_DELETE("HELMET_DELETE", "删除安全帽设备"),
    /** 批量导入安全帽设备 */
    HELMET_IMPORT("HELMET_IMPORT", "批量导入安全帽设备"),
    /** 全量同步安全帽设备 */
    HELMET_FULL_SYNC("HELMET_FULL_SYNC", "全量同步安全帽设备"), // 修正原有名称错误

    // ===================== 人员操作类型（PERSON_前缀） =====================
    /** 批量删除人员 */
    PERSON_BATCH_DELETE("PERSON_BATCH_DELETE", "批量删除人员"),
    /** 批量导入人员 */
    PERSON_IMPORT("PERSON_IMPORT", "批量导入人员"),
    /** 批量完成安全教育 */
    PERSON_COMPLETE_EDUCATION("PERSON_COMPLETE_EDUCATION", "批量完成安全教育"),
    /** 批量绑定安全帽 */
    PERSON_BIND_HELMET("PERSON_BIND_HELMET", "批量绑定安全帽"),
    /** 新增人员 */
    PERSON_ADD("PERSON_ADD", "新增人员"),
    /** 编辑人员 */
    PERSON_EDIT("PERSON_EDIT", "编辑人员"),
    /** 人员离职 */
    PERSON_DEPARTURE("PERSON_DEPARTURE", "人员离职"),
    /** 全量同步人员 */
    PERSON_FULL_SYNC("PERSON_FULL_SYNC", "全量同步人员"),

    // ===================== 信标操作类型（BEACON_前缀） =====================
    /** 新增信标 */
    BEACON_ADD("BEACON_ADD", "新增信标"),
    /** 编辑信标 */
    BEACON_EDIT("BEACON_EDIT", "编辑信标"),
    /** 删除信标 */
    BEACON_DELETE("BEACON_DELETE", "删除信标"),
    /** 批量导入信标 */
    BEACON_IMPORT("BEACON_IMPORT", "批量导入信标"),
    /** 全量同步信标 */
    BEACON_FULL_SYNC("BEACON_FULL_SYNC", "全量同步信标");

    /** 操作类型编码 */
    private final String code;

    /** 操作类型名称 */
    private final String name;

    SyncDataOperateTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据编码获取枚举
     */
    public static SyncDataOperateTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SyncDataOperateTypeEnum operateType : values()) {
            if (operateType.getCode().equals(code)) {
                return operateType;
            }
        }
        return null;
    }

}