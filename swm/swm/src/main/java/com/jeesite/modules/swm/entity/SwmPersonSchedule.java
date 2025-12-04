package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 人员排班表实体类
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Table(name = "swm_person_schedule", alias = "a", label = "人员排班表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "person_name", attrName = "personName", label = "人员姓名", queryType = QueryType.LIKE),
        @Column(name = "month", attrName = "month", label = "排班月份", queryType = QueryType.EQ),
        @Column(name = "classes", attrName = "classes", label = "班次"),
        @Column(name = "id_card", attrName = "idCard", label = "身份证号码", queryType = QueryType.LIKE),
        @Column(name = "employee_id", attrName = "employeeId", label = "员工ID"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmPersonSchedule extends DataEntity<SwmPersonSchedule> {

    private static final long serialVersionUID = 1L;

    private String personName; // 人员姓名
    private String month; // 排班月份
    private String classes; // 班次
    private String idCard; // 身份证号码
    private String employeeId; // 员工ID

    // 用于显示的文本属性，不对应数据库字段
    private String classesText; // 班次显示文本
    private String workGroupName; // 班组名称（非数据库字段）
    private String personId; // 前端传入的personId（非数据库字段，用于设置employeeId）

    // 组织架构查询字段（非数据库字段，用于查询条件）
    // Author: Shawn
    // Date: 2025/01/27
    private String organization; // 所属单位（查询字段）
    private String workshop; // 所属车间（查询字段）
    private String process; // 所属产线（查询字段）

    /**
     * 随机值，目的取消一级缓存
     */
    private Integer random;

    public SwmPersonSchedule() {
        this(null);
    }

    public SwmPersonSchedule(String id) {
        super(id);
    }

    @NotBlank(message = "人员姓名不能为空")
    @Length(min = 0, max = 100, message = "人员姓名不能超过100个字符")
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    @NotBlank(message = "排班月份不能为空")
    @Length(min = 0, max = 7, message = "排班月份不能超过7个字符")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "排班月份格式不正确，应为yyyy-MM格式")
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    @NotBlank(message = "班次不能为空")
    @Length(min = 0, max = 20, message = "班次不能超过20个字符")
    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    @Length(min = 0, max = 18, message = "身份证号码不能超过18个字符")
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    @Length(min = 0, max = 64, message = "员工ID不能超过64个字符")
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * 获取班次显示文本
     */
    public String getClassesText() {
        if (this.classesText == null && this.classes != null) {
            this.classesText = SwmScheduleTime.ShiftTypeEnum.getText(this.classes);
        }
        return this.classesText;
    }

    public void setClassesText(String classesText) {
        this.classesText = classesText;
    }

    /**
     * 获取班组名称
     */
    public String getWorkGroupName() {
        return workGroupName;
    }

    public void setWorkGroupName(String workGroupName) {
        this.workGroupName = workGroupName;
    }

    /**
     * 获取personId (前端传入)
     */
    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    /**
     * 获取所属单位（查询字段）
     * Author: Shawn
     * Date: 2025/01/27
     */
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * 获取所属车间（查询字段）
     * Author: Shawn
     * Date: 2025/01/27
     */
    public String getWorkshop() {
        return workshop;
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    /**
     * 获取所属产线（查询字段）
     * Author: Shawn
     * Date: 2025/01/27
     */
    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }
}