package cn.iocoder.yudao.module.iot.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Dictionary lookup helper for migrated IOT code.
 *
 * <p>Old JeeSite callers use static methods, so this class keeps that shape while delegating to
 * RuoYi's system dictionary API.
 */
@Slf4j
@Component
public class DictUtils {

    private static DictDataApi dictDataApi;

    @Autowired
    public void setDictDataApi(DictDataApi dictDataApi) {
        DictUtils.dictDataApi = dictDataApi;
    }

    public static String getDictLabel(String dictType, String dictValue, String defaultLabel) {
        if (dictDataApi == null || StrUtil.hasBlank(dictType, dictValue)) {
            return defaultLabel;
        }
        try {
            List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
            return list.stream()
                    .filter(DictUtils::enabled)
                    .filter(item -> Objects.equals(item.getValue(), dictValue))
                    .map(DictDataRespDTO::getLabel)
                    .filter(StrUtil::isNotBlank)
                    .findFirst()
                    .orElse(defaultLabel);
        } catch (Exception ex) {
            log.warn("Lookup dict label failed, dictType={}, dictValue={}", dictType, dictValue, ex);
            return defaultLabel;
        }
    }

    public static String getDictValue(String dictType, String dictLabel, String defaultValue) {
        if (dictDataApi == null || StrUtil.hasBlank(dictType, dictLabel)) {
            return defaultValue;
        }
        try {
            List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
            return list.stream()
                    .filter(DictUtils::enabled)
                    .filter(item -> Objects.equals(item.getLabel(), dictLabel))
                    .map(DictDataRespDTO::getValue)
                    .filter(StrUtil::isNotBlank)
                    .findFirst()
                    .orElse(defaultValue);
        } catch (Exception ex) {
            log.warn("Lookup dict value failed, dictType={}, dictLabel={}", dictType, dictLabel, ex);
            return defaultValue;
        }
    }

    private static boolean enabled(DictDataRespDTO item) {
        return item != null && Objects.equals(item.getStatus(), CommonStatusEnum.ENABLE.getStatus());
    }
}
