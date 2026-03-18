package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 人员班次批量修改日志实体
 */
@Table(name = "swm_person_schedule_log", alias = "a", label = "人员排班日志表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "operate_user", attrName = "operateUser", label = "操作人编码（关联sys_user.user_code）", queryType = QueryType.LIKE),
        @Column(name = "operate_time", attrName = "operateTime", label = "操作时间", queryType = QueryType.EQ),
        @Column(name = "target_classes", attrName = "targetClasses", label = "目标班次（1=早班，2=晚班，3=其他，可根据业务调整）"),
        @Column(name = "person_id", attrName = "personId", label = "本次修改的人员ID列表（逗号分隔，如：2000460065136517120,2000460065136517121）", queryType = QueryType.LIKE),
        @Column(name = "before_classes", attrName = "beforeClasses", label = "修改前班次（可选，若需追溯修改前状态可添加，默认空）"),
        @Column(name = "operate_desc", attrName = "operateDesc", label = "格式化操作描述（如：李XX于2025年12月31日09点45分，班次调整为‘白班’）"),
        @Column(name = "del_flag", attrName = "delFlag", label = "逻辑删除标记（0=未删除，1=已删除，Jeesite规范）"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmPersonScheduleLog extends DataEntity<SwmPersonScheduleLog> {
    private static final long serialVersionUID = 1L;

    private String operateUser; // 操作人编码
    private Date operateTime; // 操作时间
    private String targetClasses; // 目标班次
    private String personId; // 人员ID列表（逗号分隔）
    private String remark; // 备注
    private String operateDesc; // 操作描述
    private String delFlag;
    private String beforeClasses;
}
