package com.jeesite.modules.swm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警管理表实体类
 *
 * @author zwf
 * @version 2025-05-16
 */
@Table(name = "swm_warning_management", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "预警单号", isPK = true, queryType = QueryType.LIKE),
        @Column(name = "person_name", attrName = "personName", label = "人员名称", queryType = QueryType.LIKE),
        @Column(name = "warning_type", attrName = "warningType", label = "预警性质"),
        @Column(name = "warning_content", attrName = "warningContent", label = "预警类型"),
        @Column(name = "warning_time", attrName = "warningTime", label = "预警时间"),
        @Column(name = "alarm_record", attrName = "alarmRecord", label = "报警记录"),
        @Column(name = "alarm_time", attrName = "alarmTime", label = "报警时间"),
        @Column(name = "trigger_reason", attrName = "triggerReason", label = "触发原因"),
        @Column(name = "handler", attrName = "handler", label = "处置人"),
        @Column(name = "handle_time", attrName = "handleTime", label = "处置时间"),
        @Column(name = "handle_process", attrName = "handleProcess", label = "处置过程"),
        @Column(name = "handle_status", attrName = "handleStatus", label = "处置状态"),
        @Column(name = "attachment", attrName = "attachment", label = "附件路径", comment = "附件路径"),
        @Column(name = "disposal_duration", attrName = "disposalDuration", label = "处置时长", comment = "处置时长(min)"),
        @Column(name = "device_id", attrName = "deviceId", label = "设备ID"),
        @Column(name = "id_card", attrName = "idCard", label = "身份证号"),
        @Column(name = "front_alarm", attrName = "frontAlarm", label = "前端弹框提示", comment = "前端全局弹框提示（0不弹框，1弹框）"),
        @Column(name = "type", attrName = "type", label = "告警类型", comment = "与alarm_config表的alarm_key匹配"),
        @Column(name = "x", attrName = "x", label = "x坐标", comment = "x坐标"),
        @Column(name = "y", attrName = "y", label = "y坐标", comment = "y坐标"),
        @Column(name = "hazard_category", attrName = "hazardCategory", label = "危险源类别"),
        @Column(name = "location", attrName = "location", label = "位置"),
        @Column(name = "area", attrName = "area", label = "区域"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.warning_time DESC")
public class SwmWarningManagement extends DataEntity<SwmWarningManagement> {

    private static final long serialVersionUID = 1L;

    /**
     * 预警类型枚举
     */
    public static class WarningTypeEnum {
        /** 主动报警 */
        public static final String ACTIVE = "1";
        /** 被动预警 */
        public static final String PASSIVE = "2";

        /**
         * 获取预警类型显示文本
         */
        public static String getText(String value) {
            System.out.println("WarningTypeEnum.getText被调用，参数value=" + value);
            String result;

            // 特殊处理warningType=1的情况，直接返回主动报警
            if ("1".equals(value)) {
                System.out.println("WarningTypeEnum.getText: 特殊处理warningType=1，返回主动报警");
                return "主动报警";
            }

            // 如果是数字，从字典获取标签
            if (value != null && value.matches("\\d+")) {
                result = DictUtils.getDictLabel("warning_type_enum", value, "");
            } else {
                // 如果已经是文本，尝试反向查找字典值，如果找不到则直接返回原文本
                String dictValue = DictUtils.getDictValue("warning_type_enum", value, "");
                if (!dictValue.isEmpty()) {
                    // 找到对应的字典值，转为标签
                    result = DictUtils.getDictLabel("warning_type_enum", dictValue, value);
                } else {
                    // 未找到对应字典值，直接返回原文本
                    result = value;
                }
            }

            System.out.println("WarningTypeEnum.getText返回结果：" + result);
            return result;
        }
    }

    /**
     * 处置状态枚举
     */
    public static class HandleStatusEnum {
        /** 未处置 */
        public static final String UNHANDLED = "0";
        /** 已处置 */
        public static final String HANDLED = "1";
        /** 草稿 */
        public static final String DRAFT = "2";

        /**
         * 获取处置状态显示文本
         */
        public static String getText(String value) {
            System.out.println("HandleStatusEnum.getText被调用，参数value=" + value);
            String result;

            // 如果是数字，从字典获取标签
            if (value != null && value.matches("\\d+")) {
                result = DictUtils.getDictLabel("handle_status_enum", value, "");
            } else {
                // 如果已经是文本，尝试反向查找字典值，如果找不到则直接返回原文本
                String dictValue = DictUtils.getDictValue("handle_status_enum", value, "");
                if (!dictValue.isEmpty()) {
                    // 找到对应的字典值，转为标签
                    result = DictUtils.getDictLabel("handle_status_enum", dictValue, value);
                } else {
                    // 未找到对应字典值，直接返回原文本
                    result = value;
                }
            }

            System.out.println("HandleStatusEnum.getText返回结果：" + result);
            return result;
        }
    }

    private String personName; // 人员名称
    private String warningType; // 预警类型
    private String warningContent; // 预警内容
    private Date warningTime; // 预警时间
    private String alarmRecord; // 报警记录
    private Date alarmTime; // 报警时间
    private String triggerReason; // 触发原因
    private String handler; // 处置人
    private Date handleTime; // 处置时间
    private String handleProcess; // 处置过程
    private String handleStatus; // 处置状态
    private String attachment; // 附件路径
    private Long disposalDuration; // 处置时长
    private String deviceId; // 设备ID
    private String idCard; // 身份证号
    private String frontAlarm; // 前端弹框提示
    private String type; // 告警类型，与alarm_config表的alarm_key匹配
    private String x; // x坐标
    private String y; // y坐标
    private String hazardCategory; // 危险源类别
    private String location; // 位置
    private String area; // 区域

    // 用于显示的属性，不对应数据库字段
    private String workGroupName; // 班组名称
    private String warningTypeText; // 预警类型显示文本
    private String handleStatusText; // 处置状态显示文本
    private String areaName; // 位置信息

    // 查询条件，不对应数据库字段
    private transient boolean excludeSOS; // 是否排除一键SOS预警
    private transient boolean excludeAttendance; // 是否排除考勤打卡预警
    private transient boolean excludeGateEntry; // 是否排除进入大门预警
    private Date beginAlarmTime; // 查询开始预警/报警时间
    private Date endAlarmTime; // 查询结束预警/报警时间
    private String personType ;
    private List<String> warningContentList;
    private Date startDate;
    private Date endDate;

    private boolean isMasterDataAlarm = false;  //主数据看板，今日报警总数只统计：静默、跌落、应急呼叫这三个预警类型

    // 用于存储额外数据的Map
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> extraData = new HashMap<>();

    public SwmWarningManagement() {
        this(null);
        this.handleStatus = HandleStatusEnum.UNHANDLED; // 默认未处置
        // 默认过滤掉这三种类型的预警
        this.excludeSOS = true;
        this.excludeAttendance = true;
        this.excludeGateEntry = true;
    }

    public SwmWarningManagement(String id) {
        super(id);
    }

    @Length(min = 0, max = 100, message = "人员名称不能超过100个字符")
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    @Length(min = 0, max = 20, message = "预警类型不能超过20个字符")
    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }

    /**
     * 获取预警类型显示文本
     */
    public String getWarningTypeText() {
        if (this.warningTypeText == null && this.warningType != null) {
            // 修复warningType=1时显示为"主动报警"的问题
            if ("1".equals(this.warningType)) {
                this.warningTypeText = "主动报警"; // 直接使用硬编码值，绕过字典查询
            } else {
                this.warningTypeText = WarningTypeEnum.getText(this.warningType);
            }
        }
        return this.warningTypeText;
    }

    public void setWarningTypeText(String warningTypeText) {
        this.warningTypeText = warningTypeText;
    }

    @Length(min = 0, max = 50, message = "预警内容不能超过50个字符")
    public String getWarningContent() {
        return warningContent;
    }

    public void setWarningContent(String warningContent) {
        this.warningContent = warningContent;
    }

    public Date getWarningTime() {
        return warningTime;
    }

    public void setWarningTime(Date warningTime) {
        this.warningTime = warningTime;
    }

    @Length(min = 0, max = 200, message = "报警记录不能超过200个字符")
    public String getAlarmRecord() {
        return alarmRecord;
    }

    public void setAlarmRecord(String alarmRecord) {
        this.alarmRecord = alarmRecord;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(String triggerReason) {
        this.triggerReason = triggerReason;
    }

    @Length(min = 0, max = 100, message = "处置人不能超过100个字符")
    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public String getHandleProcess() {
        return handleProcess;
    }

    public void setHandleProcess(String handleProcess) {
        this.handleProcess = handleProcess;
    }

    @Length(min = 0, max = 2, message = "处置状态不能超过2个字符")
    public String getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(String handleStatus) {
        this.handleStatus = handleStatus;
    }

    /**
     * 获取处置状态显示文本
     */
    public String getHandleStatusText() {
        if (this.handleStatusText == null && this.handleStatus != null) {
            this.handleStatusText = HandleStatusEnum.getText(this.handleStatus);
        }
        return this.handleStatusText;
    }

    public void setHandleStatusText(String handleStatusText) {
        this.handleStatusText = handleStatusText;
    }

    // mediumtext类型，无需长度限制
    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    @Length(min = 0, max = 64, message = "设备ID不能超过64个字符")
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Length(min = 0, max = 18, message = "身份证号不能超过18个字符")
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    @Length(min = 0, max = 1, message = "前端弹框提示不能超过1个字符")
    public String getFrontAlarm() {
        return frontAlarm;
    }

    public void setFrontAlarm(String frontAlarm) {
        this.frontAlarm = frontAlarm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Length(min = 0, max = 50, message = "x坐标不能超过50个字符")
    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    @Length(min = 0, max = 50, message = "y坐标不能超过50个字符")
    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    @Length(min = 0, max = 100, message = "班组名称不能超过100个字符")
    public String getWorkGroupName() {
        return workGroupName;
    }

    public void setWorkGroupName(String workGroupName) {
        this.workGroupName = workGroupName;
    }

    // 添加预警时间不为空的查询条件
    public Boolean getWarningTime_isNotNull() {
        return sqlMap.getWhere().getValue("warning_time", QueryType.IS_NOT_NULL);
    }

    public void setWarningTimeIsNotNull(Boolean isNotNull) {
        if (isNotNull != null && isNotNull) {
            sqlMap.getWhere().and("warning_time", QueryType.IS_NOT_NULL, null);
        }
    }

    // 添加报警时间不为空的查询条件
    public Boolean getAlarmTime_isNotNull() {
        return sqlMap.getWhere().getValue("alarm_time", QueryType.IS_NOT_NULL);
    }

    public void setAlarmTimeIsNotNull(Boolean isNotNull) {
        if (isNotNull != null && isNotNull) {
            sqlMap.getWhere().and("alarm_time", QueryType.IS_NOT_NULL, null);
        }
    }

    /**
     * 获取临时数据
     */
    public Map<String, Object> getExtraData() {
        return extraData;
    }

    /**
     * 设置临时数据
     */
    public void setExtraData(String key, Object value) {
        extraData.put(key, value);
    }

    /**
     * 获取指定键的临时数据
     */
    public Object getExtraDataValue(String key) {
        return extraData.get(key);
    }

    // 新增 excludeSOS 的 getter/setter
    public boolean isExcludeSOS() {
        return excludeSOS;
    }

    public void setExcludeSOS(boolean excludeSOS) {
        this.excludeSOS = excludeSOS;
    }

    // 新增 excludeAttendance 的 getter/setter
    public boolean isExcludeAttendance() {
        return excludeAttendance;
    }

    public void setExcludeAttendance(boolean excludeAttendance) {
        this.excludeAttendance = excludeAttendance;
    }

    // 新增 excludeGateEntry 的 getter/setter
    public boolean isExcludeGateEntry() {
        return excludeGateEntry;
    }

    public void setExcludeGateEntry(boolean excludeGateEntry) {
        this.excludeGateEntry = excludeGateEntry;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    @Length(min = 0, max = 50, message = "处置时长不能超过50个字符")
    public Long getDisposalDuration() {
        return disposalDuration;
    }

    public void setDisposalDuration(Long disposalDuration) {
        this.disposalDuration = disposalDuration;
    }

    public String getHazardCategory() {
        return hazardCategory;
    }

    public void setHazardCategory(String hazardCategory) {
        this.hazardCategory = hazardCategory;
    }

    @Length(min = 0, max = 50, message = "位置不能超过50个字符")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Length(min = 0, max = 50, message = "区域不能超过50个字符")
    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
    @JsonIgnore
    public Date getBeginAlarmTime() {
        return beginAlarmTime;
    }

    public void setBeginAlarmTime(Date beginAlarmTime) {
        this.beginAlarmTime = beginAlarmTime;
    }

    @JsonIgnore
    public Date getEndAlarmTime() {
        return endAlarmTime;
    }

    public void setEndAlarmTime(Date endAlarmTime) {
        this.endAlarmTime = endAlarmTime;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public boolean isMasterDataAlarm() {
        return isMasterDataAlarm;
    }

    public void setMasterDataAlarm(boolean masterDataAlarm) {
        isMasterDataAlarm = masterDataAlarm;
    }

    public List<String> getWarningContentList() {
        return warningContentList;
    }

    public void setWarningContentList(List<String> warningContentList) {
        this.warningContentList = warningContentList;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
