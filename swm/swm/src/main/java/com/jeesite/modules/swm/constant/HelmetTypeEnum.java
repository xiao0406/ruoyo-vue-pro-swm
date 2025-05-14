package com.jeesite.modules.swm.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 头盔类型枚举
 * 
 * @author Shawn
 */
public enum HelmetTypeEnum {

    PORTABLE(1, "便携式"),
    HEADBAND(2, "头箍式");

    private final int code;
    private final String description;

    HelmetTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    @JsonCreator
    public static HelmetTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (HelmetTypeEnum type : HelmetTypeEnum.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    public static String getDescriptionByCode(Integer code) {
        HelmetTypeEnum type = fromCode(code);
        return type != null ? type.getDescription() : null;
    }
}