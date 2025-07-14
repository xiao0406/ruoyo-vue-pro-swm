/**
 * @author Shawn
 * @date 2025-01-15
 */
package com.jeesite.modules.swm.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 人员登记Excel导入增强版模型（支持组织架构字段）
 */
@Data
public class SwmPersonExcelEnhancedModel {

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

    @ExcelProperty("是否场内员工")
    private String isExternalPersonnel;

    @ExcelProperty("所属单位")
    private String company;

    @ExcelProperty("所属车间")
    private String department;

    @ExcelProperty("所属产线")
    private String prodLine;

    @ExcelProperty("所属班组")
    private String team;

    @ExcelProperty("所属工种")
    private String jobType;

    @ExcelProperty("备注")
    private String remarks;

    // 错误信息（不会导出到模板中）
    private String errorMsg;

    // 行号（用于错误定位）
    private Integer rowIndex;

    /**
     * 检查是否为厂内员工
     */
    public boolean isInternalPersonnel() {
        if (isExternalPersonnel == null)
            return false;
        String value = isExternalPersonnel.trim().toLowerCase();
        return "是".equals(value) || "1".equals(value) || "true".equals(value);
    }

    /**
     * 获取标准化的是否场内员工值
     */
    public String getStandardizedExternalPersonnel() {
        return isInternalPersonnel() ? "1" : "0";
    }

    /**
     * 检查组织架构字段是否填写完整
     */
    public boolean hasCompleteOrgInfo() {
        return isNotBlank(company) && isNotBlank(department)
                && isNotBlank(prodLine) && isNotBlank(team) && isNotBlank(jobType);
    }

    /**
     * 清空组织架构字段
     */
    public void clearOrgInfo() {
        this.company = null;
        this.department = null;
        this.prodLine = null;
        this.team = null;
        this.jobType = null;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}