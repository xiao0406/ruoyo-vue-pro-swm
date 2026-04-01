package com.jeesite.modules.swm.entity;

import lombok.Data;

/**
 * 人员组织查询条件
 * @author: cjie
 * @date: 2025/7/2
 */
@Data
public class PersonnelOrganizationQueryParam {
    /**
     * 组织编码
     */
    private String officeCode;

    /**
     * 车间ID
     */
    private String positionArchiveId;

    /**
     * 产线ID（工序）
     */
    private String prodLineId;

    /**
     * 班组ID
     */
    private String workGroupId;

    /**
     * 工种
     */
    private String jobType;

    /**
     * 1-白班，3-夜班
     */
    private String shiftType;

}
