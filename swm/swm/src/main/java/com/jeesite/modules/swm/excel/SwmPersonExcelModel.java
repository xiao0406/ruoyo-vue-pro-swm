/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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

    @ExcelProperty("身份证号码")
    private String identityCard;

    @ExcelProperty("手机号码")
    private String phoneNumber;

    @ExcelProperty("备注")
    private String remarks;

    @ExcelProperty("安全帽编码")
    private String safetyHelmetCode;

    // 错误信息（不会导出到模板中）
    private String errorMsg;

    /**
     * 去除各字段前后空格，确保导入数据一致性
     */
    public void trimAll() {
        name = trim(name);
        personType = trim(personType);
        gender = trim(gender);
        identityCard = trim(identityCard);
        phoneNumber = trim(phoneNumber);
        remarks = trim(remarks);
        safetyHelmetCode = trim(safetyHelmetCode);
    }

    /**
     * 获取去除首尾空格的安全帽编码
     */
    public String getTrimmedSafetyHelmetCode() {
        if (safetyHelmetCode == null) {
            return null;
        }
        String value = safetyHelmetCode.trim();
        return value.isEmpty() ? null : value;
    }

    private String trim(String value) {
        return value == null ? null : StringUtils.trim(value);
    }
}
