package cn.iocoder.yudao.module.swm.dal.dataobject.dto;

import lombok.Data;

/**
 * 排班导出 DTO
 */
@Data
public class SwmPersonDOScheduleExportDO {
    private String id;
    private String name;
    private String identityCard;
    private String personName;
    private String classes;
    private String month;
}
