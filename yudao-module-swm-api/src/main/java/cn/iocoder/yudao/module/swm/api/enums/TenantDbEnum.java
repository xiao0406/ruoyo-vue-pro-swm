package cn.iocoder.yudao.module.swm.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum TenantDbEnum {

    ZJGGGD(1L, "ZJGGGD", "plb"),
    ZJGGJS(2L, "ZJGGJS", "plb_ZJGGJS"),
    ZJGGSC(3L, "ZJGGSC", "plb_ZJGGSC"),
    ZJZK(4L, "ZJZK", "plb_ZJZK"),
    ZHY(5L, "ZHY", "plb_ZHY"),
    DFXCYY(6L, "DFXCYY", "plb_DFXCYY");

    private final Long tenantId;
    /**
     * Legacy project code used only at integration boundaries such as algorithm URL dictionaries.
     */
    private final String tenantCode;
    private final String dbName;

    private static final Map<Long, TenantDbEnum> TENANT_MAP = new HashMap<>();

    static {
        for (TenantDbEnum value : values()) {
            TENANT_MAP.put(value.getTenantId(), value);
        }
    }

    public static String getDbNameByTenantId(Long tenantId) {
        TenantDbEnum value = TENANT_MAP.get(tenantId);
        return value == null ? null : value.getDbName();
    }

    public static String getTenantCodeByTenantId(Long tenantId) {
        TenantDbEnum value = TENANT_MAP.get(tenantId);
        return value == null ? null : value.getTenantCode();
    }

    public static Map<Long, String> getAllTenantDbMapping() {
        Map<Long, String> map = new HashMap<>();
        for (TenantDbEnum value : values()) {
            map.put(value.getTenantId(), value.getDbName());
        }
        return map;
    }
}
