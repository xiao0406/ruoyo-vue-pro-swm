package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_person")
public class SwmPersonDO extends SwmBaseDO {

    private String name;
    private String personNumber;
    private String age;
    private String personType;
    private String gender;
    private String company;
    private String department;
    private String prodLine;
    private String workProcess;
    private String team;
    private String jobType;
    private String dept;
    private String position;
    private String safetyHelmetId;
    private String personnelStatus;
    private String safetyEducation;
    private String identityCard;
    private String phoneNumber;
    private String urgentPerson;
    private String urgentPhoneNumber;
    private String helmetReturned;
    private String departureType;
    private String departureReason;
    private LocalDate departureDate;
    private String isExternalPersonnel;
    private String bloodType;

    @TableField(exist = false)
    private String classes;
    @TableField(exist = false)
    private List<String> idCards;
    @TableField(exist = false)
    private List<String> ids;
    @TableField(exist = false)
    private String powerOnStatus;
    @TableField(exist = false)
    private List<String> todayOnSiteIdCards;
}
