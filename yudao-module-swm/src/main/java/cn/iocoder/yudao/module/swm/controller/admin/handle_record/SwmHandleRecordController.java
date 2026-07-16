package cn.iocoder.yudao.module.swm.controller.admin.handle_record;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo.SwmHandleRecordPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo.SwmHandleRecordRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo.SwmHandleRecordSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHandleRecordDO;
import cn.iocoder.yudao.module.swm.service.SwmHandleRecordService;
import cn.iocoder.yudao.module.swm.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementPageReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
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

@Tag(name = "管理后台 - 处置记录")
@RestController
@RequestMapping("/swm/handle-record")
@Validated
public class SwmHandleRecordController {

    @Resource
    private SwmHandleRecordService handleRecordService;

    @Resource
    private SwmWarningManagementService warningManagementService;

    @PostMapping("/create")
    @Operation(summary = "创建处置记录")
    @PreAuthorize("@ss.hasPermission('swm:handle-record:create')")
    public CommonResult<String> createSwmHandleRecord(@Valid @RequestBody SwmHandleRecordSaveReqVO createReqVO) {
        String id = handleRecordService.createHandleRecord(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新处置记录")
    @PreAuthorize("@ss.hasPermission('swm:handle-record:update')")
    public CommonResult<Boolean> updateSwmHandleRecord(@Valid @RequestBody SwmHandleRecordSaveReqVO updateReqVO) {
        handleRecordService.updateHandleRecord(updateReqVO);
        return success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "兼容旧前端 POST 更新处置记录")
    @PreAuthorize("@ss.hasPermission('swm:handle-record:update')")
    public CommonResult<Boolean> updateSwmHandleRecordByPost(@Valid @RequestBody SwmHandleRecordSaveReqVO updateReqVO) {
        handleRecordService.updateHandleRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除处置记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:handle-record:delete')")
    public CommonResult<Boolean> deleteSwmHandleRecord(@RequestParam("id") String id) {
        handleRecordService.deleteHandleRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取处置记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:handle-record:query')")
    public CommonResult<SwmHandleRecordRespVO> getSwmHandleRecord(@RequestParam("id") String id) {
        SwmHandleRecordDO handleRecord = handleRecordService.getHandleRecord(id);
        return success(BeanUtils.toBean(handleRecord, SwmHandleRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询处置记录")
    @PreAuthorize("@ss.hasPermission('swm:handle-record:query')")
    public CommonResult<PageResult<SwmHandleRecordRespVO>> getSwmHandleRecordPage(@Valid SwmHandleRecordPageReqVO pageReqVO) {
        PageResult<SwmHandleRecordDO> pageResult = handleRecordService.getHandleRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmHandleRecordRespVO.class));
    }

    @GetMapping("/unhandled-warnings")
    @Operation(summary = "兼容旧前端未处置预警选择列表")
    @PreAuthorize("@ss.hasPermission('swm:handle-record:query')")
    public CommonResult<Map<String, Object>> getUnhandledWarnings() {
        SwmWarningManagementPageReqVO reqVO = new SwmWarningManagementPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(200);
        reqVO.setHandleStatus("0");
        PageResult<SwmWarningManagementDO> page = warningManagementService.getWarningManagementPage(reqVO);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", page.getList().size());
        result.put("total", page.getTotal());
        result.put("pageNum", 1);
        result.put("pageSize", 200);
        result.put("totalPages", (page.getTotal() + 199) / 200);
        result.put("list", page.getList());
        return success(result);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出处置记录")
    @PreAuthorize("@ss.hasPermission('swm:handle-record:export')")
    public void export(SwmHandleRecordPageReqVO reqVO, HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SwmHandleRecordRespVO> list = BeanUtils.toBean(
                handleRecordService.getHandleRecordPage(reqVO).getList(), SwmHandleRecordRespVO.class);
        ExcelUtils.write(response, "报警处置记录.xls", "处置记录", SwmHandleRecordRespVO.class, list);
    }

}
