package com.jeesite.modules.entity;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import lombok.Data;

@Data
public class SwmPersonExport {

    @ExcelFields({
            @ExcelField(title = "姓名", attrName = "name", align = ExcelField.Align.CENTER, sort = 10, width = 256 * 30),
            @ExcelField(title = "身份证号码", attrName = "identityCard", align = ExcelField.Align.CENTER, sort = 10, width = 256 * 30),
            @ExcelField(title = "人员编码", attrName = "personNumber", align = ExcelField.Align.CENTER, sort = 20, width = 256 * 30),
            @ExcelField(title = "年龄", attrName = "age", align = ExcelField.Align.CENTER, sort = 20, width = 256 * 30),
            @ExcelField(title = "紧急联系人", attrName = "urgentPerson", align = ExcelField.Align.CENTER, sort = 20, width = 256 * 30),
            @ExcelField(title = "紧急联系人手机号", attrName = "urgentPhoneNumber", align = ExcelField.Align.CENTER, sort = 20, width = 256 * 30),
    })

    public SwmPersonExport() {
    }

    private String name;
    private String identityCard;
    private String personNumber;
    private String age;
    private String urgentPerson;
    private String urgentPhoneNumber;


}
