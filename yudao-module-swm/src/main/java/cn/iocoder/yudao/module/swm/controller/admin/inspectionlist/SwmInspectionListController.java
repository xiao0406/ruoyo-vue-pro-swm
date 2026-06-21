package cn.iocoder.yudao.module.swm.controller.admin.inspectionlist;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.inspectionlist.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionListDO;
import cn.iocoder.yudao.module.swm.service.SwmInspectionListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 巡检单")
@RestController
@RequestMapping("/swm/inspection-list")
@Validated
public class SwmInspectionListController {
    @Resource
    private SwmInspectionListService service;

    @PostMapping("/create")
    @Operation(summary = "创建巡检单")
    @PreAuthorize("@ss.hasPermission('swm:inspection-list:create')")
    public CommonResult<String> create(@Valid @RequestBody SwmInspectionListSaveReqVO reqVO) {
        return success(service.createSwmInspectionList(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新巡检单")
    @PreAuthorize("@ss.hasPermission('swm:inspection-list:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SwmInspectionListSaveReqVO reqVO) {
        service.updateSwmInspectionList(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除巡检单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:inspection-list:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") String id) {
        service.deleteSwmInspectionList(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取巡检单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:inspection-list:query')")
    public CommonResult<SwmInspectionListRespVO> get(@RequestParam("id") String id) {
        return success(BeanUtils.toBean(service.getSwmInspectionList(id), SwmInspectionListRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询巡检单")
    @PreAuthorize("@ss.hasPermission('swm:inspection-list:query')")
    public CommonResult<PageResult<SwmInspectionListRespVO>> getPage(@Valid SwmInspectionListPageReqVO pageReqVO) {
        return success(BeanUtils.toBean(service.getSwmInspectionListPage(pageReqVO), SwmInspectionListRespVO.class));
    }
}
