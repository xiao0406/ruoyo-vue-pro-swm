package cn.iocoder.yudao.module.swm.controller.admin.warning;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictDataDO;
import cn.iocoder.yudao.module.swm.service.SwmDictDataService;
import cn.iocoder.yudao.module.swm.service.SwmWarningManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 预警管理")
@RestController
@RequestMapping("/swm/warning")
@Validated
public class SwmWarningManagementController {

    @Resource
    private SwmWarningManagementService warningManagementService;
    @Resource
    private SwmDictDataService dictDataService;

    @PostMapping("/create")
    @Operation(summary = "创建预警管理")
    @PreAuthorize("@ss.hasPermission('swm:warning:create')")
    public CommonResult<String> createWarningManagement(@Valid @RequestBody SwmWarningManagementSaveReqVO createReqVO) {
        String id = warningManagementService.createWarningManagement(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新预警管理")
    @PreAuthorize("@ss.hasPermission('swm:warning:update')")
    public CommonResult<Boolean> updateWarningManagement(@Valid @RequestBody SwmWarningManagementSaveReqVO updateReqVO) {
        warningManagementService.updateWarningManagement(updateReqVO);
        return success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "兼容旧前端 POST 更新预警")
    @PreAuthorize("@ss.hasPermission('swm:warning:update')")
    public CommonResult<Boolean> updateWarningManagementByPost(@Valid @RequestBody SwmWarningManagementSaveReqVO updateReqVO) {
        warningManagementService.updateWarningManagement(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除预警管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:warning:delete')")
    public CommonResult<Boolean> deleteWarningManagement(@RequestParam("id") String id) {
        warningManagementService.deleteWarningManagement(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取预警管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:warning:query')")
    public CommonResult<SwmWarningManagementRespVO> getWarningManagement(@RequestParam("id") String id) {
        SwmWarningManagementDO warningManagement = warningManagementService.getWarningManagement(id);
        return success(BeanUtils.toBean(warningManagement, SwmWarningManagementRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询预警管理")
    @PreAuthorize("@ss.hasPermission('swm:warning:query')")
    public CommonResult<PageResult<SwmWarningManagementRespVO>> getWarningManagementPage(@Valid SwmWarningManagementPageReqVO pageReqVO) {
        PageResult<SwmWarningManagementDO> pageResult = warningManagementService.getWarningManagementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmWarningManagementRespVO.class));
    }

    @GetMapping("/enum-options")
    @Operation(summary = "预警枚举选项")
    @PreAuthorize("@ss.hasPermission('swm:warning:query')")
    public CommonResult<Map<String, Object>> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> warningTypes = toOptions(dictDataService.getDictDataList("warning_type_enum"));
        List<Map<String, String>> warningContents = toOptions(dictDataService.getDictDataList("warning_content_enum"));
        List<Map<String, String>> handleStatuses = toOptions(dictDataService.getDictDataList("handle_status_enum"));
        if (warningTypes.isEmpty()) {
            warningTypes = List.of(Map.of("label", "主动预警", "value", "1"),
                    Map.of("label", "被动预警", "value", "2"));
        }
        if (handleStatuses.isEmpty()) {
            handleStatuses = List.of(Map.of("label", "未处理", "value", "0"),
                    Map.of("label", "已处理", "value", "1"));
        }
        result.put("warningTypes", warningTypes);
        result.put("warningContents", warningContents);
        result.put("handleStatuses", handleStatuses);
        result.put("warningTypeOptions", toMap(warningTypes));
        result.put("warningContentOptions", toMap(warningContents));
        result.put("handleStatusOptions", toMap(handleStatuses));
        return success(result);
    }

    @GetMapping("/popup-warnings")
    @Operation(summary = "待弹窗预警列表")
    @PreAuthorize("@ss.hasPermission('swm:warning:query')")
    public CommonResult<Map<String, Object>> getPopupWarnings() {
        SwmWarningManagementPageReqVO reqVO = new SwmWarningManagementPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        reqVO.setHandleStatus("0");
        List<SwmWarningManagementRespVO> warnings = BeanUtils.toBean(
                warningManagementService.getWarningManagementPage(reqVO).getList(),
                SwmWarningManagementRespVO.class);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("confirmList", warnings);
        result.put("notificationList", warnings);
        result.put("data", warnings);
        result.put("list", warnings);
        result.put("total", warnings.size());
        return success(result);
    }

    @GetMapping("/confirm")
    @Operation(summary = "确认预警")
    @PreAuthorize("@ss.hasPermission('swm:warning:update')")
    public CommonResult<Map<String, Object>> confirmWarning(@RequestParam("id") String id) {
        SwmWarningManagementDO warning = warningManagementService.getWarningManagement(id);
        if (warning == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "预警不存在");
            return success(result);
        }
        SwmWarningManagementSaveReqVO update = BeanUtils.toBean(warning, SwmWarningManagementSaveReqVO.class);
        update.setHandleStatus("1");
        warningManagementService.updateWarningManagement(update);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "OK");
        return success(result);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出预警记录")
    @PreAuthorize("@ss.hasPermission('swm:warning:export')")
    public void export(SwmWarningManagementPageReqVO reqVO, HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SwmWarningManagementRespVO> list = BeanUtils.toBean(
                warningManagementService.getWarningManagementPage(reqVO).getList(),
                SwmWarningManagementRespVO.class);
        ExcelUtils.write(response, "预警记录.xls", "预警记录", SwmWarningManagementRespVO.class, list);
    }

    private List<Map<String, String>> toOptions(List<SwmDictDataDO> list) {
        return list.stream().map(item -> Map.of("label", item.getDictLabel(), "value", item.getDictValue())).toList();
    }

    private Map<String, String> toMap(List<Map<String, String>> options) {
        Map<String, String> result = new HashMap<>();
        options.forEach(option -> result.put(option.get("value"), option.get("label")));
        return result;
    }

}
