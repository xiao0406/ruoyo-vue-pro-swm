package com.jeesite.modules.swm.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 批量修改人员排班表 参数封装
 */
@Data
public class SwmPersonScheduleDto {
    @NotBlank(message = "人员ID不能为空")
    private List<String> ids; // 主键ID数组

    @NotBlank(message = "班次不能为空")
    private String classes; // 统一的班次值
}
