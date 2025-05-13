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
        @Column(name = "personnel_status", attrName = "personnelStatus", label = "人员状态"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.update_date DESC")
public class SwmPerson extends DataEntity<SwmPerson> {

    private static final long serialVersionUID = 1L;

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

    public void setSafetyEducation(String safetyEducation) {
        this.safetyEducation = safetyEducation;
    }

    @Length(max = 20, message = "人员状态长度不能超过20个字符")
    public String getPersonnelStatus() {
        return personnelStatus;
    }

    public void setPersonnelStatus(String personnelStatus) {
        this.personnelStatus = personnelStatus;
    }
}