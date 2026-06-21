package cn.iocoder.yudao.module.swm.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段工具类 - 通过 Lambda 方法引用获取字段名
 *
 * 迁移自 JeeSite: com.jeesite.modules.utils.FieldUtil
 */
public class FieldUtil {

    private static final Map<FieldFunction<?>, Field> FUNCTION_CACHE = new ConcurrentHashMap<>();

    public static <T> String getName(FieldFunction<T> function) {
        Field field = getField(function);
        return field.getName();
    }

    public static <T> String getULName(FieldFunction<T> function) {
        Field field = getField(function);
        return StrUtil.toUnderlineCase(field.getName());
    }

    public static <T> Field getField(FieldFunction<T> function) {
        return FUNCTION_CACHE.computeIfAbsent(function, FieldUtil::findField);
    }

    public static <T> Field findField(FieldFunction<T> function) {
        final SerializedLambda serializedLambda = getSerializedLambda(function);
        final String implClass = serializedLambda.getImplClass();
        final String implMethodName = serializedLambda.getImplMethodName();
        final String fieldName = convertToFieldName(implMethodName);
        final Field field = getField(fieldName, serializedLambda);
        if (field == null) {
            throw new RuntimeException("No such class 「" + implClass + "」 field 「" + fieldName + "」.");
        }
        return field;
    }

    static Field getField(String fieldName, SerializedLambda serializedLambda) {
        try {
            String declaredClass = serializedLambda.getImplClass().replace("/", ".");
            Class<?> aClass = Class.forName(declaredClass, false, ClassUtils.getDefaultClassLoader());
            return ReflectionUtils.findField(aClass, fieldName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("get class field exception.", e);
        }
    }

    static String convertToFieldName(String getterMethodName) {
        String prefix = null;
        if (getterMethodName.startsWith("get")) {
            prefix = "get";
        } else if (getterMethodName.startsWith("is")) {
            prefix = "is";
        }
        if (prefix == null) {
            throw new IllegalArgumentException("invalid getter method: " + getterMethodName);
        }
        return Introspector.decapitalize(getterMethodName.replace(prefix, ""));
    }

    static <T> SerializedLambda getSerializedLambda(FieldFunction<T> function) {
        try {
            Method method = function.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            return (SerializedLambda) method.invoke(function);
        } catch (Exception e) {
            throw new RuntimeException("get SerializedLambda exception.", e);
        }
    }

    public static Class<?>[] getParameterTypes(Object... args) {
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        return classes;
    }

}
