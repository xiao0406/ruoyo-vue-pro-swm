package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Table;

import java.util.Date;

/**
 * 人员追踪信息实体类
 * 
 * @author Shawn
 * @date 2025-01-14
 */
@Table(name = "swm_person", alias = "a", label = "人员追踪信息")
public class PersonTrackInfo extends DataEntity<PersonTrackInfo> {

    private static final long serialVersionUID = 1L;

    private String name; // 姓名
    private String personType; // 人员类型
    private String gender; // 性别
    private String organization; // 组织单位
    private String workShop; // 车间
    private String prodLine; // 产线
    private String teamGroup; // 班组
    private String workType; // 工种
    private String safetyHelmetId; // 安全帽编号
    private String personnelStatus; // 人员状态
    private String safetyEducation; // 安全教育
    private String identityCard; // 身份证号码
    private String phoneNumber; // 手机号码
    private String helmetReturned; // 是否归还安全帽
    private String departureType; // 离职类型
    private String departureReason; // 离职原因
    private Date departureDate; // 离职时间

    public PersonTrackInfo() {
        this(null);
    }

    public PersonTrackInfo(String id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getWorkShop() {
        return workShop;
    }

    public void setWorkShop(String workShop) {
        this.workShop = workShop;
    }

    public String getProdLine() {
        return prodLine;
    }

    public void setProdLine(String prodLine) {
        this.prodLine = prodLine;
    }

    public String getTeamGroup() {
        return teamGroup;
    }

    public void setTeamGroup(String teamGroup) {
        this.teamGroup = teamGroup;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getSafetyHelmetId() {
        return safetyHelmetId;
    }

    public void setSafetyHelmetId(String safetyHelmetId) {
        this.safetyHelmetId = safetyHelmetId;
    }

    public String getPersonnelStatus() {
        return personnelStatus;
    }

    public void setPersonnelStatus(String personnelStatus) {
        this.personnelStatus = personnelStatus;
    }

    public String getSafetyEducation() {
        return safetyEducation;
    }

    public void setSafetyEducation(String safetyEducation) {
        this.safetyEducation = safetyEducation;
    }

    public String getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getHelmetReturned() {
        return helmetReturned;
    }

    public void setHelmetReturned(String helmetReturned) {
        this.helmetReturned = helmetReturned;
    }

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
}