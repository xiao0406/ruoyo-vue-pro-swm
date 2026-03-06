package com.jeesite.modules.swm.excel;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeeklyListDtoExprot {

    @ExcelFields({
            @ExcelField(title="姓名", attrName = "employeeName", align = ExcelField.Align.CENTER, sort = 10),
            @ExcelField(title="时间范围", attrName = "timeRange",align = ExcelField.Align.CENTER, sort = 20,width =256*40 ),
            @ExcelField(title="手机号码", attrName = "phoneNumber",align = ExcelField.Align.CENTER, sort = 30),
            @ExcelField(title="班组", attrName = "team",align = ExcelField.Align.CENTER, sort = 40),
            @ExcelField(title="工种", attrName = "jobType",align = ExcelField.Align.CENTER, sort = 50),
            @ExcelField(title="出勤天数（天）", attrName = "attendanceDay",align = ExcelField.Align.CENTER, sort = 60),
            @ExcelField(title="周出勤率", attrName = "monthlyAttendanceRate",align = ExcelField.Align.CENTER, sort = 70),
            @ExcelField(title="有效考勤天数", attrName = "validAttendanceDays",align = ExcelField.Align.CENTER, sort = 80),
            @ExcelField(title="本周有效考勤时长", attrName = "actualHours",align = ExcelField.Align.CENTER, sort = 90),
            @ExcelField(title="本周怠工时长", attrName = "idleHours",align = ExcelField.Align.CENTER, sort = 100),
    })


    public WeeklyListDtoExprot() {

    }

    private String employeeName;
    private String timeRange;
    private String phoneNumber;
    private String team;
    private String jobType;
    private BigDecimal attendanceDay;
    private String monthlyAttendanceRate;
    private BigDecimal validAttendanceDays;
    private BigDecimal actualHours;
    private BigDecimal idleHours;
}
