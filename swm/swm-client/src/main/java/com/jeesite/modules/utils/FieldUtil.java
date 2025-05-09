package com.jeesite.modules.utils;

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
 * @Author：cuihu
 * @Package：com.jeesite.modules.utils
 * @Project：cscec-jessite-cloud-qms1
 * @name：FieldUtil
 * @Date：2024/10/9 9:22
 */
public class FieldUtil {
    private static final Map<FieldFunction<?>, Field> FUNCTION_CACHE = new ConcurrentHashMap<>();

    public static <T> String getName(FieldFunction<T> function) {
        Field field = FieldUtil.getField(function);
        return field.getName();
    }
    public static <T> String getULName(FieldFunction<T> function) {
        Field field = FieldUtil.getField(function);
        return StrUtil.toUnderlineCase(field.getName());
    }

    public static <T> Field getField(FieldFunction<T> function) {
        return FUNCTION_CACHE.computeIfAbsent(function, FieldUtil::findField);
    }

    public static <T> Field findField(FieldFunction<T> function) {
        // 第1步 获取SerializedLambda
        final SerializedLambda serializedLambda = getSerializedLambda(function);
        // 第2步 implMethodName 即为Field对应的Getter方法名
        final String implClass = serializedLambda.getImplClass();
        final String implMethodName = serializedLambda.getImplMethodName();
        final String fieldName = convertToFieldName(implMethodName);
        // 第3步  Spring 中的反射工具类获取Class中定义的Field
        final Field field = getField(fieldName, serializedLambda);
        // 第4步 如果没有找到对应的字段应该抛出异常
        if (field == null) {
            throw new RuntimeException("No such class 「" + implClass + "」 field 「" + fieldName + "」.");
        }
        return field;
    }

    static Field getField(String fieldName, SerializedLambda serializedLambda) {
        try {            // 获取的Class是字符串，并且包名是“/”分割，需要替换成“.”，才能获取到对应的Class对象
            String declaredClass = serializedLambda.getImplClass().replace("/", ".");
            Class<?> aClass = Class.forName(declaredClass, false, ClassUtils.getDefaultClassLoader());
            return ReflectionUtils.findField(aClass, fieldName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("get class field exception.", e);
        }
    }

    static String convertToFieldName(String getterMethodName) {
        // 获取方法名
        String prefix = null;
        if (getterMethodName.startsWith("get")) {
            prefix = "get";
        } else if (getterMethodName.startsWith("is")) {
            prefix = "is";
        }
        if (prefix == null) {
            throw new IllegalArgumentException("invalid getter method: " + getterMethodName);
        }
        // 截取get/is之后的字符串并转换首字母为小写
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
