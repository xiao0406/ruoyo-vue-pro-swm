/**
 * @author Shawn
 * @date 2025-05-13
 * @update 2025-06-25 移除产线字段，与数据库表结构保持一致
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * 离职登记表实体类
 * 
 * @author Shawn
 */
@Table(name = "swm_person_departure", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "编号", isPK = true),
        @Column(name = "name", attrName = "name", label = "姓名", queryType = QueryType.LIKE),
        @Column(name = "person_type", attrName = "personType", label = "人员类型"),
        @Column(name = "gender", attrName = "gender", label = "性别"),
        @Column(name = "company", attrName = "company", label = "所属单位", queryType = QueryType.LIKE),
        @Column(name = "department", attrName = "department", label = "所属车间"),
        @Column(name = "work_process", attrName = "workProcess", label = "所属工序"),
        @Column(name = "team", attrName = "team", label = "所属班组"),
        @Column(name = "job_type", attrName = "jobType", label = "所属工种"),
        @Column(name = "safety_helmet_id", attrName = "safetyHelmetId", label = "关联安全帽编号"),
        @Column(name = "safety_education", attrName = "safetyEducation", label = "入场安全教育"),
        @Column(name = "identity_card", attrName = "identityCard", label = "身份证号码"),
        @Column(name = "phone_number", attrName = "phoneNumber", label = "手机号码"),
        @Column(name = "personnel_status", attrName = "personnelStatus", label = "人员状态"),
        @Column(name = "helmet_returned", attrName = "helmetReturned", label = "是否归还安全帽"),
        @Column(name = "departure_type", attrName = "departureType", label = "离职类型"),
        @Column(name = "departure_reason", attrName = "departureReason", label = "离职原因"),
        @Column(name = "departure_date", attrName = "departureDate", label = "离职时间"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmPersonDeparture extends DataEntity<SwmPersonDeparture> {

    private static final long serialVersionUID = 1L;

    private String name; // 姓名
    private String personType; // 人员类型
    private String gender; // 性别
    private String company; // 所属单位
    private String department; // 所属车间
    private String workProcess; // 所属工序
    private String team; // 所属班组
    private String jobType; // 所属工种
    private String safetyHelmetId; // 关联安全帽编号
    private String safetyEducation; // 入场安全教育
    private String identityCard; // 身份证号码
    private String phoneNumber; // 手机号码
    private String personnelStatus; // 人员状态
    private String helmetReturned; // 是否归还安全帽
    private String departureType; // 离职类型
    private String departureReason; // 离职原因
    private Date departureDate; // 离职时间
    private String helmetReturnedText; // 是否归还安全帽文本
    private String departureTypeText; // 离职类型文本

    public SwmPersonDeparture() {
        this(null);
    }

    public SwmPersonDeparture(String id) {
        super(id);
    }

    @NotBlank(message = "姓名不能为空")
    @Length(min = 0, max = 50, message = "姓名不能超过50个字符")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Length(min = 0, max = 50, message = "人员类型不能超过50个字符")
    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    @Length(min = 0, max = 10, message = "性别不能超过10个字符")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Length(min = 0, max = 200, message = "所属单位不能超过200个字符")
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Length(min = 0, max = 100, message = "所属车间不能超过100个字符")
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Length(min = 0, max = 100, message = "所属工序不能超过100个字符")
    public String getWorkProcess() {
        return workProcess;
    }

    public void setWorkProcess(String workProcess) {
        this.workProcess = workProcess;
    }

    @Length(min = 0, max = 100, message = "所属班组不能超过100个字符")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @Length(min = 0, max = 100, message = "所属工种不能超过100个字符")
    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    @Length(min = 0, max = 50, message = "关联安全帽编号不能超过50个字符")
    public String getSafetyHelmetId() {
        return safetyHelmetId;
    }

    public void setSafetyHelmetId(String safetyHelmetId) {
        this.safetyHelmetId = safetyHelmetId;
    }

    @Length(min = 0, max = 20, message = "入场安全教育不能超过20个字符")
    public String getSafetyEducation() {
        return safetyEducation;
    }

    public void setSafetyEducation(String safetyEducation) {
        this.safetyEducation = safetyEducation;
    }

    @Length(min = 0, max = 18, message = "身份证号码不能超过18个字符")
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号码格式不正确")
    public String getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    @Length(min = 0, max = 20, message = "手机号码不能超过20个字符")
    @Pattern(regexp = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", message = "手机号码格式不正确")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Length(min = 0, max = 20, message = "人员状态不能超过20个字符")
    public String getPersonnelStatus() {
        return personnelStatus;
    }

    public void setPersonnelStatus(String personnelStatus) {
        this.personnelStatus = personnelStatus;
    }

    @Length(min = 0, max = 1, message = "是否归还安全帽不能超过1个字符")
    public String getHelmetReturned() {
        return helmetReturned;
    }

    public void setHelmetReturned(String helmetReturned) {
        this.helmetReturned = helmetReturned;
    }

    @Length(min = 0, max = 1, message = "离职类型不能超过1个字符")
    public String getDepartureType() {
        return departureType;
    }

    public void setDepartureType(String departureType) {
        this.departureType = departureType;
    }

    public String getDepartureReason() {
        return departureReason;
    }

    public void setDepartureReason(String departureReason) {
        this.departureReason = departureReason;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public String getHelmetReturnedText() {
        return helmetReturnedText;
    }

    public void setHelmetReturnedText(String helmetReturnedText) {
        this.helmetReturnedText = helmetReturnedText;
    }

    public String getDepartureTypeText() {
        return departureTypeText;
    }

    public void setDepartureTypeText(String departureTypeText) {
        this.departureTypeText = departureTypeText;
    }
}