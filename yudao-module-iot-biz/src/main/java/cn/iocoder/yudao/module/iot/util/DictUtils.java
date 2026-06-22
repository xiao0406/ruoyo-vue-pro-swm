package cn.iocoder.yudao.module.iot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字典工具类（兼容 JeeSite DictUtils 接口）
 * TODO: 后续接入 Yudao 的 DictDataService 实现真正的字典查询
 */
public class DictUtils {

    private static final Logger log = LoggerFactory.getLogger(DictUtils.class);

    /**
     * 获取字典标签（兼容 JeeSite）
     *
     * @param dictType  字典类型
     * @param dictValue 字典值
     * @param defaultLabel 默认标签
     * @return 字典标签
     */
    public static String getDictLabel(String dictType, String dictValue, String defaultLabel) {
        // TODO: 实际接入 Yudao 字典服务
        log.debug("DictUtils.getDictLabel 调用, dictType={}, dictValue={}, 返回默认值={}", dictType, dictValue, defaultLabel);
        return defaultLabel;
    }
}
