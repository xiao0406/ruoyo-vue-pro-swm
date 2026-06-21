package cn.iocoder.yudao.module.swm.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作工具类
 *
 * 迁移自 JeeSite: com.jeesite.modules.utils.BatchOperationsUtil
 */
public class BatchOperationsUtil {

    /**
     * 按照固定等份切割集合
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
