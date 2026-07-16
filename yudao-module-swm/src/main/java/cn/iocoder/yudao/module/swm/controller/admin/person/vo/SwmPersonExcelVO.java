package cn.iocoder.yudao.module.swm.controller.admin.person.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 人员导入导出模型。
 *
 * 字段名称保持与原 SWM 人员模板一致，组织字段保存对应的业务编码或主键。
 */
@Data
public class SwmPersonExcelVO {

    @ExcelProperty("人员编码")
    private String personNumber;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("年龄")
    private String age;

    @ExcelProperty("身份证号码")
    private String identityCard;

    @ExcelProperty("手机号码")
    private String phoneNumber;

    @ExcelProperty("人员类型")
    private String personType;

    @ExcelProperty("是否厂内员工")
    private String isExternalPersonnel;

    @ExcelProperty("所属单位")
    private String company;

    @ExcelProperty("所属车间")
    private String department;

    @ExcelProperty("所属产线")
    private String prodLine;

    @ExcelProperty("所属班组")
    private String team;

    @ExcelProperty("工种")
    private String jobType;

    @ExcelProperty("安全帽编号")
    private String safetyHelmetId;

    @ExcelProperty("人员状态")
    private String personnelStatus;

    @ExcelProperty("入场安全教育")
    private String safetyEducation;

    @ExcelProperty("紧急联系人")
    private String urgentPerson;

    @ExcelProperty("紧急联系人手机号")
    private String urgentPhoneNumber;

    @ExcelProperty("血型")
    private String bloodType;

    @ExcelProperty("备注")
    private String remarks;
}
