package com.jeesite.modules.swm.excel;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SwmPersonDtoExport {

    @ExcelFields({
            @ExcelField(title="人员编码", attrName = "personNumber", align = ExcelField.Align.CENTER, sort = 10),
            @ExcelField(title="姓名", attrName = "name",align = ExcelField.Align.CENTER, sort = 20),
            @ExcelField(title="性别", attrName = "gender",align = ExcelField.Align.CENTER, sort = 30),
            @ExcelField(title="手机号码", attrName = "phoneNumber",align = ExcelField.Align.CENTER, sort = 40),
            @ExcelField(title="是否厂内员工", attrName = "isExternalPersonnel",align = ExcelField.Align.CENTER,dictType = "external_personnel_enum", sort = 50),
            @ExcelField(title="人员类型", attrName = "personType",align = ExcelField.Align.CENTER, sort = 60),
            @ExcelField(title="所属单位", attrName = "company",align = ExcelField.Align.CENTER, sort = 70),
            @ExcelField(title="所属车间", attrName = "department",align = ExcelField.Align.CENTER, sort = 80),
            @ExcelField(title="所属产线", attrName = "prodLine",align = ExcelField.Align.CENTER, sort = 90),
            @ExcelField(title="所属班组", attrName = "team",align = ExcelField.Align.CENTER,sort = 100),
            @ExcelField(title="所属工种", attrName = "jobType",align = ExcelField.Align.CENTER,sort = 110),
            @ExcelField(title="安全帽编码", attrName = "safetyHelmetId",align = ExcelField.Align.CENTER, sort = 120),
            @ExcelField(title="安全帽状态", attrName = "powerOnStatus",align = ExcelField.Align.CENTER,dictType = "swm_power_on_status", sort = 130),
            @ExcelField(title="人员状态", attrName = "personnelStatus",align = ExcelField.Align.CENTER, dictType = "person_status_enum",sort = 140),
    })


    public SwmPersonDtoExport() {

    }

    @ApiModelProperty(value = "人员编码")
    private String personNumber;
    private String name; // 姓名
    private String gender; // 性别
    private String phoneNumber; // 手机号码
    private String isExternalPersonnel; // 是否厂内员工
    private String personType; // 人员类型
    private String company; // 所属单位
    private String department; // 所属车间
    private String prodLine; // 产线
    private String workProcess; // 所属工序
    private String team; // 所属班组
    private String jobType; // 所属工种
    private String safetyHelmetId; // 关联安全帽编号
    private String personnelStatus; // 人员状态
    @ApiModelProperty(value = "开机状态  0-开机，1-关机")
    private String powerOnStatus;
}
