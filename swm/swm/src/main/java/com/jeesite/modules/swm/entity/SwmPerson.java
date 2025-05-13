/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 人员登记表实体类
 * 
 * @author Shawn
 */
@Table(name = "swm_person", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "name", attrName = "name", label = "姓名", queryType = QueryType.LIKE),
        @Column(name = "person_type", attrName = "personType", label = "人员类型"),
        @Column(name = "gender", attrName = "gender", label = "性别"),
        @Column(name = "company", attrName = "company", label = "所属单位", queryType = QueryType.LIKE),
        @Column(name = "department", attrName = "department", label = "所属车间"),
        @Column(name = "work_process", attrName = "workProcess", label = "所属工序"),
        @Column(name = "team", attrName = "team", label = "所属班组"),
        @Column(name = "job_type", attrName = "jobType", label = "所属工种"),
        @Column(name = "safety_helmet_id", attrName = "safetyHelmetId", label = "关联安全帽"),
        @Column(name = "safety_education", attrName = "safetyEducation", label = "入场安全教育"),
        @Column(name = "identity_card", attrName = "identityCard", label = "身份证号码", queryType = QueryType.LIKE),
        @Column(name = "phone_number", attrName = "phoneNumber", label = "手机号码", queryType = QueryType.LIKE),
        @Column(name = "personnel_status", attrName = "personnelStatus", label = "人员状态"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.update_date DESC")
public class SwmPerson extends DataEntity<SwmPerson> {

    private static final long serialVersionUID = 1L;

    /**
     * 人员状态枚举
     */
    public static class PersonStatusEnum {
        /** 离职 */
        public static final String INACTIVE = "0";
        /** 在职 */
        public static final String ACTIVE = "1";

        /**
         * 获取人员状态显示文本
         */
        public static String getText(String value) {
            if (ACTIVE.equals(value)) {
                return "在职";
            } else if (INACTIVE.equals(value)) {
                return "离职";
            }
            return "";
        }
    }

    /**
     * 安全教育枚举
     */
    public static class SafetyEducationEnum {
        /** 未开始 */
        public static final String NOT_STARTED = "0";
        /** 已培训 */
        public static final String COMPLETED = "1";

        /**
         * 获取安全教育状态显示文本
         */
        public static String getText(String value) {
            if (NOT_STARTED.equals(value)) {
                return "未开始";
            } else if (COMPLETED.equals(value)) {
                return "已培训";
            }
            return "";
        }
    }

    private String name; // 姓名
    private String personType; // 人员类型
    private String gender; // 性别
    private String company; // 所属单位
    private String department; // 所属车间
    private String workProcess; // 所属工序
    private String team; // 所属班组
    private String jobType; // 所属工种
    private String safetyHelmetId; // 关联安全帽
    private String safetyEducation; // 入场安全教育
    private String identityCard; // 身份证号码
    private String phoneNumber; // 手机号码
    private String personnelStatus; // 人员状态

    public SwmPerson() {
        this(null);
    }

    public SwmPerson(String id) {
        super(id);
    }

    @NotBlank(message = "姓名不能为空")
    @Length(min = 1, max = 50, message = "姓名长度不能超过50个字符")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Length(max = 50, message = "人员类型长度不能超过50个字符")
    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    @Length(max = 10, message = "性别长度不能超过10个字符")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Length(max = 200, message = "所属单位长度不能超过200个字符")
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Length(max = 100, message = "所属车间长度不能超过100个字符")
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Length(max = 100, message = "所属工序长度不能超过100个字符")
    public String getWorkProcess() {
        return workProcess;
    }

    public void setWorkProcess(String workProcess) {
        this.workProcess = workProcess;
    }

    @Length(max = 100, message = "所属班组长度不能超过100个字符")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @Length(max = 100, message = "所属工种长度不能超过100个字符")
    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    @Length(max = 50, message = "关联安全帽长度不能超过50个字符")
    public String getSafetyHelmetId() {
        return safetyHelmetId;
    }

    public void setSafetyHelmetId(String safetyHelmetId) {
        this.safetyHelmetId = safetyHelmetId;
    }

    @Length(max = 20, message = "入场安全教育长度不能超过20个字符")
    public String getSafetyEducation() {
        return safetyEducation;
    }

    /**
     * 获取安全教育显示值
     */
    public String getSafetyEducationText() {
        return SafetyEducationEnum.getText(safetyEducation);
    }

    public void setSafetyEducation(String safetyEducation) {
        this.safetyEducation = safetyEducation;
    }

    @Length(max = 18, message = "身份证号码长度不能超过18个字符")
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号码格式不正确")
    public String getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    @Length(max = 20, message = "手机号码长度不能超过20个字符")
    @Pattern(regexp = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", message = "手机号码格式不正确")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Length(max = 20, message = "人员状态长度不能超过20个字符")
    public String getPersonnelStatus() {
        return personnelStatus;
    }

    /**
     * 获取人员状态显示值
     */
    public String getPersonnelStatusText() {
        return PersonStatusEnum.getText(personnelStatus);
    }

    public void setPersonnelStatus(String personnelStatus) {
        this.personnelStatus = personnelStatus;
    }
}