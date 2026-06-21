package cn.iocoder.yudao.module.swm.controller.admin.area;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAreaDO;
import cn.iocoder.yudao.module.swm.service.SwmAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 区域管理")
@RestController
@RequestMapping("/swm/area")
@Validated
public class SwmAreaController {

    @Resource
    private SwmAreaService areaService;

    @PostMapping("/create")
    @Operation(summary = "创建区域")
    @PreAuthorize("@ss.hasPermission('swm:area:create')")
    public CommonResult<String> createSwmArea(@Valid @RequestBody SwmAreaSaveReqVO createReqVO) {
        String id = areaService.createArea(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新区域")
    @PreAuthorize("@ss.hasPermission('swm:area:update')")
    public CommonResult<Boolean> updateSwmArea(@Valid @RequestBody SwmAreaSaveReqVO updateReqVO) {
        areaService.updateArea(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除区域")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:area:delete')")
    public CommonResult<Boolean> deleteSwmArea(@RequestParam("id") String id) {
        areaService.deleteArea(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取区域")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:area:query')")
    public CommonResult<SwmAreaRespVO> getSwmArea(@RequestParam("id") String id) {
        SwmAreaDO area = areaService.getArea(id);
        return success(BeanUtils.toBean(area, SwmAreaRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询区域")
    @PreAuthorize("@ss.hasPermission('swm:area:query')")
    public CommonResult<PageResult<SwmAreaRespVO>> getSwmAreaPage(@Valid SwmAreaPageReqVO pageReqVO) {
        PageResult<SwmAreaDO> pageResult = areaService.getAreaPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmAreaRespVO.class));
    }

}
