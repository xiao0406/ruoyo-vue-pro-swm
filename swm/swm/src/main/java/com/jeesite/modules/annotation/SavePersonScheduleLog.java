package com.jeesite.modules.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SavePersonScheduleLog {
    String remarkPrefix() default "修改人员班次：目标班次"; // 调整默认备注，适配单/批量场景
}
