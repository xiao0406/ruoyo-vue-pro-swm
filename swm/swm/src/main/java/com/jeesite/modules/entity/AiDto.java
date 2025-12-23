package com.jeesite.modules.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.jeesite.common.entity.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AiDto implements Serializable{
    private static final long serialVersionUID = 1L;

    //    工效统计 班组、车间、全体工人
    //白班应到、白班实到、白班出勤率、白班有效作业时长，夜班应到、夜班实到、夜班出勤率、夜班有效作业时长，全天应到、全天实到、全天出勤率、全天有效作业时长
    @Data
    public static class WorkEfficiencyDto implements Serializable{
        private static final long serialVersionUID = 1L;
        @ApiModelProperty(value = "员工ID")
        private String employeeId;
        @ApiModelProperty(value = "类型，班组、车间")
        private String type;
        @ApiModelProperty(value = "名称")
        private String name;
        @ApiModelProperty("白班考勤数据")
        private AttendanceData dayShift;
        @ApiModelProperty("夜班考勤数据")
        private AttendanceData nightShift;
        @ApiModelProperty("全天考勤数据")
        private AttendanceData allDayShift;

    }

    @Data
    //考勤数据
    public static class AttendanceData implements Serializable{
        private static final long serialVersionUID = 1L;
        @ApiModelProperty("应到")
        private Integer shouldArrive;
        @ApiModelProperty("实到")
        private Integer actualArrive;
        @ApiModelProperty("出勤率")
        private BigDecimal attendanceRate;
        @ApiModelProperty("有效作业时长")
        private BigDecimal effectiveWorkingHours;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    //工人疲劳
    public static class WorkerFatigue extends BaseEntity<WorkerFatigue> {
        private static final long serialVersionUID = 1L;
        @ApiModelProperty("员工姓名")
        private String employeeName;
        @ApiModelProperty("绑定设备号")
        private String deviceId;
        @ApiModelProperty("手机号码")
        private String phoneNumber;
        @ApiModelProperty(value = "所属车间")
        private String department;
        @ApiModelProperty(value = "所属车间")
        private String departmentName;
        @ApiModelProperty(value = "所属班组")
        private String team;
        @ApiModelProperty(value = "所属班组")
        private String teamName;
        @ApiModelProperty("有效出勤时长")
        private BigDecimal actualHours;

        String startDate;
        String endDate;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    //风险统计
    public static class RiskStatistics extends BaseEntity<RiskStatistics> {
        private static final long serialVersionUID = 1L;
        String startDate;
        String endDate;

        //静止报警	脱帽报警	应急呼叫	跌落报警	危险源闯入
        @ApiModelProperty("员工姓名")
        private String employeeName;
        @ApiModelProperty("表名")
        private String tableName;
        @ApiModelProperty("长时间静止报警")
        private Integer staticAlarm = 0;
        @ApiModelProperty("脱帽报警")
        private Integer unsealAlarm = 0;
        @ApiModelProperty("应急呼叫")
        private Integer emergencyCall =0;
        @ApiModelProperty("跌落报警")
        private Integer fallAlarm =0;
        @ApiModelProperty("危险区域闯入提示")
        private Integer dangerousSourceEntry =0;
        @ApiModelProperty("报警总数")
        private Integer totalAlarm =0;

        @ApiModelProperty("时间段")
        private String hours;
        @ApiModelProperty("区域名称")
        private String area;
        @ApiModelProperty("区域时间")
        private String areaHours;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    //风险统计
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskStatisticsAreaDate extends BaseEntity<RiskStatisticsAreaDate> {
        private static final long serialVersionUID = 1L;
        private String startDate;
        private String endDate;

        @ApiModelProperty("报警总数")
        private Integer totalAlarm ;
        @ApiModelProperty("时间段，例如 00~01")
        private String hours;
        @ApiModelProperty("区域名称（子项使用）")
        private String area;
        @ApiModelProperty("区域列表（父节点使用）")
        private List<RiskStatisticsAreaDate> areaList;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    //风险统计
    public static class Trajectory extends BaseEntity<Trajectory> {
        private static final long serialVersionUID = 1L;
        String startDate;
        String endDate;

//        人员信息：姓名、绑定安全帽ID、手机号、车间、班组
//        定位坐标：X坐标，Y坐标，所在区域

        @ApiModelProperty("员工姓名")
        private String employeeName;
        @ApiModelProperty("绑定设备号")
        private String deviceId;
        @ApiModelProperty("身份证")
        private String idCard;
        @ApiModelProperty("手机号码")
        private String phoneNumber;
        @ApiModelProperty(value = "所属车间")
        private String department;
        @ApiModelProperty(value = "所属车间")
        private String departmentName;
        @ApiModelProperty(value = "所属班组")
        private String team;
        @ApiModelProperty(value = "所属班组")
        private String teamName;


        @ApiModelProperty("x")
        private Integer x;
        @ApiModelProperty("y")
        private Integer y;
        @ApiModelProperty("所在区域")
        private String areaName;
        @ApiModelProperty("通过这个查询区域")
        private String address;
        @ApiModelProperty("定时时间")
        private String time;

        List<Trajectory> xyList;

        @ApiModelProperty("员工姓名")
        private String name;
        @ApiModelProperty("绑定设备号")
        private String id;
        @ApiModelProperty(value = "所属车间")
        private String group;

        List<Trajectory> details;

    }


    @Data
    //风险统计
    public static class TeamActualHours implements Serializable {
        private static final long serialVersionUID = 1L;
        String startDate;
        String endDate;

        @ApiModelProperty(value = "所属班组")
        private String teamName;

        @ApiModelProperty(value = "有效时长")
        private BigDecimal actualHours;
    }



}
