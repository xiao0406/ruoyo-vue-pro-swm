package cn.iocoder.yudao.module.swm.controller.admin.safetyeducation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyEducationDO;
import cn.iocoder.yudao.module.swm.service.SwmSafetyEducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 安全教育")
@RestController
@RequestMapping("/swm/safety-education")
@Validated
public class SwmSafetyEducationController {
    @Resource
    private SwmSafetyEducationService service;

    @PostMapping("/create")
    @Operation(summary = "创建安全教育")
    @PreAuthorize("@ss.hasPermission('swm:safety-education:create')")
    public CommonResult<String> create(@Valid @RequestBody SwmSafetyEducationSaveReqVO reqVO) {
        return success(service.createSwmSafetyEducation(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新安全教育")
    @PreAuthorize("@ss.hasPermission('swm:safety-education:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SwmSafetyEducationSaveReqVO reqVO) {
        service.updateSwmSafetyEducation(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除安全教育")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:safety-education:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") String id) {
        service.deleteSwmSafetyEducation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取安全教育")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:safety-education:query')")
    public CommonResult<SwmSafetyEducationRespVO> get(@RequestParam("id") String id) {
        return success(BeanUtils.toBean(service.getSwmSafetyEducation(id), SwmSafetyEducationRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询安全教育")
    @PreAuthorize("@ss.hasPermission('swm:safety-education:query')")
    public CommonResult<PageResult<SwmSafetyEducationRespVO>> getPage(@Valid SwmSafetyEducationPageReqVO pageReqVO) {
        return success(BeanUtils.toBean(service.getSwmSafetyEducationPage(pageReqVO), SwmSafetyEducationRespVO.class));
    }
}
