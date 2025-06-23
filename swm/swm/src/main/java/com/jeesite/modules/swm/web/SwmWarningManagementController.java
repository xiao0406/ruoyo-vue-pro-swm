package com.jeesite.modules.swm.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import com.jeesite.modules.swm.entity.SwmHandleRecord;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.SwmHandleRecordService;
import com.jeesite.modules.swm.service.SwmWarningManagementService;
import com.jeesite.modules.swm.dao.SwmWarningManagementDao;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.utils.R;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 添加需要的导入语句
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.utils.UserUtils;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.common.utils.excel.ExcelExport;

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
        
        // 添加排除一键SOS的条件
        swmWarningManagement.setExcludeSOS(true);
        
        logger.info("查询参数: personName={}, warningType={}, warningContent={}, handleStatus={}, excludeSOS=true",
            swmWarningManagement.getPersonName(),
            swmWarningManagement.getWarningType(),
            swmWarningManagement.getWarningContent(),
            swmWarningManagement.getHandleStatus());

        // 调用服务层方法，使用混合查询获取数据（时序数据库 + MySQL）
        // 时区调整已在SQL查询中完成，无需再次调整
        Page<SwmWarningManagement> resultPage = swmWarningManagementService.hybridFindPage(page, swmWarningManagement);
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
            }
            
            logger.info("返回数据中，来自MySQL的记录: {}条，来自时序数据库的记录: {}条", mysqlCount, tdEngineCount);
        } else {
            logger.info("返回数据为空或没有记录");
        }

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
            // 先从MySQL数据库查询
            logger.info("从MySQL数据库查询预警记录，ID: {}", swmWarningManagement.getId());
            SwmWarningManagement mysqlRecord = swmWarningManagementDao.findInMySqlByIdAndIdCard(swmWarningManagement.getId(), null);
            
            // 如果MySQL中没有找到，再从时序数据库查询
            if (mysqlRecord == null) {
                logger.info("MySQL中未找到记录，尝试从时序数据库查询");
                mysqlRecord = swmWarningManagementService.get(swmWarningManagement.getId());
            } else {
                logger.info("在MySQL中找到预警记录");
            }
            
            // 如果找到了记录
            if (mysqlRecord != null) {
                Map<String, Object> warningData = new HashMap<>();
                
                // 复制基本属性
                warningData.put("id", mysqlRecord.getId());
                
                // 修改personName格式，添加idCard信息和触发的预警内容
                String idCard = mysqlRecord.getIdCard() != null ? mysqlRecord.getIdCard() : "未知";
                String originalPersonName = mysqlRecord.getPersonName() != null ? mysqlRecord.getPersonName() : "未知人员";
                String warningContent = mysqlRecord.getWarningContent() != null ? mysqlRecord.getWarningContent() : "未知预警";
                
                // 处理预警内容，如果是数字则转换为对应的文本
                if (warningContent != null && warningContent.matches("\\d+")) {
                    warningContent = DictUtils.getDictLabel("warning_content_enum", warningContent, warningContent);
                }
                
                // 新的personName格式：【idCard】某某触发warningContent
                String formattedPersonName = "【" + idCard + "】" + originalPersonName + "触发" + warningContent;
                warningData.put("personName", formattedPersonName);
                
                // 预警时间已在SQL中调整时区，这里直接使用
                warningData.put("warningTime", mysqlRecord.getWarningTime());
                
                warningData.put("alarmRecord", mysqlRecord.getAlarmRecord());
                
                // 报警时间已在SQL中调整时区，这里直接使用
                warningData.put("alarmTime", mysqlRecord.getAlarmTime());
                
                warningData.put("triggerReason", mysqlRecord.getTriggerReason());
                warningData.put("handler", mysqlRecord.getHandler());
                warningData.put("handleTime", mysqlRecord.getHandleTime());
                warningData.put("handleProcess", mysqlRecord.getHandleProcess());
                warningData.put("deviceId", mysqlRecord.getDeviceId());
                warningData.put("idCard", mysqlRecord.getIdCard());

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
    public String process(String id, String handler, String handleTime, String handleProcess, String handleStatus, String attachment) {
       
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
        handleRecord.setAlarmTime(swmWarningManagement.getAlarmTime() != null ? 
                swmWarningManagement.getAlarmTime() : swmWarningManagement.getWarningTime());
        
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
     * 2. notificationList：只需通知的告警列表（配置为enableAlarm=1，包含needConfirm=0和needConfirm=1的告警）
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
                
                // 确保warningTypeText正确显示
                String warningTypeText;
                if ("2".equals(item.getWarningType())) {
                    warningTypeText = "被动报警";
                } else if ("1".equals(item.getWarningType())) {
                    warningTypeText = "主动报警";
                } else {
                    // 尝试从字典获取
                    warningTypeText = DictUtils.getDictLabel("warning_type_enum", item.getWarningType(), item.getWarningTypeText());
                    // 如果仍然为空，设置默认值
                    if (warningTypeText == null || warningTypeText.isEmpty() || warningTypeText.equals(item.getWarningType())) {
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
                
                // 确保warningTypeText正确显示
                String warningTypeText;
                if ("2".equals(item.getWarningType())) {
                    warningTypeText = "被动报警";
                } else if ("1".equals(item.getWarningType())) {
                    warningTypeText = "主动报警";
                } else {
                    // 尝试从字典获取
                    warningTypeText = DictUtils.getDictLabel("warning_type_enum", item.getWarningType(), item.getWarningTypeText());
                    // 如果仍然为空，设置默认值
                    if (warningTypeText == null || warningTypeText.isEmpty() || warningTypeText.equals(item.getWarningType())) {
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
    public Page<SwmWarningManagement> sosListData(SwmWarningManagement swmWarningManagement, HttpServletRequest request, HttpServletResponse response) {
        // 创建分页对象
        Page<SwmWarningManagement> page = new Page<>(request, response);

        // 确保只筛选一键SOS的预警数据
        if (swmWarningManagement == null) {
            swmWarningManagement = new SwmWarningManagement();
        }
        
        // 设置固定的warningContent为"一键SOS"
        swmWarningManagement.setWarningContent("一键SOS");
        
        logger.info("查询SOS报警参数: personName={}, warningType={}, warningContent={}, handleStatus={}",
            swmWarningManagement.getPersonName(),
            swmWarningManagement.getWarningType(),
            swmWarningManagement.getWarningContent(),
            swmWarningManagement.getHandleStatus());

        // 调用服务层方法，使用混合查询获取数据（时序数据库 + MySQL）
        // 时区调整已在SQL查询中完成，无需再次调整
        Page<SwmWarningManagement> resultPage = swmWarningManagementService.hybridFindPage(page, swmWarningManagement);
        logger.info("SOS报警查询完成，返回数据总条数: {}", resultPage != null ? resultPage.getCount() : 0);

        return resultPage;
    }
    

    

}