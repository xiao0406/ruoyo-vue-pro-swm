//package com.jeesite.modules.utils;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.util.ReflectUtil;
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONObject;
//import com.jeesite.common.entity.DataEntity;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * @Author：cuihu
// * @Package：com.jeesite.modules.utils
// * @Project：cscec-jessite-cloud-qms1
// * @name：JsonObjectConverter
// * @Date：2024/11/7 9:24
// */
//@Component
//public class JsonObjectConverter<T> {
//
////    public static JSONObject convertData(Object param, String... ignoreField) throws IllegalAccessException {
////        JSONObject jsonObject = new JSONObject();
////        if (param!= null) {
////            List<String> ignoreFieldNames = Arrays.asList(ignoreField);
////            Class<?> clazz = param.getClass();
////            Field[] fields = clazz.getDeclaredFields();
////            for (Field field : fields) {
////                field.setAccessible(true);
////                if (!ignoreFieldNames.contains(field.getName()) && field.get(param)!= null) {
////                    jsonObject.put(field.getName(), field.get(param));
////                }
////            }
////        }
////        return jsonObject;
////    }
//    public JSONObject convertData(T param, String... ignoreField) {
//        JSONObject jsonObject = new JSONObject();
//        if (param != null) {
//            List<String> ignoreFieldNames = Arrays.asList(ignoreField);
//            java.util.Map<String, Object> beanMap = BeanUtil.beanToMap(param, false, true);
//            for (java.util.Map.Entry<String, Object> entry : beanMap.entrySet()) {
//                if (!ignoreFieldNames.contains(entry.getKey()) && entry.getValue() != null) {
//                    jsonObject.put(entry.getKey(), entry.getValue());
//                }
//            }
//        }
//        return jsonObject;
//    }
//    public <T> List<T> convertJsonArray(JSONArray jsonArray, Class<T> targetClass) {
//        List<T> resultList = new ArrayList<>();
//        if (CollUtil.isNotEmpty(jsonArray)) {
//            for (Object obj : jsonArray) {
//                if (obj instanceof JSONObject) {
//                    JSONObject json = (JSONObject) obj;
//                    T instance = ReflectUtil.newInstanceIfPossible(targetClass);
//                    if (instance != null) {
//                        // 将 JSONObject 的属性设置到实例对象中
//                        for (String key : json.keySet()) {
//                            if (ReflectUtil.hasField(targetClass, key)) {
//                                ReflectUtil.setFieldValue(instance, key, json.get(key));
//                            }
//                        }
//                        resultList.add(instance);
//                    }
//                }
//            }
//        }
//        return resultList;
//    }
//}
