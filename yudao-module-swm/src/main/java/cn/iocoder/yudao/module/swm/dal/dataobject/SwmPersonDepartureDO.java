package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 人员退场记录 DO
 * 表: swm_person_departure
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_person_departure")
public class SwmPersonDepartureDO extends SwmBaseDO {
    private String name;
    private String personType;
    private String gender;
    private String company;
    private String department;
    private String workProcess;
    private String team;
    private String jobType;
    private String safetyHelmetId;
    private String safetyEducation;
    private String identityCard;
    private String phoneNumber;
    private String personnelStatus;
    private String helmetReturned;
    private String departureType;
    private String departureReason;
    private LocalDate departureDate;
}
