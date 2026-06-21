package cn.iocoder.yudao.module.swm.controller.admin.dashboard.vo;

import lombok.Data;

/**
 * 考勤分析 VO（按班组/部门统计）
 */
@Data
public class AttendanceAnalysisVO {
    private String name;
    private Long count;
}
