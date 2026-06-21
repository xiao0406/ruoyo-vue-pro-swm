package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * 人员管理 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmPerson
 * 表: swm_person
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_person")
public class SwmPersonDO extends SwmBaseDO {

    /** 姓名 */
    private String name;
    /** 人员编码 */
    private String personNumber;
    /** 年龄 */
    private String age;
    /** 人员类型（枚举 SwmEnums.PersonTypeEnum） */
    private String personType;
    /** 性别 */
    private String gender;
    /** 所属单位 */
    private String company;
    /** 所属车间 */
    private String department;
    /** 产线 */
    private String prodLine;
    /** 所属工序 */
    private String workProcess;
    /** 所属班组 */
    private String team;
    /** 所属工种 */
    private String jobType;
    /** 部门 */
    private String dept;
    /** 职务 */
    private String position;
    /** 关联安全帽编号 */
    private String safetyHelmetId;
    /** 人员状态（枚举 SwmEnums.PersonStatusEnum） */
    private String personnelStatus;
    /** 入场安全教育（枚举 SwmEnums.SafetyEducationStatusEnum） */
    private String safetyEducation;
    /** 身份证号码 */
    private String identityCard;
    /** 手机号码 */
    private String phoneNumber;
    /** 紧急联系人 */
    private String urgentPerson;
    /** 紧急联系人手机号 */
    private String urgentPhoneNumber;
    /** 是否归还安全帽（枚举 SwmEnums.HelmetReturnedEnum） */
    private String helmetReturned;
    /** 离职类型（枚举 SwmEnums.DepartureTypeEnum） */
    private String departureType;
    /** 离职原因 */
    private String departureReason;
    /** 离职时间 */
    private LocalDate departureDate;
    /** 是否厂内员工 */
    private String isExternalPersonnel;
    /** 血型 */
    private String bloodType;
    /** 企业编码（兼容 JeeSite 老逻辑，来自 tenantId 映射） */
    private String corpCode;
    /** 企业名称（兼容 JeeSite 老逻辑） */
    private String corpName;

    // ===== 非数据库字段（查询/显示用） =====

    /** 所属班次 */
    @TableField(exist = false)
    private String classes;
    /** ID 卡号列表（批量查询用） */
    @TableField(exist = false)
    private List<String> idCards;
    /** 主键 ID 列表 */
    @TableField(exist = false)
    private List<String> ids;
    /** 开机状态 */
    @TableField(exist = false)
    private String powerOnStatus;
    /** 今日在场 ID 卡号列表 */
    @TableField(exist = false)
    private List<String> todayOnSiteIdCards;

}
