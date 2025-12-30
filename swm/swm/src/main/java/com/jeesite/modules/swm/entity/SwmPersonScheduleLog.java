package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 人员班次批量修改日志实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SwmPersonScheduleLog extends DataEntity<SwmPersonScheduleLog> {
    private static final long serialVersionUID = 1L;

    private String operateUser; // 操作人编码
    private Date operateTime; // 操作时间
    private String targetClasses; // 目标班次
    private String personId; // 人员ID列表（逗号分隔）
    private String remark; // 备注
    private String operateDesc; // 操作描述
}
