package com.jeesite.modules.utils.excelConverter;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.jeesite.common.utils.SpringUtils;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.service.DictDataService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Author：cuihu
 * @Package：com.jeesite.modules.utils
 * @Project：cscec-jessite-cloud-qms1
 * @name：ExcelConverter
 * @Date：2024/11/4 18:47
 */
public class DictDataConverter implements Converter<String> {
    private Map<String, List<DictData>> name_valueMap;
    private static final DictDataService dictDataService = SpringUtils.getBean(DictDataService.class);

    @Override
    public Class supportJavaTypeKey() {
        return DictData.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        DictData dictData = new DictData();
        String searchString = cellData.getStringValue();
        if(CollUtil.isEmpty(name_valueMap)){
            DictData dictDataParam = new DictData();
            dictDataParam.setDictType("qms_qualified");
            List<DictData> list = dictDataService.findList(dictDataParam);
            name_valueMap = list.stream().collect(Collectors.groupingBy(DictData::getDictLabel));
        }
        if (name_valueMap.keySet().contains(searchString)) {
            List<DictData> item = name_valueMap.get(searchString);
            return item.get(0).getDictValue();
        }
        dictData.setDictLabel(searchString);
        return null;
    }

    @Override
    public WriteCellData<String> convertToExcelData(String value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        return new WriteCellData<>(value);
    }
}
