package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHandleRecord;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.SwmHandleRecordService;
import com.jeesite.modules.swm.service.SwmWarningManagementService;
import com.jeesite.modules.sys.utils.DictUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 处置记录Controller
 * 
 * @author zwf
 * @version 2025-05-16
 */
@Controller
@RequestMapping(value = "${adminPath}/handleRecord")
@Api(value = "处置记录接口", tags = "处置记录接口")
public class SwmHandleRecordController extends BaseController {

    @Autowired
    private SwmHandleRecordService swmHandleRecordService;

    @Autowired
    private SwmWarningManagementService swmWarningManagementService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmHandleRecord get(String id, boolean isNewRecord) {
        return swmHandleRecordService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmHandleRecord swmHandleRecord, Model model) {
        model.addAttribute("swmHandleRecord", swmHandleRecord);
        return "modules/swm/swmHandleRecordList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmHandleRecord> listData(SwmHandleRecord swmHandleRecord, HttpServletRequest request,
            HttpServletResponse response) {
        // 创建分页对象
        Page<SwmHandleRecord> page = new Page<>(request, response);

        // 清除默认的handleStatus值，除非前端明确传入
        if (swmHandleRecord != null && StringUtils.isBlank(request.getParameter("handleStatus"))) {
            swmHandleRecord.setHandleStatus(null);
        }

        // 处理时间范围查询条件
        String beginTime = request.getParameter("timeRange[0]");
        String endTime = request.getParameter("timeRange[1]");

        if (StringUtils.isNotBlank(beginTime) && StringUtils.isNotBlank(endTime)) {
            // 设置开始时间和结束时间条件
            swmHandleRecord.setBeginHandleTime(DateUtils.parseDate(beginTime));
            swmHandleRecord.setEndHandleTime(DateUtils.parseDate(endTime));
        }

        // 注意：不需要在这里手动添加%，实体类中已经设置了LIKE查询类型
        // JeeSite框架会自动处理LIKE查询类型的字段，添加%进行模糊匹配

        // 调用服务层方法，获取带文本值的分页数据
        return swmHandleRecordService.findPageWithTextValues(page, swmHandleRecord);
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmHandleRecord swmHandleRecord) {
        Map<String, Object> result = new HashMap<>();
        if (swmHandleRecord != null) {
            Map<String, Object> recordData = new HashMap<>();
            // 复制基本属性
            recordData.put("id", swmHandleRecord.getId());
            recordData.put("recordName", swmHandleRecord.getRecordName());
            recordData.put("warningId", swmHandleRecord.getWarningId());
            recordData.put("warningRecord", swmHandleRecord.getWarningRecord());
            recordData.put("alarmTime", swmHandleRecord.getAlarmTime());
            recordData.put("handler", swmHandleRecord.getHandler());
            recordData.put("handleTime", swmHandleRecord.getHandleTime());
            recordData.put("handleProcess", swmHandleRecord.getHandleProcess());

            // 将handleStatus直接转换为文本值返回
            String handleStatusValue = swmHandleRecord.getHandleStatus();
            recordData.put("handleStatus", SwmHandleRecord.HandleStatusEnum.getText(handleStatusValue));

            recordData.put("remarks", swmHandleRecord.getRemarks());
            recordData.put("attachment", swmHandleRecord.getAttachment());

            // 获取关联预警的触发原因
            String warningId = swmHandleRecord.getWarningId();
            if (StringUtils.isNotBlank(warningId)) {
                SwmWarningManagement warning = swmWarningManagementService.get(warningId);
                if (warning != null) {
                    recordData.put("triggerReason", warning.getTriggerReason());
                }
            }

            result.putAll(recordData);
        }
        return result;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmHandleRecord swmHandleRecord, HttpServletRequest request) {
        // 确保新记录有默认状态
        if (swmHandleRecord.getIsNewRecord() && StringUtils.isBlank(swmHandleRecord.getHandleStatus())) {
            swmHandleRecord.setHandleStatus(SwmHandleRecord.HandleStatusEnum.UNHANDLED);
        }

        // 从recordName生成warningRecord（如果recordName存在且warningRecord为空）
        if (StringUtils.isNotBlank(swmHandleRecord.getRecordName())
                && StringUtils.isBlank(swmHandleRecord.getWarningRecord())) {
            String recordName = swmHandleRecord.getRecordName();
            if (recordName.startsWith("处置")) {
                // 移除"处置"前缀，保留后面的部分
                String warningRecord = recordName.substring(2);
                swmHandleRecord.setWarningRecord(warningRecord);
            } else {
                // 如果没有前缀，直接使用
                swmHandleRecord.setWarningRecord(recordName);
            }
        }

        // 保存处置记录
        swmHandleRecordService.save(swmHandleRecord);

        // 同步更新关联的预警管理记录
        String warningId = swmHandleRecord.getWarningId();
        if (StringUtils.isNotBlank(warningId)) {
            // 获取预警记录
            SwmWarningManagement warning = swmWarningManagementService.get(warningId);
            if (warning != null) {
                // 更新预警记录的处置相关信息
                warning.setHandler(swmHandleRecord.getHandler());
                warning.setHandleTime(swmHandleRecord.getHandleTime());
                warning.setHandleProcess(swmHandleRecord.getHandleProcess());

                // 检查附件路径
                String attachment = request.getParameter("attachment");
                if (StringUtils.isNotBlank(attachment)) {
                    // attachment是mediumtext类型，可以存储大量文本数据
                    warning.setAttachment(attachment);
                }

                // 更新处置状态
                warning.setHandleStatus(swmHandleRecord.getHandleStatus());

                // 保存更新的预警记录
                swmWarningManagementService.save(warning);
            }
        }

        return renderResult(Global.TRUE, text("保存处置记录成功！"));
    }

    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmHandleRecord swmHandleRecord) {
        swmHandleRecordService.delete(swmHandleRecord);
        return renderResult(Global.TRUE, text("删除处置记录成功！"));
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
            SwmHandleRecord swmHandleRecord = swmHandleRecordService.get(id);
            if (swmHandleRecord != null) {
                swmHandleRecordService.delete(swmHandleRecord);
            }
        }
        return renderResult(Global.TRUE, text("批量删除处置记录成功！"));
    }

    /**
     * 获取枚举选项
     */
    @GetMapping(value = "enumOptions")
    @ResponseBody
    @ApiOperation("获取枚举选项")
    public Map<String, Object> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();

        // 处置状态选项
        Map<String, String> handleStatusOptions = new HashMap<>();
        handleStatusOptions.put(SwmHandleRecord.HandleStatusEnum.UNHANDLED, "草稿");
        handleStatusOptions.put(SwmHandleRecord.HandleStatusEnum.HANDLED, "已完成");
        result.put("handleStatusOptions", handleStatusOptions);

        return result;
    }

    /**
     * 根据预警ID查询处置记录
     */
    @GetMapping(value = "byWarningId")
    @ResponseBody
    @ApiOperation("根据预警ID查询处置记录")
    public Map<String, Object> getByWarningId(String warningId) {
        Map<String, Object> result = new HashMap<>();
        if (warningId != null && !warningId.isEmpty()) {
            List<SwmHandleRecord> recordList = swmHandleRecordService.findByWarningId(warningId);

            // 转换为前端所需的格式
            List<Map<String, Object>> formattedList = new ArrayList<>();
            for (SwmHandleRecord record : recordList) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", record.getId());
                item.put("recordName", record.getRecordName());
                item.put("warningId", record.getWarningId());
                item.put("warningRecord", record.getWarningRecord());
                item.put("alarmTime", record.getAlarmTime());
                item.put("handler", record.getHandler());
                item.put("handleTime", record.getHandleTime());
                item.put("handleProcess", record.getHandleProcess());
                // 转换处置状态为文本
                item.put("handleStatus", SwmHandleRecord.HandleStatusEnum.getText(record.getHandleStatus()));
                item.put("remarks", record.getRemarks());
                item.put("attachment", record.getAttachment());

                formattedList.add(item);
            }

            result.put("list", formattedList);
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "预警ID不能为空");
        }
        return result;
    }

    /**
     * 新增处置记录
     */
    @PostMapping(value = "addByWarningId")
    @ResponseBody
    @ApiOperation("新增处置记录")
    public String addByWarningId(String warningId, String recordName, String handler,
            String handleProcess, String handleStatus, String remarks, String attachment) {
        if (warningId == null || warningId.isEmpty()) {
            return renderResult(Global.FALSE, text("预警ID不能为空！"));
        }

        // 获取预警记录，用于获取预警内容
        SwmWarningManagement warning = swmWarningManagementService.get(warningId);
        if (warning == null) {
            return renderResult(Global.FALSE, text("预警记录不存在！"));
        }

        // 创建新的处置记录
        SwmHandleRecord record = SwmHandleRecord.createNewRecord();
        record.setWarningId(warningId);

        // 如果没有提供记录名称，则自动构造
        if (StringUtils.isBlank(recordName)) {
            // 获取预警内容的文本值
            String warningContentText = DictUtils.getDictLabel("warning_content_enum",
                    warning.getWarningContent(), warning.getWarningContent());

            // 构造处置记录名称：处置personName触发warningContent
            recordName = "处置" + warning.getPersonName() + "触发" + warningContentText;
        }

        record.setRecordName(recordName);

        // 设置预警记录
        if (StringUtils.isNotBlank(recordName) && recordName.startsWith("处置")) {
            // 移除"处置"前缀，保留后面的部分
            String warningRecord = recordName.substring(2);
            record.setWarningRecord(warningRecord);
        } else {
            // 如果没有前缀，直接构造
            String warningContentText = DictUtils.getDictLabel("warning_content_enum",
                    warning.getWarningContent(), warning.getWarningContent());
            record.setWarningRecord(warning.getPersonName() + "触发" + warningContentText);
        }

        record.setHandler(handler);
        record.setHandleTime(new Date()); // 使用当前时间
        record.setHandleProcess(handleProcess);
        if (StringUtils.isNotBlank(handleStatus)) {
            record.setHandleStatus(handleStatus);
        }
        record.setRemarks(remarks);

        // 设置附件路径
        if (StringUtils.isNotBlank(attachment)) {
            record.setAttachment(attachment);
        }

        // 保存记录
        swmHandleRecordService.save(record);

        return renderResult(Global.TRUE, text("添加处置记录成功！"));
    }

    /**
     * 更新处置记录状态
     */
    @PostMapping(value = "updateStatus")
    @ResponseBody
    @ApiOperation("更新处置记录状态")
    public String updateStatus(String id, String handleStatus, String handler,
            String handleProcess, String remarks, String attachment) {
        // 获取记录
        SwmHandleRecord record = swmHandleRecordService.get(id);
        if (record == null) {
            return renderResult(Global.FALSE, text("处置记录不存在！"));
        }

        // 更新处置信息
        if (handleStatus != null && !handleStatus.isEmpty()) {
            record.setHandleStatus(handleStatus);
        }
        if (handler != null && !handler.isEmpty()) {
            record.setHandler(handler);
        }
        record.setHandleTime(new Date()); // 更新处置时间
        if (handleProcess != null && !handleProcess.isEmpty()) {
            record.setHandleProcess(handleProcess);
        }
        if (remarks != null) {
            record.setRemarks(remarks);
        }

        // 设置附件路径
        if (StringUtils.isNotBlank(attachment)) {
            record.setAttachment(attachment);
        }

        // 保存更新
        swmHandleRecordService.save(record);

        return renderResult(Global.TRUE, text("更新处置记录状态成功！"));
    }

    /**
     * 获取未处置的预警记录列表
     * 
     * @author Shawn
     * @date 2025/06/25
     */
    @GetMapping(value = "unhandledWarnings")
    @ResponseBody
    @ApiOperation("获取未处置的预警记录列表")
    public Map<String, Object> getUnhandledWarnings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("开始查询未处置的预警记录，关键字: {}, 页码: {}, 每页数量: {}", keyword, pageNum, pageSize);

            // 调用业务层方法获取未处置的预警记录
            List<SwmWarningManagement> allUnhandledWarnings = swmWarningManagementService
                    .findUnhandledWarnings(keyword);
            logger.info("查询到 {} 条未处置预警记录", allUnhandledWarnings.size());

            // 手动分页处理
            int total = allUnhandledWarnings.size();
            int startIndex = (pageNum - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);

            List<SwmWarningManagement> pagedWarnings;
            if (startIndex >= total) {
                pagedWarnings = new ArrayList<>();
            } else {
                pagedWarnings = allUnhandledWarnings.subList(startIndex, endIndex);
            }

            // 构建返回数据
            List<Map<String, Object>> formattedList = formatWarningRecords(pagedWarnings);

            result.put("list", formattedList);
            result.put("success", true);
            result.put("count", formattedList.size());
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) total / pageSize));

            if (formattedList.isEmpty() && total == 0) {
                result.put("message", "暂无未处置的预警记录");
            }

        } catch (Exception e) {
            logger.error("获取未处置预警记录失败", e);
            result.put("list", new ArrayList<>());
            result.put("success", false);
            result.put("message", "获取未处置预警记录失败: " + e.getMessage());
            result.put("total", 0);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }

        return result;
    }

    /**
     * 格式化预警记录为前端需要的格式
     */
    private List<Map<String, Object>> formatWarningRecords(List<SwmWarningManagement> warnings) {
        List<Map<String, Object>> formattedList = new ArrayList<>();

        for (SwmWarningManagement warning : warnings) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", warning.getId());

            // 获取预警内容的实际文本值（从字典获取）
            String warningContentText = warning.getWarningContent();
            if (warningContentText != null && warningContentText.matches("\\d+")) {
                warningContentText = DictUtils.getDictLabel("warning_content_enum",
                        warningContentText, warningContentText);
            }

            // 构造格式：处置personName触发warningContent
            String formattedName = "处置" + warning.getPersonName() + "触发" + warningContentText;

            item.put("recordName", formattedName);
            item.put("personName", warning.getPersonName());
            // 添加身份证号字段
            item.put("idCard", warning.getIdCard());
            // 使用文本值而不是编码
            item.put("warningContent", warningContentText);
            item.put("warningTime", warning.getWarningTime());
            item.put("triggerReason", warning.getTriggerReason());
            item.put("handleStatus", SwmWarningManagement.HandleStatusEnum.getText(warning.getHandleStatus()));

            formattedList.add(item);
        }

        return formattedList;
    }

    /**
     * 根据预警ID创建处置记录并更新预警状态
     */
    @PostMapping(value = "saveHandleRecordByWarning")
    @ResponseBody
    @ApiOperation("根据预警ID创建处置记录并更新预警状态")
    public String saveHandleRecordByWarning(@RequestBody Map<String, Object> params) {
        // 获取参数
        String warningId = (String) params.get("warningId");
        String handler = (String) params.get("handler");
        String handleProcess = (String) params.get("handleProcess");
        String handleTimeStr = (String) params.get("handleTime");
        String remarks = (String) params.get("remarks");
        String attachment = (String) params.get("attachment");

        // 检查必要参数
        if (StringUtils.isBlank(warningId)) {
            return renderResult(Global.FALSE, text("预警ID不能为空！"));
        }

        if (StringUtils.isBlank(handler)) {
            return renderResult(Global.FALSE, text("处置人不能为空！"));
        }

        if (StringUtils.isBlank(handleProcess)) {
            return renderResult(Global.FALSE, text("处置过程不能为空！"));
        }

        // 获取预警记录
        SwmWarningManagement warning = swmWarningManagementService.get(warningId);
        if (warning == null) {
            return renderResult(Global.FALSE, text("预警记录不存在！"));
        }

        // 解析处置时间
        Date handleTime;
        if (StringUtils.isNotBlank(handleTimeStr)) {
            handleTime = DateUtils.parseDate(handleTimeStr);
        } else {
            handleTime = new Date(); // 默认为当前时间
        }

        // 创建处置记录
        SwmHandleRecord record = SwmHandleRecord.createNewRecord();
        record.setWarningId(warningId);

        // 获取预警内容的文本值
        String warningContentText = DictUtils.getDictLabel("warning_content_enum",
                warning.getWarningContent(), warning.getWarningContent());

        // 构造处置记录名称：处置personName触发warningContent
        String recordName = "处置" + warning.getPersonName() + "触发" + warningContentText;
        record.setRecordName(recordName);

        // 构造预警记录：personName触发warningContent (不带"处置"前缀)
        String warningRecord = warning.getPersonName() + "触发" + warningContentText;
        record.setWarningRecord(warningRecord);

        // 设置报警时间
        record.setAlarmTime(warning.getAlarmTime() != null ? warning.getAlarmTime() : warning.getWarningTime());

        record.setHandler(handler);
        record.setHandleTime(handleTime);
        record.setHandleProcess(handleProcess);
        record.setHandleStatus(SwmHandleRecord.HandleStatusEnum.HANDLED); // 已完成状态
        record.setRemarks(remarks);

        // 设置附件路径
        if (StringUtils.isNotBlank(attachment)) {
            record.setAttachment(attachment);
        }

        // 保存处置记录
        swmHandleRecordService.save(record);

        // 更新预警记录的处置状态
        warning.setHandler(handler);
        warning.setHandleTime(handleTime);
        warning.setHandleProcess(handleProcess);
        warning.setHandleStatus(SwmWarningManagement.HandleStatusEnum.HANDLED); // 已处置

        // 保存更新的预警记录
        swmWarningManagementService.save(warning);

        return renderResult(Global.TRUE, text("处置记录保存成功！"));
    }

    /**
     * 更新所有处置记录的名称格式
     */
    @PostMapping(value = "updateRecordNameFormat")
    @ResponseBody
    @ApiOperation("更新所有处置记录的名称格式")
    public String updateRecordNameFormat() {
        try {
            swmHandleRecordService.updateRecordNameFormat();
            return renderResult(Global.TRUE, text("处置记录名称格式更新成功！"));
        } catch (Exception e) {
            logger.error("更新处置记录名称格式失败", e);
            return renderResult(Global.FALSE, text("更新处置记录名称格式失败：" + e.getMessage()));
        }
    }
}