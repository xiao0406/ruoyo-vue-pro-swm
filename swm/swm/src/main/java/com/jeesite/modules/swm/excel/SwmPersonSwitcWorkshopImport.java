package com.jeesite.modules.swm.excel;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import lombok.Data;

@Data
public class SwmPersonSwitcWorkshopImport {

    @ExcelFields({
            @ExcelField(title = "姓名", attrName = "name", align = ExcelField.Align.CENTER, sort = 10),
            @ExcelField(title = "身份证号", attrName = "identityCard", align = ExcelField.Align.CENTER, sort = 20),
            @ExcelField(title = "车间", attrName = "department", align = ExcelField.Align.CENTER, sort = 30),
            @ExcelField(title = "产线", attrName = "prodLine", align = ExcelField.Align.CENTER, sort = 40),
            @ExcelField(title = "班组", attrName = "team", align = ExcelField.Align.CENTER, sort = 50),
    })

    public SwmPersonSwitcWorkshopImport() {
    }

    private String name; // 姓名
    private String identityCard; // 身份证号码
    private String department; // 所属车间
    private String prodLine; // 产线
    private String team; // 所属班组
}
