package cn.iocoder.yudao.module.swm.controller.admin.hazard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourcePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourceRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHazardSourceDO;
import cn.iocoder.yudao.module.swm.service.SwmHazardSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 危险源管理")
@RestController
@RequestMapping("/swm/hazard-source")
@Validated
public class SwmHazardSourceController {

    @Resource
    private SwmHazardSourceService hazardSourceService;

    @PostMapping("/create")
    @Operation(summary = "创建危险源")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:create')")
    public CommonResult<String> createSwmHazardSource(@Valid @RequestBody SwmHazardSourceSaveReqVO createReqVO) {
        String id = hazardSourceService.createHazardSource(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新危险源")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:update')")
    public CommonResult<Boolean> updateSwmHazardSource(@Valid @RequestBody SwmHazardSourceSaveReqVO updateReqVO) {
        hazardSourceService.updateHazardSource(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除危险源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:delete')")
    public CommonResult<Boolean> deleteSwmHazardSource(@RequestParam("id") String id) {
        hazardSourceService.deleteHazardSource(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取危险源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:query')")
    public CommonResult<SwmHazardSourceRespVO> getSwmHazardSource(@RequestParam("id") String id) {
        SwmHazardSourceDO hazardSource = hazardSourceService.getHazardSource(id);
        return success(BeanUtils.toBean(hazardSource, SwmHazardSourceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询危险源")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:query')")
    public CommonResult<PageResult<SwmHazardSourceRespVO>> getSwmHazardSourcePage(@Valid SwmHazardSourcePageReqVO pageReqVO) {
        PageResult<SwmHazardSourceDO> pageResult = hazardSourceService.getHazardSourcePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmHazardSourceRespVO.class));
    }
}
