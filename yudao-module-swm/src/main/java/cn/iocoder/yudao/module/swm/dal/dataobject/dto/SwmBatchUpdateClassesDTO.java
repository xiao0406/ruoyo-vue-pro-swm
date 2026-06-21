package cn.iocoder.yudao.module.swm.dal.dataobject.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量更新班次 DTO
 */
@Data
public class SwmBatchUpdateClassesDTO {
    /** 要更新的排班记录ID列表 */
    private List<String> ids;
    /** 新的班次值 */
    private String classes;
}
