package com.jeesite.modules.config;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SwitchCorp {
    /**
     * 是否启用租户切换，默认 true
     */
    boolean enable() default true;
}
