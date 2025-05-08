package com.jeesite.modules.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作工具类
 * @author 龚加林
 * @2024-4-29
 */
public class BatchOperationsUtil {
    /**
     * 按照固定等份切割集合
     * @param
     * @return
     */
    public static <T> List<List<T>> batchCutting(List<T> list, int chunkSize) {
        List<List<T>> batchData = new ArrayList<>();
        int startIndex = 0;
        while (startIndex < list.size()) {
            int endIndex = Math.min(startIndex + chunkSize, list.size());
            List<T> subList = list.subList(startIndex, endIndex);
            batchData.add(subList);
            startIndex += chunkSize;
        }
        return batchData;
    }
}
