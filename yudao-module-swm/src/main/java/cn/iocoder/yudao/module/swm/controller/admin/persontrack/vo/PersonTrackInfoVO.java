package cn.iocoder.yudao.module.swm.controller.admin.persontrack.vo;

import lombok.Data;

/**
 * 人员轨迹信息 VO
 */
@Data
public class PersonTrackInfoVO {
    private String id;
    private String name;
    private String identityCard;
    private String gender;
    private String phoneNumber;
    private String personType;
    private String personnelStatus;
    private String company;
    private String department;
    private String prodLine;
    private String team;
    private String jobType;
    private String organization;
    private String workShop;
    private String prodLineName;
    private String teamGroup;
    private String workerArchiveId;
}
