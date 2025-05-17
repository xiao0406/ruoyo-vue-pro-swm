package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.SwmWarningManagementService;
import com.jeesite.modules.sys.entity.DictData;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预警管理Controller
 * 
 * @author auto create
 * @version 2025-05-16
 */
@Controller
@RequestMapping(value = "${adminPath}/warningManagement")
@Api(value = "预警管理接口", tags = "预警管理接口")
public class SwmWarningManagementController extends BaseController {

    @Autowired
    private SwmWarningManagementService swmWarningManagementService;

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

        // 打印请求参数
        System.out.println("查询参数: personName=" + swmWarningManagement.getPersonName() +
                ", warningType=" + swmWarningManagement.getWarningType() +
                ", warningContent=" + swmWarningManagement.getWarningContent() +
                ", handleStatus=" + swmWarningManagement.getHandleStatus());

        // 检查字典数据是否正确加载
        System.out.println("字典检查 - 预警类型:");
        List<DictData> warningTypeDict = DictUtils.getDictList("warning_type_enum");
        if (warningTypeDict != null) {
            for (DictData dict : warningTypeDict) {
                System.out.println("  dictValue=" + dict.getDictValue() + ", dictLabel=" + dict.getDictLabel());
            }
        } else {
            System.out.println("  预警类型字典为空!");
        }

        System.out.println("字典检查 - 预警内容:");
        List<DictData> warningContentDict = DictUtils.getDictList("warning_content_enum");
        if (warningContentDict != null) {
            for (DictData dict : warningContentDict) {
                System.out.println("  dictValue=" + dict.getDictValue() + ", dictLabel=" + dict.getDictLabel());
            }
        } else {
            System.out.println("  预警内容字典为空!");
        }

        // 调用服务层方法，获取带文本值的分页数据
        Page<SwmWarningManagement> resultPage = swmWarningManagementService.findPageWithTextValues(page,
                swmWarningManagement);

        // 添加日志检查返回的数据
        if (resultPage != null && resultPage.getList() != null && !resultPage.getList().isEmpty()) {
            System.out.println("Controller - 返回数据总条数: " + resultPage.getCount());
            SwmWarningManagement first = resultPage.getList().get(0);
            System.out.println("Controller - 返回给前端的第一条数据: ID:" + first.getId() +
                    ", warningType:" + first.getWarningType() +
                    ", warningTypeText:" + first.getWarningTypeText() +
                    ", warningContent:" + first.getWarningContent() +
                    ", handleStatus:" + first.getHandleStatus() +
                    ", handleStatusText:" + first.getHandleStatusText());

            // 检查所有数据的内容
            int count = 0;
            for (SwmWarningManagement item : resultPage.getList()) {
                System.out.println("Controller - 数据[" + count + "]: ID:" + item.getId() +
                        ", warningType:" + item.getWarningType() +
                        ", warningContent:" + item.getWarningContent() +
                        ", handleStatus:" + item.getHandleStatus());
                count++;
            }
        } else {
            System.out.println("Controller - 返回数据为空或没有记录");
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
        if (swmWarningManagement != null) {
            Map<String, Object> warningData = new HashMap<>();
            // 复制基本属性
            warningData.put("id", swmWarningManagement.getId());
            warningData.put("personName", swmWarningManagement.getPersonName());
            warningData.put("warningTime", swmWarningManagement.getWarningTime());
            warningData.put("alarmRecord", swmWarningManagement.getAlarmRecord());
            warningData.put("alarmTime", swmWarningManagement.getAlarmTime());
            warningData.put("triggerReason", swmWarningManagement.getTriggerReason());
            warningData.put("handler", swmWarningManagement.getHandler());
            warningData.put("handleTime", swmWarningManagement.getHandleTime());
            warningData.put("handleProcess", swmWarningManagement.getHandleProcess());

            // 保存原始值，用于调试
            String origHandleStatus = swmWarningManagement.getHandleStatus();
            String origWarningType = swmWarningManagement.getWarningType();
            String origWarningContent = swmWarningManagement.getWarningContent();

            System.out.println("Controller form - 原始数据: handleStatus=" + origHandleStatus +
                    ", warningType=" + origWarningType +
                    ", warningContent=" + origWarningContent);

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

            warningData.put("attachment", swmWarningManagement.getAttachment());
            warningData.put("remarks", swmWarningManagement.getRemarks());

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

            System.out.println("Controller form - 转换后: handleStatus=" + handleStatusLabel +
                    ", warningType=" + warningTypeLabel +
                    ", warningContent=" + warningContentLabel);

            result.putAll(warningData);
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
    public String process(String id, String handler, String handleTime, String handleProcess, String handleStatus) {
        // 获取预警记录
        SwmWarningManagement swmWarningManagement = swmWarningManagementService.get(id);
        if (swmWarningManagement == null) {
            return renderResult(Global.FALSE, text("预警记录不存在！"));
        }

        // 更新处置信息
        swmWarningManagement.setHandler(handler);
        if (handleTime != null && !handleTime.isEmpty()) {
            swmWarningManagement.setHandleTime(DateUtils.parseDate(handleTime));
        } else {
            swmWarningManagement.setHandleTime(new Date());
        }
        swmWarningManagement.setHandleProcess(handleProcess);
        swmWarningManagement.setHandleStatus(handleStatus);

        // 保存更新
        swmWarningManagementService.save(swmWarningManagement);

        return renderResult(Global.TRUE, text("预警处置成功！"));
    }
}