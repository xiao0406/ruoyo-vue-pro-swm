package cn.iocoder.yudao.module.swm.controller.admin.inspectionplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.inspectionplan.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionPlanDO;
import cn.iocoder.yudao.module.swm.service.SwmInspectionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 巡检计划")
@RestController
@RequestMapping("/swm/inspection-plan")
@Validated
public class SwmInspectionPlanController {
    @Resource
    private SwmInspectionPlanService service;

    @PostMapping("/create")
    @Operation(summary = "创建巡检计划")
    @PreAuthorize("@ss.hasPermission('swm:inspection-plan:create')")
    public CommonResult<String> create(@Valid @RequestBody SwmInspectionPlanSaveReqVO reqVO) {
        return success(service.createSwmInspectionPlan(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新巡检计划")
    @PreAuthorize("@ss.hasPermission('swm:inspection-plan:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SwmInspectionPlanSaveReqVO reqVO) {
        service.updateSwmInspectionPlan(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除巡检计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:inspection-plan:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") String id) {
        service.deleteSwmInspectionPlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取巡检计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:inspection-plan:query')")
    public CommonResult<SwmInspectionPlanRespVO> get(@RequestParam("id") String id) {
        return success(BeanUtils.toBean(service.getSwmInspectionPlan(id), SwmInspectionPlanRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询巡检计划")
    @PreAuthorize("@ss.hasPermission('swm:inspection-plan:query')")
    public CommonResult<PageResult<SwmInspectionPlanRespVO>> getPage(@Valid SwmInspectionPlanPageReqVO pageReqVO) {
        return success(BeanUtils.toBean(service.getSwmInspectionPlanPage(pageReqVO), SwmInspectionPlanRespVO.class));
    }
}
