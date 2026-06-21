package cn.iocoder.yudao.module.swm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 租户 ID 与 TDengine 数据库映射枚举
 *
 * 迁移自 JeeSite: com.jeesite.modules.enums.CorpDbEnum
 * 已将 corpCode 替换为 tenantId，与 RuoYi-Vue-Pro 的多租户体系对齐。
 *
 * 租户 ID 映射关系（需与 sys_tenant 表一致）：
 * - 1: ZJGGGD  中建钢构广东有限公司
 * - 2: ZJGGJS  中建钢构江苏有限公司
 * - 3: ZJGGSC  中建钢构四川有限公司
 * - 4: ZJZK    中建科工
 * - 5: ZHY     天津中海油
 * - 6: DFXCYY  东方新材
 */
@Getter
@AllArgsConstructor
public enum CorpDbEnum {

    ZJGGGD(1L, "ZJGGGD", "plb"),
    ZJGGJS(2L, "ZJGGJS", "plb_ZJGGJS"),
    ZJGGSC(3L, "ZJGGSC", "plb_ZJGGSC"),
    ZJZK(4L, "ZJZK", "plb_ZJZK"),
    ZHY(5L, "ZHY", "plb_ZHY"),
    DFXCYY(6L, "DFXCYY", "plb_DFXCYY");

    private final Long tenantId;
    private final String corpCode;
    private final String dbName;

    private static final Map<Long, CorpDbEnum> TENANT_MAP = new HashMap<>();
    private static final Map<String, CorpDbEnum> CORP_MAP = new HashMap<>();

    static {
        for (CorpDbEnum e : values()) {
            TENANT_MAP.put(e.getTenantId(), e);
            CORP_MAP.put(e.getCorpCode(), e);
        }
    }

    /**
     * 根据租户 ID 获取数据库名称
     */
    public static String getDbNameByTenantId(Long tenantId) {
        CorpDbEnum e = TENANT_MAP.get(tenantId);
        return e != null ? e.getDbName() : null;
    }

    /**
     * 根据企业编码获取数据库名称（兼容旧代码）
     */
    public static String getDbNameByCorpCode(String corpCode) {
        CorpDbEnum e = CORP_MAP.get(corpCode);
        return e != null ? e.getDbName() : null;
    }

    /**
     * 根据企业编码获取租户 ID
     */
    public static Long getTenantIdByCorpCode(String corpCode) {
        CorpDbEnum e = CORP_MAP.get(corpCode);
        return e != null ? e.getTenantId() : null;
    }

    /**
     * 获取所有租户 ID - 数据库名映射
     */
    public static Map<Long, String> getAllTenantDbMapping() {
        Map<Long, String> map = new HashMap<>();
        for (CorpDbEnum e : values()) {
            map.put(e.getTenantId(), e.getDbName());
        }
        return map;
    }

    /**
     * Legacy corpCode -> database mapping for migrated JeeSite code.
     */
    public static Map<String, String> getAllCorpDbMapping() {
        Map<String, String> map = new HashMap<>();
        for (CorpDbEnum e : values()) {
            map.put(e.getCorpCode(), e.getDbName());
        }
        return map;
    }

}
