package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPersonnelBoard;
import com.jeesite.modules.swm.service.SwmPersonnelBoardService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人员看板Controller
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Controller
@RequestMapping(value = "${adminPath}/personnelBoard")
@Api(value = "人员看板管理接口", tags = "人员看板管理接口")
public class SwmPersonnelBoardController extends BaseController {

    @Autowired
    private SwmPersonnelBoardService swmPersonnelBoardService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmPersonnelBoard get(String id, boolean isNewRecord) {
        return swmPersonnelBoardService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmPersonnelBoard swmPersonnelBoard, Model model) {
        model.addAttribute("swmPersonnelBoard", swmPersonnelBoard);
        return "modules/swm/personnelBoardList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Map<String, Object> listData(SwmPersonnelBoard swmPersonnelBoard, HttpServletRequest request,
            HttpServletResponse response) {
        // 从请求中获取时间类型和时间值参数
        String timeType = request.getParameter("timeType");
        String timeValue = request.getParameter("timeValue");
        
        // 默认为当月
        if (timeType == null || timeType.isEmpty()) {
            timeType = "month";
        }
        
        // 设置时间参数到查询对象
        swmPersonnelBoard.setTimeType(timeType);
        swmPersonnelBoard.setTimeValue(timeValue);
        
        Page<SwmPersonnelBoard> page = swmPersonnelBoardService.findPage(new Page<>(request, response),
                swmPersonnelBoard);

        // 构建包含额外字段的响应数据
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> enhancedList = new ArrayList<>();

        // 处理每个对象，添加枚举的文本显示
        for (SwmPersonnelBoard board : page.getList()) {
            Map<String, Object> boardMap = new HashMap<>();

            // 复制基本属性
            boardMap.put("id", board.getId());
            boardMap.put("createBy", board.getCreateBy());
            boardMap.put("createDate", board.getCreateDate());
            boardMap.put("updateBy", board.getUpdateBy());
            boardMap.put("updateDate", board.getUpdateDate());
            boardMap.put("remarks", board.getRemarks());
            boardMap.put("status", board.getStatus());

            // 复制业务属性
            boardMap.put("name", board.getName());
            boardMap.put("organization", board.getOrganization());
            boardMap.put("workshop", board.getWorkshop());
            boardMap.put("process", board.getProcess());
            boardMap.put("team", board.getTeam());
            boardMap.put("workStatus", board.getWorkStatus());
            boardMap.put("deviceId", board.getDeviceId());
            boardMap.put("helmetStatus", board.getHelmetStatus());
            boardMap.put("personnelStatus", board.getPersonnelStatus());
            boardMap.put("attendanceCount", board.getAttendanceCount());
            boardMap.put("workingHours", board.getWorkingHours());
            boardMap.put("idleHours", board.getIdleHours());
            boardMap.put("isNewRecord", board.getIsNewRecord());
            // 添加身份证号码
            boardMap.put("id_card", board.getIdCard());

            // 添加枚举文本显示值
            boardMap.put("workStatusText", board.getWorkStatusText());
            boardMap.put("helmetStatusText", board.getHelmetStatusText());
            boardMap.put("personnelStatusText", board.getPersonnelStatusText());

            // 添加到列表
            enhancedList.add(boardMap);
        }

        // 构建分页结果
        result.put("list", enhancedList);
        result.put("count", page.getCount());
        result.put("pageNo", page.getPageNo());
        result.put("pageSize", page.getPageSize());

        return result;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmPersonnelBoard swmPersonnelBoard) {
        Map<String, Object> result = new HashMap<>();
        if (swmPersonnelBoard != null) {
            Map<String, Object> boardData = new HashMap<>();

            // 复制基本属性
            boardData.put("id", swmPersonnelBoard.getId());
            boardData.put("name", swmPersonnelBoard.getName());
            boardData.put("organization", swmPersonnelBoard.getOrganization());
            boardData.put("workshop", swmPersonnelBoard.getWorkshop());
            boardData.put("process", swmPersonnelBoard.getProcess());
            boardData.put("team", swmPersonnelBoard.getTeam());
            boardData.put("deviceId", swmPersonnelBoard.getDeviceId());
            boardData.put("attendanceCount", swmPersonnelBoard.getAttendanceCount());
            boardData.put("workingHours", swmPersonnelBoard.getWorkingHours());
            boardData.put("idleHours", swmPersonnelBoard.getIdleHours());
            boardData.put("remarks", swmPersonnelBoard.getRemarks());
            // 添加身份证号码
            boardData.put("id_card", swmPersonnelBoard.getIdCard());

            // 处理枚举值
            boardData.put("workStatus", swmPersonnelBoard.getWorkStatus());
            boardData.put("workStatusText", swmPersonnelBoard.getWorkStatusText());
            boardData.put("helmetStatus", swmPersonnelBoard.getHelmetStatus());
            boardData.put("helmetStatusText", swmPersonnelBoard.getHelmetStatusText());
            boardData.put("personnelStatus", swmPersonnelBoard.getPersonnelStatus());
            boardData.put("personnelStatusText", swmPersonnelBoard.getPersonnelStatusText());

            result.putAll(boardData);
        }
        return result;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmPersonnelBoard swmPersonnelBoard) {
        swmPersonnelBoardService.save(swmPersonnelBoard);
        return renderResult(Global.TRUE, text("保存人员看板成功！"));
    }

    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmPersonnelBoard swmPersonnelBoard) {
        swmPersonnelBoardService.delete(swmPersonnelBoard);
        return renderResult(Global.TRUE, text("删除人员看板成功！"));
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
            SwmPersonnelBoard swmPersonnelBoard = swmPersonnelBoardService.get(id);
            if (swmPersonnelBoard != null) {
                swmPersonnelBoardService.delete(swmPersonnelBoard);
            }
        }
        return renderResult(Global.TRUE, text("批量删除人员看板成功！"));
    }

    /**
     * 获取枚举选项
     */
    @GetMapping(value = "enumOptions")
    @ResponseBody
    @ApiOperation("获取枚举选项")
    public Map<String, Object> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();

        // 工作状态选项
        Map<String, String> workStatusOptions = new HashMap<>();
        List<DictData> workStatusDictList = DictUtils.getDictList("work_status_enum");
        for (DictData dict : workStatusDictList) {
            workStatusOptions.put(dict.getDictValue(), dict.getDictLabel());
        }
        result.put("workStatusOptions", workStatusOptions);

        // 安全帽状态选项
        Map<String, String> helmetStatusOptions = new HashMap<>();
        List<DictData> helmetStatusDictList = DictUtils.getDictList("helmet_status_enum");
        for (DictData dict : helmetStatusDictList) {
            helmetStatusOptions.put(dict.getDictValue(), dict.getDictLabel());
        }
        result.put("helmetStatusOptions", helmetStatusOptions);

        // 人员状态选项
        Map<String, String> personnelStatusOptions = new HashMap<>();
        List<DictData> personnelStatusDictList = DictUtils.getDictList("personnel_status_enum");
        for (DictData dict : personnelStatusDictList) {
            personnelStatusOptions.put(dict.getDictValue(), dict.getDictLabel());
        }
        result.put("personnelStatusOptions", personnelStatusOptions);

        return result;
    }

    /**
     * 批量更新工作状态
     */
    @PostMapping(value = "batchUpdateWorkStatus")
    @ResponseBody
    @ApiOperation("批量更新工作状态")
    public String batchUpdateWorkStatus(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) params.get("ids");
        String workStatus = (String) params.get("workStatus");

        if (ids == null || ids.isEmpty() || workStatus == null) {
            return renderResult(Global.FALSE, text("参数错误"));
        }

        swmPersonnelBoardService.batchUpdateWorkStatus(ids, workStatus);
        return renderResult(Global.TRUE, text("更新工作状态成功！"));
    }

    /**
     * 批量更新安全帽状态
     */
    @PostMapping(value = "batchUpdateHelmetStatus")
    @ResponseBody
    @ApiOperation("批量更新安全帽状态")
    public String batchUpdateHelmetStatus(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) params.get("ids");
        String helmetStatus = (String) params.get("helmetStatus");

        if (ids == null || ids.isEmpty() || helmetStatus == null) {
            return renderResult(Global.FALSE, text("参数错误"));
        }

        swmPersonnelBoardService.batchUpdateHelmetStatus(ids, helmetStatus);
        return renderResult(Global.TRUE, text("更新安全帽状态成功！"));
    }

    /**
     * 批量更新人员状态
     */
    @PostMapping(value = "batchUpdatePersonnelStatus")
    @ResponseBody
    @ApiOperation("批量更新人员状态")
    public String batchUpdatePersonnelStatus(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) params.get("ids");
        String personnelStatus = (String) params.get("personnelStatus");

        if (ids == null || ids.isEmpty() || personnelStatus == null) {
            return renderResult(Global.FALSE, text("参数错误"));
        }

        swmPersonnelBoardService.batchUpdatePersonnelStatus(ids, personnelStatus);
        return renderResult(Global.TRUE, text("更新人员状态成功！"));
    }
}