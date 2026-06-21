package cn.iocoder.yudao.module.swm.util;

/**
 * Lambda 字段引用函数式接口
 *
 * 用于通过方法引用获取字段名，替代硬编码的字符串。
 * 迁移自 JeeSite: com.jeesite.modules.utils.FieldFunction
 */
@FunctionalInterface
public interface FieldFunction<T> extends java.io.Serializable {

    Object apply(T source);

}
