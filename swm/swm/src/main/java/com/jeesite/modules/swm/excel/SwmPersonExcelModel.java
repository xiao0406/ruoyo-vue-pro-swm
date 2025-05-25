/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 人员登记Excel导入模型
 */
@Data
public class SwmPersonExcelModel {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("人员类型")
    private String personType;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("所属单位")
    private String company;

    @ExcelProperty("所属车间")
    private String department;

    @ExcelProperty("所属工序")
    private String workProcess;

    @ExcelProperty("所属班组")
    private String team;

    @ExcelProperty("所属工种")
    private String jobType;

    @ExcelProperty("身份证号码")
    private String identityCard;

    @ExcelProperty("手机号码")
    private String phoneNumber;

    @ExcelProperty("备注")
    private String remarks;

    // 错误信息（不会导出到模板中）
    private String errorMsg;
}