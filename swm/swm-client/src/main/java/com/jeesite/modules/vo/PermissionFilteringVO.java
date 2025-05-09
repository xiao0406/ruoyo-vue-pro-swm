package com.jeesite.modules.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 制造单位编码与其子编码零时接收类
 * @author 龚加林
 */
@Data
public class PermissionFilteringVO implements Serializable {
    /**父机构编码*/
    private String parentInstitutionCode;
    /**子机构编码*/
    private String subMechanismCode;
}
