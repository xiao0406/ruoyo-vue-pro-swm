package com.jeesite.modules.entity;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import lombok.Data;

@Data
public class SwmPersonScheduleExport {

    @ExcelFields({
            @ExcelField(title = "姓名", attrName = "personName", align = ExcelField.Align.CENTER, sort = 10, width = 256 * 20),
            @ExcelField(title = "身份证号码", attrName = "idCard", align = ExcelField.Align.CENTER, sort = 10, width = 256 * 40),
            @ExcelField(title = "班次", attrName = "classes", align = ExcelField.Align.CENTER, sort = 20, width = 256 * 15,dictType = "shift_type_enum"),
    })

    public SwmPersonScheduleExport() {
    }

    private String personName;
    private String idCard;
    private String classes;
    private String month; // 排班月份

}
