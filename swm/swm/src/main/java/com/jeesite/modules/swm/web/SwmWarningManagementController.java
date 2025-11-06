package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.dao.SwmWarningManagementDao;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import com.jeesite.modules.swm.entity.SwmHandleRecord;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.utils.R;
import com.jeesite.modules.vo.SwmAlarmConfigDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预警管理表Controller
 *
 * @author zwf
 * @version 2025-05-16
 */
@Controller
@RequestMapping(value = "${adminPath}/warningManagement")
@Api(tags = "预警管理")
public class SwmWarningManagementController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SwmWarningManagementController.class);

    @Autowired
    private SwmWarningManagementService swmWarningManagementService;

    @Autowired
    private SwmHandleRecordService swmHandleRecordService;

    @Autowired
    private SwmWarningManagementDao swmWarningManagementDao;

    @Autowired
    private SwmPersonService swmPersonService;

    @Autowired
    private SwmAlarmConfigService swmAlarmConfigService;
    @Autowired
    private SwmSendZjtService swmSendZjtService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmWarningManagement get(String id, boolean isNewRecord) {
        return swmWarningManagementService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmWarningManagement swmWarningManagement, Model model) {
        model.addAttribute("swmWarningManagement", swmWarningManagement);
        return "modules/swm/swmWarningManagementList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmWarningManagement> listData(SwmWarningManagement swmWarningManagement, HttpServletRequest request,
            HttpServletResponse response) {
        // 创建分页对象
        Page<SwmWarningManagement> page = new Page<>(request, response);

        // 确保对象不为空
        if (swmWarningManagement == null) {
            swmWarningManagement = new SwmWarningManagement();
        }

        // 处理时间范围查询条件
        String beginTime = request.getParameter("timeRange[0]");
        String endTime = request.getParameter("timeRange[1]");

        if (com.jeesite.common.lang.StringUtils.isNotBlank(beginTime) && com.jeesite.common.lang.StringUtils.isNotBlank(endTime)) {
            // 设置开始时间和结束时间条件
            swmWarningManagement.setBeginAlarmTime(DateUtils.parseDate(beginTime));
            swmWarningManagement.setEndAlarmTime(DateUtils.parseDate(endTime));
        }

        logger.info(
                "查询参数: id={}, personName={}, warningType={}, warningContent={}, handleStatus={}, excludeSOS={}, excludeAttendance={}, excludeGateEntry={}, beginAlarmTime={}, endAlarmTime={}",
                swmWarningManagement.getId(),
                swmWarningManagement.getPersonName(),
                swmWarningManagement.getWarningType(),
                swmWarningManagement.getWarningContent(),
                swmWarningManagement.getHandleStatus(),
                swmWarningManagement.isExcludeSOS(),
                swmWarningManagement.isExcludeAttendance(),
                swmWarningManagement.isExcludeGateEntry(),
                swmWarningManagement.getBeginAlarmTime(),
                swmWarningManagement.getEndAlarmTime());

        // 调用服务层方法，仅从 TDengine 查询数据
        // 时区调整已在SQL查询中完成，无需再次调整
        Page<SwmWarningManagement> resultPage = swmWarningManagementService.tdEngineFindPage(page, swmWarningManagement);
        logger.info("查询完成，数据中的时区调整已在SQL中进行");

        // 添加日志检查返回的数据
        if (resultPage != null && resultPage.getList() != null && !resultPage.getList().isEmpty()) {
            logger.info("返回数据总条数: {}", resultPage.getCount());

            // 检查数据是否来自MySQL还是时序数据库
            int mysqlCount = 0;
            int tdEngineCount = 0;

            for (SwmWarningManagement item : resultPage.getList()) {
                if (item.getDeviceId() != null || item.getIdCard() != null) {
                    mysqlCount++;
                } else {
                    tdEngineCount++;
                }

                // 确保处置时长字段即使为0也返回
                if (item.getDisposalDuration() == null) {
                    item.setDisposalDuration(0L);
                    logger.debug("ID: {}, 处置时长为空，设置为0", item.getId());
                } else {
                    logger.debug("ID: {}, 处置时长: {}", item.getId(), item.getDisposalDuration());
                }

                // 记录坐标信息，确保即使为空也在日志中显示
                logger.debug("ID: {}, x坐标: {}, y坐标: {}",
                        item.getId(),
                        item.getX() != null ? item.getX() : "null",
                        item.getY() != null ? item.getY() : "null");
            }

            logger.info("返回数据中，来自MySQL的记录: {}条，来自时序数据库的记录: {}条", mysqlCount, tdEngineCount);
        } else {
            logger.info("返回数据为空或没有记录");
        }

        return resultPage;
    }

    /**
     * 查询分页数据（包含所有类型的预警，不过滤）
     */
    @RequestMapping(value = "listDataWithAll")
    @ResponseBody
    @ApiOperation("查询分页数据（包含所有类型）")
    public Page<SwmWarningManagement> listDataWithAll(SwmWarningManagement swmWarningManagement, HttpServletRequest request, HttpServletResponse response) {
        // 创建分页对象
        Page<SwmWarningManagement> page = swmWarningManagement.getPage();
        if (page == null) {
            page = new Page<>(request, response);
            swmWarningManagement.setPage(page);
        }

        // 显式设置不过滤任何类型的预警
        swmWarningManagement.setExcludeSOS(false);
        swmWarningManagement.setExcludeAttendance(false);
        swmWarningManagement.setExcludeGateEntry(false);

        logger.info(
                "查询所有类型预警参数: id={}, personName={}, warningType={}, warningContent={}, handleStatus={}, excludeSOS=false, excludeAttendance=false, excludeGateEntry=false",
                swmWarningManagement.getId(),
                swmWarningManagement.getPersonName(),
                swmWarningManagement.getWarningType(),
                swmWarningManagement.getWarningContent(),
                swmWarningManagement.getHandleStatus());

        // 调用服务层方法，仅从 TDengine 查询数据
        Page<SwmWarningManagement> resultPage = swmWarningManagementService.tdEngineFindPage(page, swmWarningManagement);
        logger.info("查询所有类型预警完成");

        return resultPage;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmWarningManagement swmWarningManagement) {
        Map<String, Object> result = new HashMap<>();

        if (swmWarningManagement != null && swmWarningManagement.getId() != null) {
            // 直接从时序数据库查询
            logger.info("从时序数据库查询预警记录，ID: {}", swmWarningManagement.getId());
            SwmWarningManagement mysqlRecord = swmWarningManagementService.get(swmWarningManagement.getId());

            // 如果找到了记录
            if (mysqlRecord != null) {
                Map<String, Object> warningData = new HashMap<>();

                // 复制基本属性
                warningData.put("id", mysqlRecord.getId());

                // 修改personName格式，添加idCard信息和触发的预警内容
                String idCard = mysqlRecord.getIdCard() != null ? mysqlRecord.getIdCard() : "未知";
                String originalPersonName = mysqlRecord.getPersonName() != null ? mysqlRecord.getPersonName() : "未知人员";
                String warningContent = mysqlRecord.getWarningContent() != null ? mysqlRecord.getWarningContent()
                        : "未知预警";

                // 处理预警内容，如果是数字则转换为对应的文本
                if (warningContent != null && warningContent.matches("\\d+")) {
                    warningContent = DictUtils.getDictLabel("warning_content_enum", warningContent, warningContent);
                }

                // 新的personName格式：【idCard】某某触发warningContent
                String formattedPersonName = originalPersonName;
                warningData.put("personName", formattedPersonName);

                // 预警时间已在SQL中调整时区，这里直接使用
                warningData.put("warningTime", mysqlRecord.getWarningTime());

                warningData.put("alarmRecord", mysqlRecord.getAlarmRecord());

                // 报警时间已在SQL中调整时区，这里直接使用
                warningData.put("alarmTime", mysqlRecord.getAlarmTime());

                warningData.put("triggerReason", mysqlRecord.getTriggerReason());
                warningData.put("handler", mysqlRecord.getHandler());
                
                // 处理 handle_time，手动添加8小时时区调整（因为TDengine查询时未调整）
                if (mysqlRecord.getHandleTime() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(mysqlRecord.getHandleTime());
                    cal.add(Calendar.HOUR_OF_DAY, 8);
                    warningData.put("handleTime", cal.getTime());
                } else {
                    warningData.put("handleTime", mysqlRecord.getHandleTime());
                }
                
                warningData.put("handleProcess", mysqlRecord.getHandleProcess());
                warningData.put("deviceId", mysqlRecord.getDeviceId());
                warningData.put("idCard", mysqlRecord.getIdCard());

                // 确保返回x和y坐标信息
                warningData.put("x", mysqlRecord.getX());
                warningData.put("y", mysqlRecord.getY());

                // 确保返回位置和区域信息
                warningData.put("location", mysqlRecord.getLocation());
                warningData.put("area", mysqlRecord.getArea());

                // 确保返回危险源类别信息
                warningData.put("hazardCategory", mysqlRecord.getHazardCategory());
                logger.debug("表单查询 ID: {}, 危险源类别: {}", mysqlRecord.getId(), mysqlRecord.getHazardCategory());

                // 确保处置时长(disposal_duration)即使为0也返回
                if (mysqlRecord.getDisposalDuration() == null) {
                    warningData.put("disposalDuration", 0L);
                    logger.debug("表单查询 ID: {}, 处置时长为空，设置为0", mysqlRecord.getId());
                } else {
                    warningData.put("disposalDuration", mysqlRecord.getDisposalDuration());
                    logger.debug("表单查询 ID: {}, 处置时长: {}", mysqlRecord.getId(), mysqlRecord.getDisposalDuration());
                }

                // 保存原始值，用于调试
                String origHandleStatus = mysqlRecord.getHandleStatus();
                String origWarningType = mysqlRecord.getWarningType();
                String origWarningContent = mysqlRecord.getWarningContent();

                logger.debug("原始数据: handleStatus={}, warningType={}, warningContent={}",
                        origHandleStatus, origWarningType, origWarningContent);

                // 将handleStatus直接转换为文本值返回
                String handleStatusLabel;
                if (origHandleStatus != null && origHandleStatus.matches("\\d+")) {
                    handleStatusLabel = DictUtils.getDictLabel("handle_status_enum", origHandleStatus, "");
                } else {
                    handleStatusLabel = origHandleStatus;
                }
                warningData.put("handleStatus", handleStatusLabel);
                // 同时保留原始值，便于前端处理
                warningData.put("handleStatusValue", origHandleStatus);

                warningData.put("attachment", mysqlRecord.getAttachment());
                warningData.put("remarks", mysqlRecord.getRemarks());

                // 处理预警类型 - 直接使用文本值
                String warningTypeLabel;
                if (origWarningType != null && origWarningType.matches("\\d+")) {
                    warningTypeLabel = DictUtils.getDictLabel("warning_type_enum", origWarningType, "");
                } else {
                    warningTypeLabel = origWarningType;
                }
                warningData.put("warningType", warningTypeLabel);
                // 同时保留原始值，便于前端处理
                warningData.put("warningTypeValue", origWarningType);

                // 处理预警内容
                String warningContentLabel;
                if (origWarningContent != null && origWarningContent.matches("\\d+")) {
                    warningContentLabel = DictUtils.getDictLabel("warning_content_enum", origWarningContent,
                            origWarningContent);
                } else {
                    warningContentLabel = origWarningContent;
                }
                warningData.put("warningContent", warningContentLabel);
                // 同时保留原始值，便于前端处理
                warningData.put("warningContentValue", origWarningContent);

                logger.debug("转换后: handleStatus={}, warningType={}, warningContent={}",
                        handleStatusLabel, warningTypeLabel, warningContentLabel);

                result.putAll(warningData);
            }
        }
        return result;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmWarningManagement swmWarningManagement) {
        swmWarningManagementService.save(swmWarningManagement);
        //根据预警报警记录信息判断是否要推送中建通
        logger.info("预警报警记录推送中间通，预警ID：{}", swmWarningManagement.getId());
        SwmAlarmConfig swmAlarmConfig = swmAlarmConfigService.getByAlarmName(swmWarningManagement.getWarningContent());
        logger.info("预警报警记录推送中间通，查询配置查询：{}", swmAlarmConfig);
        if(swmAlarmConfig != null && swmAlarmConfig.getIsSendZjt().equals(1)){
            logger.info("预警报警记录推送中间通，开始推送处理：{}", swmAlarmConfig.getIsSendZjt());
            sendZjt(swmAlarmConfig,swmWarningManagement);
            logger.info("预警报警记录推送中间通，推送成功！");
        }
        return renderResult(Global.TRUE, text("保存预警信息成功！"));
    }

    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmWarningManagement swmWarningManagement) {
        swmWarningManagementService.delete(swmWarningManagement);
        return renderResult(Global.TRUE, text("删除预警信息成功！"));
    }

    /**
     * 批量删除数据
     */
    @PostMapping(value = "deleteAll")
    @ResponseBody
    @ApiOperation("批量删除数据")
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmWarningManagement swmWarningManagement = swmWarningManagementService.get(id);
            if (swmWarningManagement != null) {
                swmWarningManagementService.delete(swmWarningManagement);
            }
        }
        return renderResult(Global.TRUE, text("批量删除预警信息成功！"));
    }

    /**
     * 获取枚举选项
     */
    @GetMapping(value = "enumOptions")
    @ResponseBody
    @ApiOperation("获取枚举选项")
    public Map<String, Object> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();

        // 预警类型选项
        Map<String, String> warningTypeOptions = new HashMap<>();
        List<DictData> warningTypeDictList = DictUtils.getDictList("warning_type_enum");
        for (DictData dict : warningTypeDictList) {
            warningTypeOptions.put(dict.getDictValue(), dict.getDictLabel());
        }
        result.put("warningTypeOptions", warningTypeOptions);

        // 预警内容选项
        Map<String, String> warningContentOptions = new HashMap<>();
        List<DictData> warningContentDictList = DictUtils.getDictList("warning_content_enum");
        for (DictData dict : warningContentDictList) {
            warningContentOptions.put(dict.getDictValue(), dict.getDictLabel());
        }
        result.put("warningContentOptions", warningContentOptions);

        // 处置状态选项
        Map<String, String> handleStatusOptions = new HashMap<>();
        List<DictData> handleStatusDictList = DictUtils.getDictList("handle_status_enum");
        for (DictData dict : handleStatusDictList) {
            handleStatusOptions.put(dict.getDictValue(), dict.getDictLabel());
        }
        result.put("handleStatusOptions", handleStatusOptions);

        return result;
    }

    /**
     * 处理预警
     */
    @PostMapping(value = "process")
    @ResponseBody
    @ApiOperation("处理预警")
    public String process(String id, String handler, String handleTime, String handleProcess, String handleStatus,
            String attachment) {

        logger.info("收到预警处理请求，预警ID：{}", id);

        // 解析处置时间
        Date handleTimeDate;
        if (handleTime != null && !handleTime.isEmpty()) {
            handleTimeDate = DateUtils.parseDate(handleTime);
        } else {
            handleTimeDate = new Date();
        }

        // 调用服务层方法处理预警并向MySQL插入数据
        boolean result = swmWarningManagementService.processWarningToMySql(
                id, handler, handleTimeDate, handleProcess, handleStatus, attachment);

        if (!result) {
            return renderResult(Global.FALSE, text("预警处置失败！"));
        }

        // 处置记录表中创建新记录
        SwmWarningManagement swmWarningManagement = swmWarningManagementService.get(id);
        SwmHandleRecord handleRecord = SwmHandleRecord.createNewRecord();
        handleRecord.setWarningId(id);

        // 获取预警内容的文本值
        String warningContentText = DictUtils.getDictLabel("warning_content_enum",
                swmWarningManagement.getWarningContent(), swmWarningManagement.getWarningContent());

        // 构造处置记录名称：处置personName触发warningContent
        String recordName = "处置" + swmWarningManagement.getPersonName() + "触发" + warningContentText;
        handleRecord.setRecordName(recordName);

        // 构造预警记录：personName触发warningContent (不带"处置"前缀)
        String warningRecord = swmWarningManagement.getPersonName() + "触发" + warningContentText;
        handleRecord.setWarningRecord(warningRecord);

        // 设置报警时间
        handleRecord.setAlarmTime(swmWarningManagement.getAlarmTime() != null ? swmWarningManagement.getAlarmTime()
                : swmWarningManagement.getWarningTime());

        // 设置处置信息
        handleRecord.setHandler(handler);
        handleRecord.setHandleTime(handleTimeDate);
        handleRecord.setHandleProcess(handleProcess);
        handleRecord.setHandleStatus(handleStatus);

        // 设置附件路径
        if (attachment != null && !attachment.isEmpty()) {
            handleRecord.setAttachment(attachment);
        }

        // 保存处置记录
        swmHandleRecordService.save(handleRecord);

        return renderResult(Global.TRUE, text("预警处置成功！"));
    }

    /**
     * 获取需要弹窗显示的告警
     * 返回两类告警数据：
     * 1. confirmList：需要确认的告警列表（配置为enableAlarm=1且needConfirm=1）
     * 2.
     * notificationList：只需通知的告警列表（配置为enableAlarm=1，包含needConfirm=0和needConfirm=1的告警）
     */
    @RequestMapping(value = "getPopupWarnings")
    @ResponseBody
    public Map<String, Object> getPopupWarnings() {
        Map<String, Object> resultData = swmWarningManagementService.getPopupWarnings();

        // 处理需要确认的告警数据
        @SuppressWarnings("unchecked")
        List<SwmWarningManagement> confirmList = (List<SwmWarningManagement>) resultData.get("confirmList");
        List<Map<String, Object>> confirmResult = new ArrayList<>();

        if (confirmList != null && !confirmList.isEmpty()) {
            for (SwmWarningManagement item : confirmList) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", item.getId());
                map.put("personName", item.getPersonName());
                map.put("warningType", item.getWarningType());
                map.put("triggerReason", item.getTriggerReason());

                // 确保warningTypeText正确显示
                String warningTypeText;
                if ("2".equals(item.getWarningType())) {
                    warningTypeText = "被动报警";
                } else if ("1".equals(item.getWarningType())) {
                    warningTypeText = "主动报警";
                } else {
                    // 尝试从字典获取
                    warningTypeText = DictUtils.getDictLabel("warning_type_enum", item.getWarningType(),
                            item.getWarningTypeText());
                    // 如果仍然为空，设置默认值
                    if (warningTypeText == null || warningTypeText.isEmpty()
                            || warningTypeText.equals(item.getWarningType())) {
                        warningTypeText = "被动报警"; // 默认值
                    }
                }
                map.put("warningTypeText", warningTypeText);

                map.put("warningContent", item.getWarningContent());
                map.put("warningTime", item.getWarningTime());

                // 附加告警配置信息
                SwmAlarmConfig config = (SwmAlarmConfig) item.getExtraDataValue("alarmConfig");
                if (config != null) {
                    map.put("alarmName", config.getAlarmName());
                    map.put("dialogPosition", config.getDialogPosition());
                }

                confirmResult.add(map);
            }
        }

        // 处理通知类告警数据
        @SuppressWarnings("unchecked")
        List<SwmWarningManagement> notificationList = (List<SwmWarningManagement>) resultData.get("notificationList");
        List<Map<String, Object>> notificationResult = new ArrayList<>();

        if (notificationList != null && !notificationList.isEmpty()) {
            for (SwmWarningManagement item : notificationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", item.getId());
                map.put("personName", item.getPersonName());
                map.put("warningType", item.getWarningType());
                map.put("triggerReason", item.getTriggerReason());

                // 确保warningTypeText正确显示
                String warningTypeText;
                if ("2".equals(item.getWarningType())) {
                    warningTypeText = "被动报警";
                } else if ("1".equals(item.getWarningType())) {
                    warningTypeText = "主动报警";
                } else {
                    // 尝试从字典获取
                    warningTypeText = DictUtils.getDictLabel("warning_type_enum", item.getWarningType(),
                            item.getWarningTypeText());
                    // 如果仍然为空，设置默认值
                    if (warningTypeText == null || warningTypeText.isEmpty()
                            || warningTypeText.equals(item.getWarningType())) {
                        warningTypeText = "被动报警"; // 默认值
                    }
                }
                map.put("warningTypeText", warningTypeText);

                map.put("warningContent", item.getWarningContent());
                map.put("warningTime", item.getWarningTime());

                // 附加告警配置信息
                SwmAlarmConfig config = (SwmAlarmConfig) item.getExtraDataValue("alarmConfig");
                if (config != null) {
                    map.put("alarmName", config.getAlarmName());
                }

                // 添加是否需要确认的标记
                Boolean needConfirm = (Boolean) item.getExtraDataValue("needConfirm");
                map.put("needConfirm", needConfirm != null ? needConfirm : false);

                notificationResult.add(map);
            }
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        Map<String, Object> data = new HashMap<>();
        data.put("confirmList", confirmResult);
        data.put("notificationList", notificationResult);
        result.put("data", data);

        logger.info("返回告警数据，confirmList: {}条, notificationList: {}条",
                confirmResult.size(), notificationResult.size());

        return result;
    }

    /**
     * 确认告警
     */
    @RequestMapping(value = "confirmWarning")
    @ResponseBody
    @ApiOperation("确认告警")
    public R confirmWarning(String id) {
        logger.info("接收到告警确认请求，ID: {}", id);

        if (id == null || id.isEmpty()) {
            return R.fail("告警ID不能为空");
        }

        boolean success = swmWarningManagementService.confirmWarning(id);
        if (success) {
            return R.ok("告警已确认");
        } else {
            return R.fail("告警确认失败");
        }
    }

    /**
     * 查询SOS报警列表数据
     */
    @RequestMapping(value = "sosListData")
    @ResponseBody
    @ApiOperation("查询SOS报警列表数据")
    public Page<SwmWarningManagement> sosListData(SwmWarningManagement swmWarningManagement, HttpServletRequest request,
            HttpServletResponse response) {
        // 创建分页对象
        Page<SwmWarningManagement> page = new Page<>(request, response);

        // 确保对象不为空
        if (swmWarningManagement == null) {
            swmWarningManagement = new SwmWarningManagement();
        }

        // 添加SOS条件
//        swmWarningManagement.setWarningContent("一键SOS报警");
        String warningContent = DictUtils.getDictLabel("warning_content_enum", "应急呼叫", "应急呼叫");
        swmWarningManagement.setWarningContent(warningContent);
        logger.info("SOS查询参数: personName={}, warningType={}, warningContent={}, handleStatus={}",
                swmWarningManagement.getPersonName(),
                swmWarningManagement.getWarningType(),
                swmWarningManagement.getWarningContent(),
                swmWarningManagement.getHandleStatus());

        // 调用服务层方法，仅从 TDengine 查询数据
        Page<SwmWarningManagement> resultPage = swmWarningManagementService.tdEngineFindPage(page, swmWarningManagement);

        // 确保处置时长即使为0也返回，并记录坐标信息
        if (resultPage != null && resultPage.getList() != null && !resultPage.getList().isEmpty()) {
            for (SwmWarningManagement item : resultPage.getList()) {
                // 确保处置时长字段即使为0也返回
                if (item.getDisposalDuration() == null) {
                    item.setDisposalDuration(0L);
                    logger.debug("SOS ID: {}, 处置时长为空，设置为0", item.getId());
                } else {
                    logger.debug("SOS ID: {}, 处置时长: {}", item.getId(), item.getDisposalDuration());
                }

                // 记录坐标信息，确保即使为空也在日志中显示
                logger.debug("SOS ID: {}, x坐标: {}, y坐标: {}",
                        item.getId(),
                        item.getX() != null ? item.getX() : "null",
                        item.getY() != null ? item.getY() : "null");
            }
        }

        return resultPage;
    }

    /**
     * 热力图-违规数量趋势 （数据来源-swm_warning_management-筛选warning_content=危险源报警的记录)
     * 
     * @param beginDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    @GetMapping("hazardWarningStatistics")
    @ResponseBody
    @ApiOperation(value = "热力图-违规数量趋势")
    public Map<String, Object> hazardSourceStatistics(@RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> result = new HashMap<>();
        List<DictData> hazardCategoryList = DictUtils.getDictList("hazard_category_enum");
        // 创建字典值(dictValue)到标签(dictLabelRaw)的映射
        Map<String, String> valueToLabelMap = hazardCategoryList.stream()
                .collect(Collectors.toMap(
                        DictData::getDictValue,
                        DictData::getDictLabelRaw,
                        (existing, replacement) -> existing)); // 如果有重复键，保留已存在的

        // 1. 危险源违规数量趋势折线图
        result.put("totalTrend", getHazardSourceTotalTrend(beginDate, endDate, true));

        // 2. 危险源类别TOP 10 柱状图
        result.put("categoryTop10", getHazardSourceCategoryTop10(beginDate, endDate, valueToLabelMap));

        // 3. 高频违规人员榜
        result.put("violationTop10", getViolationPersonTop10(beginDate, endDate));

        // 4. 危险源类别趋势折线图
        result.put("categoryTrend", getHazardSourceCategoryTrend(beginDate, endDate, valueToLabelMap, true));

        return result;
    }

    /**
     * 热力图-告警数量趋势 （数据来源-swm_warning_management-筛选warning_content != 危险源报警的记录)
     * 
     * @param beginDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    @GetMapping("nonHazardWarningStatistics")
    @ResponseBody
    @ApiOperation(value = "热力图-告警数量趋势")
    public Map<String, Object> nonHazardWarningStatistics(@RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> result = new HashMap<>();
        List<DictData> hazardCategoryList = DictUtils.getDictList("hazard_category_enum");
        // 创建字典值(dictValue)到标签(dictLabelRaw)的映射
        Map<String, String> valueToLabelMap = hazardCategoryList.stream()
                .collect(Collectors.toMap(
                        DictData::getDictValue,
                        DictData::getDictLabelRaw,
                        (existing, replacement) -> existing)); // 如果有重复键，保留已存在的

        // 1. 非危险源总数趋势折线图
        result.put("totalTrend", getHazardSourceTotalTrend(beginDate, endDate, false));

        // 2. 高频告警榜
        result.put("warningContentTop10", getWarningContentTop10(beginDate, endDate));

        // 3. 处置效率榜
        result.put("handleEfficiencyTop10", getHandleEfficiencyTop10(beginDate, endDate));

        // 4. 非危险源类别趋势折线图
        result.put("categoryTrend", getHazardSourceCategoryTrend(beginDate, endDate, valueToLabelMap, false));
        return result;
    }

    /**
     * 热力图-违规数量趋势 -最新危险源报警记录
     */
    @GetMapping("latestHazardSourceRecord")
    @ResponseBody
    @ApiOperation(value = "热力图-违规数量趋势-最新危险源报警记录")
    public List<SwmWarningManagement> latestHazardSourceRecord() {
        return swmWarningManagementDao.latestHazardSourceRecord(10);
    }

    /**
     * 热力图-违规数量趋势-中间数据 （数据来源-swm_warning_management-筛选warning_content = 危险源报警的记录)
     * 
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return
     */
    @GetMapping("hazardSourceCounts")
    @ResponseBody
    @ApiOperation(value = "热力图-危险源数量统计-(中间数据)")
    public Map<String, Object> getHazardSourceCounts(@RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate) {

        // 处理日期范围（默认近30天）
        DateRange dateRange = parseDateRange(beginDate, endDate);

        Map<String, Object> result = new HashMap<>();

        // 1. 时间范围内的危险源报警总数
        Long rangeCount = swmWarningManagementDao.countByDateRange(
                dateRange.getBeginDate(),
                dateRange.getEndDate(),
                true);
        result.put("rangeCount", rangeCount != null ? rangeCount : 0);

        // 2. 全部危险源报警总数（不限制时间范围）
        Long totalCount = swmWarningManagementDao.countTotal(true);
        result.put("totalCount", totalCount != null ? totalCount : 0);

        // 3. 违规人员数量（时间范围内）
        Long violatorCount = swmWarningManagementDao.countDistinctPersonByWarningContentAndDateRange(
                dateRange.getBeginDate(),
                dateRange.getEndDate(),
                true);
        result.put("violatorCount", violatorCount != null ? violatorCount : 0);
        return result;
    }

    /**
     * 热力图-告警数量趋势-中间数据（数据来源-swm_warning_management-筛选warning_content != 危险源报警的记录)
     * 
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return
     */
    @GetMapping("nonHazardSourceCounts")
    @ResponseBody
    @ApiOperation(value = "热力图-危险源数量统计-(中间数据)")
    public Map<String, Object> nonHazardSourceCounts(@RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate) {

        // 处理日期范围（默认近30天）
        DateRange dateRange = parseDateRange(beginDate, endDate);

        Map<String, Object> result = new HashMap<>();

        // 1. 时间范围内的非危险源报警总数
        Long rangeCount = swmWarningManagementDao.countByDateRange(
                dateRange.getBeginDate(),
                dateRange.getEndDate(),
                false);
        result.put("rangeCount", rangeCount != null ? rangeCount : 0);

        // 2. 全部非危险源报警总数（不限制时间范围）
        Long totalCount = swmWarningManagementDao.countTotal(false);
        result.put("totalCount", totalCount != null ? totalCount : 0);

        // 3. 处置告警数量（时间范围内已处置的）
        Long handledCount = swmWarningManagementDao.countHandledByDateRange(
                dateRange.getBeginDate(),
                dateRange.getEndDate());
        result.put("handledCount", handledCount != null ? handledCount : 0);

        // 4. 告警处置率（处置数量/总数量）
        double handleRate = 0.0;
        if (rangeCount != null && rangeCount > 0) {
            handleRate = (double) handledCount / rangeCount * 100;
        }
        result.put("handleRate", Double.parseDouble(String.format("%.2f", handleRate)));

        // 5. 今日新增告警数量(非危险源告警数量）
        Long todayCount = swmWarningManagementDao.countTodayNonHazardSource();

        result.put("todayCount", todayCount != null ? todayCount : 0);
        return result;
    }

    /**
     * 获取处置效率TOP10（平均处置时间最短）
     * 
     * @param beginDate 开始时间
     * @param endDate   结束时间
     * @return List<Map> 包含处置人、处理次数、平均处置时间(小时)
     */
    private List<Map<String, Object>> getHandleEfficiencyTop10(String beginDate, String endDate) {
        // 执行查询
        return swmWarningManagementDao.findHandleEfficiencyTop10(beginDate, endDate, 10);
    }

    /**
     * 获取危险源总数趋势数据
     */
    private List<Map<String, Object>> getHazardSourceTotalTrend(String beginDateStr, String endDateStr,
            boolean isHazardSourceAlarm) {
        // 解析日期参数
        DateRange dateRange = parseDateRange(beginDateStr, endDateStr);

        // 获取日期列表
        List<String> dateList = getDateList(dateRange.getBeginDate(), dateRange.getEndDate());

        // 一次性查询所有数据
        List<Map<String, Object>> dbResults = swmWarningManagementDao.countByDateRangeGroupByDay(
                dateRange.getBeginDate(),
                dateRange.getEndDate(),
                isHazardSourceAlarm);

        // 转换为按日期索引的Map
        Map<String, Integer> countMap = dbResults.stream()
                .collect(Collectors.toMap(
                        item -> (String) item.get("date"),
                        item -> ((Number) item.get("count")).intValue()));

        // 构建返回结果 - 使用显式类型声明
        List<Map<String, Object>> result = new ArrayList<>();
        for (String date : dateList) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("count", countMap.getOrDefault(date, 0));
            result.add(item);
        }

        return result;
    }

    /**
     * 获取危险源类别TOP 10数据
     */
    private List<Map<String, Object>> getHazardSourceCategoryTop10(String beginDate, String endDate,
            Map<String, String> valueToLabelMap) {
        List<Map<String, Object>> topCategories = swmWarningManagementDao.findTopCategories(beginDate, endDate, 10);
        return transformedCategoryDict(valueToLabelMap, topCategories);
    }

    /**
     * 获取预警内容 TOP 10数据 （非危险源报警）
     */
    private List<Map<String, Object>> getWarningContentTop10(String beginDate, String endDate) {
        return swmWarningManagementDao.findTopWarningContent(beginDate, endDate, 10);
    }

    /**
     * 获取高频违规人员TOP10
     * 
     * @param beginDate 开始时间（可为空）
     * @param endDate   结束时间（可为空）
     * @return List<Map> 包含人员姓名、违规次数、预警类型
     */
    private List<Map<String, Object>> getViolationPersonTop10(String beginDate, String endDate) {

        // 1. 执行分组统计查询
        List<Map<String, Object>> violationList = swmWarningManagementDao.findViolationPersonTop10(beginDate, endDate,
                10);

        // 2. 处理预警类型显示文本
        violationList.forEach(item -> {
            String warningType = (String) item.get("warningType");
            if (warningType != null && !warningType.isEmpty()) {
                // 分割字符串并处理每个类型
                String warningTypeText = Arrays.stream(warningType.split(","))
                        .map(String::trim) // 去除前后空格
                        .filter(s -> !s.isEmpty()) // 过滤空字符串
                        .map(SwmWarningManagement.WarningTypeEnum::getText) // 转换为文本
                        .filter(Objects::nonNull) // 过滤掉null值
                        .collect(Collectors.joining(",")); // 重新用逗号连接

                item.put("warningTypeText", warningTypeText);
            } else {
                item.put("warningTypeText", ""); // 处理null或空字符串情况
            }
        });

        return violationList;
    }

    /**
     * 获取字典值转换后的数据
     * 
     * @param valueToLabelMap 字典值转换
     * @param categoryCounts  类别数量
     * @return
     */
    private static List<Map<String, Object>> transformedCategoryDict(Map<String, String> valueToLabelMap,
            List<Map<String, Object>> categoryCounts) {
        // 转换categoryCounts中的category值
        List<Map<String, Object>> transformedCategoryCounts = categoryCounts.stream()
                .map(originalMap -> {
                    Map<String, Object> newMap = new HashMap<>(originalMap);
                    if (originalMap.containsKey("category")) {
                        String dictValue = (String) originalMap.get("category");
                        String dictLabel = valueToLabelMap.getOrDefault(dictValue, dictValue);
                        newMap.put("category", dictLabel);
                    }
                    return newMap;
                })
                .collect(Collectors.toList());
        return transformedCategoryCounts;
    }

    /**
     * 获取危险源类别趋势数据
     */
    private Map<String, List<Map<String, Object>>> getHazardSourceCategoryTrend(String beginDateStr, String endDateStr,
            Map<String, String> valueToLabelMap, boolean isHazardSourceAlarm) {
        // 解析日期参数
        DateRange dateRange = parseDateRange(beginDateStr, endDateStr);

        // 一次性查询所有类别的趋势数据
        List<Map<String, Object>> allData;
        if (isHazardSourceAlarm) {
            allData = transformedCategoryDict(valueToLabelMap, swmWarningManagementDao
                    .countHazardCategoryCategoryTrendByDateRange(dateRange.getBeginDate(), dateRange.getEndDate()));
        } else {
            allData = swmWarningManagementDao.countWarningContentTrendByDateRange(dateRange.getBeginDate(),
                    dateRange.getEndDate());
        }

        // 按类别分组（使用传统方式创建Map）
        Map<String, List<Map<String, Object>>> groupedData = allData.stream()
                .collect(Collectors.groupingBy(
                        item -> (String) item.get("category"),
                        Collectors.mapping(
                                item -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("date", item.get("date"));
                                    map.put("count", item.get("count"));
                                    return map;
                                },
                                Collectors.toList())));

        // 生成完整日期列表
        List<String> dateList = getDateList(dateRange.getBeginDate(), dateRange.getEndDate());

        // 补全缺失日期数据（使用传统方式创建Map）
        groupedData.forEach((category, data) -> {
            Map<String, Integer> dateCountMap = data.stream()
                    .collect(Collectors.toMap(
                            item -> (String) item.get("date"),
                            item -> ((Number) item.get("count")).intValue()));

            List<Map<String, Object>> completeData = dateList.stream()
                    .map(date -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", date);
                        map.put("count", dateCountMap.getOrDefault(date, 0));
                        return map;
                    })
                    .collect(Collectors.toList());

            groupedData.put(category, completeData);
        });

        return groupedData;
    }

    /**
     * 解析日期范围
     */
    private DateRange parseDateRange(String beginDateStr, String endDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = null;
        Date endDate = null;

        try {
            if (StringUtils.isNotBlank(beginDateStr)) {
                beginDate = sdf.parse(beginDateStr);
            }
            if (StringUtils.isNotBlank(endDateStr)) {
                endDate = sdf.parse(endDateStr);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式不正确，请使用yyyy-MM-dd格式");
        }

        // 默认近30天
        if (beginDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            beginDate = cal.getTime();
        }

        return new DateRange(beginDate, endDate);
    }

    /**
     * 生成日期列表
     */
    private List<String> getDateList(Date beginDate, Date endDate) {
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setTime(beginDate);

        while (!cal.getTime().after(endDate)) {
            dateList.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dateList;
    }

    /**
     * 日期范围内部类
     */
    private static class DateRange {
        private final Date beginDate;
        private final Date endDate;

        public DateRange(Date beginDate, Date endDate) {
            this.beginDate = beginDate;
            this.endDate = endDate;
        }

        public Date getBeginDate() {
            return beginDate;
        }

        public Date getEndDate() {
            return endDate;
        }
    }

    /**
     * 重新推送中建通
     */
    @PostMapping(value = "resendZjt")
    @ResponseBody
    @ApiOperation("预警报警记录重新推送中建通")
    public String resendZjt(@Validated SwmWarningManagement swmWarningManagement) {
        SwmWarningManagement swmWarning = swmWarningManagementService.get(swmWarningManagement.getId());
        //根据预警报警记录信息判断是否要推送中建通
        SwmAlarmConfig swmAlarmConfig = swmAlarmConfigService.getByAlarmName(swmWarning.getWarningContent());
        if(swmAlarmConfig != null && swmAlarmConfig.getIsSendZjt().equals(1)){
            Boolean result = sendZjt(swmAlarmConfig, swmWarning);
            if(result){
                return renderResult(Global.TRUE, text("预警信息推送中建通成功！"));
            }
        }
        return renderResult(Global.TRUE, text("预警信息推送中建通失败！"));
    }

    private Boolean sendZjt(SwmAlarmConfig swmAlarmConfig,SwmWarningManagement swmWarningManagement){
        Boolean result = true;
        //推送中建通
        SwmAlarmConfigDetailVO detail = new SwmAlarmConfigDetailVO();
        detail.setMainKey(swmAlarmConfig.getAlarmKey());
        detail.setContent(swmWarningManagement.getTriggerReason());
        logger.info("预警报警记录推送中间通，推送参数组装：{},{}", detail.getMainKey(),detail.getContent());
        try {
            swmSendZjtService.send(detail);
            logger.info("预警报警记录推送中间通，推送完成！");
        } catch (Exception e) {
            logger.error("预警报警记录推送中间通，预警ID：{}", e.toString());
            result =false;
        }
        return result;
    }
}
