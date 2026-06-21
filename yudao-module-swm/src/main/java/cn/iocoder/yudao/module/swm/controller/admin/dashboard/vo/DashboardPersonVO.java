package cn.iocoder.yudao.module.swm.controller.admin.dashboard.vo;

import lombok.Data;

/**
 * 仪表盘人员出勤 VO
 */
@Data
public class DashboardPersonVO {
    private String name;
    private String gender;
    private String phone;
    private String personType;
    private String clockInDate;
}
