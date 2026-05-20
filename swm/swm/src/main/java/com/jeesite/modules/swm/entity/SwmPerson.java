/**
 * @author Shawn
 * @date 2025-05-13
 * @update 2025-06-01 添加产线字段
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 人员登记表实体类
 *
 * @author Shawn
 */
@Table(name = "swm_person", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "编号", isPK = true),
        @Column(name = "name", attrName = "name", label = "姓名", queryType = QueryType.LIKE),
        @Column(name = "person_number", attrName = "personNumber", label = "人员编码", queryType = QueryType.LIKE),
        @Column(name = "age", attrName = "age", label = "年龄"),
        @Column(name = "person_type", attrName = "personType", label = "人员类型"),
        @Column(name = "gender", attrName = "gender", label = "性别"),
        @Column(name = "company", attrName = "company", label = "所属单位"),
        @Column(name = "department", attrName = "department", label = "所属车间"),
        @Column(name = "prod_line", attrName = "prodLine", label = "产线"),
        @Column(name = "work_process", attrName = "workProcess", label = "所属工序"),
        @Column(name = "team", attrName = "team", label = "所属班组"),
        @Column(name = "job_type", attrName = "jobType", label = "所属工种"),
        @Column(name = "dept", attrName = "dept", label = "部门"),
        @Column(name = "position", attrName = "position", label = "职务"),
        @Column(name = "safety_helmet_id", attrName = "safetyHelmetId", label = "关联安全帽编号"),
        @Column(name = "personnel_status", attrName = "personnelStatus", label = "人员状态"),
        @Column(name = "safety_education", attrName = "safetyEducation", label = "入场安全教育"),
        @Column(name = "identity_card", attrName = "identityCard", label = "身份证号码", queryType = QueryType.LIKE),
        @Column(name = "phone_number", attrName = "phoneNumber", label = "手机号码", queryType = QueryType.LIKE),
        @Column(name = "urgent_person", attrName = "urgentPerson", label = "紧急联系人", queryType = QueryType.LIKE),
        @Column(name = "urgent_phone_number", attrName = "urgentPhoneNumber", label = "紧急联系人手机号", queryType = QueryType.LIKE),
        @Column(name = "helmet_returned", attrName = "helmetReturned", label = "是否归还安全帽"),
        @Column(name = "departure_type", attrName = "departureType", label = "离职类型"),
        @Column(name = "departure_reason", attrName = "departureReason", label = "离职原因"),
        @Column(name = "departure_date", attrName = "departureDate", label = "离职时间"),
        @Column(name = "is_external_personnel", attrName = "isExternalPersonnel", label = "是否厂内员工"),
        @Column(name = "blood_type", attrName = "bloodType", label = "血型"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmPerson extends DataEntity<SwmPerson> {

    private static final long serialVersionUID = 1L;

    /**
     * 人员类型枚举
     */
    public static class PersonTypeEnum {
        /** 工人 */
        public static final String WORKER = "0";
        /** 管理者 */
        public static final String MANAGER = "1";
        //班组长
        public static final String TEAMLEADER = "2";
        //特殊工种
        public static final String SPECIALTRADES = "3";

        /**
         * 获取人员状态显示文本
         */
//        public static String getText(String value) {
//            String label = DictUtils.getDictLabel("person_type_enum", value, "");
//            return label;
//        }
    }

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
            String label = DictUtils.getDictLabel("person_status_enum", value, "");
            return label;
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
            return DictUtils.getDictLabel("safety_education_enum", value, "");
        }
    }

    /**
     * 是否归还安全帽枚举
     */
    public static class HelmetReturnedEnum {
        public static final String YES = "1"; // 是
        public static final String NO = "0"; // 否

        public static String getText(String status) {
            return DictUtils.getDictLabel("helmet_returned_enum", status, "");
        }
    }

    /**
     * 离职类型枚举
     */
    public static class DepartureTypeEnum {
        public static final String NORMAL = "1"; // 正常离职
        public static final String ABNORMAL = "0"; // 异常离职

        public static String getText(String type) {
            return DictUtils.getDictLabel("departure_type_enum", type, "");
        }
    }

    private String name; // 姓名
    private String personType; // 人员类型
    private String gender; // 性别
    private String company; // 所属单位
    private String department; // 所属车间
    private String prodLine; // 产线
    private String workProcess; // 所属工序
    private String team; // 所属班组
    private String jobType; // 所属工种
    private String safetyHelmetId; // 关联安全帽编号
    private String personnelStatus; // 人员状态
    private String safetyEducation; // 入场安全教育
    private String identityCard; // 身份证号码
    private String phoneNumber; // 手机号码
    private String helmetReturned; // 是否归还安全帽
    private String departureType; // 离职类型
    private String departureReason; // 离职原因
    private Date departureDate; // 离职时间
    private String isExternalPersonnel; // 是否厂内员工
    private String classes;// 所属班次
    private String dept;// 部门
    private String position;// 职务
    private List<String> idCards;
    private List<String> ids; //主键id
    /**
     * 随机值，目的取消一级缓存
     */
    private Integer random;
    @ApiModelProperty(value = "开机状态  0-开机，1-关机")
    private String powerOnStatus;
    @ApiModelProperty(value = "人员编码")
    private String personNumber;
    @ApiModelProperty(value = "年龄 ")
    private String age;
    @ApiModelProperty(value = "紧急联系人")
    private String urgentPerson;
    @ApiModelProperty(value = "紧急联系人手机号")
    private String urgentPhoneNumber;
    //在线人数
    private List<String> todayOnSiteIdCards;
    @ApiModelProperty(value = "血型")
    private String bloodType;


    public SwmPerson() {
        this(null);
    }

    public SwmPerson(String id) {
        super(id);
    }

    @NotBlank(message = "姓名不能为空")
    @Length(min = 0, max = 100, message = "姓名不能超过100个字符")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Length(min = 0, max = 100, message = "人员类型不能超过100个字符")
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

    @Length(min = 0, max = 64, message = "产线不能超过64个字符")
    public String getProdLine() {
        return prodLine;
    }

    public void setProdLine(String prodLine) {
        this.prodLine = prodLine;
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

    @Length(min = 0, max = 100, message = "关联安全帽编号不能超过100个字符")
    public String getSafetyHelmetId() {
        return safetyHelmetId;
    }

    public void setSafetyHelmetId(String safetyHelmetId) {
        this.safetyHelmetId = safetyHelmetId;
    }

    @Length(min = 0, max = 1, message = "人员状态不能超过1个字符")
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

    @Length(min = 0, max = 1, message = "入场安全教育不能超过1个字符")
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

    @Length(min = 0, max = 20, message = "身份证号码不能超过20个字符")
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

    @Length(min = 0, max = 1, message = "是否归还安全帽不能超过1个字符")
    public String getHelmetReturned() {
        return helmetReturned;
    }

    /**
     * 获取是否归还安全帽文本
     */
    public String getHelmetReturnedText() {
        return HelmetReturnedEnum.getText(helmetReturned);
    }

    public void setHelmetReturned(String helmetReturned) {
        this.helmetReturned = helmetReturned;
    }

    @Length(min = 0, max = 1, message = "离职类型不能超过1个字符")
    public String getDepartureType() {
        return departureType;
    }

    /**
     * 获取离职类型文本
     */
    public String getDepartureTypeText() {
        return DepartureTypeEnum.getText(departureType);
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

    @Length(min = 0, max = 10, message = "是否厂内员工不能超过10个字符")
    public String getIsExternalPersonnel() {
        return isExternalPersonnel;
    }

    public void setIsExternalPersonnel(String isExternalPersonnel) {
        this.isExternalPersonnel = isExternalPersonnel;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public List<String> getIdCards() {
        return idCards;
    }

    public void setIdCards(List<String> idCards) {
        this.idCards = idCards;
    }
}
