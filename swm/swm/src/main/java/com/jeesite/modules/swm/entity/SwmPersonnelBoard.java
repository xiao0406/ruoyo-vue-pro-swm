package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

/**
 * 人员看板表实体类
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Table(name = "swm_personnel_board", alias = "a", label = "人员看板表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "name", attrName = "name", label = "姓名", queryType = QueryType.LIKE),
        @Column(name = "organization", attrName = "organization", label = "所属单位", queryType = QueryType.LIKE),
        @Column(name = "workshop", attrName = "workshop", label = "所属车间"),
        @Column(name = "process", attrName = "process", label = "所属工序"),
        @Column(name = "team", attrName = "team", label = "所属班组"),
        @Column(name = "work_status", attrName = "workStatus", label = "工作状态"),
        @Column(name = "device_id", attrName = "deviceId", label = "安全帽编号"),
        @Column(name = "helmet_status", attrName = "helmetStatus", label = "安全帽状态"),
        @Column(name = "personnel_status", attrName = "personnelStatus", label = "人员状态"),
        @Column(name = "attendance_count", attrName = "attendanceCount", label = "本月出勤次数"),
        @Column(name = "working_hours", attrName = "workingHours", label = "本月工作时长(小时)"),
        @Column(name = "idle_hours", attrName = "idleHours", label = "本月怠工时长(小时)"),
        @Column(name = "id_card", attrName = "idCard", label = "身份证号码"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmPersonnelBoard extends DataEntity<SwmPersonnelBoard> {

    private static final long serialVersionUID = 1L;

    // 查询时间类型: day, week, month
    private String timeType;

    // 查询时间值: YYYY-MM-DD, YYYY-WW, YYYY-MM
    private String timeValue;

    // 设备ID列表，用于工作状态查询过滤
    private List<String> deviceIds;

    // Getter and Setter for timeType
    public String getTimeType() {
        return timeType;
    }

    public void setTimeType(String timeType) {
        this.timeType = timeType;
    }

    // Getter and Setter for timeValue
    public String getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(String timeValue) {
        this.timeValue = timeValue;
    }

    // Getter and Setter for deviceIds
    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    /**
     * 工作状态枚举
     */
    public static class WorkStatusEnum {
        /** 工作中 */
        public static final String WORKING = "1";
        /** 休息中 */
        public static final String RESTING = "0";

        /**
         * 获取工作状态显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("work_status_enum", value, "");
        }
    }

    /**
     * 安全帽状态枚举
     */
    public static class HelmetStatusEnum {
        /** 正常 */
        public static final String NORMAL = "1";
        /** 脱帽 */
        public static final String OFF = "0";

        /**
         * 获取安全帽状态显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("helmet_status_enum", value, "");
        }
    }

    /**
     * 人员状态枚举
     */
    public static class PersonnelStatusEnum {
        /** 正常 */
        public static final String NORMAL = "1";
        /** 静止 */
        public static final String IDLE = "0";

        /**
         * 获取人员状态显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("personnel_status_enum", value, "");
        }
    }

    private String name; // 姓名
    private String organization; // 所属单位
    private String workshop; // 所属车间
    private String process; // 所属工序
    private String team; // 所属班组
    private String workStatus; // 工作状态
    private String deviceId; // 安全帽编号
    private String helmetStatus; // 安全帽状态
    private String personnelStatus; // 人员状态
    private Integer attendanceCount; // 本月出勤次数
    private BigDecimal workingHours; // 本月工作时长(小时)
    private BigDecimal idleHours; // 本月怠工时长(小时)
    private String idCard; // 身份证号码

    // 新增字段：休闲区统计
    private Integer leisureCount; // 进入休闲次数
    private Integer leisureDurationMin; // 休闲区总逗留时长(分钟)

    // 用于显示的文本属性，不对应数据库字段
    private String workStatusText; // 工作状态显示文本
    private String helmetStatusText; // 安全帽状态显示文本
    private String personnelStatusText; // 人员状态显示文本

    public SwmPersonnelBoard() {
        this(null);
    }

    public SwmPersonnelBoard(String id) {
        super(id);
    }

    @NotBlank(message = "姓名不能为空")
    @Length(min = 0, max = 50, message = "姓名不能超过50个字符")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Length(min = 0, max = 100, message = "所属单位不能超过100个字符")
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @Length(min = 0, max = 100, message = "所属车间不能超过100个字符")
    public String getWorkshop() {
        return workshop;
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    @Length(min = 0, max = 100, message = "所属工序不能超过100个字符")
    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Length(min = 0, max = 100, message = "所属班组不能超过100个字符")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @Length(min = 0, max = 20, message = "工作状态不能超过20个字符")
    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    /**
     * 获取工作状态显示文本
     */
    public String getWorkStatusText() {
        if (this.workStatusText == null && this.workStatus != null) {
            this.workStatusText = WorkStatusEnum.getText(this.workStatus);
        }
        return this.workStatusText;
    }

    public void setWorkStatusText(String workStatusText) {
        this.workStatusText = workStatusText;
    }

    @Length(min = 0, max = 50, message = "安全帽编号不能超过50个字符")
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Length(min = 0, max = 20, message = "安全帽状态不能超过20个字符")
    public String getHelmetStatus() {
        return helmetStatus;
    }

    public void setHelmetStatus(String helmetStatus) {
        this.helmetStatus = helmetStatus;
    }

    /**
     * 获取安全帽状态显示文本
     */
    public String getHelmetStatusText() {
        if (this.helmetStatusText == null && this.helmetStatus != null) {
            this.helmetStatusText = HelmetStatusEnum.getText(this.helmetStatus);
        }
        return this.helmetStatusText;
    }

    public void setHelmetStatusText(String helmetStatusText) {
        this.helmetStatusText = helmetStatusText;
    }

    @Length(min = 0, max = 20, message = "人员状态不能超过20个字符")
    public String getPersonnelStatus() {
        return personnelStatus;
    }

    public void setPersonnelStatus(String personnelStatus) {
        this.personnelStatus = personnelStatus;
    }

    /**
     * 获取人员状态显示文本
     */
    public String getPersonnelStatusText() {
        if (this.personnelStatusText == null && this.personnelStatus != null) {
            this.personnelStatusText = PersonnelStatusEnum.getText(this.personnelStatus);
        }
        return this.personnelStatusText;
    }

    public void setPersonnelStatusText(String personnelStatusText) {
        this.personnelStatusText = personnelStatusText;
    }

    public Integer getAttendanceCount() {
        return attendanceCount;
    }

    public void setAttendanceCount(Integer attendanceCount) {
        this.attendanceCount = attendanceCount;
    }

    public BigDecimal getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(BigDecimal workingHours) {
        this.workingHours = workingHours;
    }

    public BigDecimal getIdleHours() {
        return idleHours;
    }

    public void setIdleHours(BigDecimal idleHours) {
        this.idleHours = idleHours;
    }

    @Length(min = 0, max = 20, message = "身份证号码不能超过20个字符")
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Integer getLeisureCount() {
        return leisureCount;
    }

    public void setLeisureCount(Integer leisureCount) {
        this.leisureCount = leisureCount;
    }

    public Integer getLeisureDurationMin() {
        return leisureDurationMin;
    }

    public void setLeisureDurationMin(Integer leisureDurationMin) {
        this.leisureDurationMin = leisureDurationMin;
    }
}