package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 排班时间管理表实体类
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Table(name = "swm_schedule_time", alias = "a", label = "排班时间管理表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "shift_type", attrName = "shiftType", label = "班次类型", queryType = QueryType.LIKE),
        @Column(name = "start_time", attrName = "startTime", label = "开始时间"),
        @Column(name = "end_time", attrName = "endTime", label = "结束时间"),
        @Column(name = "rest_time", attrName = "restTime", label = "休息时长"),
        @Column(name = "rest_days", attrName = "restDays", label = "休息日"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmScheduleTime extends DataEntity<SwmScheduleTime> {

    private static final long serialVersionUID = 1L;

    /**
     * 班次类型枚举
     */
    public static class ShiftTypeEnum {
        /** 早班 */
        public static final String MORNING = "1";
        /** 中班 */
        public static final String MIDDLE = "2";
        /** 晚班 */
        public static final String NIGHT = "3";

        /**
         * 获取班次类型显示文本
         */
        public static String getText(String value) {
            if (MORNING.equals(value)) {
                return "早班";
            } else if (MIDDLE.equals(value)) {
                return "中班";
            } else if (NIGHT.equals(value)) {
                return "晚班";
            }
            return "";
        }
    }

    private String shiftType; // 班次类型
    private String startTime; // 开始时间
    private String endTime; // 结束时间
    private Double restTime; // 休息时长
    private String restDays; // 休息日（逗号分隔，1-7代表周一到周日）

    // 用于显示的文本属性，不对应数据库字段
    private String shiftTypeText; // 班次类型显示文本
    private String restDaysText; // 休息日显示文本

    public SwmScheduleTime() {
        this(null);
    }

    public SwmScheduleTime(String id) {
        super(id);
    }

    @NotBlank(message = "班次类型不能为空")
    @Length(min = 0, max = 20, message = "班次类型不能超过20个字符")
    public String getShiftType() {
        return shiftType;
    }

    public void setShiftType(String shiftType) {
        this.shiftType = shiftType;
    }

    /**
     * 获取班次类型显示文本
     */
    public String getShiftTypeText() {
        if (this.shiftTypeText == null && this.shiftType != null) {
            this.shiftTypeText = ShiftTypeEnum.getText(this.shiftType);
        }
        return this.shiftTypeText;
    }

    public void setShiftTypeText(String shiftTypeText) {
        this.shiftTypeText = shiftTypeText;
    }

    @NotBlank(message = "开始时间不能为空")
    @Length(min = 0, max = 8, message = "开始时间不能超过8个字符")
    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "开始时间格式不正确，应为HH:mm格式")
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @NotBlank(message = "结束时间不能为空")
    @Length(min = 0, max = 8, message = "结束时间不能超过8个字符")
    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "结束时间格式不正确，应为HH:mm格式")
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Double getRestTime() {
        return restTime;
    }

    public void setRestTime(Double restTime) {
        this.restTime = restTime;
    }

    @Length(min = 0, max = 100, message = "休息日不能超过100个字符")
    public String getRestDays() {
        return restDays;
    }

    public void setRestDays(String restDays) {
        this.restDays = restDays;
    }

    /**
     * 获取休息日显示文本
     */
    public String getRestDaysText() {
        if (this.restDaysText == null && this.restDays != null && !this.restDays.isEmpty()) {
            String[] days = this.restDays.split(",");
            StringBuilder sb = new StringBuilder();
            for (String day : days) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                switch (day.trim()) {
                    case "1":
                        sb.append("周一");
                        break;
                    case "2":
                        sb.append("周二");
                        break;
                    case "3":
                        sb.append("周三");
                        break;
                    case "4":
                        sb.append("周四");
                        break;
                    case "5":
                        sb.append("周五");
                        break;
                    case "6":
                        sb.append("周六");
                        break;
                    case "7":
                        sb.append("周日");
                        break;
                }
            }
            this.restDaysText = sb.toString();
        }
        return this.restDaysText;
    }

    public void setRestDaysText(String restDaysText) {
        this.restDaysText = restDaysText;
    }
}