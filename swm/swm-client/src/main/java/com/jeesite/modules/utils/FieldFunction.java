package com.jeesite.modules.utils;

import java.io.Serializable;

/**
 * @Author：cuihu
 * @Package：com.jeesite.modules.utils
 * @Project：cscec-jessite-cloud-qms1
 * @name：FieldFunction
 * @Date：2024/10/9 9:21
 */

@FunctionalInterface
public interface FieldFunction <T> extends Serializable {

    Object apply(T t);
}
