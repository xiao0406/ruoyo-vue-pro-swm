package com.jeesite.modules.enums;


import java.util.HashMap;
import java.util.Map;

public enum CorpDbEnum {

    ZJGGJS("ZJGGJS", "plb_ZJGGJS"),
    ZJGGGD("ZJGGGD", "plb"),
    ZJGGSC("ZJGGSC", "plb_ZJGGSC"),
    ZJZK("ZJZK", "plb_ZJZK"),
    ZHY("ZHY", "plb_ZHY");

    private final String corpCode;
    private final String dbName;

    CorpDbEnum(String corpCode, String dbName) {
        this.corpCode = corpCode;
        this.dbName = dbName;
    }

    public String getCorpCode() {
        return corpCode;
    }

    public String getDbName() {
        return dbName;
    }

    // 根据 corpCode 获取 dbName
    private static final Map<String, String> CORP_DB_MAP = new HashMap<>();

    static {
        for (CorpDbEnum e : values()) {
            CORP_DB_MAP.put(e.getCorpCode(), e.getDbName());
        }
    }

    public static String getDbNameByCorpCode(String corpCode) {
        if (corpCode == null) {
            throw new IllegalArgumentException("corpCode 不能为空");
        }

        String normalized = corpCode.trim().toUpperCase();
        String dbName = CORP_DB_MAP.get(normalized);

        if (dbName == null) {
            return CorpDbEnum.ZJZK.getDbName();
        }

        return dbName;
    }


    /**
     * 遍历所有映射
     */
    public static Map<String, String> getAllCorpDbMapping() {
        return new HashMap<>(CORP_DB_MAP);
    }
}


